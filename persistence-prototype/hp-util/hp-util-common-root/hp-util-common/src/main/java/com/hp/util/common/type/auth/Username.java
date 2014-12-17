/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.auth;

import com.hp.util.common.type.SerializableValueType;

/**
 * Username.
 * 
 * @author Fabiel Zuniga
 */
public class Username extends SerializableValueType<String> {
    private static final long serialVersionUID = 1L;

    private Username(String value) {
        super(value);

        if (value.isEmpty()) {
            throw new IllegalArgumentException("value cannot be empty");
        }
    }

    /**
     * Creates a username.
     * 
     * @param value username value
     * @return a username
     */
    public static Username valueOf(String value) {
        return new Username(value);
    }
}
