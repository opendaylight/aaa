/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import com.hp.util.common.Identifiable;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.BaseDao;

/**
 * Query to add an object to the data store.
 * 
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class AddQuery<T extends Identifiable<? super T, ?>, C> implements Query<T, C> {

    private T identifiable;
    private BaseDao<?, T, C> dao;

    private AddQuery(T identifiable, BaseDao<?, T, C> dao) {
        this.identifiable = identifiable;
        this.dao = dao;
    }

    /**
     * Creates a query.
     * <p>
     * This method is a convenience to infer the generic types.
     * 
     * @param identifiable object to add
     * @param dao DAO to assist the query
     * @return the query
     */
    public static <T extends Identifiable<? super T, ?>, C> Query<T, C> createQuery(T identifiable,
            BaseDao<?, T, C> dao) {
        return new AddQuery<T, C>(identifiable, dao);
    }

    @Override
    public T execute(C context) throws PersistenceException {
        return this.dao.create(this.identifiable, context);
    }
}
