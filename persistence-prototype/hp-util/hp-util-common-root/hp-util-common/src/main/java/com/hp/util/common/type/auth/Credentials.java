/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.auth;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Authentication credentials.
 * 
 * @author Fabiel Zuniga
 */
public class Credentials {

    private final Username username;
    private final Password password;

    /**
     * Creates authentication credentials.
     *
     * @param username username
     * @param password password
     */
    public Credentials(Username username, Password password) {
        if (username == null) {
            throw new NullPointerException("username cannot be null");
        }

        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public Username getUsername() {
        return this.username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public Password getPassword() {
        return this.password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.username.hashCode();
        result = prime * result + ((this.password == null) ? 0 : this.password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Credentials other = (Credentials)obj;

        if (!this.username.equals(other.username)) {
            return false;
        }

        if (this.password == null) {
            if (other.password != null) {
                return false;
            }
        }
        else if (!this.password.equals(other.password)) {
            return false;
        }

        return true;
    }


    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("username", this.username),
                Property.valueOf("password", this.password)
        );
    }
}
