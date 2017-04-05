/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.h2.config.IdmLightConfig;
import org.opendaylight.aaa.h2.config.IdmLightConfigBuilder;
import org.opendaylight.aaa.h2.config.IdmLightSimpleConnectionProvider;
import org.opendaylight.aaa.h2.persistence.H2Store;

/**
 * AAA CLI interface.
 * This is for a "standalone Java" environment (i.e. plain JSE; non-OSGi, no Karaf).
 *
 * @author Michael Vorburger
 */
public class StandaloneCommandLineInterface {

    private final IIDMStore identityStore;
    private final StoreBuilder storeBuilder;
    private static final String DOMAIN = IIDMStore.DEFAULT_DOMAIN;

    public StandaloneCommandLineInterface(File directoryWithDatabaseFile) throws IOException, IDMStoreException {
        IdmLightConfigBuilder configBuider = new IdmLightConfigBuilder();
        configBuider.dbDirectory(directoryWithDatabaseFile.getCanonicalPath());
        IdmLightConfig config = configBuider.build();

        H2Store h2Store = new H2Store(new IdmLightSimpleConnectionProvider(config));
        this.identityStore = h2Store;

        this.storeBuilder = new StoreBuilder(h2Store);
        storeBuilder.initDomainAndRolesWithoutUsers(DOMAIN);
    }

    public List<String> getAllUserNames() throws IDMStoreException {
        List<User> users = identityStore.getUsers().getUsers();
        return users.stream().map(user -> user.getName()).collect(Collectors.toList());
    }

    public boolean resetPassword(String userIdWithoutDomain, String newPassword) throws IDMStoreException {
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

    private Optional<User> getSingleUser(String userIdWithoutDomain) throws IDMStoreException {
        Preconditions.checkNotNull(userIdWithoutDomain, "userIdWithoutDomain == null");
        List<User> users = identityStore.getUsers(userIdWithoutDomain, DOMAIN).getUsers();
        if (users.isEmpty()) {
            return Optional.empty();
        }
        if (users.size() > 1) {
            throw new IDMStoreException("More than 1 user found: " + userIdWithoutDomain);
        }
        return Optional.of(users.get(0));
    }

    public void createNewUser(String userName, String password, boolean isAdmin) throws IDMStoreException {
        Preconditions.checkNotNull(userName, "userName == null");
        storeBuilder.createUser(DOMAIN, userName, password, isAdmin);
    }

    public boolean deleteUser(String userIdWithoutDomain) throws IDMStoreException {
        Preconditions.checkNotNull(userIdWithoutDomain, "userIdWithoutDomain == null");
        return storeBuilder.deleteUser(DOMAIN, userIdWithoutDomain);
    }
}
