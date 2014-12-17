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
import com.hp.util.model.persistence.dao.Dao;

/**
 * Query to get the number of objects from the data store that match the given filter.
 * 
 * @param <F> type of the associated filter
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class CountQuery<F, C> implements Query<Long, C> {

    private F filter;
    private Dao<?, ?, F, ?, C> dao;

    private CountQuery(F filter, Dao<?, ?, F, ?, C> dao) {
        this.filter = filter;
        this.dao = dao;
    }

    /**
     * Creates a query.
     * <p>
     * This method is a convenience to infer the generic types.
     * 
     * @param filter filter
     * @param dao DAO to assist the query
     * @return the query
     */
    public static <F, C> Query<Long, C> createQuery(F filter, Dao<?, ?, F, ?, C> dao) {
        return new CountQuery<F, C>(filter, dao);
    }

    @Override
    public Long execute(C context) throws PersistenceException {
        return Long.valueOf(this.dao.count(this.filter, context));
    }
}
