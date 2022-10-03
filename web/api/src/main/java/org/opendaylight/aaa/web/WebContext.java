/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import java.util.List;
import java.util.Map;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Web Context with URL prefix. AKA Web App or Servlet context.
 *
 * <p>
 * Its {@link WebContextBuilder} allows programmatic web component registration
 * (as opposed to declarative e.g. via web.xml, OSGi HTTP Whiteboard blueprint
 * integration, CXF BP etc.)
 *
 * <p>
 * This is preferable because:
 * <ul>
 * <li>using code instead of hiding class names in XML enables tools such as
 * e.g. BND (in the maven-bundle-plugin) to correctly figure dependencies e.g.
 * for OSGi Import-Package headers;
 *
 * <li>explicit passing of web components instances, instead of providing class
 * names in XML files and letting a web container create the new instances using
 * the default constructor, solves a pesky dependency injection (DI) related
 * problem which typically leads to weird hoops in code through
 * <code>static</code> etc. that can be avoided using this;
 *
 * <li>tests can more easily programmatically instantiate web components.
 * </ul>
 *
 * <p>
 * This, not surprisingly, looks somewhat like a Servlet (3.x)
 * {@link ServletContext}, which also allows programmatic dynamic registration
 * e.g. via {@link ServletRegistration}; however in practice direct use of that
 * API has been found to be problematic under OSGi, because it is intended for
 * JSE and <a href="https://github.com/eclipse/jetty.project/issues/1395">does
 * not easily appear to permit dynamic registration at any time</a> (only during
 * Servlet container initialization time by
 * {@link ServletContainerInitializer}), and is generally less clear to use than
 * this simple API which intentionally maps directly to what one would have
 * declared in a web.xml file. This API is also slightly more focused and drops
 * a number of concepts that API has which we do not want to support here
 * (including e.g. security, roles, multipart etc.)
 *
 * <p>
 * It also looks somewhat similar to the OSGi HttpService, but we want to avoid
 * any org.osgi dependency (both API and impl) here, and that API is also less
 * clear (and uses an ancient (!) {@link java.util.Dictionary} in its method
 * signature), and -most importantly- simply does not support Filters and Listeners, only
 * Servlets. The Pax Web API does extend the base OSGi API and adds supports for
 * Filters, Listeners and context parameters, but is still OSGi specific,
 * whereas this offers a much simpler standalone API without OSGi dependency.
 * (The Pax Web API also has confusing signatures in its registerFilter() methods,
 * where one can easily confuse which String[] is the urlPatterns;
 * which we had initially done accidentally; and left AAA broken.)
 *
 * <p>
 * This is immutable, with a Builder, because contrary to a declarative approach
 * in a file such as web.xml, the registration order very much matters (e.g. an
 * context parameter added after a Servlet registration would not be seen by that
 * Servlet; or a Filter added to protect a Servlet might not yet be active
 * for an instant if the registerServlet is before the registerFilter).
 * Therefore, this API enforces atomicity and lets clients first register
 * everything on the Builder, and only then use
 * {@link WebServer#registerWebContext(WebContext)}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public abstract class WebContext {

    /**
     * Create builder for {@code WebContext}.
     *
     * @return {@link WebContextBuilder} builder instance
     */
    public static WebContextBuilder builder() {
        return new WebContextBuilder();
    }

    /**
     * Get path which will be used as URL prefix to all registered servlets and filters.
     *
     * @return {@link String} path
     */
    public abstract String contextPath();

    /**
     * Get flag value whether this context supports web sessions, defaults to true.
     *
     * @return boolean flag value
     */
    @Default
    public boolean supportsSessions() {
        return true;
    }

    /**
     * Get list of servlets.
     *
     * @return {@link List} list of {@link ServletDetails}
     */
    public abstract List<ServletDetails> servlets();

    /**
     * Get list of filters.
     *
     * @return {@link List} list of {@link FilterDetails}
     */
    public abstract List<FilterDetails> filters();

    /**
     * Get list of servlet context listeners.
     *
     * @return {@link List} list of {@link ServletContextListener}
     */
    public abstract List<ServletContextListener> listeners();

    /**
     * Get lis of resources (e.g. html files) that can be accessed via the URI namespace.
     *
     * @return {@link List} list of {@link ResourceDetails}
     */
    public abstract List<ResourceDetails> resources();

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
    public abstract Map<String, String> contextParams();

    /**
     * Check if filters and servlets in context are not empty.
     */
    @Value.Check
    protected void check() {
        servlets().forEach(servlet -> {
            if (servlet.urlPatterns().isEmpty()) {
                throw new IllegalArgumentException("Servlet has no URL: " + servlet.name());
            }
        });
        filters().forEach(filter -> {
            if (filter.urlPatterns().isEmpty()) {
                throw new IllegalArgumentException("Filter has no URL: " + filter.name());
            }
        });
    }
}
