/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.api;

import java.io.Serializable;
import java.util.Map;

/**
 * A generic object store interface.
 *
 * @author liemmn
 * @author Mark Mozolewski
 *
 * @param <T>
 *            Object type to be stored
 * @param <ID>
 *            Identifier type of object to be stored
 */
public interface ObjectStore<T, ID extends Serializable> {

    /**
     * Create or update an object.
     *
     * @param object
     *            object to be saved or updated
     * @return created or updated object
     */
    <S extends T> S save(S object);

    /**
     * Create or update an object with specified ID.
     * (Note: Provided id will take precedence over any identifier defined in the object.)
     *
     * @param id
     *            object's ID
     * @param object
     *            object to be saved or updated
     * @return created or updated object
     */
    <S extends T> S save(ID id, S object);

    /**
     * Create or update a collection of objects.
     *
     * @param objects
     *            collection of objects to be created or updated
     * @return collection of objects created or updated
     */
    <S extends T> Iterable<S> save(Iterable<S> objects);

    /**
     * Find by ID.
     *
     * @param id
     *            ID
     * @return Object identified by ID or null if not found
     */
    T findById(ID id);

    /**
     * Find by IDs.
     *
     * @param ids
     *            IDs
     * @return Objects identified by IDs or null if none found
     */
    Map<ID, T> findById(Iterable<ID> ids);

    /**
     * Find all objects of type T
     *
     * @return all objects of type T
     */
    Iterable<T> findAll();

    /**
     * Find a page of objects of type T with the given paging limits.
     *
     * @param p
     *            page limit
     * 
     * @return a page of objects of type T
     */
    Page<T, ID> findAll(Pageable<ID> p);

    /**
     * Find all objects of type T that match the given criteria.
     * 
     * @param c
     *            criteria to match
     * @return all objects that match the given example and criteria
     */
    Iterable<T> findAll(Criteria c);

    /**
     * Find a page of objects of type T with the given paging limits.
     *
     * @param p
     *            page limit
     * @param c
     *            criteria to match
     * 
     * @return a page of objects of type T
     */
    Page<T, ID> findAll(Pageable<ID> p, Criteria c);

    /**
     * Count the total number of objects.
     * 
     * @return total number of objects
     */
    Long count();

    /**
     * Count the number of objects that match the given criteria.
     * 
     * @param c
     *            criteria for the example
     * @return number of objects that match the given example
     */
    Long count(Criteria c);

    /**
     * Delete the given object.
     *
     * @param object
     *            object to be deleted
     */
    void delete(T object);

    /**
     * Delete the object with the given ID.
     *
     * @param id
     *            ID of the object to be deleted
     */
    void delete(ID id);

    /**
     * Delete the objects with the given IDs.
     *
     * @param ids
     *            IDs of the object to be deleted
     * @return count of deleted objects
     */
    void delete(Iterable<ID> ids);

    /**
     * Delete all objects of type T.
     *
     * @return count of deleted objects
     */
    Long deleteAll();

    /**
     * Delete all objects that match the given example and restrictions.
     * 
     * @param c
     *            criteria for the example
     * @return number of objects that match the given example
     */
    Long deleteAll(Criteria c);

    /**
     * Delete a collection of objects.
     * 
     * @param objects
     *            objects to be deleted
     * @return count of deleted objects
     */
    Long deleteAll(Iterable<? extends T> objects);

    /**
     * Check to see if an object with the given ID exists.
     *
     * @param id
     *            ID
     * @return true if exists, false otherwise
     */
    boolean exists(ID id);

}
