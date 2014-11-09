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
     * Find all objects of type T that match the given example.
     * 
     * @param example
     *            example to match
     * @return all objects that match the given example
     */
    Iterable<T> findAll(T example);

    /**
     * Find all objects of type T that match the given example and additional
     * criteria. Note that in the event of a conflict between the example and
     * the specified criteria, the criteria will take precedence. For example,
     * if the example specifies that attribute X must have value equal to Y, and
     * the criteria specifies that X must have values greater than Y, the search
     * result will include only instances where attribute X having values
     * greater than Y.
     * 
     * @param example
     *            example to match
     * @param c
     *            criteria to match
     * @return all objects that match the given example and criteria
     */
    Iterable<T> findAll(T example, Criteria c);

    /**
     * Count the total number of objects.
     * 
     * @return total number of objects
     */
    Long count();

    /**
     * Count the number of objects that match the given example.
     * 
     * @param example
     *            example to match
     * 
     * @return number of objects that match the given example
     */
    Long count(T example);

    /**
     * Count the number of objects that match the given example.
     * 
     * @param example
     *            example to match
     * @param r
     *            additional criteria for the example
     * @return number of objects that match the given example
     */
    Long count(T example, Criteria c);

    /**
     * Delete the given object.
     *
     * @param object
     *            object to be deleted
     */
    void delete(T object);

    /**
     * Delete all objects of type T.
     *
     * @return count of deleted objects
     */
    Long deleteAll();

    /**
     * Delete all objects that match the given example.
     * 
     * @param example
     *            example to match
     * @return count of deleted objects
     */
    Long deleteAll(T example);

    /**
     * Delete all objects that match the given example and restrictions.
     * 
     * @param example
     *            example to match
     * @param c
     *            additional criteria for the example
     * @return number of objects that match the given example
     */
    Long deleteAll(T example, Criteria c);

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
