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
     * Builder for ServletDetails.
     * @return {@link ServletDetailsBuilder}
     */
    public static ServletDetailsBuilder builder() {
        return new ServletDetailsBuilder();
    }

    /**
     * Details about a {@link Servlet}.
     * @return {@link Servlet} instance
     */
    public abstract Servlet servlet();

    /**
     * Servlet's class name.
     * @return {@link String} name of class
     */
    @Default
    public String name() {
        return servlet().getClass().getName();
    }

    /**
     * Servlet url patterns. Used for mapping servlets. This is controls how you access a servlet.
     * <p>
     * Restrictions to urls and how it should look like are next:
     * <ul>
     * <li>A string beginning with a ‘ / ’ character and ending with a ‘ /*’ suffix is used for path mapping.
     * <li>A string beginning with a ‘ *. ’ prefix is used as an extension mapping.
     * <li>The empty string ("") is a special URL pattern that exactly maps to the application's context root, i.e.,
     * requests of the form http://host:port/"context-root"/. In this case the path info is ’ / ’ and the servlet
     * path and context path is empty string (““).
     * <li>A string containing only the ’ / ’ character indicates the "default" servlet of the application. In this case
     * the servlet path is the request URI minus the context path and the path info is null.
     * <li>All other strings are used for exact matches only.
     * </ul>
     * @return {@link List} of web resource mapping
     */
    public abstract List<String> urlPatterns();

    /**
     * Servlet initial parameters.
     * @return {@link Map} that contains initial parameters
     */
    public abstract Map<String, String> initParams();

    /**
     * Flag that used to allow async requests by AAA because SSE (Server Sent Events) use async communication.
     * @return {@link Boolean} support async requests flag
     */
    @Default
    public Boolean getAsyncSupported() {
        return false;
    }

    /**
     * Checking url patterns according to specification.
     */
    @Value.Check
    protected void check() {
        urlPatterns().forEach(pattern -> {
            assertValidServletPathSpec(pattern);
        });
    }

    private static void assertValidServletPathSpec(String servletPathSpec) {
        if (servletPathSpec != null && !servletPathSpec.equals("")) {
            int len = servletPathSpec.length();
            int idx;
            if (servletPathSpec.charAt(0) == '/') {
                if (len == 1) {
                    return;
                }

                idx = servletPathSpec.indexOf(42);
                if (idx < 0) {
                    return;
                }

                if (idx != len - 1) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: glob '*' can only exist at end of"
                            + "prefix based matches: bad spec \"" + servletPathSpec + "\"");
                }
            } else {
                if (!servletPathSpec.startsWith("*.")) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: path spec must start with \"/\" or"
                            + "\"*.\": bad spec \"" + servletPathSpec + "\"");
                }

                idx = servletPathSpec.indexOf(47);
                if (idx >= 0) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have"
                            + "path separators: bad spec \"" + servletPathSpec + "\"");
                }

                idx = servletPathSpec.indexOf(42, 2);
                if (idx >= 1) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have"
                            + "multiple glob '*': bad spec \"" + servletPathSpec + "\"");
                }
            }
        }
    }
}
