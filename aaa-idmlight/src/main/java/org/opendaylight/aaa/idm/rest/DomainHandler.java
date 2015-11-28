/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.model.Claim;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.UserPwd;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.aaa.idm.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/domains")
public class DomainHandler {
   private static Logger logger = LoggerFactory.getLogger(DomainHandler.class);

   @GET
   @Produces("application/json")
   public Response getDomains() {
      logger.info("Get /domains");
      Domains domains=null;
      try {
         domains = ServiceLocator.INSTANCE.getStore().getDomains();
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting domains");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      return Response.ok(domains).build();
   }

   @GET
   @Path("/{id}")
   @Produces("application/json")
   public Response getDomain(@PathParam("id") String domainId)  {
      logger.info("Get /domains/" + domainId);
      Domain domain = null;
      try {
         domain = ServiceLocator.INSTANCE.getStore().readDomain(domainId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }

      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! domain id :" + domainId);
         return Response.status(404).entity(idmerror).build();
      }
      return Response.ok(domain).build();
   }

   @POST
   @Consumes("application/json")
   @Produces("application/json")
   public Response createDomain(@Context UriInfo info,Domain domain) {
      logger.info("Post /domains");
      try {
         if (domain.isEnabled()==null) {
            domain.setEnabled(false);
         }
         if (domain.getName()==null) {
            domain.setName("");
         }
         if (domain.getDescription()==null) {
            domain.setDescription("");
         }
         domain = ServiceLocator.INSTANCE.getStore().writeDomain(domain);
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error creating domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      return Response.status(201).entity(domain).build();
   }

   @PUT
   @Path("/{id}")
   @Consumes("application/json")
   @Produces("application/json")
   public Response putDomain(@Context UriInfo info,Domain domain,@PathParam("id") String domainId) {
      logger.info("Put /domains/" + domainId);
      try {
         domain.setDomainid(domainId);
         domain = ServiceLocator.INSTANCE.getStore().updateDomain(domain);
         if (domain==null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
         }
         IdmLightProxy.clearClaimCache();
         return Response.status(200).entity(domain).build();
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error putting domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
   }

   @DELETE
   @Path("/{id}")
   public Response deleteDomain(@Context UriInfo info,@PathParam("id") String domainId) {
      logger.info("Delete /domains/" + domainId);

      try {
         Domain domain = ServiceLocator.INSTANCE.getStore().deleteDomain(domainId);
         if (domain==null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
         }
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error deleting Domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }


   @POST
   @Path("/{did}/users/{uid}/roles")
   @Consumes("application/json")
   @Produces("application/json")
   public Response createGrant( @Context UriInfo info,
                                @PathParam("did") String domainId,
                                @PathParam("uid") String userId,
                                Grant grant) {
      logger.info("Post /domains/"+domainId+"/users/"+userId+"/roles");
      Domain domain=null;
      User user=null;
      Role role=null;
      String roleId=null;

      // validate domain id
      try {
         domain = ServiceLocator.INSTANCE.getStore().readDomain(domainId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! domain id :" + domainId);
         return Response.status(404).entity(idmerror).build();
      }
      grant.setDomainid(domainId);

      try {
         user = ServiceLocator.INSTANCE.getStore().readUser(userId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting user");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (user==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! User id :" + userId);
         return Response.status(404).entity(idmerror).build();
      }
      grant.setUserid(userId);

      // validate role id
      try {
         roleId= grant.getRoleid();
         logger.info("roleid = " + roleId);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Role id :" + grant.getRoleid());
         return Response.status(404).entity(idmerror).build();
      }
      try {
         role = ServiceLocator.INSTANCE.getStore().readRole(roleId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting role");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (role==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! role :" + grant.getRoleid());
         return Response.status(404).entity(idmerror).build();
      }

      // see if grant already exists for this
      try {
         Grant existingGrant = ServiceLocator.INSTANCE.getStore().readGrant(domainId,userId,roleId);
         if (existingGrant != null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Grant already exists for did:"+domainId+" uid:"+userId+" rid:"+roleId);
            return Response.status(403).entity(idmerror).build();
         }
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error creating grant");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }


      // create grant
      try {
         grant = ServiceLocator.INSTANCE.getStore().writeGrant(grant);
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error creating grant");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }

      IdmLightProxy.clearClaimCache();
      return Response.status(201).entity(grant).build();
   }


   @POST
   @Path("/{did}/users/roles")
   @Consumes("application/json")
   @Produces("application/json")
   public Response validateUser( @Context UriInfo info,
                                 @PathParam("did") String domainId,
                                 UserPwd userpwd) {

      logger.info("GET /domains/"+domainId+"/users");
      Domain domain=null;
      Claim claim = new Claim();
      List<Role> roleList = new ArrayList<Role>();

      try {
         domain = ServiceLocator.INSTANCE.getStore().readDomain(domainId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Domain id :" + domainId);
         return Response.status(404).entity(idmerror).build();
      }

      // check request body for username and pwd
      String username = userpwd.getUsername();
      if (username==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("username not specfied in request body");
         return Response.status(400).entity(idmerror).build();
      }
      String pwd = userpwd.getUserpwd();
      if (pwd==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("userpwd not specfied in request body");
         return Response.status(400).entity(idmerror).build();
      }

      // find userid for user
      try {
         Users users = ServiceLocator.INSTANCE.getStore().getUsers(username,domainId);
         List<User> userList = users.getUsers();
         if (userList.size()==0) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("did not find username: "+username);
            return Response.status(404).entity(idmerror).build();
         }
         User user = userList.get(0);
         String userPwd = user.getPassword();
         String reqPwd = userpwd.getUserpwd();
         if (!userPwd.equals(reqPwd)) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("password does not match for username: "+username);
            return Response.status(401).entity(idmerror).build();
         }
         claim.setDomainid(domainId);
         claim.setUsername(username);
         claim.setUserid(user.getUserid());
         try {
            Grants grants = ServiceLocator.INSTANCE.getStore().getGrants(domainId,user.getUserid());
            List<Grant> grantsList = grants.getGrants();
            for (int i=0; i < grantsList.size(); i++) {
               Grant grant = grantsList.get(i);
               Role role = ServiceLocator.INSTANCE.getStore().readRole(grant.getRoleid());
               roleList.add(role);
            }
         }
         catch (IDMStoreException se) {
            logger.error("StoreException : " + se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Roles");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
         }
         claim.setRoles(roleList);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting user");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }

      return Response.ok(claim).build();
   }

   @GET
   @Path("/{did}/users/{uid}/roles")
   @Produces("application/json")
   public Response getRoles( @Context UriInfo info,
                             @PathParam("did") String domainId,
                             @PathParam("uid") String userId) {
      logger.info("GET /domains/"+domainId+"/users/"+userId+"/roles");
      Domain domain=null;
      User user=null;
      Roles roles = new Roles();
      List<Role> roleList = new ArrayList<Role>();

      try {
         domain = ServiceLocator.INSTANCE.getStore().readDomain(domainId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Domain id :" + domainId);
         return Response.status(404).entity(idmerror).build();
      }

      try {
         user = ServiceLocator.INSTANCE.getStore().readUser(userId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting user");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (user==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! User id :" + userId);
         return Response.status(404).entity(idmerror).build();
      }

      try {
         Grants grants = ServiceLocator.INSTANCE.getStore().getGrants(domainId,userId);
         List<Grant> grantsList = grants.getGrants();
         for (int i=0; i < grantsList.size(); i++) {
            Grant grant = grantsList.get(i);
            Role role = ServiceLocator.INSTANCE.getStore().readRole(grant.getRoleid());
            roleList.add(role);
         }
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting Roles");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }

      roles.setRoles(roleList);
      return Response.ok(roles).build();
   }

   @DELETE
   @Path("/{did}/users/{uid}/roles/{rid}")
   public Response deleteGrant( @Context UriInfo info,
                                @PathParam("did") String domainId,
                                @PathParam("uid") String userId,
                                @PathParam("rid") String roleId) {
      Domain domain=null;
      User user=null;
      Role role=null;

      try {
         domain = ServiceLocator.INSTANCE.getStore().readDomain(domainId);
      }
      catch(IDMStoreException se) {
         logger.error("Error deleting Grant  : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting domain");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Domain id :" + domainId);
         return Response.status(404).entity(idmerror).build();
      }

      try {
         user = ServiceLocator.INSTANCE.getStore().readUser(userId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting user");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (user==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! User id :" + userId);
         return Response.status(404).entity(idmerror).build();
      }

      try {
         role = ServiceLocator.INSTANCE.getStore().readRole(roleId);
      }
      catch(IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error getting Role");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      if (role==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Role id :" + roleId);
         return Response.status(404).entity(idmerror).build();
      }

      // see if grant already exists
      try {
         Grant existingGrant = ServiceLocator.INSTANCE.getStore().readGrant(domainId,userId,roleId);
         if (existingGrant == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Grant does not exist for did:"+domainId+" uid:"+userId+" rid:"+roleId);
            return Response.status(404).entity(idmerror).build();
         }
         existingGrant = ServiceLocator.INSTANCE.getStore().deleteGrant(existingGrant.getDomainid());
      }
      catch (IDMStoreException se) {
         logger.error("StoreException : " + se);
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Internal error creating grant");
         idmerror.setDetails(se.getMessage());
         return Response.status(500).entity(idmerror).build();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }

}
