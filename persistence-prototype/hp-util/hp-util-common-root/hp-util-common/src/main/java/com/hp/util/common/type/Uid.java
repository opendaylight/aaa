/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.util.UUID;

/**
 * Unique identifier.
 * 
 * @author Fabiel Zuniga
 */
public final class Uid extends SerializableValueType<UUID> {
    private static final long serialVersionUID = 1L;

    /**
     * @param value
     */
    private Uid(UUID value) {
        super(value);
    }

    /**
     * Creates a unique identifier with the given value.
     *
     * @param value unique identifier value
     * @return a unique identifier
     */
    public static Uid valueOf(UUID value) {
        return new Uid(value);
    }

    /**
     * Generates a globally unique identifier
     * <p>
     * GUIDs are usually stored as 128-bit values, and are commonly displayed as 32 hexadecimal
     * digits with groups separated by hyphens, such as {21EC2020-3AEA-1069-A2DD-08002B30309D}.
     *
     * @return a unique identifier
     */
    /*
    public static Uid generateGuid() {

    }
    */

    /**
     * Generates a universally unique identifier.
     * <p>
     * The intent of UUIDs is to enable distributed systems to uniquely identify information without
     * significant central coordination. A UUID is a 16-octet (128-bit) number. In its canonical
     * form, a UUID is represented by 32 hexadecimal digits, displayed in five groups separated by
     * hyphens, in the form 8-4-4-4-12 for a total of 36 characters (32 alphanumeric characters and
     * four hyphens). For example: 550e8400-e29b-41d4-a716-446655440000
     *
     * @return a unique identifier
     */
    public static Uid generateUuid() {
        return valueOf(UUID.randomUUID());
    }
}
