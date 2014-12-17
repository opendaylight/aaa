/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

/**
 * Serial number.
 * 
 * @author Fabiel Zuniga
 */
public final class SerialNumber extends SerializableValueType<String> {

    private static final long serialVersionUID = 1L;

    private SerialNumber(String value) throws IllegalArgumentException {
        super(value);
    }

    /**
     * Creates an SSN from the given value.
     * 
     * @param value SSN's value
     * @return an SSN
     * @throws IllegalArgumentException if {@code value} does not represent an SSN
     */
    public static SerialNumber valueOf(String value) throws IllegalArgumentException {
        return new SerialNumber(value);
    }
}
