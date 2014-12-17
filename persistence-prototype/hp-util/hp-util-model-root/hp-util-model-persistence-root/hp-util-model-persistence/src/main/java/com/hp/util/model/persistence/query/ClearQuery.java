/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.KeyValueDao;

/**
 * Query to delete all objects from the data store.
 * 
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class ClearQuery<C> implements Query<Void, C> {

    private KeyValueDao<?, ?, C> dao;

    private ClearQuery(KeyValueDao<?, ?, C> dao) {
        this.dao = dao;
    }

    /**
     * Creates a query.
     * <p>
     * This method is a convenience to infer the generic types.
     * 
     * @param dao DAO to assist the query
     * @return the query
     */
    public static <C> Query<Void, C> createQuery(KeyValueDao<?, ?, C> dao) {
        return new ClearQuery<C>(dao);
    }

    @Override
    public Void execute(C context) throws PersistenceException {
        this.dao.clear(context);
        return null;
    }
}
