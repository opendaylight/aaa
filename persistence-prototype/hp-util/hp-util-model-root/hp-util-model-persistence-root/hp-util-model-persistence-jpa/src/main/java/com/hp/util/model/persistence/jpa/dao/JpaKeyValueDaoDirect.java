/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.io.Serializable;
import java.util.Collection;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.KeyValueDao;
import com.hp.util.model.persistence.jpa.JpaContext;

/**
 * {@link KeyValueDao} where the data transfer object pattern is not used and thus the
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
 * @author Fabiel Zuniga
 */
public class JpaKeyValueDaoDirect<I extends Serializable, T extends Identifiable<? super T, I>> implements
        KeyValueDao<I, T, JpaContext> {

    private final Class<T> entityClass;

    /**
     * Creates a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     */
    public JpaKeyValueDaoDirect(Class<T> entityClass) {
        if (entityClass == null) {
            throw new NullPointerException("entityClass cannot be null");
        }

        this.entityClass = entityClass;
    }

    @Override
    public T create(T identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        JpaUtil.persist(identifiable, context);
        return identifiable;
    }

    @Override
    public void update(T identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        if (identifiable.getId() == null) {
            throw new NullPointerException("identifiable has no id");
        }

        JpaUtil.updateDetached(identifiable, context);
    }

    @Override
    public void delete(Id<T, I> id, JpaContext context) throws PersistenceException {
        T entity = get(id, context);
        deleteEntity(entity, context);
    }

    @Override
    public T get(Id<T, I> id, JpaContext context) throws PersistenceException {
        return getEntity(id, context);
    }

    @Override
    public boolean exist(Id<T, I> id, JpaContext context) throws PersistenceException {
        return JpaUtil.exist(this.entityClass, getEntityId(id), context);
    }

    @Override
    public Collection<T> getAll(JpaContext context) throws PersistenceException {
        return JpaUtil.loadAll(getEntityClass(), context);
    }

    @Override
    public long size(JpaContext context) throws PersistenceException {
        return JpaUtil.size(getEntityClass(), context);
    }

    @Override
    public void clear(JpaContext context) throws PersistenceException {
        JpaUtil.delete(getEntityClass(), null, context);
    }

    /**
     * Gets the entity class.
     * 
     * @return the entity class
     */
    protected Class<T> getEntityClass() {
        return this.entityClass;
    }

    /**
     * Gets the entity Id.
     * 
     * @param id the corresponding identifiable object's id
     * @return the entity's id
     */
    protected Object getEntityId(Id<T, I> id) {
        return id.getValue();
    }

    /**
     * Loads an entity from the data store
     * 
     * @param id entiry's id
     * @param context context
     * @return the entity if found, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected T getEntity(Id<T, I> id, JpaContext context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        return JpaUtil.get(this.entityClass, getEntityId(id), context);
    }

    /**
     * Deletes the entity from the data store. Subclasses could override this method to do any
     * pre-processing / post-processing work.
     * 
     * @param entity entity to delete
     * @param context context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected void deleteEntity(T entity, JpaContext context) throws PersistenceException {
        JpaUtil.delete(entity, context);
    }
}
