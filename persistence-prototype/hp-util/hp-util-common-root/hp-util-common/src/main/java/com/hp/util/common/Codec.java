/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * A Codec is a device or computer program capable of encoding and/or decoding a digital data stream
 * or signal.
 * 
 * @param <E> type of the element to encode
 * @param <C> type of the coding format
 * @author Fabiel Zuniga
 */
public interface Codec<E, C> extends Decoder<E, C> {

    /**
     * Encodes the given entity.
     * 
     * @param entity entity to encode
     * @return the code for {@code entity}
     * @throws IllegalArgumentException if {@code entity} is not recognized and cannot be encoded
     */
    public C encode(E entity) throws IllegalArgumentException;
}
