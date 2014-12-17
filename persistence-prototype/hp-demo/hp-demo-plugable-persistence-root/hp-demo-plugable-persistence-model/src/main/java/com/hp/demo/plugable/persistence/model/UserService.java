/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.util.common.DuplicateException;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;
import com.hp.util.common.type.net.Email;

/**
 * Business logic for users.
 * 
 * @author Fabiel Zuniga
 */
public interface UserService {

    /**
     * Creates a user.
     * 
     * @param username username
     * @param password password
     * @param email user's e-mail
     * @return the created user
     * @throws DuplicateException if a user with the given {@code username} already exists
     */
    public User signUp(Username username, Password password, Email email) throws DuplicateException;

    /**
     * Authenticates a user.
     * 
     * @param username username
     * @param password password
     * @return the user if the authentication is valid and the user is enabled
     */
    public User signIn(Username username, Password password);

    /**
     * Disables a user.
     * 
     * @param id id of the user to disable
     * @return the updated user
     */
    public User disable(Id<User, Username> id);

    /**
     * Gets all enabled users.
     * 
     * @return enabled users
     */
    public List<User> getEnabled();

    /**
     * Gets all disabled users.
     * 
     * @return disabled users
     */
    public List<User> getDisabled();
}
