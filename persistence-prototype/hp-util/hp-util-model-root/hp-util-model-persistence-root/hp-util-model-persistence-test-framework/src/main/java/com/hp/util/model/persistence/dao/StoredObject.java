/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Stored object.
 * 
 * @param <T> type of the identifiable object (object to store in the data store)
 * @author Fabiel Zuniga
 */
public class StoredObject<T> {

    private final T original;
    private final T stored;

    private StoredObject(T original, T stored) {
        this.original = original;
        this.stored = stored;
    }

    /**
     * Creates a stored object
     * 
     * @param original object used to store
     * @param stored the object as it is in the data store
     * @return a stored object
     */
    public static <T> StoredObject<T> valueOf(T original, T stored) {
        return new StoredObject<T>(original, stored);
    }

    /**
     * Gets the object used to store.
     * 
     * @return the original
     */
    public T getOriginal() {
        return this.original;
    }

    /**
     * Gets the stored the object as it is in the data store.
     * 
     * @return the stored
     */
    public T getStored() {
        return this.stored;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("original", this.original),
                Property.valueOf("stored", this.stored)
        );
    }
}
