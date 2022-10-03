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
     * Builder for FilterDetails.
     * @return {@link FilterDetailsBuilder}
     */
    public static FilterDetailsBuilder builder() {
        return new FilterDetailsBuilder();
    }

    /**
     * Details about a {@link Filter}.
     * @return {@link Filter} instance
     */
    public abstract Filter filter();

    /**
     * Filter's class name.
     * @return {@link String} name of class
     */
    @Default
    public String name() {
        return filter().getClass().getName();
    }

    /**
     * Filter url patterns. Used for mapping filters. This is controls where filter is applied.
     * Restrictions to urls and how it should look like are same as for {@link ServletDetails#urlPatterns()}.
     * @return {@link List} of web resource mapping
     */
    public abstract List<String> urlPatterns();

    /**
     * Filter initial parameters.
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
        urlPatterns().forEach(ServletPathSpecValidator::checkUrlPattern);
    }
}
