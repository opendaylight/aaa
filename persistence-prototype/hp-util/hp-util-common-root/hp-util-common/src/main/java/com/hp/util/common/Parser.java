/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Parser.
 * 
 * @param <E> type of the object to parse
 * @author Fabiel Zuniga
 */
public interface Parser<E> {

    /**
     * Converts the specified entity to a parsable string.
     * 
     * @param entity entity to convert to parsable string
     * @return the specified entity in a string format
     */
    public String toParsable(E entity);

    /**
     * Reconstructs an object from its string format representation.
     * 
     * @param s the string to be parsed
     * @return a reconstructed object from its string format representation
     * @throws IllegalArgumentException if the string does not have the appropriate format
     */
    public E parse(String s) throws IllegalArgumentException;
}
