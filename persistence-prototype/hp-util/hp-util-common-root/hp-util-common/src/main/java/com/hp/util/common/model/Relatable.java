/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.model;

/**
 * Relatable.
 * 
 * @param <T> type of the relative
 * @author Fabiel Zuniga
 */
public interface Relatable<T> {

    /**
     * Gets the relative.
     *
     * @return the relative
     */
    public T getRelative();
}
