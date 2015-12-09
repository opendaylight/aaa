/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peter.mellquist@hp.com
 * @author saichler@cisco.com
 */

public class StoreBuilder {

    private static Logger logger = LoggerFactory.getLogger(StoreBuilder.class);

    public static void init(IIDMStore store) throws IDMStoreException {
        logger.info("creating idmlight schema in store");

        //Check if default domain exist, if it exist then do not create default data in the store
        Domain defaultDomain = store.readDomain(IIDMStore.DEFAULT_DOMAIN);
        if(defaultDomain!=null){
            logger.info("Found default domain in Store, skipping insertion of default data");
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

        // create users
        //admin user
        adminUser.setEnabled(true);
        adminUser.setName("admin");
        adminUser.setDomainid(domain.getDomainid());
        adminUser.setDescription("admin user");
        adminUser.setEmail("");
        adminUser.setPassword("admin");
        adminUser = store.writeUser(adminUser);
        // user user
        userUser.setEnabled(true);
        userUser.setName("user");
        userUser.setDomainid(domain.getDomainid());
        userUser.setDescription("user user");
        userUser.setEmail("");
        userUser.setPassword("user");
        userUser = store.writeUser(userUser);

        // create Roles
        adminRole.setName("admin");
        adminRole.setDomainid(domain.getDomainid());
        adminRole.setDescription("a role for admins");
        adminRole = store.writeRole(adminRole);
        userRole.setName("user");
        userRole.setDomainid(domain.getDomainid());
        userRole.setDescription("a role for users");
        userRole = store.writeRole(userRole);

        // create grants
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
}
