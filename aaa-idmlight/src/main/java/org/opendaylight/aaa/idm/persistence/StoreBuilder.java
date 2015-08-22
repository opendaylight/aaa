/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.persistence;

import java.io.File;

import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
/**
 *
 * @author peter.mellquist@hp.com
 *
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreBuilder {
   private static Logger logger = LoggerFactory.getLogger(StoreBuilder.class);
   private static DomainStore domainStore = new DomainStore();
   private static UserStore userStore = new UserStore();
   private static RoleStore roleStore = new RoleStore();
   private static GrantStore grantStore = new GrantStore();
   public static String DEFAULT_DOMAIN = "sdn";
   // IdmLight appends ".mv.db" to the end of a database file name
   private static final String IDM_LIGHT_EXTENSION = ".mv.db";

   private String getIdmLightFileName(final String databaseName) {
      StringBuilder fileName = new StringBuilder();
      fileName.append(databaseName);
      fileName.append(IDM_LIGHT_EXTENSION);
      return fileName.toString();
   }

   public boolean exists() {
      String idmLightFileName = this.getIdmLightFileName(IdmLightApplication.getConfig().getDbName());
      File f = new File(idmLightFileName);
      return f.exists();
   }

   public void init() {
      logger.info("creating idmlight db");
      // make domain
      Domain domain = new Domain();
      User adminUser = new User();
      User userUser = new User();
      Role adminRole = new Role();
      Role userRole = new Role();
      try {
         domain.setEnabled(true);
         domain.setName(DEFAULT_DOMAIN);
         domain.setDescription("default odl sdn domain");
         domain = domainStore.createDomain(domain);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }
      // create users
      try {
         // admin user
         adminUser.setEnabled(true);
         adminUser.setName("admin");
         adminUser.setDomainID(domain.getDomainid());
         adminUser.setDescription("admin user");
         adminUser.setEmail("");
         adminUser.setPassword("admin");
         adminUser = userStore.createUser(adminUser);

         // user user
         userUser.setEnabled(true);
         userUser.setName("user");
         userUser.setDomainID(domain.getDomainid());
         userUser.setDescription("user user");
         userUser.setEmail("");
         userUser.setPassword("user");
         userUser = userStore.createUser(userUser);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }

      // create Roles
      try {
         adminRole.setName("admin");
         adminRole.setDomainID(domain.getDomainid());
         adminRole.setDescription("a role for admins");
         adminRole = roleStore.createRole(adminRole);
         userRole.setName("user");
         userRole.setDomainID(domain.getDomainid());
         userRole.setDescription("a role for users");
         userRole = roleStore.createRole(userRole);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }

      // create grants
      Grant grant = new Grant();
      try {
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(userUser.getUserid());
         grant.setRoleid(userRole.getRoleid());
         grant = grantStore.createGrant(grant);

         grant.setDomainid(domain.getDomainid());
         grant.setUserid(adminUser.getUserid());
         grant.setRoleid(userRole.getRoleid());
         grant = grantStore.createGrant(grant);

         grant.setDomainid(domain.getDomainid());
         grant.setUserid(adminUser.getUserid());
         grant.setRoleid(adminRole.getRoleid());
         grant = grantStore.createGrant(grant);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }

   }
}
