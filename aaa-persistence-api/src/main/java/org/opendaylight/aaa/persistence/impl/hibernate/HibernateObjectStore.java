/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.TypeMismatchException;
import org.hibernate.Query;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.opendaylight.aaa.persistence.api.*;
import org.opendaylight.aaa.persistence.impl.hibernate.search.SearchPage;

import java.io.Serializable;
import java.util.*;


public class HibernateObjectStore<T extends Transportable<ID>, ID extends Serializable> implements ObjectStore<T, ID> {

    private static SessionFactory factory;

    private static Logger log = Logger.getLogger(HibernateObjectStore.class.getName());

    private Class<T> tClass;

    public HibernateObjectStore(Class<T> tClass, SessionFactory factory) {
        this.tClass = tClass;
        this.factory = factory;
    }

    @Override
    public <S extends T> S save(final S object) {
        Preconditions.checkNotNull(object, "object should not be null");

        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        try {
            tx.begin();

            session.save(object);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to save object.", e);
        } finally {
            session.close();
        }

        return object;
    }

    @Override
    public <S extends T> S save(final ID id, final S object) {
        Preconditions.checkNotNull(id, "id should not be null");
        Preconditions.checkNotNull(object, "object should not be null");

        S objectWithId = object;
        objectWithId.setId(id);

        return save(objectWithId);
    }

    @Override
    public <S extends T> Iterable<S> save(final Iterable<S> objects) {
        Preconditions.checkNotNull(objects, "objects should not be null");

        List<S> savedObjects = new ArrayList<S>();

        for (S object : objects) {
            savedObjects.add(save(object));
        }

        return savedObjects;
    }

    @Override
    public T findById(final ID id) {
        Preconditions.checkNotNull(id, "id should not be null");

        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        T entity = null;

        try {
            tx.begin();

            entity = (T) session.get(tClass, id);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to find object by id.", e);
        } finally {
            session.close();
        }

        return entity;
    }

    @Override
    public Map<ID, T> findById(final Iterable<ID> ids) {
        Preconditions.checkNotNull(ids, "ids should not be null");

        Map<ID, T> foundObjects = new HashMap<ID, T>();

        for (ID id : ids) {
            T foundObject = findById(id);

            if (foundObject != null) {
                foundObjects.put(id, foundObject);
            }
        }

        return foundObjects;
    }

    @Override
    public Iterable<T> findAll() {
        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        List<T> entities = new ArrayList<T>();

        try {
            tx.begin();

            org.hibernate.Criteria criteria = session.createCriteria(tClass);
            entities = criteria.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to find all objects.", e);
        } finally {
            session.close();
        }

        return entities;
    }

    @Override
    public Page<T, ID> findAll(final Pageable<ID> p) {
        return findPage(p, null);
    }

    @Override
    public Iterable<T> findAll(final Criteria c) {
        Preconditions.checkNotNull(c, "criteria should not be null");

        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        List<T> entities = new ArrayList<T>();

        try {
            tx.begin();

            org.hibernate.Criteria criteria = session.createCriteria(tClass);
            criteria = getCriteria(c, criteria);
            entities = criteria.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to find all objects for criteria.", e);
        } finally {
            session.close();
        }

        return entities;
    }

    @Override
    public Page<T, ID> findAll(final Pageable<ID> p, final Criteria c) {
        return findPage(p, c);
    }

    @Override
    public Long count() {
        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        Long numEntities = 0L;

        try {
            tx.begin();

            org.hibernate.Criteria criteria = session.createCriteria(tClass);
            numEntities = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to count all objects.", e);
        } finally {
            session.close();
        }

        return numEntities;
    }

    @Override
    public Long count(final Criteria c) {
        Preconditions.checkNotNull(c, "criteria should not be null");

        Iterator iter = findAll(c).iterator();
        Long numEntities = 0L;

        while (iter.hasNext()) {
            iter.next();
            numEntities++;
        }

        return numEntities;
    }

    @Override
    public void delete(final T object) {
        Preconditions.checkNotNull(object, "object should not be null");

        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        try {
            tx.begin();

            session.delete(object);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to delete object.", e);
        } finally {
            session.close();
        }
    }

    @Override
    public void delete(final ID id) {
        Preconditions.checkNotNull(id, "id should not be null");

        T object = findById(id);

        if (object != null) {
            delete(object);
        }
    }

    @Override
    public void delete(final Iterable<ID> ids) {
        Preconditions.checkNotNull(ids, "ids should not be null");

        Iterator<ID> iter = ids.iterator();

        while (iter.hasNext()) {
            delete(iter.next());
        }
    }

    @Override
    public Long deleteAll() {
        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        int numEntities = 0;

        try {
            tx.begin();

            String hql = String.format("delete from %s", tClass.getName());
            Query query = session.createQuery(hql);
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to delete all objects.", e);
        } finally {
            session.close();
        }

        return (long) numEntities;
    }

    @Override
    public Long deleteAll(final Criteria c) {
        Preconditions.checkNotNull(c, "criteria should not be null");

        Iterator<T> iter = findAll(c).iterator();
        Long numDeleted = 0L;

        while (iter.hasNext()) {
            delete(iter.next());
            numDeleted++;
        }

        return numDeleted;
    }

    @Override
    public Long deleteAll(Iterable<? extends T> objects) {
        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        Long beginNumEntities;

        if (objects == null)
            return 0L;

        beginNumEntities = count();
        Iterator iter = objects.iterator();
        try {
            tx.begin();

            while (iter.hasNext()) {
                delete((T) iter.next());
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to delete all objects.", e);
        } finally {
            session.close();
        }

        return beginNumEntities - count();
    }

    @Override
    public boolean exists(ID id) {
        if (id == null)
            return false;
        else
            return (findById(id) != null);
    }

    private void checkSession(Session session, Transaction tx) {
        Preconditions.checkNotNull(session, "session should not be null");
        Preconditions.checkNotNull(tx, "transaction should not be null");
    }

    private org.hibernate.Criteria getCriteria(Criteria c, org.hibernate.Criteria criteria) {
        if (c != null) {
            criteria = getEntryCriteria((Restrictable) c, criteria);
            criteria = getOrderCriteria((Orderable) c, criteria);
        }
        return criteria;
    }

    private boolean typeCheckRestriction(String property, Restriction restriction) {

        for (Object value : restriction.values()) {
            try {
                if (tClass.getDeclaredField(property).getType() != value.getClass()) {
                    throw new TypeMismatchException("property type and restriction value type differ");
                }
            } catch (NoSuchFieldException e) {
                return false;
            }
        }

        return true;
    }

    // Converts between generic Criteria interface of ObjectStore to Hibernate Criteria API.
    private org.hibernate.Criteria getEntryCriteria(Restrictable r, org.hibernate.Criteria searchCriteria) {

        if (r == null || r.restrictions() == null)
            return searchCriteria;

        for (Map.Entry<String, Restriction> entry : r.restrictions().entrySet()) {
            String property = entry.getKey();
            Restriction restriction = entry.getValue();

            boolean validRestriction = typeCheckRestriction(property, restriction);

            if (validRestriction) {
                List<?> values = restriction.values();

                //FIXME - Better way to do # args checking (and possible type checking).
                switch (restriction.predicate()) {

                    case EQ:
                        Preconditions.checkArgument(values.size() == 1, "EQ predicate requires only 1 value");
                        searchCriteria.add(Restrictions.eq(property, values.get(0)));
                        break;

                    case NEQ:
                        Preconditions.checkArgument(values.size() == 1, "NEQ predicate requires only 1 value");
                        searchCriteria.add(Restrictions.ne(property, values.get(0)));
                        break;

                    case LT:
                        Preconditions.checkArgument(values.size() == 1, "LT predicate requires only 1 value");
                        searchCriteria.add(Restrictions.lt(property, values.get(0)));
                        break;

                    case LTE:
                        Preconditions.checkArgument(values.size() == 1, "LTE predicate requires only 1 value");
                        searchCriteria.add(Restrictions.le(property, values.get(0)));
                        break;

                    case GT:
                        Preconditions.checkArgument(values.size() == 1, "GT predicate requires only 1 value");
                        searchCriteria.add(Restrictions.gt(property, values.get(0)));
                        break;

                    case GTE:
                        Preconditions.checkArgument(values.size() == 1, "GTE predicate requires only 1 value");
                        searchCriteria.add(Restrictions.ge(property, values.get(0)));
                        break;

                    case IN:
                        searchCriteria.add(Restrictions.in(property, values));
                        break;

                    case BETWEEN:
                        Preconditions.checkArgument(values.size() == 2, "BETWEEN predicate requires only 2 values");
                        searchCriteria.add(Restrictions.between(property, values.get(0), values.get(1)));
                        break;

                    case REGEX:
                        searchCriteria.add(Restrictions.like(property, values));
                        break;

                    default:
                        break;
                }
            }
        }

        return searchCriteria;
    }

    // Converts between generic Criteria interface of ObjectStore to Hibernate Criteria API.
    private org.hibernate.Criteria getOrderCriteria(Orderable o, org.hibernate.Criteria searchCriteria) {

        if (o == null || o.orders() == null)
            return searchCriteria;

        for (Order order : o.orders()) {
            String property = order.attributeName();

            switch (order.direction()) {

                case ASC:
                    searchCriteria.addOrder(org.hibernate.criterion.Order.asc(property));
                    break;

                case DESC:
                    searchCriteria.addOrder(org.hibernate.criterion.Order.desc(property));
                    break;

                default:
                    break;
            }
        }

        return searchCriteria;
    }

    private SearchPage<T, ID> findPage(Pageable<ID> p, Criteria c) {
        Preconditions.checkNotNull(p, "page should not be null");

        Session session = factory.openSession();
        Transaction tx = session.getTransaction();
        checkSession(session, tx);

        boolean extraSearchCriteria = (c != null);

        List<T> entries = new ArrayList<T>();
        int maxResults = p.limit();

        try {
            tx.begin();

            // Find ID for Page.
            SimpleExpression findById = Restrictions.le("id", p.marker());      // All entries including requested id.
            org.hibernate.Criteria findByIdCriteria = session.createCriteria(tClass).add(findById);
            long entryRowNumber = (Long) findByIdCriteria.setProjection(Projections.rowCount()).uniqueResult() - 1;     // -1 for inclusive upper bound.

            // Get Paging Result
            org.hibernate.Criteria findPageCriteria = session.createCriteria(tClass);
            findPageCriteria.setFirstResult((int) entryRowNumber);

            if (extraSearchCriteria) {
                // Get all results (filter later)
                // FIXME - Performance concern since they will be put in memory (could do multiple pages till have # of requested/expected results for a page.)
                findPageCriteria = getCriteria(c, findPageCriteria);

            } else if (maxResults != 0) {
                findPageCriteria.setMaxResults(maxResults + 1);   // 1 extra for next marker determination
            }
            entries = findPageCriteria.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }

            log.error("Unable to find all objects for criteria by page.", e);
        } finally {
            session.close();
        }

        // Put Results in to Page Format
        SearchPage<T, ID> pageResult = new SearchPage<T, ID>();

        if (extraSearchCriteria) {

            // Select subset of results to get proper paging+criteria behavior.
            if (entries.size() >= maxResults) {
                pageResult.setNextMarker(entries.get(maxResults).getId());
                entries = entries.subList(0, maxResults); // Limit criteria results. [low,high)
            }

        } else if (maxResults != 0 && entries.size() >= maxResults) {
            pageResult.setNextMarker(entries.get(maxResults).getId());
            entries.remove(maxResults);   // Remove Last (extra was for nextMarker)
        }
        pageResult.setPreviousMarker(p.marker());
        pageResult.setContent(entries);

        return pageResult;
    }

}
