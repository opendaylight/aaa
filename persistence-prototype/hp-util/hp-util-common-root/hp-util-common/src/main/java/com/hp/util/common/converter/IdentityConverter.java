/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import com.hp.util.common.Converter;

/**
 * Identity {@link Converter}.
 * <p>
 * In mathematics, an identity element (or neutral element) is a special type of element of a set
 * with respect to a binary operation on that set. It leaves other elements unchanged when combined
 * with them. This is used for groups and related concepts.
 * <p>
 * Examples:
 * <p>
 * <table border="1">
 * <tr>
 * <th>Set</th>
 * <th>Operand</th>
 * <th>Identity</th>
 * </tr>
 * <tr>
 * <td>Real Numbers</td>
 * <td>Addition</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>Real Numbers</td>
 * <td>Multiplication</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>Sets</td>
 * <td>Union</td>
 * <td>Empty set</td>
 * </tr>
 * <tr>
 * <td>Character strings</td>
 * <td>Concatenation</td>
 * <td>Empty string</td>
 * </tr>
 * </table>
 * 
 * @param <E> Type of the source and the target
 * @author Fabiel Zuniga
 */
public final class IdentityConverter<E> implements Converter<E, E> {
    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    @SuppressWarnings("rawtypes")
    private static final IdentityConverter INSTANCE = new IdentityConverter();

    private IdentityConverter() {

    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    @SuppressWarnings("unchecked")
    public static <E> IdentityConverter<E> getInstance() {
        return INSTANCE;
    }

    @Override
    public E convert(E source) {
        return source;
    }
}
