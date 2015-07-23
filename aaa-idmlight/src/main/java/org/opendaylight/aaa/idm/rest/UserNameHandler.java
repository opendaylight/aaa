/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.aaa.idm.rest;

/**
 *
 * @author saichler@cisco.com
 *
 */

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.aaa.idm.model.IDMError;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.Users;
import org.opendaylight.aaa.idm.persistence.StoreException;
import org.opendaylight.aaa.idm.persistence.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/user/name")
public class UserNameHandler {
   private static Logger logger = LoggerFactory.getLogger(UserNameHandler.class);
   private static UserStore userStore = new UserStore();
   protected final static String DEFAULT_PWD = "changeme";
   public static final String REDACTED_PASSWORD = "**********";

   @GET
   @Path("/{name}")
   @Produces("application/json")
   public Response getUser(@PathParam("name") String name)  {
      logger.info("get /user/name/" + name);
      Users users=null;
      try {
          users = userStore.getUsers(name);
      } catch (StoreException e) {
          return new IDMError(500,"internal error getting a user by name",e.message).response();
      }
      if(users.getUsers()==null || users.getUsers().size()==0)
          return new IDMError(400,"invalid user name :" + name,"").response();
      
      User user=users.getUsers().get(0);
      // obsfucate pwd
      user.setPassword(REDACTED_PASSWORD);
      return Response.ok(user).build();
   }

   @DELETE
   @Path("/{name}")
   public Response deleteUser(@Context UriInfo info,@PathParam("name") String name) {
      logger.info("delete /user/name/" + name);
      Users users=null;
      try {
          users = userStore.getUsers(name);
      } catch (StoreException e) {
          return new IDMError(500,"internal error deleting a user by name",e.message).response();
      }

      if(users.getUsers()==null || users.getUsers().size()==0)
          return new IDMError(400,"invalid user name :" + name,"").response();

      try {
         User user = new User();
         user.setUserid(users.getUsers().get(0).getUserid());
         user = userStore.deleteUser(user);
         if (user==null) {
            return new IDMError(404,"user id not found id :"+users.getUsers().get(0).getUserid(),"").response();
         }
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error deleting user",se.message).response();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }
}
