/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Entity that uses an auto-increment long type as primary key.
 * <P>
 * Note that entities must have a default constructor.
 * <P>
 * Notice that using generics in abstract entity classes which are resolved in concrete classes is
 * fine. Concrete entity classes must not use generics. Attributes types must be known at compile
 * time since the JPA provider generates some metadata classes representing the columns information.
 * 
 * @author Fabiel Zuniga
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private Long id;

    /**
     * Returns the entity's id.
     * 
     * @return the id
     */
    public Long getId() {
        return this.id;
    }

    /*
     * It is possible to map Set, SortedSet, List, Collection, Map, and SortedMap data structures.
     * The collection properties can hold references to entity objects and value types (Like String).
     *
     * Example of value type collection attribute:
     *
     * @javax.persistence.ElementCollection
     * @javax.persistence.CollectionTable(name = "BaseEntityStrings", joinColumns = @javax.persistence.JoinColumn(name = "BaseEntityId"))
     * @javax.persistence.Column(name = "CollectionOfStrings")
     * private List<String> collectionOfStrings;
     *
     * The collectionOfStrings are stored in a table named BaseEntityStrings, the collection table. From the point of view of
     * the database, this table is a separated entity, a separate table, but the JAP provider hides this for you.
     */
}
