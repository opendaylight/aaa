/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Details about a resource registration.
 *
 * @author Thomas Pantelis
 */
public interface ResourceDetails {
    /**
     * Get resource base name.
     *
     * <p>
     * The base name of the resources that will be registered, typically a directory in the bundle/jar where "/"
     * is used to denote the root.
     *
     * @return {@link String} base name
     */
    @NonNull String name();

    /**
     * Get resource mapped alias.
     *
     * <p>
     * The name in the URI namespace to which the resources are mapped. This defaults to the {@link #name()}.
     *
     * @return {@link String} mapped alias
     */
    @NonNull String alias();

    /**
     * Create builder for {@code ResourceDetails}.
     *
     * @return {@link Builder} builder instance
     */
    static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Builds instances of type {@link ResourceDetails ResourceDetails}. Initialize attributes and then invoke the
     * {@link #build()} method to create an immutable instance.
     *
     * <p><em>{@code ResourceDetails.Builder} is not thread-safe and generally should not be stored in a field or
     * collection, but instead used immediately to create instances.</em>
     */
    final class Builder {
        private record ImmutableResourceDetails(String name, String alias) implements ResourceDetails {
            // Not much else here
        }

        private String name;
        private String alias;

        private Builder() {
            // Hidden on purpose
        }

        /**
         * Initializes the value for the {@link ResourceDetails#name() name} attribute.
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
         * Initializes the value for the {@link ResourceDetails#alias() alias} attribute.
         *
         * <p><em>If not set, this attribute will have the same as {@link ResourceDetails#name() name}.</em>
         *
         * @param alias The value for alias
         * @return {@code this} builder for use in a chained invocation
         * @throws NullPointerException if {code alias} is {@code null}
         */
        @SuppressWarnings("checkstyle:hiddenField")
        public @NonNull Builder alias(final String alias) {
            this.alias = requireNonNull(alias);
            return this;
        }

        /**
         * Builds a new {@link ResourceDetails ResourceDetails}.
         *
         * @return An immutable instance of ResourceDetails
         * @throws IllegalStateException if any required attributes are missing
         */
        public @NonNull ResourceDetails build() {
            if (name == null) {
                throw new IllegalStateException("name not specified");
            }
            return new ImmutableResourceDetails(name, alias == null ? name : alias);
        }
    }
}
