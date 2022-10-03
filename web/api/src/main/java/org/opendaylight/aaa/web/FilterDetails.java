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
import javax.servlet.Filter;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Details about a {@link Filter}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public abstract class FilterDetails {

    /**
     * Create a builder for {@link FilterDetails}.
     *
     * @return {@link FilterDetailsBuilder} builder instance
     */
    public static FilterDetailsBuilder builder() {
        return new FilterDetailsBuilder();
    }

    /**
     * Get a {@link Filter} instance.
     *
     * @return {@link Filter} instance
     */
    public abstract Filter filter();

    /**
     * Get Filter's instance class name.
     *
     * @return {@link String} name of class
     */
    @Default
    public String name() {
        return filter().getClass().getName();
    }

    /**
     * Get list of Filter URL patterns.
     *
     * <p>
     * These patterns control where filter is applied.
     *
     * <p>
     * Restrictions to URLs and how it should look like are next:
     * <ul>
     * <li>A string beginning with a ‘ / ’ character and ending with a ‘ /*’ suffix is used for path mapping.
     * <li>A string beginning with a ‘ *. ’ prefix is used as an extension mapping.
     * <li>The empty string ("") is a special URL pattern that exactly maps to the application's context root, i.e.,
     * requests of the form {@code http://host:port/context-root/}. In this case the path info is ’ / ’ and the servlet
     * path and context path is empty string (““).
     * <li>A string containing only the ’ / ’ character indicates the "default" servlet of the application. In this case
     * the servlet path is the request URI minus the context path and the path info is null.
     * <li>All other strings are used for exact matches only.
     * </ul>
     *
     * <p>
     * For more info refer to <a href="https://javaee.github.io/servlet-spec/downloads/servlet-4.0/servlet-4_0_FINAL.pdf">Java Servlet Specification</a>.
     *
     * @return {@link List} of Filter URL patterns
     */
    public abstract List<String> urlPatterns();

    /**
     * Get Filter initial parameters.
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
