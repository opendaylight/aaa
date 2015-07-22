/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
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
   private static JDBCObjectStore store = new JDBCObjectStore();
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
         domain = (Domain)store.createPOJO(domain);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }
      // create users
      try {
         // admin user
         adminUser.setEnabled(true);
         adminUser.setName("admin");
         adminUser.setDescription("admin user");
         adminUser.setEmail("");
         adminUser.setPassword("admin");
         adminUser = (User)store.createPOJO(adminUser);

         // user user
         userUser.setEnabled(true);
         userUser.setName("user");
         userUser.setDescription("user user");
         userUser.setEmail("");
         userUser.setPassword("user");
         userUser = (User)store.createPOJO(userUser);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }

      // create Roles
      try {
         adminRole.setName("admin");
         adminRole.setDescription("a role for admins");
         adminRole = (Role)store.createPOJO(adminRole);
         userRole.setName("user");
         userRole.setDescription("a role for users");
         userRole = (Role)store.createPOJO(userRole);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }

      // create grants
      Grant grant = new Grant();
      try {
         grant.setDescription("user with user role");
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(userUser.getUserid());
         grant.setRoleid(userRole.getRoleid());
         grant = (Grant)store.createPOJO(grant);

         grant.setDescription("admin with user role");
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(adminUser.getUserid());
         grant.setRoleid(userRole.getRoleid());
         grant = (Grant)store.createPOJO(grant);

         grant.setDescription("admin with admin role");
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(adminUser.getUserid());
         grant.setRoleid(adminRole.getRoleid());
         grant = (Grant)store.createPOJO(grant);
      } catch (StoreException se) {
         logger.error("StoreException : " + se);
      }

   }
}
