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

import junit.framework.Assert;

import org.junit.Assume;
import org.junit.Test;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.page.OffsetPage;
import com.hp.util.common.type.page.OffsetPageRequest;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;

/**
 * Integration test for {@link OffsetPageDependentDao} implementations.
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
public abstract class OffsetPageDependentDaoTest<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, F, S, E extends Serializable, O extends Identifiable<? super O, E>, C, D extends OffsetPageDependentDao<I, T, F, S, E, O, C>>
        extends DependentDaoTest<I, T, F, S, E, O, C, D> {

    /**
     * Creates a new dependent DAO integration test.
     * 
     * @param dataStore data store
     */
    public OffsetPageDependentDaoTest(DataStore<C> dataStore) {
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

            for (int limit = 1; limit <= totalRecords; limit++) {
                int totalPages = totalRecords / limit;

                // handle extra non-full page at the end
                if (totalPages * limit < totalRecords) {
                    totalPages = totalPages + 1;
                }

                // Checks each page

                // Search result will contain the aggregated records from all pages to compare at
                // the end
                List<T> aggregatedResult = new ArrayList<T>(totalRecords);

                for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {

                    long offset = pageIndex * limit;
                    final OffsetPageRequest pageRequest = new OffsetPageRequest(offset, limit);

                    OffsetPage<T> resultPage = execute(new DaoQuery<OffsetPage<T>>() {

                        @Override
                        protected OffsetPage<T> execute(D dao, C context) throws PersistenceException {
                            return dao.find(searchCase.getFilter(), searchCase.getSortSpecification(), pageRequest,
                                    context);
                        }
                    });

                    // Compares result

                    Assert.assertEquals(getMessage(searchCase), totalPages, resultPage.getTotalPageCount());
                    Assert.assertEquals(getMessage(searchCase), totalRecords, resultPage.getTotalRecordCount());
                    if (pageIndex < totalPages - 1) {
                        Assert.assertEquals(getMessage(searchCase), limit, resultPage.getData().size());
                    }
                    else {
                        Assert.assertEquals(getMessage(searchCase), totalRecords - (pageIndex * limit), resultPage
                                .getData().size());
                    }

                    aggregatedResult.addAll(resultPage.getData());
                }

                assertSearch(searchCase.getExpectedResult(), aggregatedResult, persistedSearchSpace,
                        searchCase.isOrdered(), getMessage(searchCase));
            }

            clearThroughOwner();
        }
    }
}
