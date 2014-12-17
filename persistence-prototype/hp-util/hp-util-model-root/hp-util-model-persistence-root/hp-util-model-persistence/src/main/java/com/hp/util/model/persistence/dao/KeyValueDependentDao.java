/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;
import java.util.Collection;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Key-Value Dependent Data Access Object.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <E> type of the owner's id. This type should be immutable and it is critical it implements
 *            {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public interface KeyValueDependentDao<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, E extends Serializable, O extends Identifiable<? super O, E>, C>
        extends BaseDependentDao<I, T, E, O, C> {

    /**
     * Loads all objects from the data store.
     * 
     * @param context data store context
     * @return all the objects from the data store
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public Collection<T> getAll(C context) throws PersistenceException;

    /**
     * Returns the number of objects from the data store.
     * 
     * @param context data store context
     * @return the objects count
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public long size(C context) throws PersistenceException;
}
