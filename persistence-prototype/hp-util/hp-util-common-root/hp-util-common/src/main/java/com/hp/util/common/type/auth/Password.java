/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.auth;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Password.
 * 
 * @author Fabiel Zuniga
 */
public final class Password {

    /*
     * This class should not be serializable for security reasons.
     */

    private String value;

    private Password(String value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }

        this.value = value;
    }

    /**
     * Creates a password.
     * 
     * @param value password value
     * @return a password
     */
    public static Password valueOf(String value) {
        if (value == null) {
            return null;
        }
        return new Password(value);
    }

    /**
     * Gets the password value.
     * 
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Password)) {
            return false;
        }

        Password other = (Password) obj;

        if (!this.value.equals(other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("value", "****")
        );
    }
}
