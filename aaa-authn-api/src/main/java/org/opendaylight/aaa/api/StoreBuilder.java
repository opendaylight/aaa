/*
 * Copyright (c) 2014, 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
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
 * </ol>
 *
 * @author peter.mellquist@hp.com
 * @author saichler@cisco.com
 * @author Michael Vorburger.ch - some refactoring, for new CLI tool
 */
public class StoreBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(StoreBuilder.class);

    private final IIDMStore store;

    public StoreBuilder(final IIDMStore store) {
        this.store = store;
    }

    /**
     * Initialize IIDMStore with the default domain and the 'user' and 'admin' roles, if needed.
     * This does not create any default user entries (because they are an inherent security risk).
     *
     * @param domainID ID (same as name) of the "authentication domain"
     * @return ID of the just newly created Domain, or null if no new one had to be created
     * @throws IDMStoreException for issues coming from the IIDMStore
     */
    public String initDomainAndRolesWithoutUsers(final String domainID) throws IDMStoreException {
        LOG.info("Checking if default entries must be created in IDM store");

        // Check whether the default domain exists. If it exists, then do not
        // create default data in the store.
        // FIXME: Address the fact that someone may delete the sdn domain, or make sdn mandatory.
        Domain defaultDomain = store.readDomain(domainID);
        if (defaultDomain != null) {
            LOG.info("Found default domain in IDM store, skipping insertion of default data");
            return null;
        }

        // Create default domain
        Domain domain = new Domain();
        domain.setEnabled(true);
        domain.setName(IIDMStore.DEFAULT_DOMAIN);
        domain.setDescription("default odl sdn domain");
        domain = store.writeDomain(domain);
        LOG.info("Created default domain");
        String newDomainID = domain.getDomainid();

        // Create default Roles ("admin" and "user")
        Role adminRole = new Role();
        adminRole.setName("admin");
        adminRole.setDomainid(newDomainID);
        adminRole.setDescription("a role for admins");
        store.writeRole(adminRole);
        LOG.info("Created 'admin' role");

        Role userRole = new Role();
        userRole.setName("user");
        userRole.setDomainid(newDomainID);
        userRole.setDescription("a role for users");
        store.writeRole(userRole);
        LOG.info("Created 'user' role");

        return newDomainID;
    }

    /**
     * Initialize IIDMStore with the default domain and the 'user' and 'admin'
     * roles AND a default admin account (with default password, which is bad practice).
     *
     * @param domainID ID (same as name) of the "authentication domain"
     * @throws IDMStoreException for issues coming from the IIDMStore
     */
    public void initWithDefaultUsers(final String domainID) throws IDMStoreException {
        String newDomainID = initDomainAndRolesWithoutUsers(domainID);
        if (newDomainID != null) {
            createUser(newDomainID, "admin", "admin", true);
        }
    }

    public List<String> getRoleIDs(final String domainID, final List<String> roleNames) throws IDMStoreException {
        Map<String, String> roleNameToID = new HashMap<>();
        List<Role> roles = store.getRoles().getRoles();
        for (Role role : roles) {
            if (domainID.equals(role.getDomainid())) {
                roleNameToID.put(role.getName(), role.getRoleid());
            }
        }

        List<String> roleIDs = new ArrayList<>(roleNames.size());
        for (String roleName : roleNames) {
            String roleID = roleNameToID.get(roleName);
            if (roleID == null) {
                throw new IllegalStateException("'" + roleName + "' role not found (in domain '" + domainID + "')");
            }
            roleIDs.add(roleID);
        }

        return roleIDs;
    }

    /**
     * Create new user.
     *
     * @param domainID ID (same as name) of the "authentication domain"
     * @param userName new user name (without the domain prefix which gets automatically added)
     * @param password the new user's initial password
     * @param roleIDs list of IDs of roles to grant the new user (e.g. ["user", "admin"])
     *
     * @return ID of the just newly created user, useful to reference it e.g. in grants
     * @throws IDMStoreException for issues coming from the IIDMStore
     */
    public String createUser(final String domainID, final String userName, final String password,
            final List<String> roleIDs) throws IDMStoreException {
        User newUser = new User();
        newUser.setEnabled(true);
        newUser.setDomainid(domainID);
        newUser.setName(userName);
        newUser.setDescription(userName + " user");
        newUser.setEmail("");
        newUser.setPassword(password);
        newUser = store.writeUser(newUser);
        LOG.debug("Created '{}' user in domain '{}'", userName, domainID);

        String newUserID = newUser.getUserid();
        for (String roleID : roleIDs) {
            createGrant(domainID, newUserID, roleID);
        }
        return newUserID;
    }

    public String createUser(final String domainID, final String userName, final String password, final boolean isAdmin)
            throws IDMStoreException {
        List<String> roleIDs;
        if (isAdmin) {
            roleIDs = getRoleIDs(domainID, Arrays.asList("user", "admin"));
        } else {
            roleIDs = getRoleIDs(domainID, Arrays.asList("user"));
        }
        return createUser(domainID, userName, password, roleIDs);
    }

    public boolean deleteUser(final String domainID, final String userName) throws IDMStoreException {
        String userID = IDMStoreUtil.createUserid(userName, domainID);
        // NOT store.getGrants(domainID, userName) !
        Grants grants = store.getGrants(userID);
        for (Grant grant : grants.getGrants()) {
            store.deleteGrant(grant.getGrantid());
        }
        return store.deleteUser(userID) != null;
    }

    private void createGrant(final String domainID, final String userID, final String roleID) throws IDMStoreException {
        Grant grant = new Grant();
        grant.setDomainid(domainID);
        grant.setUserid(userID);
        grant.setRoleid(roleID);
        store.writeGrant(grant);
        LOG.debug("Granted '{}' user the '{}' role in domain '{}'", userID, roleID, domainID);
    }
}
