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
import javax.servlet.Servlet;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Details about a {@link Servlet}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public abstract class ServletDetails {

    /**
     * Create a builder for {@link ServletDetails}.
     *
     * @return {@link ServletDetailsBuilder} builder instance
     */
    public static ServletDetailsBuilder builder() {
        return new ServletDetailsBuilder();
    }

    /**
     * Get a {@link Servlet} instance.
     *
     * @return {@link Servlet} instance
     */
    public abstract Servlet servlet();

    /**
     * Get Servlet's instance class name.
     *
     * @return {@link String} name of class
     */
    @Default
    public String name() {
        return servlet().getClass().getName();
    }

    /**
     * Get list of servlet URL patterns.
     *
     * <p>
     * These patterns control how you access a servlet.
     *
     * <p>
     * Restrictions to URLs and how it should look like are next:
     * <ul>
     * <li>A string beginning with a ‘ / ’ character and ending with a ‘ /*’ suffix is used for path mapping.
     * <li>A string beginning with a ‘ *. ’ prefix is used as an extension mapping.
     * <li>The empty string ("") is a special URL pattern that exactly maps to the application's context root, i.e.,
     * requests of the form {@code http://host:port/context-root}. In this case the path info is ’ / ’ and the servlet
     * path and context path is empty string (““).
     * <li>A string containing only the ’ / ’ character indicates the "default" servlet of the application. In this case
     * the servlet path is the request URI minus the context path and the path info is null.
     * <li>All other strings are used for exact matches only.
     * </ul>
     *
     * <p>
     * For more info refer to <a href="https://javaee.github.io/servlet-spec/downloads/servlet-4.0/servlet-4_0_FINAL.pdf">Java Servlet Specification</a>.
     *
     * @return {@link List} of Servlet URL patterns
     */
    public abstract List<String> urlPatterns();

    /**
     * Get Servlet initial parameters.
     *
     * @return {@link Map} that contains initial parameters
     */
    public abstract Map<String, String> initParams();

    /**
     * Get flag to see if async support is on.
     *
     * <p>
     * Flag is used to allow async requests by AAA via SSE (Server Sent Events) async communication.
     *
     * @return {@link Boolean} support async requests flag
     */
    @Default
    public Boolean getAsyncSupported() {
        return false;
    }

    /**
     * Check url patterns according to Java servlet specification.
     */
    @Value.Check
    protected void check() {
        urlPatterns().forEach(ServletPathSpecValidator::checkUrlPattern);
    }
}
