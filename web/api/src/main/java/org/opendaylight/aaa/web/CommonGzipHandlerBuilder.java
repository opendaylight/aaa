/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.yangtools.concepts.Builder;

/**
 * Builds instances of type {@link CommonGzipHandler CommonGzipHandler}.
 * Initialize attributes and then invoke the {@link #build()} method to create an instance.
 *
 * <p><em>{@code CommonGzipHandlerBuilder} is not thread-safe and generally should not be stored in a field or
 * collection, but instead used immediately to create instances.</em>
 */
public class CommonGzipHandlerBuilder implements Builder<CommonGzipHandler> {

    private final List<String> includedMimeTypes = new ArrayList<>();
    private final List<String> includedPaths = new ArrayList<>();

    /**
     * Sets elements to included MIME types.
     *
     * @param elements An array of included MIME type elements
     * @return {@code this} builder for use in a chained invocation
     */
    public final CommonGzipHandlerBuilder setIncludedMimeTypes(String... elements) {
        this.includedMimeTypes.addAll(Arrays.asList(elements));

        return this;
    }

    /**
     * Sets elements to included paths.
     *
     * @param elements An array of included path elements
     * @return {@code this} builder for use in a chained invocation
     */
    public final CommonGzipHandlerBuilder setIncludedPaths(String... elements) {
        this.includedPaths.addAll(Arrays.asList(elements));

        return this;
    }

    /**
     * Builds a new {@link CommonGzipHandler CommonGzipHandler}.
     *
     * @return An immutable instance of CommonGzipHandler
     */
    @Override
    public CommonGzipHandler build() {
        return new CommonGzipHandler(ImmutableList.copyOf(includedMimeTypes), ImmutableList.copyOf(includedPaths));
    }
}
