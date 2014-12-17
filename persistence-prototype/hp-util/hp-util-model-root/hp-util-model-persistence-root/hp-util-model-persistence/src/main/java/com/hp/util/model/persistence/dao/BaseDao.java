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
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Base Data Access Object.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * <p>
 * A DAO should be created for model objects for which it is natural to have an identity, otherwise
 * a value type relation could be used. A DAO allows loading and querying a model object directly,
 * without loading the objects it relates too. If a dependent object cannot exist without its
 * related object, and it is not possible to load it without loading its related object, then it
 * could be a value type object for which a DAO is not needed.
 * <p>
 * It is recommended to use the data transfer pattern when implementing a DAO. Data Transfer Object
 * Pattern: Provides the ability for the transport objects to carry the data between application
 * layers (DTO). A DTO (Here, {@link Identifiable}) should be a type-safe POJO with object value
 * types for attributes when appropriated. The DAO should internally use an object that the
 * underlying database technology understands (Like an Entity in JPA-based implementations for
 * example) where attributes are directly translated to the database native data types. In this way
 * the internals of the storable object are not exposed (For example, changes in column names should
 * not affect the DTO).
 * <p>
 * The big advantage of separating the two objects (DTO and Storable) is that restrictions of the
 * underlying technology are hidden from the consumer. Examples of JPA restrictions:
 * <ul>
 * <li>Entities must provide a default constructor. This is not good for model objects. Constructors
 * are defines to make sure an object is in a valid state after creation. This restriction defeats
 * the purpose of constructors or factory methods.</li>
 * </ul>
 * <p>
 * By separating the two objects (DTO and Storable) allows taking advantage of lazy initialization
 * if supported. A DAO subclass could define extra methods that return light versions of the DTO
 * where data that is not required is not loaded. The result of a query not necessarily has to fit
 * into a single DTO. Normally the model object-transport object mapping will be 1:1, however
 * nothing prevents to have multiple transport objects for the same model object). In JPA
 * implementations for example, entities use lazy loading for dependent objects, thus if a query
 * needs no dependents, loaded persistent objects could be converted to transport objects with no
 * dependents; dependents wouldn't be loaded by JPA since they wouldn't be referenced.
 * <p>
 * Conceptually, for each object to store a DAO in charge of doing so is defined. This DAO takes
 * care of the specifics of the object: like defining the attributes (and their database native
 * types) to store, handling filtering and sorting, etc. However, it is up to the implementations to
 * define their own abstractions. In most cases one specific DAO class will be needed to take care
 * of the specifics (especially if filtering and sorting is supported). However, if the specifics
 * can be defined in a systematic way, then a single DAO could take care of all objects to store.
 * For example, assume the objects are serialized and no filtering or sorting is needed (Similar
 * case with JPA-based implementations, if no filtering and sorting are supported the specifics are
 * defined by annotating the object or defining a configuration file). Example:
 * 
 * <pre>
 * public final class MyDao&lt;I extends Serializable, T extends Identifiable&lt;? super T, I&gt; &amp; Serializable&gt; implements BaseDao&lt;I, T, MyContext&gt; {
 * 
 *     &#064;SuppressWarnings(&quot;rawtypes&quot;)
 *     private static final MyDao INSTANCE = new MyDao();
 * 
 *     private MyDao() {
 * 
 *     }
 * 
 *     &#064;SuppressWarnings(&quot;unchecked&quot;)
 *     public static &lt;I extends Serializable, T extends Identifiable&lt;? super T, I&gt; &amp; Serializable&gt; MyDao&lt;I, T&gt; getInstance() {
 *         return INSTANCE;
 *     }
 *     
 *     ...
 * }
 * 
 * </pre>
 * <p>
 * Note that this works only if the specifics can be defined in a systematic way and especially if
 * filtering and sorting are not supported. A DAO should encapsulate database specifics. For
 * example, if filtering is supported, Use {@link Dao} which defines a find method that make use of
 * a filter. DO NOT expose the specifics. With JPA for example it is very tempting to create a
 * single DAO implementation that persists any object and then expose a method that receives a JPQL
 * query. If the database is ported to a NoSQL one for example (like Cassandra), then it could be
 * impossible to know which clients are broken because there is no control on the supported queries.
 * It would be impractical to implement all possible queries in a NoSQL database that can be defined
 * via SQL or JPQL.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Robert Gagnon
 * @author Fabiel Zuniga
 */
public interface BaseDao<I extends Serializable, T extends Identifiable<? super T, I>, C> {

    /**
     * Creates the given object in the data store.
     * 
     * @param identifiable object to store
     * @param context data store context
     * @return the new created object which may include auto-generated primary keys
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public T create(T identifiable, C context) throws PersistenceException;

    /**
     * Updates the given object in the data store
     * 
     * @param identifiable object to store
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void update(T identifiable, C context) throws PersistenceException;

    /*
     * Note: Delete methods don't return anything (nor boolean nor count of deleted items) because
     * that would force implementations to perform a read before write. If the client needs to know
     * whether an element is actually deleted by Id or the number of deleted items by filter, it can
     * always perform the read query before deleting in a critical section.
     */

    /**
     * Deletes an object from the data store.
     * 
     * @param id object's id
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void delete(Id<T, I> id, C context) throws PersistenceException;

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
