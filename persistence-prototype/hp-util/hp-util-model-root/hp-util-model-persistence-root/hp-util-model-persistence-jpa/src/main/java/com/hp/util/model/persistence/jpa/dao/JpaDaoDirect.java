/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.Converter;
import com.hp.util.common.Identifiable;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.BaseDao;
import com.hp.util.model.persistence.dao.Dao;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaUtil.PredicateProvider;

/**
 * {@link BaseDao} where the data transfer object pattern is not used and thus the
 * {@link Identifiable} and the entity (the object annotated with {@link javax.persistence.Entity})
 * are the same object.
 * <p>
 * This class must remain state-less so it is thread safe.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable and entity object (object to store in the data store
 *            annotated with {@link javax.persistence.Entity})
 * @param <F> type of the associated filter. A DAO is responsible for translating this filter to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            predicates in JPA-based implementations, or WHERE clauses in SQL-base implementations.
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications. A DAO is responsible for translating this specification to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            ORDER BY clauses in SQL-based implementations.
 * @author Fabiel Zuniga
 */
public abstract class JpaDaoDirect<I extends Serializable, T extends Identifiable<? super T, I>, F, S> extends
        JpaKeyValueDaoDirect<I, T> implements Dao<I, T, F, S, JpaContext> {

    // QueryPredicateGenerator is state-less, so this class remains thread safe.
    private final JpaQueryPredicateGenerator<T> queryPredicateGenerator = JpaQueryPredicateGenerator.getInstance();

    // sortKeyConverter is state-less, so this class remains thread safe.
    private Converter<S, SingularAttribute<? super T, ?>> sortKeyConverter = new SortKeyConverter();

    /**
     * Construct a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     */
    protected JpaDaoDirect(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    public List<T> find(final F filter, SortSpecification<S> sortSpecification, JpaContext context)
            throws PersistenceException {
        PredicateProvider<T> predicateProvider = new PredicateProvider<T>() {
            @Override
            public Predicate getPredicate(CriteriaBuilder criteriaBuilder, Root<T> root) {
                return getQueryPredicate(filter, criteriaBuilder, root);
            }
        };
        return JpaUtil.find(getEntityClass(), predicateProvider, convertSortSpecification(sortSpecification), context);
    }

    @Override
    public long count(final F filter, JpaContext context) throws PersistenceException {
        PredicateProvider<T> predicateProvider = new PredicateProvider<T>() {
            @Override
            public Predicate getPredicate(CriteriaBuilder criteriaBuilder, Root<T> root) {
                return getQueryPredicate(filter, criteriaBuilder, root);
            }
        };
        return JpaUtil.count(getEntityClass(), predicateProvider, context);
    }

    @Override
    public void delete(final F filter, JpaContext context) throws PersistenceException {
        PredicateProvider<T> predicateProvider = new PredicateProvider<T>() {
            @Override
            public Predicate getPredicate(CriteriaBuilder criteriaBuilder, Root<T> root) {
                return getQueryPredicate(filter, criteriaBuilder, root);
            }
        };

        JpaUtil.delete(getEntityClass(), predicateProvider, context);
    }

    /**
     * Gets a helper class to create query predicates based on filters.
     * 
     * @return A query pedicateGenerator
     */
    protected JpaQueryPredicateGenerator<T> getQueryPredicateGenerator() {
        return this.queryPredicateGenerator;
    }

    /**
     * Converts the sort specification to a singular attribute based one.
     * 
     * @param source sort specification to convert
     * @return the converted sort specification
     */
    protected SortSpecification<SingularAttribute<? super T, ?>> convertSortSpecification(SortSpecification<S> source) {
        if (source == null) {
            return null;
        }
        return source.convert(this.sortKeyConverter);
    }

    /**
     * Augment the underlying database query with specifics to be included as part of the where
     * clause.
     * 
     * @param filter The input filter from which the criteria is set
     * @param builder the criteria builder object for augmenting the query object
     * @param root the root object type associated with the query
     * @return predicate object to be utilized in the query
     */
    protected abstract Predicate getQueryPredicate(F filter, CriteriaBuilder builder, Root<T> root);

    /**
     * Gets the singular attribute (JPA entity attribute -Column- definition)
     * 
     * @param sortKey sort key to map a column to
     * @return the singular attribute associated to the sorting key
     */
    protected abstract SingularAttribute<? super T, ?> getSingularAttribute(S sortKey);

    private class SortKeyConverter implements Converter<S, SingularAttribute<? super T, ?>> {

        @Override
        public SingularAttribute<? super T, ?> convert(S source) {
            return getSingularAttribute(source);
        }
    }
}
