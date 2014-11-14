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
   //private static DomainStore domainStore = new DomainStore();   
   //private static UserStore userStore = new UserStore(); 
   //private static RoleStore roleStore = new RoleStore(); 
   //private static GrantStore grantStore = new GrantStore();
   public static String DEFAULT_DOMAIN = "sdn";
  
   public boolean exists() {
      File f = new File(IdmLightApplication.config.dbName);
      return f.exists();
   }
 

   public void init() {
      logger.info("creating idmlight db");
      // make domain
      Domain domain = (Domain)OStore.newStorable(Domain.class);//new Domain();
      User adminUser = (User)OStore.newStorable(User.class);
      User userUser = (User)OStore.newStorable(User.class);
      Role adminRole = (Role)OStore.newStorable(Role.class);
      Role userRole = (Role)OStore.newStorable(Role.class);
      //try {
         domain.setEnabled(true);
         domain.setName(DEFAULT_DOMAIN);
         domain.setDescription("default odl sdn domain");
         Domain dtemp = (Domain)domain.get();
         if(dtemp==null){
        	 domain = (Domain)domain.write();
         }else
        	 domain = dtemp;
         //domain = domainStore.createDomain(domain);
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
      //}
      // create users
      //try {
         // admin user
         adminUser.setEnabled(true);
         adminUser.setName("admin");
         adminUser.setDescription("admin user");
         adminUser.setEmail("");
         adminUser.setPassword(MD5Calculator.getMD5("admin"));
         //adminUser = (userStore.createUser(adminUser);
         User uTemp = (User)adminUser.get();
         if(uTemp==null){
        	 adminUser = (User)adminUser.write();
         }else
        	 adminUser = uTemp;

         // user user
         userUser.setEnabled(true);
         userUser.setName("user");
         userUser.setDescription("user user");
         userUser.setEmail("");
         userUser.setPassword(MD5Calculator.getMD5("user"));
         //userUser = userStore.createUser(userUser);
         uTemp = (User)userUser.get();
         if(uTemp==null){
        	 userUser = (User)userUser.write();
         }else
        	 userUser = uTemp;
      //}
      //catch (StoreException se) {
        // logger.error("StoreException : " + se);
      //}

      // create Roles
      //try {
         adminRole.setName("admin");
         adminRole.setDescription("a role for admins");
         adminRole = (Role)adminRole.write();
         //adminRole = roleStore.createRole(adminRole);
         userRole.setName("user");
         userRole.setDescription("a role for users");
         Role rTemp = (Role)userRole.get();
         if(rTemp==null){
        	 userRole = (Role)userRole.write();
         }else
        	 userRole = rTemp;
         //userRole = roleStore.createRole(userRole);
      //}
       //catch (StoreException se) {
         //logger.error("StoreException : " + se);
      //}

      // create grants
      Grant grant = (Grant)OStore.newStorable(Grant.class);
      //try {
         grant.setDescription("user with user role");
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(userUser.getUserid());
         grant.setRoleid(userRole.getRoleid());
         grant = (Grant)grant.write();

         grant.setDescription("admin with user role");
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(adminUser.getUserid());
         grant.setRoleid(userRole.getRoleid());
         grant = (Grant)grant.write();

         grant.setDescription("admin with admin role");
         grant.setDomainid(domain.getDomainid());
         grant.setUserid(adminUser.getUserid());
         grant.setRoleid(adminRole.getRoleid());
         grant = (Grant)grant.write();
      //}
      //catch (StoreException se) {
        // logger.error("StoreException : " + se);
      //}

   }
}

