/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Details about a {@link Filter}.
 *
 * @author Michael Vorburger.ch
 */
public interface FilterDetails {

    @NonNull Filter filter();

    @NonNull String name();

    @NonNull List<String> urlPatterns();

    @NonNull Map<String, String> initParams();

    boolean asyncSupported();

    static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Builds instances of type {@link FilterDetails FilterDetails}. Initialize attributes and then invoke the
     * {@link #build()} method to create an immutable instance.
     *
     * <p><em>{@code FilterDetails.Builder} is not thread-safe and generally should not be stored in a field or
     * collection, but instead used immediately to create instances.</em>
     */
    final class Builder {
        private record ImmutableFilterDetails(Filter filter, String name, ImmutableList<String> urlPatterns,
                ImmutableMap<String, String> initParams, boolean asyncSupported) implements FilterDetails {
            ImmutableFilterDetails {
                if (urlPatterns.isEmpty()) {
                    throw new IllegalStateException("No urlPattern specified");
                }
            }
        }

        private final ImmutableMap.Builder<String, String> initParams = ImmutableMap.builder();
        private final ImmutableList.Builder<String> urlPatterns = ImmutableList.builder();
        private Filter filter;
        private String name;
        private boolean asyncSupported;

        private Builder() {
            // Hidden on purpose
        }

        /**
         * Initializes the value for the {@link FilterDetails#filter() filter} attribute.
         *
         * @param filter The value for filter
         * @return {@code this} builder for use in a chained invocation
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder filter(final Filter filter) {
            this.filter = requireNonNull(filter);
            return this;
        }

        /**
         * Initializes the value for the {@link FilterDetails#name() name} attribute.
         *
         * <p><em>If not set, this attribute will have a value corresponding to {@code filter().getClass().getName()}.
         * </em>
         *
         * @param name The value for name
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code name} is {@code null}
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder name(final String name) {
            this.name = requireNonNull(name);
            return this;
        }

        /**
         * Adds one element to {@link FilterDetails#urlPatterns() urlPatterns} list.
         *
         * @param urlPattern A urlPatterns element
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code urlPattern} is {@code null}
         * @throws IllegalArgumentException if {@code urlPattern} does not meet specification criteria
         */
        public @NonNull Builder addUrlPattern(final String urlPattern) {
            ServletPathSpecValidator.checkUrlPattern(urlPattern);
            urlPatterns.add(urlPattern);
            return this;
        }

        /**
         * Put one entry to the {@link FilterDetails#initParams() initParams} map.
         *
         * @param key The key in the initParams map
         * @param value The associated value in the initParams map
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if any argument is {@code null}
         */
        public @NonNull Builder putInitParam(final String key, final String value) {
            initParams.put(key, value);
            return this;
        }

        /**
         * Initializes the value for the {@link FilterDetails#asyncSupported() asyncSupported} attribute.
         *
         * <p><em>If not set, this attribute will have a default value of {@code false}.</em>
         *
         * @param asyncSupported The value for asyncSupported
         * @return {@code this} builder for use in a chained invocation
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder asyncSupported(final boolean asyncSupported) {
            this.asyncSupported = asyncSupported;
            return this;
        }

        /**
         * Builds a new {@link FilterDetails FilterDetails}.
         *
         * @return An immutable instance of FilterDetails
         * @throws IllegalStateException if any required attributes are missing
         */
        public @NonNull FilterDetails build() {
            if (filter == null) {
                throw new IllegalStateException("No filter specified");
            }
            return new ImmutableFilterDetails(filter, name != null ? name : filter.getClass().getName(),
                urlPatterns.build(), initParams.build(), asyncSupported);
        }
    }
}
