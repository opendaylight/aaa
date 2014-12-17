/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.model.PersistenceService;
import com.hp.demo.plugable.persistence.model.UserService;
import com.hp.util.common.DuplicateException;
import com.hp.util.common.Util;
import com.hp.util.common.log.Logger;
import com.hp.util.common.log.LoggerProvider;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;
import com.hp.util.common.type.net.Email;
import com.hp.util.model.persistence.PersistenceException;

/**
 * @author Fabiel Zuniga
 */
class UserServiceImpl implements UserService {

    private final PersistenceService persistenceService;
    private final Logger logger;

    public UserServiceImpl(PersistenceService persistenceService, LoggerProvider<Class<?>> loggerProvider) {
        this.persistenceService = persistenceService;
        this.logger = loggerProvider.getLogger(getClass());
    }

    @Override
    public User signUp(Username username, Password password, Email email) throws DuplicateException {
        Id<User, Username> id = Id.valueOf(username);
        User user = new User(id);
        user.setPassword(password);
        user.setEmail(email);
        user.setEnabled(true);

        // TODO: Verify if the user already exists

        try {
            this.persistenceService.user().store(user);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to create user " + user, e);
            throw new RuntimeException("Unable to sign up");
        }

        return user;
    }

    @Override
    public User signIn(Username username, Password password) {
        Id<User, Username> id = Id.valueOf(username);
        User user = null;
        try {
            user = this.persistenceService.user().get(id);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to retrieve user with id " + id, e);
            throw new RuntimeException("Unable to retrieve user with id " + id);
        }

        if (Util.equals(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    @Override
    public User disable(Id<User, Username> id) {
        User user = null;
        try {
            user = this.persistenceService.user().get(id);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to retrieve user with id " + id, e);
            throw new RuntimeException("Unable to disable user");
        }

        user.setEnabled(false);

        try {
            this.persistenceService.user().store(user);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to update user " + user, e);
            throw new RuntimeException("Unable to disable user");
        }

        return user;
    }

    @Override
    public List<User> getEnabled() {
        UserFilter filter = UserFilter.filterByEnabledStatus(true);
        try {
            return this.persistenceService.user().find(filter);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to retrieve users with filter " + filter, e);
            throw new RuntimeException("Unable to retrieve users");
        }
    }

    @Override
    public List<User> getDisabled() {
        UserFilter filter = UserFilter.filterByEnabledStatus(false);
        try {
            return this.persistenceService.user().find(filter);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to retrieve users with filter " + filter, e);
            throw new RuntimeException("Unable to retrieve users");
        }
    }
}
