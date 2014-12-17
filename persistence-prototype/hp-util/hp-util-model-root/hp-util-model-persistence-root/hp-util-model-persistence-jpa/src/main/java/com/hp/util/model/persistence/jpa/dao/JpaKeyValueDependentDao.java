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
import java.util.List;

import com.hp.util.common.Converter;
import com.hp.util.common.Identifiable;
import com.hp.util.common.Util;
import com.hp.util.common.converter.CollectionConverter;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.Dao;
import com.hp.util.model.persistence.dao.DependentDao;
import com.hp.util.model.persistence.dao.KeyValueDependentDao;
import com.hp.util.model.persistence.dao.UpdateStrategy;
import com.hp.util.model.persistence.jpa.JpaContext;

/**
 * JPA {@link KeyValueDependentDao}.
 * <p>
 * This class must remain state-less so it is thread safe.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * <p>
 * This implementation follows the data transfer pattern. Data Transfer Object Pattern: Provides the
 * ability for the transport objects to carry the data between application layers (DTO). A DTO
 * (Here, {@link Identifiable}) should be a type-safe POJO with object value types for attributes
 * when appropriated. The DAO internally use an entity (an object annotated with
 * {@link javax.persistence.Entity}) where attributes are directly translated to the database native
 * data types. In this way the internals of the entity are not exposed (For example, changes in
 * column names should not affect the DTO).
 * <P>
 * <Strong>One to Many Relations with Dependent Entities</Strong>
 * <P>
 * Note that any other type of relation (One to many or many to many with independent entities)
 * could be implemented using {@link Dao}s.
 * <Ul>
 * <Li>Let {@code Item} be a model object.</Li>
 * <Li>Let {@code Image} be a model object.</Li>
 * <Li>One item may have several images (One-to-Many relation). So {@code Item} has a collection of
 * {@code Image}. Dependent classes are modeled as value type (Collection of value type).</Li>
 * <Li>Let {@code ItemEntity} be a storable.</Li>
 * <Li>Let {@code ItemImageEntity} be the dependent storable. The dependent object has no life
 * outside of the collection; it's dependent on an {@code Item} entity.</Li>
 * <Li>A regular {@link Dao} may be used to access {@code ItemEntity}.</Li>
 * <Li>If the association from {@code Item} to {@code Image} is <Strong>unidirectional</Strong> then
 * the same regular {@link Dao} that access {@code ItemEntity} could also access
 * {@code ItemImageEntity}. No need of a separated DAO for {@code ItemImageEntity} since JPA will
 * propagate updates when the collection of dependent entities is modified. The {@link Dao} will
 * just need to create an {@code ItemImageEntity} from {@code Image} and vice-verza. With an
 * unidirectional relation you can navigate to the images by accessing the collection through an
 * item instance. <Strong> This is the only way you can get the images objects; no other entity
 * holds a reference to them (dependent entity value type).</Strong></Li>
 * <Li>If the association from {@code Item} to {@code Image} is <Strong>bidirectional</Strong> a
 * {@link DependentDao} should be used to access {@code ItemImageEntity}. In a bidirectional
 * relation it is possible to navigate from an {@code Image} back to an {@code Item}. Note this type
 * of navigation does not make much sense and should be avoided if possible. The JPA provider (Like
 * Hibernate) can fill in this property for you if you annotate your entity accordingly.
 * <Strong>True bidirectional navigation is impossible with JPA, however. You can't retrieve an
 * {@code ItemImageEntity} independently and then navigate back to its parent {@code ItemEntity}.
 * This is an important issue: You can load {@code ImageEntity} instances by querying for them. But
 * these images won't have a reference to their owner (the property is null). For this reason this
 * implementation introduces {@link Dependent} to keep a reference of the owner's id so it can be
 * manually retrieved (Since JPA provider won't fill the owner if the dependent object is queried by
 * itself). </Strong></Li>
 * </Ul>
 * <P>
 * <Strong>Annotations Examples:</Strong>
 * <P>
 * One to many association in the independent object
 * 
 * <Pre>
 *  {@literal @}OneToMany(cascade=CascadeType.ALL, mappedBy="owner", orphanRemoval=true)
 *  private Collection&lt;ItemImageEntity&gt; images = new ArrayList&lt;ItemImageEntity&gt;();
 * </Pre>
 * 
 * One to many association in the dependent object
 * 
 * <Pre>
 * {@literal @}ManyToOne
 * {@literal @}JoinColumn(name = "owner_fk")
 * private ItemEntity owner;
 * 
 * // Needed to implement true bidirectional navigation. See {@link Dependent} note above.
 * {@literal @}Column(name = "owner_id")
 * private Long ownerId;
 * </Pre>
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <P> type of the entity (an object annotated with {@link javax.persistence.Entity})
 * @param <E> type of the owner's id. This type should be immutable and it is critical it implements
 *            {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <O> type of the owner (the independent identifiable object)
 * @param <W> type of the owner's entity (an object annotated with {@link javax.persistence.Entity})
 * @author Fabiel Zuniga
 */
public abstract class JpaKeyValueDependentDao<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, P, E extends Serializable, O extends Identifiable<? super O, E>, W>
        implements KeyValueDependentDao<I, T, E, O, JpaContext>, Converter<P, T> {

    private final Class<P> entityClass;

    // UpdateStrategy is state-less, so this class remains thread safe.
    private final UpdateStrategy<P, T> updateStrategy;

    /**
     * Creates a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     */
    protected JpaKeyValueDependentDao(Class<P> entityClass) {
        this(entityClass, null);
    }

    /**
     * Creates a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param updateStrategy update strategy
     */
    protected JpaKeyValueDependentDao(Class<P> entityClass, UpdateStrategy<P, T> updateStrategy) {
        if (entityClass == null) {
            throw new NullPointerException("entityClass cannot be null");
        }

        this.entityClass = entityClass;
        this.updateStrategy = updateStrategy;
    }

    @Override
    public void update(T identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        if (identifiable.getId() == null) {
            throw new NullPointerException("identifiable has no id");
        }

        P entity = getEntity(identifiable.<T> getId(), context);

        if (entity == null) {
            throw new IllegalArgumentException("entity with id " + identifiable.getId() + " not found");
        }

        if (this.updateStrategy != null) {
            this.updateStrategy.validateWrite(entity, identifiable);
        }

        conform(entity, identifiable);
    }

    @Override
    public T get(Id<T, I> id, JpaContext context) throws PersistenceException {
        P entity = getEntity(id, context);
        return convert(entity);
    }

    @Override
    public boolean exist(Id<T, I> id, JpaContext context) throws PersistenceException {
        return JpaUtil.exist(this.entityClass, getEntityId(id), context);
    }

    @Override
    public Collection<T> getAll(JpaContext context) throws PersistenceException {
        return convert(JpaUtil.loadAll(getEntityClass(), context));
    }

    @Override
    public long size(JpaContext context) throws PersistenceException {
        return JpaUtil.size(getEntityClass(), context);
    }

    @Override
    public T convert(P source) {
        T target = doConvert(source);
        if (!Util.equals(getId(source), target.getId().getValue())) {
            throw new IllegalStateException("Invalid id: source=" + getId(source) + ", target="
                    + target.getId().getValue());
        }

        if (this.updateStrategy != null) {
            this.updateStrategy.validateRead(source, target);
        }

        return target;
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
    protected P getEntity(Id<T, I> id, JpaContext context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }
        return JpaUtil.get(this.entityClass, getEntityId(id), context);
    }

    /**
     * Convert a List of entities to their corresponding identifiable object.
     * 
     * @param entities entities to convert
     * @return a list of identifiable objects
     */
    protected List<T> convert(final Collection<P> entities) {
        return CollectionConverter.convert(entities, this, CollectionConverter.<T> getArrayListFactory());
    }

    /**
     * Gets the entity class.
     * 
     * @return the entity class
     */
    protected Class<P> getEntityClass() {
        return this.entityClass;
    }

    /**
     * Creates the instance of the entity.
     * <p>
     * This method is meant to be used by the owner DAO when creating the owner.
     * 
     * @param identifiable transport object to get the data from
     * @param owner owner
     * @return a new instance of the persistent object
     */
    public abstract P create(T identifiable, W owner);

    /**
     * Synchronizes the identifiable object and the entity state. This method is called from
     * {@link #update(Identifiable, JpaContext)}, thus if {@link #update(Identifiable, JpaContext)}
     * is overwritten this method could have an empty implementation.
     * <p>
     * This method is meant to be used by the owner DAO when updating the owner.
     * 
     * @param target object to update
     * @param source object to take the data from
     */
    public abstract void conform(P target, T source);

    /**
     * Gets the Id of the entity.
     * 
     * @param entity entity to get the id for
     * @return the id
     */
    protected abstract I getId(P entity);

    /**
     * Converts the entity to its corresponding identifiable object.
     * 
     * @param source object to convert
     * @return an identifiable object with the same data than {@code source}
     */
    protected abstract T doConvert(P source);
}
