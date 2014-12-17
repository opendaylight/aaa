/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.model;

/**
 * Dependent.
 * 
 * @param <T> type of the object this dependent depends on
 * @author Fabiel Zuniga
 */
public interface Dependent<T> {

    /**
     * Gets the object this dependent depends on.
     * 
     * @return the independent object
     */
    public T getIndependent();
}
