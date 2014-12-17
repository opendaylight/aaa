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
import com.hp.util.model.persistence.dao.DependentDao;

/**
 * Query that returns the number of objects from the data store.
 * 
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class SizeDependentQuery<C> implements Query<Long, C> {

    private DependentDao<?, ?, ?, ?, ?, ?, C> dao;

    private SizeDependentQuery(DependentDao<?, ?, ?, ?, ?, ?, C> dao) {
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
    public static <C> Query<Long, C> createQuery(DependentDao<?, ?, ?, ?, ?, ?, C> dao) {
        return new SizeDependentQuery<C>(dao);
    }

    @Override
    public Long execute(C context) throws PersistenceException {
        return Long.valueOf(this.dao.size(context));
    }
}
