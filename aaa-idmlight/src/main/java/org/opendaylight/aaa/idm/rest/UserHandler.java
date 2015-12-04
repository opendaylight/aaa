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
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/users")
public class UserHandler {
   private static Logger logger = LoggerFactory.getLogger(UserHandler.class);
   protected final static String DEFAULT_PWD = "changeme";
   public static final String REDACTED_PASSWORD = "**********";

   @GET
   @Produces("application/json")
   public Response getUsers() {
      logger.info("get all users");
      Users users=null;
      try {
         users = AAAIDMLightModule.getStore().getUsers();
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error getting users",se.getMessage()).response();
      }

      // obsfucate pwd
      for (int z=0;z<users.getUsers().size();z++) {
          users.getUsers().get(z).setPassword(REDACTED_PASSWORD);
      }

      return Response.ok(users).build();
   }


   @GET
   @Path("/{id}")
   @Produces("application/json")
   public Response getUser(@PathParam("id") String id)  {
      logger.info("get /users/" + id);
      User user=null;
      try {
         user = AAAIDMLightModule.getStore().readUser(id);
      }
      catch(IDMStoreException se) {
         return new IDMError(500,"internal error getting user",se.getMessage()).response();
      }
      if (user==null) {
         return new IDMError(404,"user not found! id:" + id,"").response();
      }
      // obsfucate pwd
      user.setPassword(REDACTED_PASSWORD);
      user.setSalt(REDACTED_PASSWORD);
      return Response.ok(user).build();
   }

   @POST
   @Consumes("application/json")
   @Produces("application/json")
   public Response createUser(@Context UriInfo info,User user) {
      logger.info("post /users");
      try {
         // enabled by default
         if (user.isEnabled()==null) {
            user.setEnabled(true);
         }

         // user name is required
         if (user.getName()==null) {
            return new IDMError(400,"user name is required","").response();
         }
         else if (user.getName().length()> IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"user name max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // domain id/name is required
         if (user.getDomainid()==null) {
            return new IDMError(400,"user domain is required","").response();
         }
         else if (user.getDomainid().length()>IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"user domain max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // user description is optional
         if (user.getDescription()==null) {
            user.setDescription("");
         }
         else if (user.getDescription().length()>IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"user description max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // user email is optional
         if (user.getEmail()==null) {
            user.setEmail("");
         }
         else if (user.getEmail().length()>IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"user email max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // user password optional and will default if not provided
         if (user.getPassword()==null) {
            user.setPassword(DEFAULT_PWD);
         }
         else if (user.getPassword().length()>IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"user password max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // create user
         User createdUser = AAAIDMLightModule.getStore().writeUser(user);
         user.setUserid(createdUser.getUserid());
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error creating user",se.getMessage()).response();
      }

      // created!
      return Response.status(201).entity(user).build();
   }


   @PUT
   @Path("/{id}")
   @Consumes("application/json")
   @Produces("application/json")
   public Response putUser(@Context UriInfo info,User user,@PathParam("id") String id) {
      logger.info("put /users/" + id);

      try {
         user.setUserid(id);
         user = AAAIDMLightModule.getStore().updateUser(user);
         if (user==null) {
            return new IDMError(404,"user id not found id :"+id,"").response();
         }
         IdmLightProxy.clearClaimCache();
         user.setPassword(REDACTED_PASSWORD);
         return Response.status(200).entity(user).build();
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error putting user",se.getMessage()).response();
      }
   }

   @DELETE
   @Path("/{id}")
   public Response deleteUser(@Context UriInfo info,@PathParam("id") String id) {
      logger.info("delete /users/" + id);

      try {
         User user = AAAIDMLightModule.getStore().deleteUser(id);
         if (user==null) {
            return new IDMError(404,"user id not found id :"+id,"").response();
         }
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error deleting user",se.getMessage()).response();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }
}
