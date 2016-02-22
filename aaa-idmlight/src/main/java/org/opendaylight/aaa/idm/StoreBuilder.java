/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StoreBuilder is triggered during feature installation by
 * <code>AAAIDMLightModule.createInstance()</code>. StoreBuilder is responsible
 * for initializing the H2 database with initial default user account
 * information. By default, the following users are created:
 * <ol>
 * <li>admin</li>
 * <li>user</li>
 * </ol>
 *
 * By default, the following domain is created:
 * <ol>
 * <li>sdn</li>
 * </ol>
 *
 * By default, the following grants are created:
 * <ol>
 * <li>admin with admin role on sdn</li>
 * <li>admin with user role on sdn</li>
 * <li>user with user role on sdn</li>
 * </ol>
 *
 * @author peter.mellquist@hp.com
 * @author saichler@cisco.com
 */
public class StoreBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(StoreBuilder.class);

    public static void init(IIDMStore store) throws IDMStoreException {
        LOG.info("creating idmlight schema in store");

        // Check whether the default domain exists. If it exists, then do not
        // create default data in the store.
        // TODO Address the fact that someone may delete the sdn domain, or make
        // sdn mandatory.
        Domain defaultDomain = store.readDomain(IIDMStore.DEFAULT_DOMAIN);
        if (defaultDomain != null) {
            LOG.info("Found default domain in Store, skipping insertion of default data");
            return;
        }

        // make domain
        Domain domain = new Domain();
        User adminUser = new User();
        User userUser = new User();
        Role adminRole = new Role();
        Role userRole = new Role();
        domain.setEnabled(true);
        domain.setName(IIDMStore.DEFAULT_DOMAIN);
        domain.setDescription("default odl sdn domain");
        domain = store.writeDomain(domain);

        // Create default users
        // "admin" user
        adminUser.setEnabled(true);
        adminUser.setName("admin");
        adminUser.setDomainid(domain.getDomainid());
        adminUser.setDescription("admin user");
        adminUser.setEmail("");
        adminUser.setPassword("admin");
        adminUser = store.writeUser(adminUser);
        // "user" user
        userUser.setEnabled(true);
        userUser.setName("user");
        userUser.setDomainid(domain.getDomainid());
        userUser.setDescription("user user");
        userUser.setEmail("");
        userUser.setPassword("user");
        userUser = store.writeUser(userUser);

        // Create default Roles ("admin" and "user")
        adminRole.setName("admin");
        adminRole.setDomainid(domain.getDomainid());
        adminRole.setDescription("a role for admins");
        adminRole = store.writeRole(adminRole);
        userRole.setName("user");
        userRole.setDomainid(domain.getDomainid());
        userRole.setDescription("a role for users");
        userRole = store.writeRole(userRole);

        // Create default grants
        Grant grant = new Grant();
        grant.setDomainid(domain.getDomainid());
        grant.setUserid(userUser.getUserid());
        grant.setRoleid(userRole.getRoleid());
        grant = store.writeGrant(grant);

        grant.setDomainid(domain.getDomainid());
        grant.setUserid(adminUser.getUserid());
        grant.setRoleid(userRole.getRoleid());
        grant = store.writeGrant(grant);

        grant.setDomainid(domain.getDomainid());
        grant.setUserid(adminUser.getUserid());
        grant.setRoleid(adminRole.getRoleid());
        grant = store.writeGrant(grant);
    }

    public static Claim createUserInStore(PasswordCredentials creds, IIDMStore store) throws IDMStoreException {
        if(store!=null) {
            Users users = store.getUsers(creds.username(), creds.domain());
            // Local User does not exist, create it with a default user role
            if (users != null && users.getUsers() != null && users.getUsers().size() == 0) {
                Domain domain = store.readDomain(IIDMStore.DEFAULT_DOMAIN);
                Role role = store.readRole("user@sdn");

                User userUser = new User();
                userUser.setEnabled(true);
                userUser.setName(creds.username());
                userUser.setDomainid(domain.getDomainid());
                userUser.setDescription(creds.username()+" external user");
                userUser.setEmail("");
                userUser.setPassword(creds.password());
                userUser = store.writeUser(userUser);
                Grant grant = new Grant();
                grant.setRoleid(role.getRoleid());
                grant.setDomainid(domain.getDomainid());
                grant.setUserid(userUser.getUserid());
                grant = store.writeGrant(grant);

                ClaimBuilder claim = new ClaimBuilder();

                claim.setUserId(userUser.getUserid());
                claim.setUser(creds.username());
                claim.setDomain(domain.getDomainid());
                claim.addRole(role.getRoleid());
                return claim.build();

            }
        }
        return null;
    }
}
