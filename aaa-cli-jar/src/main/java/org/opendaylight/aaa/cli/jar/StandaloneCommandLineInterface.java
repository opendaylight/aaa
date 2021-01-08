/*
 * Copyright (c) 2016 - 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.authn.h2.H2Store;
import org.opendaylight.aaa.authn.h2.IdmLightConfig;
import org.opendaylight.aaa.authn.h2.IdmLightConfigBuilder;
import org.opendaylight.aaa.authn.h2.IdmLightSimpleConnectionProvider;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;

/**
 * AAA CLI interface.
 * This is for a "standalone Java" environment (i.e. plain JSE; non-OSGi, no Karaf).
 *
 * @author Michael Vorburger.ch
 */
public class StandaloneCommandLineInterface {

    private final IIDMStore identityStore;
    private final StoreBuilder storeBuilder;
    private static final String DOMAIN = IIDMStore.DEFAULT_DOMAIN;
    private final PasswordHashService passwordService;

    public StandaloneCommandLineInterface(final File directoryWithDatabaseFile) throws IOException, IDMStoreException {
        IdmLightConfigBuilder configBuider = new IdmLightConfigBuilder();
        configBuider.dbDirectory(directoryWithDatabaseFile.getCanonicalPath()).dbUser("foo").dbPwd("bar");
        IdmLightConfig config = configBuider.build();

        passwordService = new DefaultPasswordHashService();

        H2Store h2Store = new H2Store(new IdmLightSimpleConnectionProvider(config), passwordService);
        this.identityStore = h2Store;

        this.storeBuilder = new StoreBuilder(h2Store);
        storeBuilder.initDomainAndRolesWithoutUsers(DOMAIN);
    }

    public List<String> getAllUserNames() throws IDMStoreException {
        List<User> users = identityStore.getUsers().getUsers();
        return users.stream().map(User::getName).collect(Collectors.toList());
    }

    public boolean resetPassword(final String userIdWithoutDomain, final String newPassword) throws IDMStoreException {
        Optional<User> optUser = getSingleUser(userIdWithoutDomain);
        if (!optUser.isPresent()) {
            return false;
        } else {
            User user = optUser.get();
            user.setPassword(newPassword);
            identityStore.updateUser(user);
            return true;
        }
    }

    /**
     * Check a user's password.
     * See <a href="https://bugs.opendaylight.org/show_bug.cgi?id=8721">Bug 8721 requirement</a>.
     */
    public boolean checkUserPassword(final String userIdWithoutDomain, final String password)
            throws IDMStoreException {
        Optional<User> optUser = getSingleUser(userIdWithoutDomain);
        if (!optUser.isPresent()) {
            return false;
        } else {
            User user = optUser.get();
            return passwordService.passwordsMatch(password, user.getPassword(), user.getSalt());
        }
    }

    private Optional<User> getSingleUser(final String userIdWithoutDomain) throws IDMStoreException {
        requireNonNull(userIdWithoutDomain, "userIdWithoutDomain == null");
        List<User> users = identityStore.getUsers(userIdWithoutDomain, DOMAIN).getUsers();
        if (users.isEmpty()) {
            return Optional.empty();
        }
        if (users.size() > 1) {
            throw new IDMStoreException("More than 1 user found: " + userIdWithoutDomain);
        }
        return Optional.of(users.get(0));
    }

    public void createNewUser(final String userName, final String password, final boolean isAdmin)
            throws IDMStoreException {
        requireNonNull(userName, "userName == null");
        storeBuilder.createUser(DOMAIN, userName, password, isAdmin);
    }

    public boolean deleteUser(final String userIdWithoutDomain) throws IDMStoreException {
        requireNonNull(userIdWithoutDomain, "userIdWithoutDomain == null");
        return storeBuilder.deleteUser(DOMAIN, userIdWithoutDomain);
    }
}
