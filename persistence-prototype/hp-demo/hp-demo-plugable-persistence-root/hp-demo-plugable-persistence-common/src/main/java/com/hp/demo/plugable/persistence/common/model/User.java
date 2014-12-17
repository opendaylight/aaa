/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.common.model;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.model.AbstractIdentifiable;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;
import com.hp.util.common.type.net.Email;

/**
 * User.
 * 
 * @author Fabiel Zuniga
 */
public final class User extends AbstractIdentifiable<User, Username> {
    /*
     * This class is not serializable for security reasons; to avoid serializing the password.
     */

    private Password password;
    private Email email;
    private String description;
    private boolean enabled;

    /**
     * Creates an user
     * 
     * @param id id
     */
    public User(Id<User, Username> id) {
        super(id);
    }

    /**
     * @return the password
     */
    public Password getPassword() {
        return this.password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(Password password) {
        this.password = password;
    }

    /**
     * @return the email
     */
    public Email getEmail() {
        return this.email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(Email email) {
        this.email = email;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("id", getId()),
                Property.valueOf("password", this.password),
                Property.valueOf("email", this.email),
                Property.valueOf("description", this.description),
                Property.valueOf("isEnabled", Boolean.valueOf(this.enabled))
        );
    }
}
