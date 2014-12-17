/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * A decoder is a device which does the reverse of an encoder, undoing the encoding so that the
 * original information can be retrieved. The same method used to encode is usually just reversed in
 * order to decode.
 * 
 * @param <E> type of the element to encode
 * @param <C> type of the coding format
 * @author Fabiel Zuniga
 */
public interface Decoder<E, C> {

    /**
     * Decodes a code to reconstruct the original entity.
     * 
     * @param code code representing an entity
     * @return an entity with the state given by the code
     * @throws IllegalArgumentException if {@code code} does not have the appropriate format
     */
    public E decode(C code) throws IllegalArgumentException;
}
