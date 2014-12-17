/*
 * Copyright (c) 2010 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.parser;

import com.hp.util.common.Parser;

/**
 * Enumeration parser.
 * 
 * @param <E> Type of the enumeration to reconstruct from the text representation
 * @author Fabiel Zuniga
 */
public class EnumParser<E extends Enum<E>> implements Parser<E> {
    private Class<E> enumClass;

    /**
     * Constructs an enum parser.
     *
     * @param enumClass Enum class
     */
    public EnumParser(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String toParsable(E entity) {
        if (entity != null) {
            return entity.name();
        }
        return null;
    }

    @Override
    public E parse(String s) throws IllegalArgumentException {
        /*
         * This code does not throw IllegalArgumentException, if s is not found it returns null
         *

        for (E e : this.enumClass.getEnumConstants()) {
            if (e.name().equals(s)) {
                return e;
            }
        }
        return null;
        */

        if(s == null) {
            return null;
        }

        return Enum.valueOf(this.enumClass, s);
    }
}
