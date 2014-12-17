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
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.page.Page;
import com.hp.util.common.type.page.PageRequest;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Dependent Data Access Object that supports paging.
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
 * @param <R> type of the page request
 * @param <D> type of the page
 * @param <E> type of the owner's id. This type should be immutable and it is critical it implements
 *            {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public interface PagedDependentDao<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, F, S, R extends PageRequest, D extends Page<R, T>, E extends Serializable, O extends Identifiable<? super O, E>, C>
        extends DependentDao<I, T, F, S, E, O, C> {

    /**
     * Get a page of persistent objects based on the given filter.
     * 
     * @param filter filter
     * @param sortSpecification sort specification
     * @param pageRequest page request
     * @param context data store context
     * @return A page of persistent object instances
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public D find(F filter, SortSpecification<S> sortSpecification, R pageRequest, C context)
            throws PersistenceException;
}
