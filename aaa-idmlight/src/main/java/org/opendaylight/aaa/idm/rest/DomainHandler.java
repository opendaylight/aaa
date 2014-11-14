/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
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

import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.aaa.idm.model.Claim;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Domains;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Grants;
import org.opendaylight.aaa.idm.model.IDMError;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.Roles;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.UserPwd;
import org.opendaylight.aaa.idm.model.Users;
import org.opendaylight.aaa.idm.persistence.MD5Calculator;
import org.opendaylight.aaa.idm.persistence.OStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/domains")
public class DomainHandler {
   private static Logger logger = LoggerFactory.getLogger(DomainHandler.class);
   //private static DomainStore domainStore = new DomainStore();
   //private static UserStore userStore = new UserStore();
   //private static RoleStore roleStore = new RoleStore();
   //private static GrantStore grantStore = new GrantStore(); 
   
   @GET
   @Produces("application/json")
   public Response getDomains() {
      logger.info("Get /domains");
      Domains domains= new Domains();//null;
      //try {
        // domains = domainStore.getDomains();
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting domains");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      return Response.ok(domains).build();
   }

   @GET
   @Path("/{id}")
   @Produces("application/json")
   public Response getDomain(@PathParam("id") String id)  {
      logger.info("Get /domains/" + id);
      Domain domain = (Domain)OStore.newStorable(Domain.class);
      long longId=0;
      try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + id);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
    	  domain.setDomainid((int)longId);
    	  domain = (Domain)domain.get();
         //domain = domainStore.getDomain(longId);
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}

      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! domain id :" + id);
         return Response.status(404).entity(idmerror).build();
      }
      return Response.ok(domain).build();
   }

   @POST
   @Consumes("application/json")
   @Produces("application/json")
   public Response createDomain(@Context UriInfo info,Domain domain) {
      logger.info("Post /domains");
      //try {
         if (domain.getEnabled()==null)
            domain.setEnabled(false);
         if (domain.getName()==null)
            domain.setName("");
         if (domain.getDescription()==null)
            domain.setDescription("");
         domain = (Domain)domain.write();
         //domain = domainStore.createDomain(domain);
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error creating domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //} 
      return Response.status(201).entity(domain).build();
   } 

   @PUT
   @Path("/{id}")
   @Consumes("application/json")
   @Produces("application/json")
   public Response putDomain(@Context UriInfo info,Domain domain,@PathParam("id") String id) {
      long longId=0;
      logger.info("Put /domains/" + id);
       try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + id);
         return Response.status(404).entity(idmerror).build();
      }

      //try {
         domain.setDomainid((int)longId);
         domain = (Domain)domain.write();
         //domain = domainStore.putDomain(domain);
         if (domain==null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + id);
            return Response.status(404).entity(idmerror).build();
         }
         IdmLightProxy.clearClaimCache();
         return Response.status(200).entity(domain).build();
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error putting domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
   }

   @DELETE
   @Path("/{id}")
   public Response deleteDomain(@Context UriInfo info,@PathParam("id") String id) {
      long longId=0;
      logger.info("Delete /domains/" + id);
      try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + id);
         return Response.status(404).entity(idmerror).build();
      }

      //try {
         Domain domain = (Domain)OStore.newStorable(Domain.class);//new Domain();
         domain.setDomainid((int)longId);
         domain = (Domain)domain.delete();
         //domain = domainStore.deleteDomain(domain);
         if (domain==null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + id);
            return Response.status(404).entity(idmerror).build();
         }
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error deleting Domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }


   @POST
   @Path("/{did}/users/{uid}/roles")
   @Consumes("application/json")
   @Produces("application/json")
   public Response createGrant( @Context UriInfo info, 
                                @PathParam("did") String did,
                                @PathParam("uid") String uid,  
                                Grant grant) {
      logger.info("Post /domains/"+did+"/users/"+uid+"/roles");
      Domain domain=(Domain)OStore.newStorable(Domain.class);
      User user=(User)OStore.newStorable(User.class);
      Role role=(Role)OStore.newStorable(Role.class);
      long longDid=0;
      long longUid=0;
      long longRid=0;

      if (grant.getDescription()==null)
         grant.setDescription("");

      // validate domain id
      try {
         longDid= Long.parseLong(did);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
    	  domain.setDomainid((int)longDid);
    	  domain = (Domain)domain.get();
         //domain = domainStore.getDomain(longDid);
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }
      grant.setDomainid((int)longDid);

      // validate user id
      try {
         longUid= Long.parseLong(uid);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid User id :" + uid);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
    	  user.setUserid((int)longUid);
         //user = userStore.getUser(longUid);
    	  user = (User)user.get();
      //}
      //catch(StoreException se) {
        // logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting user");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (user==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! User id :" + uid);
         return Response.status(404).entity(idmerror).build();
      }
      grant.setUserid((int)longUid);

      // validate role id
      try {
         longRid= grant.getRoleid();
         logger.info("roleid = " + longRid);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Role id :" + grant.getRoleid());
         return Response.status(404).entity(idmerror).build();
      }
      //try {
         //role = roleStore.getRole(longRid);
         role.setRoleid((int)longRid);
         role = (Role)role.get();
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting role");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (role==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! role :" + grant.getRoleid());
         return Response.status(404).entity(idmerror).build();
      }

      // see if grant already exists for this 
     // try {
    	  Grant existingGrant = (Grant)OStore.newStorable(Grant.class);
    	  existingGrant.setUserid((int)longUid);
    	  existingGrant.setRoleid((int)longRid);
    	  existingGrant.setDomainid((int)longDid);
    	  existingGrant = (Grant)existingGrant.get();
         //Grant existingGrant = grantStore.getGrant(longDid,longUid,longRid);
         if (existingGrant != null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Grant already exists for did:"+longDid+" uid:"+longUid+" rid:"+longRid);
            return Response.status(403).entity(idmerror).build();
         }
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error creating grant");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}


      // create grant 
      //try {
    	  grant = (Grant)grant.write();
         //grant = grantStore.createGrant(grant);
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error creating grant");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}

      IdmLightProxy.clearClaimCache();
      return Response.status(201).entity(grant).build();
   }


   @POST
   @Path("/{did}/users/roles")
   @Consumes("application/json")
   @Produces("application/json")
   public Response validateUser( @Context UriInfo info,
                                 @PathParam("did") String did,
                                 UserPwd userpwd) {

      logger.info("GET /domains/"+did+"/users");
      long longDid=0;
      Domain domain=(Domain)OStore.newStorable(Domain.class);
      Claim claim = new Claim();
      List<Role> roleList = new ArrayList<Role>();

      // validate domain id
      try {
         longDid= Long.parseLong(did);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
      	 domain.setDomainid((int)longDid);
      	 domain = (Domain)domain.get();
         //domain = domainStore.getDomain(longDid);
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Domain id :" + did);
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
      //try {
         Users users = new Users();//userStore.getUsers(username);
         List<User> userList = users.getUsers();
         if (userList.size()==0) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("did not find username: "+username);
            return Response.status(404).entity(idmerror).build();
         }
         User user = userList.get(0);
         String userPwd = MD5Calculator.getMD5(user.getPassword());
         String reqPwd = userpwd.getUserpwd();
         if (!userPwd.equals(reqPwd)) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("password does not match for username: "+username);
            return Response.status(401).entity(idmerror).build(); 
         }
         claim.setDomainid((int)longDid);
         claim.setUsername(username);
         claim.setUserid(user.getUserid());
         //try {
            Grants grants = new Grants((int)longDid, user.getUserid());//getGrants(longDid,user.getUserid());
            List<Grant> grantsList = grants.getGrants();
            for (int i=0; i < grantsList.size(); i++) {
               Grant grant = grantsList.get(i);
               Role role = (Role)OStore.newStorable(Role.class);
               role.setRoleid(grant.getRoleid());;
               role = (Role)role.get();
               //Role role = roleStore.getRole(grant.getRoleid());
               roleList.add(role);
            }
         //}
         //catch (StoreException se) {
            //logger.error("StoreException : " + se);
            //IDMError idmerror = new IDMError();
            //idmerror.setMessage("Internal error getting Roles");
            //idmerror.setDetails(se.message);
            //return Response.status(500).entity(idmerror).build();
         //}
         claim.setRoles(roleList); 
      //}
      //catch(StoreException se) {
        // logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting user");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}

      return Response.ok(claim).build();
   }  

   @GET
   @Path("/{did}/users/{uid}/roles")
   @Produces("application/json")
   public Response getRoles( @Context UriInfo info,
                             @PathParam("did") String did,
                             @PathParam("uid") String uid) {
      logger.info("GET /domains/"+did+"/users/"+uid+"/roles");
      long longDid=0;
      long longUid=0;
      Domain domain=(Domain)OStore.newStorable(Domain.class);
      User user=(User)OStore.newStorable(User.class);
      Roles roles = new Roles();
      List<Role> roleList = new ArrayList<Role>();

      // validate domain id
      try {
         longDid= Long.parseLong(did);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
         domain.setDomainid((int)longDid);
         domain = (Domain)domain.get();
         //domain = domainStore.getDomain(longDid);
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }

      // validate user id
      try {
         longUid=Long.parseLong(uid);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid User id :" + uid);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
    	  user.setUserid((int)longUid);
         //user = userStore.getUser(longUid);
    	  user = (User)user.get();
      //}
      //catch(StoreException se) {
        // logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting user");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (user==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! User id :" + uid);
         return Response.status(404).entity(idmerror).build();
      }

      //try {
    	 Grants grants = new Grants((int)longDid, user.getUserid());
         //Grants grants = grantStore.getGrants(longDid,longUid);
         List<Grant> grantsList = grants.getGrants();
         for (int i=0; i < grantsList.size(); i++) {
            Grant grant = grantsList.get(i);
            Role role = (Role)OStore.newStorable(Role.class);
            role.setRoleid(grant.getRoleid());;
            role = (Role)role.get();            
            //Role role = roleStore.getRole(grant.getRoleid());
            roleList.add(role);
         }
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting Roles");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}

      roles.setRoles(roleList);
      return Response.ok(roles).build();
   }

   @DELETE
   @Path("/{did}/users/{uid}/roles/{rid}")
   public Response deleteGrant( @Context UriInfo info,
                                @PathParam("did") String did,
                                @PathParam("uid") String uid,
                                @PathParam("rid") String rid) {
      long longDid=0;
      long longUid=0;
      long longRid=0;
      Domain domain=(Domain)OStore.newStorable(Domain.class);
      User user=(User)OStore.newStorable(User.class);
      Role role=(Role)OStore.newStorable(Role.class);

      // validate domain id
      try {
         longDid= Long.parseLong(did);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
         domain.setDomainid((int)longDid);
         domain = (Domain)domain.get();
         //domain = domainStore.getDomain(longDid);
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting domain");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (domain==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Domain id :" + did);
         return Response.status(404).entity(idmerror).build();
      }

      // validate user id
      try {
         longUid=Long.parseLong(uid);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid User id :" + uid);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
    	  user.setUserid((int)longUid);
         //user = userStore.getUser(longUid);
    	  user = (User)user.get();
      //}
      //catch(StoreException se) {
        // logger.error("StoreException : " + se);
        // IDMError idmerror = new IDMError();
        // idmerror.setMessage("Internal error getting user");
        // idmerror.setDetails(se.message);
        // return Response.status(500).entity(idmerror).build();
      //}
      if (user==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! User id :" + uid);
         return Response.status(404).entity(idmerror).build();
      }

      // validate role id
      try {
         longRid=Long.parseLong(rid);
      }
      catch (NumberFormatException nfe) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Invalid Role id :" + rid);
         return Response.status(404).entity(idmerror).build();
      }
      //try {
    	  role.setRoleid((int)longRid);
    	  role = (Role)role.get();
         //role = roleStore.getRole(longRid);
      //}
      //catch(StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error getting Role");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      if (role==null) {
         IDMError idmerror = new IDMError();
         idmerror.setMessage("Not found! Role id :" + rid);
         return Response.status(404).entity(idmerror).build();
      }
      
      // see if grant already exists 
      //try {
         //Grant existingGrant = grantStore.getGrant(longDid,longUid,longRid);
    	  Grant existingGrant = (Grant)OStore.newStorable(Grant.class);
    	  existingGrant.setUserid((int)longUid);
    	  existingGrant.setRoleid((int)longRid);
    	  existingGrant.setDomainid((int)longDid);
    	  existingGrant = (Grant)existingGrant.get();
         
         if (existingGrant == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Grant does not exist for did:"+longDid+" uid:"+longUid+" rid:"+longRid);
            return Response.status(404).entity(idmerror).build();
         }
         existingGrant = (Grant)existingGrant.delete();//grantStore.deleteGrant(existingGrant);
      //}
      //catch (StoreException se) {
         //logger.error("StoreException : " + se);
         //IDMError idmerror = new IDMError();
         //idmerror.setMessage("Internal error creating grant");
         //idmerror.setDetails(se.message);
         //return Response.status(500).entity(idmerror).build();
      //}
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }

}
