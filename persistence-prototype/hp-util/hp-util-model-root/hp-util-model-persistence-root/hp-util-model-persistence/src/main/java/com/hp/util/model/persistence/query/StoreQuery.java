/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import java.io.Serializable;

import com.hp.util.common.Identifiable;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.BaseDao;

/**
 * Query to store an object in the data store: Adds it if it does not exists, otherwise updates it.
 * <p>
 * Note: This should not be used for persistent objects with auto-generated keys.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class StoreQuery<I extends Serializable, T extends Identifiable<? super T, I>, C> implements Query<Void, C> {

    private T identifiable;
    private BaseDao<I, T, C> dao;

    private StoreQuery(T identifiable, BaseDao<I, T, C> dao) {
        this.identifiable = identifiable;
        this.dao = dao;
    }

    /**
     * Creates a query.
     * <p>
     * This method is a convenience to infer the generic types.
     * 
     * @param identifiable object to store
     * @param dao DAO to assist the query
     * @return the query
     */
    public static <I extends Serializable, T extends Identifiable<? super T, I>, C> Query<Void, C> createQuery(
            T identifiable, BaseDao<I, T, C> dao) {
        return new StoreQuery<I, T, C>(identifiable, dao);
    }

    @Override
    public Void execute(C context) throws PersistenceException {
        if (this.dao.exist(this.identifiable.<T> getId(), context)) {
            this.dao.update(this.identifiable, context);
        }
        else {
            this.dao.create(this.identifiable, context);
        }
        return null;
    }
}
