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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

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
 * @param <E> type of the owner's id
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class KeyValueDependentDaoTest<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, E extends Serializable, O extends Identifiable<? super O, E>, C, D extends KeyValueDependentDao<I, T, E, O, C>>
        extends BaseDependentDaoTest<I, T, E, O, C, D> {

    /**
     * Creates a new DAO integration test.
     * 
     * @param dataStore data store
     */
    public KeyValueDependentDaoTest(DataStore<C> dataStore) {
        super(dataStore);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testGetAll() throws Exception {
        List<T> expected = createIdentifiables(5);
        final Map<I, StoredObject<T>> searchSpace = store(expected);
        Assert.assertEquals(expected.size(), size());

        Collection<T> actual = execute(new DaoQuery<Collection<T>>() {
            @Override
            protected Collection<T> execute(D dao, C context) throws PersistenceException {
                return dao.getAll(context);
            }
        });

        assertSearch(expected, new ArrayList<T>(actual), searchSpace, false, null);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testSize() throws Exception {
        List<T> expected = createIdentifiables(5);
        store(expected);
        Assert.assertEquals(expected.size(), size());
    }

    @Override
    protected long size() throws Exception {
        return execute(new DaoQuery<Long>() {
            @Override
            protected Long execute(D dao, C context) throws PersistenceException {
                return Long.valueOf(dao.size(context));
            }
        }).longValue();
    }
}
