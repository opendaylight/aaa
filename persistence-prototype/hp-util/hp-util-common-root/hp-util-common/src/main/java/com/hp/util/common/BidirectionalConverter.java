/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Bidirectional converter.
 * 
 * @param <S> type of the source
 * @param <T> type of the target
 * @author Fabiel Zuniga
 */
public interface BidirectionalConverter<S, T> extends Converter<S, T> {

    /**
     * Restores (Brings back) the original source.
     * 
     * @param target result of the conversion of the original source
     * @return a replica of the original source
     * @throws IllegalArgumentException if {@code target} does not have the appropriate format
     */
    public S restore(T target) throws IllegalArgumentException;
}
