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
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.DependentDao;

/**
 * Query to verify if an object with the given id exists in the data store.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <E> type of the owner's id
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class ExistDependentQuery<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, E extends Serializable, O extends Identifiable<? super O, E>, C>
        implements Query<Boolean, C> {

    private Id<T, I> id;
    private DependentDao<I, T, ?, ?, E, O, C> dao;

    private ExistDependentQuery(Id<T, I> id, DependentDao<I, T, ?, ?, E, O, C> dao) {
        this.id = id;
        this.dao = dao;
    }

    /**
     * Creates a query.
     * <p>
     * This method is a convenience to infer the generic types.
     * 
     * @param id the object's id
     * @param dao DAO to assist the query
     * @return the query
     */
    public static <E extends Serializable, I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, O extends Identifiable<? super O, E>, C> Query<Boolean, C> createQuery(
            Id<T, I> id, DependentDao<I, T, ?, ?, E, O, C> dao) {
        return new ExistDependentQuery<I, T, E, O, C>(id, dao);
    }

    @Override
    public Boolean execute(C context) throws PersistenceException {
        return Boolean.valueOf(this.dao.exist(this.id, context));
    }
}
