/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Factory.
 * 
 * @param <T> type of the object to create
 * @param <I> type of the factory input
 * @author Fabiel Zuniga
 */
public interface ParameterizedFactory<T, I> {

    /**
     * Creates an object.
     * 
     * @param input input
     * @return a new object
     */
    public T create(I input);
}
