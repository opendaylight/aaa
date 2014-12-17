/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Mutator.
 * <p>
 * This interface can be used to abstract instantiation when dealing with immutable objects or
 * interfaces and copies with some modifications are needed. The mutator creates the instance of the
 * object replacing the values defined by the mutation.
 * 
 * @param <T> type of the object to mutate.
 * @param <M> type of the mutation
 * @author Fabiel Zuniga
 */
public interface Mutator<T, M> {
    /**
     * Mutates an object.
     * 
     * @param target object to mutate
     * @param mutation mutation
     * @return a mutated object
     */
    public T mutate(T target, M mutation);
}
