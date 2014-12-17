/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.SortSpecification.SortComponent;
import com.hp.util.common.type.page.OffsetPage;
import com.hp.util.common.type.page.OffsetPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.jpa.JpaContext;

/**
 * JPA utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class JpaUtil {

    private JpaUtil() {

    }

    /**
     * Persists an entity.
     * 
     * @param entity entity to persist (an object annotated with {@link javax.persistence.Entity})
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> void persist(P entity, JpaContext context) throws PersistenceException {
        if (entity == null) {
            throw new NullPointerException("entity cannot be null");
        }

        try {
            context.getEntityManager().persist(entity);
        }
        catch (Exception e) {
            throw new PersistenceException("Unable to persist entity", e);
        }
    }

    /**
     * Loads an entity.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param id entity's id
     * @param context data store context
     * @return the entity if found, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P, I> P get(Class<P> entityClass, I id, JpaContext context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        try {
            return context.getEntityManager().find(entityClass, id);
        }
        catch (Exception e) {
            throw new PersistenceException("Unable to find entity", e);
        }
    }

    /**
     * Updates a detached entity. To update an attached entity it is only required to set the new
     * values for its attributes; JPA will update it automatically in the database as long as it is
     * attached.
     * 
     * @param entity entity to attach (an object annotated with {@link javax.persistence.Entity})
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> void updateDetached(P entity, JpaContext context) throws PersistenceException {
        if (entity == null) {
            throw new NullPointerException("entity cannot be null");
        }

        try {
            context.getEntityManager().merge(entity);
        }
        catch (Exception e) {
            throw new PersistenceException("Unable to merge entity", e);
        }
    }

    /**
     * Deletes an entity.
     * 
     * @param entity entity to delete (an object annotated with {@link javax.persistence.Entity})
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> void delete(P entity, JpaContext context) throws PersistenceException {
        if (entity == null) {
            throw new NullPointerException("entity cannot be null");
        }

        try {
            context.getEntityManager().remove(entity);
        }
        catch (Exception e) {
            throw new PersistenceException("Unable to delete entity", e);
        }
    }

    /**
     * Verifies if an entity with the given id exists in the database.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param id entity's id
     * @param context data store context
     * @return {@code true} if an object with the given id already exists, {@code false} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P, I> boolean exist(Class<P> entityClass, I id, JpaContext context) throws PersistenceException {
        // TODO: There is no need of reading the entire object
        return get(entityClass, id, context) != null;
    }

    /**
     * Loads all entities.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param context data store context
     * @return all the entities from the database
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> Collection<P> loadAll(Class<P> entityClass, JpaContext context) throws PersistenceException {
        return find(entityClass, null, null, context);
    }

    /**
     * Returns the number of entities in the database.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param context data store context
     * @return the entities count
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static long size(Class<?> entityClass, JpaContext context) throws PersistenceException {
        return count(entityClass, null, context);
    }

    /**
     * Get the entities that match the given predicate.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param predicateProvider predicate provider, {@code null} to consider all entities
     * @param sortSpecification sort specification
     * @param context data store context
     * @return the entities that match {@code predicate} sorted as stated by
     *         {@code sortSpecification}
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> List<P> find(Class<P> entityClass, PredicateProvider<P> predicateProvider,
            SortSpecification<SingularAttribute<? super P, ?>> sortSpecification, JpaContext context)
            throws PersistenceException {
        CriteriaBuilder criteriaBuilder = context.getEntityManager().getCriteriaBuilder();

        CriteriaQuery<P> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<P> root = criteriaQuery.from(entityClass);

        if (predicateProvider != null) {
            Predicate predicate = predicateProvider.getPredicate(criteriaBuilder, root);
            if (predicate != null) {
                criteriaQuery.where(predicate);
            }
        }

        if (sortSpecification != null) {
            List<Order> queryOrder = getOrder(sortSpecification, criteriaBuilder, root);
            if (queryOrder != null && !queryOrder.isEmpty()) {
                criteriaQuery.orderBy(queryOrder);
            }
        }

        try {
            TypedQuery<P> typedQuery = context.getEntityManager().createQuery(criteriaQuery);
            List<P> entities = typedQuery.getResultList();
            return entities;
        }
        catch (Exception e) {
            throw new PersistenceException("Unable to find entities", e);
        }
    }

    /**
     * Gets the number of entities that match the given predicate.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param predicateProvider predicate provider, {@code null} to consider all entities
     * @param context data store context
     * @return the number of entities that match {@code predicate}
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> long count(Class<P> entityClass, PredicateProvider<P> predicateProvider, JpaContext context)
            throws PersistenceException {
        CriteriaBuilder criteriaBuilder = context.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<P> root = criteriaQuery.from(entityClass);
        if (predicateProvider != null) {
            Predicate predicate = predicateProvider.getPredicate(criteriaBuilder, root);
            if (predicate != null) {
                criteriaQuery.where(predicate);
            }
        }

        criteriaQuery.select(criteriaBuilder.count(root));

        try {
            Long count = context.getEntityManager().createQuery(criteriaQuery).getSingleResult();
            return count.longValue();
        }
        catch (Exception e) {
            throw new PersistenceException("Unable to count entities", e);
        }
    }

    /**
     * Gets a page of entities that match the given predicate.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param predicateProvider predicate provider, {@code null} to consider all entities
     * @param sortSpecification sort specification
     * @param pageRequest page request
     * @param context data store context
     * @return a page of entities that match {@code predicate} sorted as stated by
     *         {@code sortSpecification}
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> OffsetPage<P> find(Class<P> entityClass, PredicateProvider<P> predicateProvider,
            SortSpecification<SingularAttribute<? super P, ?>> sortSpecification, OffsetPageRequest pageRequest,
            JpaContext context) throws PersistenceException {
        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        long totalRecords = count(entityClass, predicateProvider, context);

        /*
         * JPA restricts offset to be integer, so it is not possible to define very small pages in
         * big data (last pages would not be accessible if the index doesn't fit in an integer).
         */
        int offset = (int) pageRequest.getOffset();
        if (offset != pageRequest.getOffset()) {
            throw new UnsupportedOperationException("offsets greater than integer are not suported: " + pageRequest);
        }

        if (totalRecords > 0) {
            CriteriaBuilder criteriaBuilder = context.getEntityManager().getCriteriaBuilder();

            CriteriaQuery<P> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<P> root = criteriaQuery.from(entityClass);

            if (predicateProvider != null) {
                Predicate predicate = predicateProvider.getPredicate(criteriaBuilder, root);
                if (predicate != null) {
                    criteriaQuery.where(predicate);
                }
            }

            if (sortSpecification != null) {
                List<Order> queryOrder = getOrder(sortSpecification, criteriaBuilder, root);
                if (queryOrder != null && !queryOrder.isEmpty()) {
                    criteriaQuery.orderBy(queryOrder);
                }
            }

            try {
                TypedQuery<P> typedQuery = context.getEntityManager().createQuery(criteriaQuery);

                typedQuery.setFirstResult(offset);
                typedQuery.setMaxResults(pageRequest.getSize());

                List<P> results = typedQuery.getResultList();

                return new OffsetPage<P>(pageRequest, results, totalRecords);
            }
            catch (Exception e) {
                throw new PersistenceException("Unable to find entities", e);
            }
        }

        return OffsetPage.emptyPage();
    }

    /**
     * Deletes all entities that match the given predicate.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param predicateProvider predicate provider, {@code null} to consider all entities
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <P> void delete(Class<P> entityClass, PredicateProvider<P> predicateProvider, JpaContext context)
            throws PersistenceException {
        /*
         * TODO: Implement a query that uses the predicate instead. The problem is that relations
         * created by JPA (Like value type collections) must be also deleted.
         */
        OffsetPageRequest pageRequest = new OffsetPageRequest(1000);
        OffsetPage<P> page = null;
        do {
            page = find(entityClass, predicateProvider, null, pageRequest, context);
            for (P entity : page.getData()) {
                delete(entity, context);
            }
        }
        while (!page.isEmpty());
    }

    private static <P> List<Order> getOrder(SortSpecification<SingularAttribute<? super P, ?>> sortSpecification,
            CriteriaBuilder criteriaBuilder, Root<? extends P> root) {
        List<Order> orderList = new ArrayList<Order>();

        for (SortComponent<SingularAttribute<? super P, ?>> sortComponent : sortSpecification.getSortComponents()) {
            if (sortComponent != null) {
                Path<?> path = root.get(sortComponent.getSortBy());
                switch (sortComponent.getSortOrder()) {
                    case ASCENDING:
                        orderList.add(criteriaBuilder.asc(path));
                        break;
                    default:
                        orderList.add(criteriaBuilder.desc(path));
                }
            }
        }
        return orderList;
    }

    /**
     * Provides a predicate for a JPA query.
     * 
     * @param <P> entity to provide the predicate for
     */
    public static interface PredicateProvider<P> {

        /**
         * Provides a predicate.
         * 
         * @param criteriaBuilder criteriaBuider object for creating the predicate
         * @param root the root element type of the table
         * @return predicate to evaluate in a JPA query, {@code null} to consider all entities
         */
        public Predicate getPredicate(CriteriaBuilder criteriaBuilder, Root<P> root);
    }
}
