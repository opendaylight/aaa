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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import org.opendaylight.aaa.idm.model.Users;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.IDMError;
import org.opendaylight.aaa.idm.persistence.UserStore;
import org.opendaylight.aaa.idm.persistence.StoreException;
import org.opendaylight.aaa.idm.IdmLightProxy;

@Path("/v1/users")
public class UserHandler {
   private static Logger logger = LoggerFactory.getLogger(UserHandler.class);
   private static UserStore userStore = new UserStore();
   protected final static String DEFAULT_PWD = "changeme";
   public static final String REDACTED_PASSWORD = "**********";

   @GET
   @Produces("application/json")
   public Response getUsers() {
      logger.info("get all users");
      Users users=null;
      try {
         users = userStore.getUsers();
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error getting users",se.message).response();
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
      long longId=0;
      try {
         longId=Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         return new IDMError(400,"invalid user id :" + id,"").response();
      }
      try {
         user = userStore.getUser(longId);
      }
      catch(StoreException se) {
         return new IDMError(500,"internal error getting user",se.message).response();
      }
      if (user==null) {
         return new IDMError(404,"user not found! id:" + id,"").response();
      }
      // obsfucate pwd
      user.setPassword(REDACTED_PASSWORD);
      return Response.ok(user).build();
   }

   @POST
   @Consumes("application/json")
   @Produces("application/json")
   public Response createUser(@Context UriInfo info,User user) {
      logger.info("post /users");
      try {
         // enabled by default
         if (user.getEnabled()==null) {
            user.setEnabled(true);
         }

         // user name is required
         if (user.getName()==null) {
            return new IDMError(400,"user name is required","").response();
         }
         else if (user.getName().length()>UserStore.MAX_FIELD_LEN) {
            return new IDMError(400,"user name max length is :" + UserStore.MAX_FIELD_LEN,"").response();
         }

         // user description is optional
         if (user.getDescription()==null) {
            user.setDescription("");
         }
         else if (user.getDescription().length()>UserStore.MAX_FIELD_LEN) {
            return new IDMError(400,"user description max length is :" + UserStore.MAX_FIELD_LEN,"").response();
         }

         // user email is optional
         if (user.getEmail()==null) {
            user.setEmail("");
         }
         else if (user.getEmail().length()>UserStore.MAX_FIELD_LEN) {
            return new IDMError(400,"user email max length is :" + UserStore.MAX_FIELD_LEN,"").response();
         }

         // user password optional and will default if not provided
         if (user.getPassword()==null) {
            user.setPassword(DEFAULT_PWD);
         }
         else if (user.getPassword().length()>UserStore.MAX_FIELD_LEN) {
            return new IDMError(400,"user password max length is :" + UserStore.MAX_FIELD_LEN,"").response();
         }

         // create user
         user = userStore.createUser(user);
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error creating user",se.message).response();
      }

      // created!
      return Response.status(201).entity(user).build();
   }


   @PUT
   @Path("/{id}")
   @Consumes("application/json")
   @Produces("application/json")
   public Response putUser(@Context UriInfo info,User user,@PathParam("id") String id) {
      long longId=0;
      logger.info("put /users/" + id);
       try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         return new IDMError(400,"invalid user id:"+id,"").response();
      }

      try {
         user.setUserid((int)longId);
         user = userStore.putUser(user);
         if (user==null) {
            return new IDMError(404,"user id not found id :"+id,"").response();
         }
         IdmLightProxy.clearClaimCache();
         user.setPassword(REDACTED_PASSWORD);
         return Response.status(200).entity(user).build();
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error putting user",se.message).response();
      }
   }

   @DELETE
   @Path("/{id}")
   public Response deleteUser(@Context UriInfo info,@PathParam("id") String id) {
      long longId=0;
      logger.info("delete /users/" + id);
       try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         return new IDMError(400,"invalid user id:"+id,"").response();
      }

      try {
         User user = new User();
         user.setUserid((int)longId);
         user = userStore.deleteUser(user);
         if (user==null) {
            return new IDMError(404,"user id not found id :"+id,"").response();
         }
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error deleting user",se.message).response();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }


}
