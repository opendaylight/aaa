/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Read/Write memory.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public interface Memory<D> {

    /**
     * Reads the memory.
     * 
     * @return memory's data
     */
    public D read();

    /**
     * Writes data to the memory.
     *
     * @param data data to write
     */
    public void write(D data);
}
