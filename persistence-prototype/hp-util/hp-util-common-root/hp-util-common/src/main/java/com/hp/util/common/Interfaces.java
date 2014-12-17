/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Container for a subset of the interfaces a specific type implements.
 * 
 * @param <T> type of the object these interfaces are implemented by
 * @author Fabiel Zuniga
 */
public class Interfaces<T> {

    private Set<Class<?>> interfaces;

    private Interfaces(Set<Class<?>> interfaces) {
        this.interfaces = new HashSet<Class<?>>(interfaces);
        this.interfaces = Collections.unmodifiableSet(this.interfaces);
    }

    /**
     * Extracts all interfaces implemented by the given {@code subject}.
     * 
     * @param subject subject to get interfaces for
     * @return the set of interfaces implemented by {@code subject}
     */
    public static <T> Interfaces<T> all(Class<T> subject) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();

        if (subject.isInterface()) {
            interfaces.add(subject);
        }

        Class<?> currentClass = subject;
        do {
            interfaces.addAll(Arrays.asList(currentClass.getInterfaces()));
            currentClass = currentClass.getSuperclass();
        }
        while (currentClass != null);

        return new Interfaces<T>(interfaces);
    }

    /**
     * Extracts all interfaces implemented by the given {@code subject}.
     * 
     * @param subject subject to get interfaces for
     * @return the set of interfaces implemented by {@code subject}
     */
    public static <T> Interfaces<T> single(Class<? super T> subject) {
        Builder<T> builder = createBuilder();
        builder.add(subject);
        return builder.build();
    }

    /**
     * Creates an interfaces builder.
     * 
     * @return a builder
     */
    public static <T> Builder<T> createBuilder() {
        return new Builder<T>();
    }

    /**
     * Returns the interfaces.
     * 
     * @return the interfaces
     */
    public Set<Class<?>> get() {
        return this.interfaces;
    }

    /**
     * Interfaces builder.
     * <p>
     * This class allows creating the interfaces set in a type-safe manner.
     * 
     * @param <T> type of the object these builder creates the interfaces set for
     */
    public static class Builder<T> {
        private Set<Class<?>> interfaces;

        private Builder() {
            this.interfaces = new HashSet<Class<?>>();
        }

        /**
         * Adds an interface to the set.
         * 
         * @param theInterface interface to add
         * @return this builder to allow method chaining
         */
        public Builder<T> add(Class<? super T> theInterface) {
            if (theInterface == null) {
                throw new NullPointerException("the interface cannot be null");
            }

            if (!theInterface.isInterface()) {
                throw new IllegalArgumentException("the class provided is not an interface");
            }

            this.interfaces.add(theInterface);
            return this;
        }

        /**
         * Builds the interfaces set.
         * 
         * @return the interfaces
         */
        public Interfaces<T> build() {
            return new Interfaces<T>(this.interfaces);
        }
    }
}
