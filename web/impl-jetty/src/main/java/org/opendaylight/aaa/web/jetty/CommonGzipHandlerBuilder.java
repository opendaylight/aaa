/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.jetty;

/**
 * Builds instances of type {@link CommonGzipHandler CommonGzipHandler}.
 * Initialize attributes and then invoke the {@link #build()} method to create an instance.
 *
 * <p><em>{@code CommonGzipHandlerBuilder} is not thread-safe and generally should not be stored in a field or
 * collection, but instead used immediately to create instances.</em>
 */
public class CommonGzipHandlerBuilder {

    private String[] includedMimeTypes;
    private String[] includedPaths;

    /**
     * Adds elements to included MIME types.
     * @param elements An array of included MIME type elements
     * @return {@code this} builder for use in a chained invocation
     */
    public final CommonGzipHandlerBuilder addIncludedMimeTypes(String... elements) {
        this.includedMimeTypes = elements;

        return this;
    }

    /**
     * Adds elements to included paths.
     * @param elements An array of included path elements
     * @return {@code this} builder for use in a chained invocation
     */
    public final CommonGzipHandlerBuilder addIncludedPaths(String... elements) {
        this.includedPaths = elements;

        return this;
    }

    /**
     * Builds a new {@link CommonGzipHandler CommonGzipHandler}.
     * @return An immutable instance of FilterDetails
     */
    public CommonGzipHandler build() {
        return new CommonGzipHandlerImpl(includedMimeTypes, includedPaths);
    }
}
