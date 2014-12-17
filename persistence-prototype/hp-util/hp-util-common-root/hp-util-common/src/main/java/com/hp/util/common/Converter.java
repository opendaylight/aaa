/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Converter.
 * 
 * @param <S> type of the source
 * @param <T> type of the target
 * @author Fabiel Zuniga
 */
public interface Converter<S, T> {

    /**
     * Converts a source to the target.
     * 
     * @param source source
     * @return source converted to target type
     */
    public T convert(S source);
}
