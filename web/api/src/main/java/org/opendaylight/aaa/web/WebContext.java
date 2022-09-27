/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Web Context with URL prefix. AKA Web App or Servlet context.
 *
 * <p>
 * Its {@link WebContext.Builder} allows programmatic web component registration (as opposed to declarative e.g. via
 * web.xml, OSGi HTTP Whiteboard blueprint integration, CXF BP etc.)
 *
 * <p>
 * This is preferable because:
 * <ul>
 *   <li>using code instead of hiding class names in XML enables tools such as e.g. BND (in the maven-bundle-plugin) to
 *       correctly figure dependencies e.g. for OSGi Import-Package headers;</li>
 *   <li>explicit passing of web components instances, instead of providing class names in XML files and letting a web
 *       container create the new instances using the default constructor, solves a pesky dependency injection (DI)
 *       related problem which typically leads to weird hoops in code through {@code static} etc. that can be avoided
 *       using this;</li>
 *   <li>tests can more easily programmatically instantiate web components.</li>
 * </ul>
 *
 * <p>
 * This, not surprisingly, looks somewhat like a Servlet (3.x+) {@link ServletContext}, which also allows programmatic
 * dynamic registration e.g. via {@link ServletRegistration}; however in practice direct use of that API has been found
 * to be problematic under OSGi, because it is intended for JSE and
 * <a href="https://github.com/eclipse/jetty.project/issues/1395">does not easily appear to permit dynamic registration
 * at any time</a> (only during Servlet container initialization time by {@link ServletContainerInitializer}), and is
 * generally less clear to use than this simple API which intentionally maps directly to what one would have declared in
 * a web.xml file. This API is also slightly more focused and drops a number of concepts that API has which we do not
 * want to support here (including e.g. security, roles, multipart etc.)
 *
 * <p>
 * It also looks somewhat similar to the OSGi HttpService, but we want to avoid any org.osgi dependency (both API and
 * impl) here, and that API is also less clear (and uses an ancient (!) {@link java.util.Dictionary} in its method
 * signature), and -most importantly- simply does not support Filters and Listeners, only Servlets. The Pax Web API does
 * extend the base OSGi API and adds supports for Filters, Listeners and context parameters, but is still OSGi specific,
 * whereas this offers a much simpler standalone API without OSGi dependency. (The Pax Web API also has confusing
 * signatures in its registerFilter() methods, where one can easily confuse which String[] is the urlPatterns; which we
 * had initially done accidentally; and left AAA broken.)
 *
 * <p>
 * This is immutable, with a Builder, because contrary to a declarative approach in a file such as web.xml, the
 * registration order very much matters (e.g. an context parameter added after a Servlet registration would not be seen
 * by that Servlet; or a Filter added to protect a Servlet might not yet be active for an instant if the registerServlet
 * is before the registerFilter). Therefore, this API enforces atomicity and lets clients first register everything on
 * the Builder, and only then use {@link WebServer#registerWebContext(WebContext)}.
 *
 * @author Michael Vorburger.ch
 */
public interface WebContext {
    /**
     * Get path which will be used as URL prefix to all registered servlets and filters. Guaranteed to be non-empty
     *
     * @return {@link String} path
     * @see "Java Servlet Specification Version 3.1, Section 3.5 Request Path Elements"
     */
    @NonNull String contextPath();

    /**
     * Get flag value whether this context supports web sessions.
     *
     * @return boolean flag value
     */
    boolean supportsSessions();

    /**
     * Get list of servlets.
     *
     * @return {@link List} list of {@link ServletDetails}
     */
    @NonNull List<ServletDetails> servlets();

    /**
     * Get list of filters.
     *
     * @return {@link List} list of {@link FilterDetails}
     */
    @NonNull List<FilterDetails> filters();

    /**
     * Get list of servlet context listeners.
     *
     * @return {@link List} list of {@link ServletContextListener}
     */
    @NonNull List<ServletContextListener> listeners();

    /**
     * Get lis of resources (e.g. html files) that can be accessed via the URI namespace.
     *
     * @return {@link List} list of {@link ResourceDetails}
     */
    @NonNull List<ResourceDetails> resources();

    /**
     * Get map of context params.
     *
     * <p>
     * These are the {@link ServletContext}s initial parameters; contrary to individual
     * {@link ServletDetails#initParams()} and {@link FilterDetails#initParams()}. While a ServletContext accepts
     * any Object as a parameter, that is not accepted in all implementations. Most notably OSGi HTTP Whiteboard
     * specification allows only String values, hence we are enforcing that.
     *
     * @return {@link Map} context parameters map
     */
    @NonNull Map<String, String> contextParams();

    /**
     * Create builder for {@code WebContext}.
     *
     * @return {@link Builder} builder instance
     */
    static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Builds instances of type {@link WebContext WebContext}. Initialize attributes and then invoke the
     * {@link #build()} method to create an immutable instance.
     *
     * <p><em>{@code WebContext.Builder} is not thread-safe and generally should not be stored in a field or
     * collection, but instead used immediately to create instances.</em>
     */
    final class Builder {
        private record ImmutableWebContext(String contextPath, ImmutableList<ServletDetails> servlets,
            ImmutableList<FilterDetails> filters, ImmutableList<ServletContextListener> listeners,
            ImmutableList<ResourceDetails> resources, ImmutableMap<String, String> contextParams,
            boolean supportsSessions) implements WebContext {
            // Not much else here
        }

        private final ImmutableMap.Builder<String, String> contextParams = ImmutableMap.builder();
        private final ImmutableList.Builder<ServletDetails> servlets = ImmutableList.builder();
        private final ImmutableList.Builder<FilterDetails> filters = ImmutableList.builder();
        private final ImmutableList.Builder<ServletContextListener> listeners = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceDetails> resources = ImmutableList.builder();
        private String contextPath;
        private boolean supportsSessions = true;

        private Builder() {
            // Hidden on purpose
        }

        /**
         * Initializes the value for the {@link WebContext#contextPath() contextPath} attribute. As per Servlet
         *
         * @param contextPath The value for contextPath
         * @return {@code this} builder for use in a chained invocation
         * @throws IllegalArgumentException if {@code contextPath} does not meet specification criteria
         * @throws NullPointerException if {code contextPath} is {@code null}
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder contextPath(final String contextPath) {
            this.contextPath = ServletSpec.requireContextPath(contextPath);
            return this;
        }

        /**
         * Adds one element to {@link WebContext#servlets() servlets} list.
         *
         * @param servlet A servlets element
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code servlet} is {@code null}
         */
        public @NonNull Builder addServlet(final ServletDetails servlet) {
            servlets.add(servlet);
            return this;
        }

        /**
         * Adds one element to {@link WebContext#filters() filters} list.
         *
         * @param filter A filters element
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code filter} is {@code null}
         */
        public @NonNull Builder addFilter(final FilterDetails filter) {
            filters.add(filter);
            return this;
        }

        /**
         * Adds one element to {@link WebContext#listeners() listeners} list.
         *
         * @param listener A listeners element
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code listener} is {@code null}
         */
        public @NonNull Builder addListener(final ServletContextListener listener) {
            listeners.add(listener);
            return this;
        }

        /**
         * Adds one element to {@link WebContext#resources() resources} list.
         *
         * @param resource A resources element
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code resource} is {@code null}
         */
        public @NonNull Builder addResource(final ResourceDetails resource) {
            resources.add(resource);
            return this;
        }

        /**
         * Put one entry to the {@link WebContext#contextParams() contextParams} map.
         *
         * @param key The key in the contextParams map
         * @param value The associated value in the contextParams map
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if any argument is {@code null}
         */
        public @NonNull Builder putContextParam(final String key, final String value) {
            contextParams.put(key, value);
            return this;
        }

        /**
         * Initializes the value for the {@link WebContext#supportsSessions() supportsSessions} attribute.
         *
         * <p><em>If not set, this attribute will have a default value of {@code true}.</em>
         *
         * @param supportsSessions The value for supportsSessions
         * @return {@code this} builder for use in a chained invocation
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public Builder supportsSessions(final boolean supportsSessions) {
            this.supportsSessions = supportsSessions;
            return this;
        }

        /**
         * Builds a new {@link WebContext WebContext}.
         *
         * @return An immutable instance of WebContext
         * @throws IllegalStateException if any required attributes are missing
         */
        public @NonNull WebContext build() {
            if (contextPath == null) {
                throw new IllegalStateException("No contextPath specified");
            }
            return new ImmutableWebContext(contextPath, servlets.build(), filters.build(), listeners.build(),
                resources.build(), contextParams.build(), supportsSessions);
        }
    }
}
