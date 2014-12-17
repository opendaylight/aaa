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
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.page.Page;
import com.hp.util.common.type.page.PageRequest;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.PagedDependentDao;

/**
 * Query to get a page of persistent objects based on the given filter.
 * 
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications
 * @param <R> type of the page request
 * @param <D> type of the page
 * @param <E> type of the owner's id
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class PagedFindDependentQuery<T extends Identifiable<? super T, ?> & Dependent<Id<O, E>>, F, S, R extends PageRequest, D extends Page<R, T>, E extends Serializable, O extends Identifiable<? super O, E>, C>
        implements Query<D, C> {

    private F filter;
    private SortSpecification<S> sortSpecification;
    private R pageRequest;
    private PagedDependentDao<?, T, F, S, R, D, E, O, C> dao;

    private PagedFindDependentQuery(F filter, SortSpecification<S> sortSpecification, R pageRequest,
            PagedDependentDao<?, T, F, S, R, D, E, O, C> dao) {
        this.filter = filter;
        this.sortSpecification = sortSpecification;
        this.pageRequest = pageRequest;
        this.dao = dao;
    }

    /**
     * Creates a query.
     * <p>
     * This method is a convenience to infer the generic types.
     * 
     * @param filter filter
     * @param sortSpecification sort specification
     * @param pageRequest page request
     * @param dao DAO to assist the query
     * @return the query
     */
    public static <E extends Serializable, T extends Identifiable<? super T, ?> & Dependent<Id<O, E>>, F, S, R extends PageRequest, D extends Page<R, T>, O extends Identifiable<? super O, E>, C> Query<D, C> createQuery(
            F filter, SortSpecification<S> sortSpecification, R pageRequest,
            PagedDependentDao<?, T, F, S, R, D, E, O, C> dao) {
        return new PagedFindDependentQuery<T, F, S, R, D, E, O, C>(filter, sortSpecification, pageRequest, dao);
    }

    @Override
    public D execute(C context) throws PersistenceException {
        return this.dao.find(this.filter, this.sortSpecification, this.pageRequest, context);
    }
}
