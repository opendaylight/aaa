/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.tuple.UnaryTuple;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;

/**
 * Integration test for {@link MarkPageDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class MarkPageDaoTest<I extends Serializable, T extends Identifiable<? super T, I>, F, S, C, D extends MarkPageDao<I, T, F, S, C>>
        extends DaoTest<I, T, F, S, C, D> {

    /**
     * Creates a new DAO integration test.
     * 
     * @param dataStore data store
     */
    public MarkPageDaoTest(DataStore<C> dataStore) {
        super(dataStore);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testPagedFind() throws Exception {
        List<SearchCase<T, F, S>> searchCases = getSearchCases();
        Assume.assumeNotNull(searchCases);

        for (final SearchCase<T, F, S> searchCase : searchCases) {
            // Persists the search space

            final Map<I, StoredObject<T>> persistedSearchSpace = store(searchCase.getSearchSpace());

            // Performs paged find

            // Try different page sizes

            int totalRecords = searchCase.getExpectedResult().size();

            for (int size = 1; size <= totalRecords; size++) {
                int totalPages = totalRecords / size;

                // handle extra non-full page at the end
                if (totalPages * size < totalRecords) {
                    totalPages = totalPages + 1;
                }

                // Checks each page

                // Search result will contain the aggregated records from all pages to compare at
                // the end
                List<T> aggregatedResult = new ArrayList<T>(totalRecords);

                MarkPage<T> page = null;
                final UnaryTuple<MarkPageRequest<T>> pageRequest = UnaryTuple.valueOf(new MarkPageRequest<T>(size));

                do {

                    page = execute(new DaoQuery<MarkPage<T>>() {

                        @Override
                        protected MarkPage<T> execute(D dao, C context) throws PersistenceException {
                            return dao.find(searchCase.getFilter(), searchCase.getSortSpecification(),
                                    pageRequest.getFirst(), context);
                        }
                    });

                    aggregatedResult.addAll(page.getData());

                    pageRequest.setFirst(page.getNextPageRequest());

                }
                while (!page.getData().isEmpty());

                assertSearch(searchCase.getExpectedResult(), aggregatedResult, persistedSearchSpace,
                        searchCase.isOrdered(), getMessage(searchCase));
            }

            clear();
        }
    }
}
