/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.io.Serializable;

/**
 * Identifier.
 * 
 * @param <T> type of the object the identifier identifies
 * @param <I> type of the identifier value. It should be an immutable type. It is critical this type
 *            implements equals() and hashCode() correctly.
 * @author Robert Gagnon
 * @author Fabiel Zuniga
 */
public class Id<T, I extends Serializable> extends SerializableValueType<I> {
    private static final long serialVersionUID = 1L;

    Id(I value) {
        super(value);
    }

    /**
     * Creates an identifier with the given value.
     *
     * @param value value
     * @return an identifier
     */
    public static <T, I extends Serializable> Id<T, I> valueOf(I value) {
        return new Id<T, I>(value);
    }
}
