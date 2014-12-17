/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.hp.util.common.Converter;
import com.hp.util.common.ParameterizedFactory;

/**
 * Collection converter.
 * 
 * @author Fabiel Zuniga
 */
public final class CollectionConverter {

    private CollectionConverter() {

    }

    /**
     * Convert a collection.
     * <P>
     * if {@code source} is {@code null}, an empty collection will be returned.
     * 
     * @param source list to convert
     * @param converter converter
     * @param factory factory to create the instance of the returned collection; the factory input
     *            is the suggested collection capacity
     * @return a list where its elements were converted by {@code converter}
     * @throws NullPointerException if either {@code converter} or {@code collectionFactory} is
     *             {@code null}
     */
    public static <S, T, C extends Collection<T>> C convert(Collection<S> source, Converter<S, T> converter,
            ParameterizedFactory<C, Integer> factory) throws NullPointerException {
        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }

        if (factory == null) {
            throw new NullPointerException("factory cannot be null");
        }

        int capacity = source != null ? source.size() : 0;
        C target = factory.create(Integer.valueOf(capacity));

        if (source != null) {
            for (S s : source) {
                target.add(converter.convert(s));
            }
        }

        return target;
    }

    /**
     * Gets an {@link List} factory where the underlying implementation is an {@link ArrayList}.
     * 
     * @return a {@link List} factory
     */
    public static <T> ParameterizedFactory<List<T>, Integer> getArrayListFactory() {
        return ArrayListFactory.getInstance();
    }

    /**
     * Gets an {@link List} factory where the underlying implementation is a {@link LinkedList}.
     * 
     * @return a {@link List} factory
     */
    public static <T> ParameterizedFactory<List<T>, Integer> getLinkedListFactory() {
        return LinkedListFactory.getInstance();
    }

    /**
     * Gets an {@link Set} factory where the underlying implementation is a {@link HashSet}.
     * 
     * @return a {@link Set} factory
     */
    public static <T> ParameterizedFactory<Set<T>, Integer> getHashSetFactory() {
        return HashSetFactory.getInstance();
    }

    /**
     * Gets an {@link Set} factory where the underlying implementation is a {@link TreeSet}.
     * 
     * @return a {@link Set} factory
     */
    public static <T> ParameterizedFactory<Set<T>, Integer> getTreeSetFactory() {
        return TreeSetFactory.getInstance();
    }

    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */

    private static class ArrayListFactory<T> implements ParameterizedFactory<List<T>, Integer> {
        @SuppressWarnings("rawtypes")
        private static final ParameterizedFactory INSTANCE = new ArrayListFactory();

        private ArrayListFactory() {

        }

        /**
         * Gets the only instance of this class.
         * 
         * @return the only instance of this class
         */
        @SuppressWarnings("unchecked")
        public static <T> ParameterizedFactory<List<T>, Integer> getInstance() {
            return INSTANCE;
        }

        @Override
        public List<T> create(Integer capacity) {
            if (capacity.intValue() == 0) {
                return Collections.emptyList();
            }
            return new ArrayList<T>(capacity.intValue());
        }
    }

    private static class LinkedListFactory<T> implements ParameterizedFactory<List<T>, Integer> {
        @SuppressWarnings("rawtypes")
        private static final ParameterizedFactory INSTANCE = new LinkedListFactory();

        private LinkedListFactory() {

        }

        /**
         * Gets the only instance of this class.
         * 
         * @return the only instance of this class
         */
        @SuppressWarnings("unchecked")
        public static <T> ParameterizedFactory<List<T>, Integer> getInstance() {
            return INSTANCE;
        }

        @Override
        public List<T> create(Integer capacity) {
            if (capacity.intValue() == 0) {
                return Collections.emptyList();
            }
            return new LinkedList<T>();
        }
    }

    private static class HashSetFactory<T> implements ParameterizedFactory<Set<T>, Integer> {
        @SuppressWarnings("rawtypes")
        private static final ParameterizedFactory INSTANCE = new HashSetFactory();

        private HashSetFactory() {

        }

        /**
         * Gets the only instance of this class.
         * 
         * @return the only instance of this class
         */
        @SuppressWarnings("unchecked")
        public static <T> ParameterizedFactory<Set<T>, Integer> getInstance() {
            return INSTANCE;
        }

        @Override
        public Set<T> create(Integer capacity) {
            if (capacity.intValue() == 0) {
                return Collections.emptySet();
            }
            return new HashSet<T>(capacity.intValue());
        }
    }

    private static class TreeSetFactory<T> implements ParameterizedFactory<Set<T>, Integer> {
        @SuppressWarnings("rawtypes")
        private static final ParameterizedFactory INSTANCE = new TreeSetFactory();

        private TreeSetFactory() {

        }

        /**
         * Gets the only instance of this class.
         * 
         * @return the only instance of this class
         */
        @SuppressWarnings("unchecked")
        public static <T> ParameterizedFactory<Set<T>, Integer> getInstance() {
            return INSTANCE;
        }

        @Override
        public Set<T> create(Integer capacity) {
            if (capacity.intValue() == 0) {
                return Collections.emptySet();
            }
            return new TreeSet<T>();
        }
    }
}
