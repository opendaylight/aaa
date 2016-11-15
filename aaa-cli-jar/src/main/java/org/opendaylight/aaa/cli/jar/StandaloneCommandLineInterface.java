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
import java.util.stream.Collectors;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
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

    public StandaloneCommandLineInterface(File directoryWithDatabaseFile) throws IOException {
        IdmLightConfigBuilder configBuider = new IdmLightConfigBuilder();
        configBuider.dbDirectory(directoryWithDatabaseFile.getCanonicalPath());
        IdmLightConfig config = configBuider.build();

        H2Store h2Store = new H2Store(new IdmLightSimpleConnectionProvider(config));
        this.identityStore = h2Store;
    }

    public List<String> getAllUserNames() throws IDMStoreException {
        List<User> users = identityStore.getUsers().getUsers();
        return users.stream().map(user -> user.getName()).collect(Collectors.toList());
    }

    public boolean resetPassword(String userIdWithoutDomain, String newPassword) throws IDMStoreException {
        Preconditions.checkNotNull(userIdWithoutDomain, "userIdWithoutDomain == null");
        String userId = IDMStoreUtil.createUserid(userIdWithoutDomain, IIDMStore.DEFAULT_DOMAIN);
        User user = identityStore.readUser(userId);
        if (user != null) {
            user.setPassword(newPassword);
            identityStore.updateUser(user);
            return true;
        } else {
            return false;
        }
    }

    public void createNewUser(String userName, String newPassword) throws IDMStoreException {
        Preconditions.checkNotNull(userName, "userName == null");
        // TODO The following code should ideally be shared better between here, StoreBuilder & UserHandler
        User newUser = new User();
        newUser.setEnabled(true);
        // do NOT setUserid here, because UserStore.createUser() does that
        // by concatenating name & domainId using IDMStoreUtil.createUserid()
        newUser.setName(userName);
        newUser.setDomainid(IIDMStore.DEFAULT_DOMAIN);
        newUser.setDescription(
                userName + " user (created by " + StandaloneCommandLineInterface.class.getSimpleName() + ")");
        newUser.setEmail("");
        newUser.setPassword(newPassword);
        identityStore.writeUser(newUser);
    }
}
