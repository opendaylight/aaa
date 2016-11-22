/*
 * Copyright (c) 2014, 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StoreBuilder is triggered during feature installation by
 * <code>AAAIDMLightModule.createInstance()</code>. StoreBuilder is responsible
 * for initializing the IIDMStore with initial default user account
 * information. By default, the following users are created:
 * <ol>
 * <li>admin</li>
 * <li>user</li>
 * </ol>
 *
 * <p>By default, the following domain is created:
 * <ol>
 * <li>sdn</li>
 * </ol>
 *
 * <p>By default, the following grants are created:
 * <ol>
 * <li>admin with admin role on sdn</li>
 * <li>admin with user role on sdn</li>
 * <li>user with user role on sdn</li>
 * </ol>
 *
 * @author peter.mellquist@hp.com
 * @author saichler@cisco.com
 * @author Michael Vorburger.ch - some refactoring, for new CLI tool
 */
public class StoreBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(StoreBuilder.class);

    private final IIDMStore store;

    public StoreBuilder(IIDMStore store) {
        super();
        this.store = store;
    }

    /**
     * Initialize IIDMStore with some default entries.
     * @deprecated Better explicitly use {@link #initDomainAndRolesWithoutUsers()} or {@link #initWithDefaultUsers()}
     */
    @Deprecated
    public void init() throws IDMStoreException {
        initWithDefaultUsers(IIDMStore.DEFAULT_DOMAIN);
    }

    /**
     * Initialize IIDMStore with the default domain and the 'user' and 'admin' roles, if needed.
     * This does not create any default user entries (because they are an inherent security risk).
     * @return true if initialization took place, false if it wasn't needed
     */
    public boolean initDomainAndRolesWithoutUsers() throws IDMStoreException {
        LOG.info("Checking if default entries must be created in IDM store");

        // Check whether the default domain exists. If it exists, then do not
        // create default data in the store.
        // TODO Address the fact that someone may delete the sdn domain, or make
        // sdn mandatory.
        Domain defaultDomain = store.readDomain(IIDMStore.DEFAULT_DOMAIN);
        if (defaultDomain != null) {
            LOG.info("Found default domain in IDM store, skipping insertion of default data");
            return false;
        }

        // Create default domain
        Domain domain = new Domain();
        domain.setEnabled(true);
        domain.setName(IIDMStore.DEFAULT_DOMAIN);
        domain.setDescription("default odl sdn domain");
        domain = store.writeDomain(domain);
        LOG.info("Created default default domain");

        // Create default Roles ("admin" and "user")
        Role adminRole = new Role();
        adminRole.setName("admin");
        adminRole.setDomainid(domain.getDomainid());
        adminRole.setDescription("a role for admins");
        adminRole = store.writeRole(adminRole);
        LOG.info("Created 'admin' role");

        Role userRole = new Role();
        userRole.setName("user");
        userRole.setDomainid(domain.getDomainid());
        userRole.setDescription("a role for users");
        userRole = store.writeRole(userRole);
        LOG.info("Created 'user' role");

        return true;
    }

    /**
     * Initialize IIDMStore with the default domain and the 'user' and 'admin'
     * roles AND 2 default user accounts (with default passwords, which is bad practice).
     */
    public void initWithDefaultUsers(String domainID) throws IDMStoreException {
        if (initDomainAndRolesWithoutUsers()) {
            createUser(domainID, "admin", "admin", true);
            createUser(domainID, "user", "user", false);
        }
    }

    /**
     * Create new user.
     *
     * @param userName user ID (without the domain prefix which gets automatically added)
     * @param isAdmin whether to also grant the user the 'admin' role, or just the 'user' role
     * @return new users's full ID (with the domain prefix)
     */
    public void createUser(String domainID, String userName, String password, boolean isAdmin)
            throws IDMStoreException {
        User newUser = new User();
        newUser.setEnabled(true);
        newUser.setDomainid(domainID);
        newUser.setName(userName);
        newUser.setDescription(userName + " user");
        newUser.setEmail("");
        newUser.setPassword(password);
        newUser = store.writeUser(newUser);
        LOG.info("Created '" + userName + "' user in domain '" + domainID + "'");
        createGrant(domainID, userName, "user");
        if (isAdmin) {
            createGrant(domainID, userName, "admin");
        }
    }

    private void createGrant(String domainID, String userName, String roleName)
            throws IDMStoreException {
        String userID = IDMStoreUtil.createUserid(userName, domainID);
        String fullRoleIdWithDomain = IDMStoreUtil.createRoleid(roleName, domainID);
        Grant grant = new Grant();
        grant.setDomainid(domainID);
        grant.setUserid(userID);
        grant.setRoleid(fullRoleIdWithDomain);
        grant = store.writeGrant(grant);
        LOG.info("Granted '" + userName + "' user the '" + roleName + "' role in domain '" + domainID + "'");
    }
}
