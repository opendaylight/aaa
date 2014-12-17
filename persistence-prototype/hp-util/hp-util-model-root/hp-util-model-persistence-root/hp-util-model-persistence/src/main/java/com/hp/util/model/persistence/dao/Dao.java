/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;
import java.util.List;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Data Access Object.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter. A DAO is responsible for translating this filter to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            predicates in JPA-based implementations, or WHERE clauses in SQL-base implementations.
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications. A DAO is responsible for translating this specification to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            ORDER BY clauses in SQL-based implementations.
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Robert Gagnon
 * @author Fabiel Zuniga
 */
public interface Dao<I extends Serializable, T extends Identifiable<? super T, I>, F, S, C> extends
        KeyValueDao<I, T, C> {

    /**
     * Gets the objects from the data store that match the given filter.
     * 
     * @param filter filter to apply, {@code null} to retrieve all objects
     * @param sortSpecification sort specification
     * @param context data store context
     * @return the objects that match {@code filter} sorted as stated by {@code sortSpecification}
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public List<T> find(F filter, SortSpecification<S> sortSpecification, C context) throws PersistenceException;

    /**
     * Gets the number of objects from the data store that match the given filter.
     * 
     * @param filter filter to apply, {@code null} to count all objects
     * @param context data store context
     * @return the number of objects that match {@code filter}
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public long count(F filter, C context) throws PersistenceException;

    /**
     * Deletes all objects from the data store that match the given filter.
     * 
     * @param filter filter to apply, {@code null} to delete all objects
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void delete(F filter, C context) throws PersistenceException;
}
