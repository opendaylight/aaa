/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.model;

import java.io.Serializable;

import com.hp.util.common.Identifiable;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;

/**
 * Serialized version of {@link AbstractIdentifiable}, however this class does not extends from
 * {@link AbstractIdentifiable} to avoid providing an accessible parameterless constructor: If a
 * class that is designed for inheritance is not {@link Serializable}, it may be impossible to write
 * a {@link Serializable} subclass. Specifically, it will be impossible if the superclass does not
 * provide an accessible parameterless constructor.
 * 
 * @param <T> type of the identified object
 * @param <I> type of the id. This type should be immutable and it is critical it implements
 *            {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @author Fabiel Zuniga
 */
public abstract class SerializableAbstractIdentifiable<T, I extends Serializable> implements Identifiable<T, I>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Id<? extends T, I> id;

    /**
     * Creates an identifiable object with no id.
     */
    protected SerializableAbstractIdentifiable() {
        this(null);
    }

    /**
     * Creates an identifiable object.
     * 
     * @param id object's id
     */
    protected SerializableAbstractIdentifiable(Id<? extends T, I> id) {
        this.id = id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends T> Id<E, I> getId() {
        return (Id<E, I>)this.id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        SerializableAbstractIdentifiable<?, ?> other = (SerializableAbstractIdentifiable<?, ?>)obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!this.id.equals(other.id)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this, 
                Property.valueOf("id", this.id)
            );
    }
}
