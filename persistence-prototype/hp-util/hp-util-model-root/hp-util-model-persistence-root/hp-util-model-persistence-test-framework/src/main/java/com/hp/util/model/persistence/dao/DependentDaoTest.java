/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Assume;
import org.junit.Test;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;

/**
 * Integration test for {@link DependentDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications
 * @param <E> type of the owner's id
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class DependentDaoTest<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, F, S, E extends Serializable, O extends Identifiable<? super O, E>, C, D extends DependentDao<I, T, F, S, E, O, C>>
        extends KeyValueDependentDaoTest<I, T, E, O, C, D> {

    /**
     * Creates a new dependent DAO integration test.
     * 
     * @param dataStore data store
     */
    public DependentDaoTest(DataStore<C> dataStore) {
        super(dataStore);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testFind() throws Exception {
        List<SearchCase<T, F, S>> searchCases = getSearchCases();
        Assume.assumeNotNull(searchCases);

        for (final SearchCase<T, F, S> searchCase : searchCases) {
            final Map<I, StoredObject<T>> persistedSearchSpace = store(searchCase.getSearchSpace());

            List<T> searchResult = execute(new DaoQuery<List<T>>() {
                @Override
                protected List<T> execute(D dao, C context) throws PersistenceException {
                    return dao.find(searchCase.getFilter(), searchCase.getSortSpecification(), context);
                }
            });

            assertSearch(searchCase.getExpectedResult(), searchResult, persistedSearchSpace, searchCase.isOrdered(),
                    getMessage(searchCase));
            clearThroughOwner();
        }
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testCount() throws Exception {
        List<SearchCase<T, F, S>> searchCases = getSearchCases();
        Assume.assumeNotNull(searchCases);

        for (final SearchCase<T, F, S> searchCase : searchCases) {
            store(searchCase.getSearchSpace());

            Long count = execute(new DaoQuery<Long>() {
                @Override
                protected Long execute(D dao, C context) throws PersistenceException {
                    return Long.valueOf(dao.count(searchCase.getFilter(), context));
                }
            });

            Assert.assertEquals(getMessage(searchCase), searchCase.getExpectedResult().size(), count.longValue());
            clearThroughOwner();
        }
    }

    /**
     * Gets a message for the given search case.
     * 
     * @param searchCase Search case to get a message for
     * @return a message to use when a search case fails
     */
    protected static String getMessage(SearchCase<?, ?, ?> searchCase) {
        return searchCase.toString();
    }

    /**
     * Gets the test cases to run when testing methods with filters.
     * 
     * @return the test cases to run when testing methods with filters
     */
    protected abstract List<SearchCase<T, F, S>> getSearchCases();
}
