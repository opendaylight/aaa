/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.io.Serializable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.page.OffsetPage;
import com.hp.util.common.type.page.OffsetPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.OffsetPageDao;
import com.hp.util.model.persistence.dao.UpdateStrategy;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaUtil.PredicateProvider;

/**
 * JPA {@link OffsetPageDao}.
 * <p>
 * This class must remain state-less so it is thread safe.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <P> type of the entity (an object annotated with {@link javax.persistence.Entity})
 * @param <F> type of the associated filter. A DAO is responsible for translating this filter to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            predicates in JPA-based implementations, or WHERE clauses in SQL-base implementations.
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications. A DAO is responsible for translating this specification to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            ORDER BY clauses in SQL-based implementations.
 * @author Fabiel Zuniga
 */
public abstract class JpaOffsetPageDao<I extends Serializable, T extends Identifiable<? super T, I>, P, F, S> extends
        JpaDao<I, T, P, F, S> implements OffsetPageDao<I, T, F, S, JpaContext> {

    /**
     * Construct a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     */
    protected JpaOffsetPageDao(Class<P> entityClass) {
        super(entityClass);
    }

    /**
     * Creates a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param updateStrategy update strategy
     */
    protected JpaOffsetPageDao(Class<P> entityClass, UpdateStrategy<P, T> updateStrategy) {
        super(entityClass, updateStrategy);
    }

    /**
     * Get a page of entities from the data store that match the given filter.
     * 
     * @param filter filter to apply, {@code null} to consider all entities
     * @param sortSpecification sort specification
     * @param pageRequest page request
     * @param context data store context
     * @return a page of objects that match {@code filter} sorted as stated by
     *         {@code sortSpecification}
     * @throws IndexOutOfBoundsException if the {@code pageRequest} is invalid
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected OffsetPage<P> findEntities(final F filter, SortSpecification<S> sortSpecification,
            OffsetPageRequest pageRequest,
            JpaContext context) throws IndexOutOfBoundsException, PersistenceException {
        PredicateProvider<P> predicateProvider = new PredicateProvider<P>() {
            @Override
            public Predicate getPredicate(CriteriaBuilder criteriaBuilder, Root<P> root) {
                return getQueryPredicate(filter, criteriaBuilder, root);
            }
        };
        return JpaUtil.find(getEntityClass(), predicateProvider, convertSortSpecification(sortSpecification),
                pageRequest, context);
    }

    @Override
    public OffsetPage<T> find(F filter, SortSpecification<S> sortSpecification, OffsetPageRequest pageRequest,
            JpaContext context) throws IndexOutOfBoundsException, PersistenceException {
        OffsetPage<P> entitiesPage = findEntities(filter, sortSpecification, pageRequest, context);
        return entitiesPage.convert(this);
    }
}
