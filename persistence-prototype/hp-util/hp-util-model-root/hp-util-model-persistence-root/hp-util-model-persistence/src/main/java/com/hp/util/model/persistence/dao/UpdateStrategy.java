/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

/**
 * Update Strategy.
 * 
 * @param <P> type of the object directly written or read from the data store (an object that can be
 *            directly used by the underlying data store or database technology)
 * @param <T> type of the identifiable object (object to store in the data store)
 * @author Fabiel Zuniga
 */
public interface UpdateStrategy<P, T> {

    /**
     * This method is called when a persisted object is converted to an identifiable object.
     * 
     * @param source object to take the data from
     * @param target object to update.
     */
    public void validateRead(P source, T target);

    /**
     * Validates an update.
     * 
     * @param target object to update
     * @param source object to take the data from
     */
    public void validateWrite(P target, T source);
}
