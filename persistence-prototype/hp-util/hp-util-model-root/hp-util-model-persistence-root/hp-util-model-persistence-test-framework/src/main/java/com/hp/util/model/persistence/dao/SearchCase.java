/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.SortSpecification;

/**
 * Search case.
 * 
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications
 * @author Fabiel Zuniga
 */
public final class SearchCase<T, F, S> {
    private List<T> searchSpace;
    private F filter;
    private SortSpecification<S> sortSpecification;
    private List<T> expectedResult;

    private SearchCase(List<T> searchSpace, F filter, SortSpecification<S> sortSpecification, List<T> expectedResult) {
        this.searchSpace = new ArrayList<T>(searchSpace);
        this.filter = filter;
        this.sortSpecification = sortSpecification;
        if (expectedResult != null) {
            this.expectedResult = Collections.unmodifiableList(new ArrayList<T>(expectedResult));
        }
        else {
            this.expectedResult = Collections.emptyList();
        }
    }

    /**
     * Creates a search case.
     * 
     * @param searchSpace a collection of identifiable objects to use in a test case. They will
     *            represent the content of the table for the test case. These identifiable objects
     *            must be a valid storable collection since they will be persisted all together. So
     *            if an attribute is unique (unique column in the database table), such attribute
     *            must be is unique for the search space. The search space can be considered as the
     *            entire table content, so it is enough to make sure unique fields are unique just
     *            for the search space; These objects will be removed from the database after the
     *            test.
     * @param filter filter to apply
     * @param sortSpecification sort specification to apply
     * @param expectedResult expected search result. This must be a subset of {@code searchSpace}
     *            using the same objects references.
     * @return search case
     */
    public static <T, F, S> SearchCase<T, F, S> forCase(List<T> searchSpace, F filter,
            SortSpecification<S> sortSpecification, List<T> expectedResult) {
        return new SearchCase<T, F, S>(searchSpace, filter, sortSpecification, expectedResult);
    }

    /**
     * Creates a search case.
     * 
     * @param searchSpace search space
     * @param filter filter to apply
     * @param sortSpecification sort specification to apply
     * @param expectedResult expected search result. This must be a subset of {@code searchSpace}
     *            using the same objects references.
     * @return search case
     */
    @SafeVarargs
    public static <T, F, S> SearchCase<T, F, S> forCase(List<T> searchSpace, F filter,
            SortSpecification<S> sortSpecification, T... expectedResult) {
        return forCase(searchSpace, filter, sortSpecification, Arrays.asList(expectedResult));
    }

    /**
     * Gets the search space.
     * 
     * @return the search space
     */
    public List<T> getSearchSpace() {
        return this.searchSpace;
    }

    /**
     * Gets the filter.
     * 
     * @return the filter
     */
    public F getFilter() {
        return this.filter;
    }

    /**
     * Gets the sort specification.
     * 
     * @return the sort specification
     */
    public SortSpecification<S> getSortSpecification() {
        return this.sortSpecification;
    }

    /**
     * Gets the expected result.
     * 
     * @return the expected result
     */
    public List<T> getExpectedResult() {
        return this.expectedResult;
    }

    /**
     * Verifies if order has been considered in this search case.
     * 
     * @return {@code true} if the expected result is ordered, {@code false} otherwise
     */
    public boolean isOrdered() {
        return this.sortSpecification != null && !this.sortSpecification.getSortComponents().isEmpty();
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(this, Property.valueOf("filter", this.filter),
                Property.valueOf("sortSpecification", this.sortSpecification),
                Property.valueOf("expectedResult", this.expectedResult),
                Property.valueOf("searchSpace", this.searchSpace));
    }
}
