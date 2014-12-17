/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Base Data Access Object for a storable that is a dependent object (Cannot exist without the
 * owner) in a relation.
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
public interface BaseDependentDao<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, E extends Serializable, O extends Identifiable<? super O, E>, C> {

    /*
     * Unfortunately there is no common interface to extend because of creating and deleting depends
     * is done through the owner.
     */

    /**
     * Updates the given object in the data store
     * 
     * @param identifiable object to store
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void update(T identifiable, C context) throws PersistenceException;

    /**
     * Loads the object with the given id from the data store.
     * 
     * @param id object's id
     * @param context data store context
     * @return the object if found, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public T get(Id<T, I> id, C context) throws PersistenceException;

    /**
     * Verifies if an object with the given id exists in the data store.
     * 
     * @param id object's id
     * @param context data store context
     * @return {@code true} if an object with the given id already exists, {@code false} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public boolean exist(Id<T, I> id, C context) throws PersistenceException;
}
