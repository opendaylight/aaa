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
import org.opendaylight.aaa.idm.model.Roles;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.IDMError;
import org.opendaylight.aaa.idm.persistence.RoleStore;
import org.opendaylight.aaa.idm.persistence.StoreException;
import org.opendaylight.aaa.idm.IdmLightProxy;

@Path("/v1/roles")
public class RoleHandler {
   private static Logger logger = LoggerFactory.getLogger(RoleHandler.class);
   private static RoleStore roleStore = new RoleStore();

   @GET
   @Produces("application/json")
   public Response getRoles() {
      logger.info("get /roles");
      Roles roles=null;
      try {
         roles = roleStore.getRoles();
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error getting roles",se.message).response();
      }
      return Response.ok(roles).build();
   }

   @GET
   @Path("/{id}")
   @Produces("application/json")
   public Response getRole(@PathParam("id") String id)  {
      logger.info("get /roles/" + id);
      Role role=null;
      long longId=0;
      try {
         longId=Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         return new IDMError(404,"invalid role id :" + id,"").response();
      }

      try {
         role = roleStore.getRole(longId);
      }
      catch(StoreException se) {
         return new IDMError(500,"internal error getting roles",se.message).response();
      }

      if (role==null) {
         return new IDMError(404,"role not found id :" + id,"").response();
      }
      return Response.ok(role).build();
   }

   @POST
   @Consumes("application/json")
   @Produces("application/json")
   public Response createRole(@Context UriInfo info,Role role) {
      logger.info("Post /roles");
      try {
         // TODO: role names should be unique!
         // name
         if (role.getName()==null) {
            return new IDMError(404,"name must be defined on role create","").response();
         }
         else if (role.getName().length()>RoleStore.MAX_FIELD_LEN) {
            return new IDMError(400,"role name max length is :" + RoleStore.MAX_FIELD_LEN,"").response();
         }

         // description
         if (role.getDescription()==null) {
            role.setDescription("");
         }
         else if (role.getDescription().length()>RoleStore.MAX_FIELD_LEN) {
            return new IDMError(400,"role description max length is :" + RoleStore.MAX_FIELD_LEN,"").response();
         }

         role = roleStore.createRole(role);
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error creating role",se.message).response();
      }

      return Response.status(201).entity(role).build();
   }

   @PUT
   @Path("/{id}")
   @Consumes("application/json")
   @Produces("application/json")
   public Response putRole(@Context UriInfo info,Role role,@PathParam("id") String id) {
      long longId=0;
      logger.info("put /roles/" + id);
       try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         return new IDMError(404,"invalid role id :" + id,"").response();
      }

      try {
         role.setRoleid((int)longId);

         // name
         // TODO: names should be unique
         if ((role.getName()!=null) && (role.getName().length()>RoleStore.MAX_FIELD_LEN)) {
            return new IDMError(400,"role name max length is :" + RoleStore.MAX_FIELD_LEN,"").response();
         }

         // description
         if ((role.getDescription()!=null) && (role.getDescription().length()>RoleStore.MAX_FIELD_LEN)) {
            return new IDMError(400,"role description max length is :" + RoleStore.MAX_FIELD_LEN,"").response();
         }

         role = roleStore.putRole(role);
         if (role==null) {
            return new IDMError(404,"role id not found :" + id,"").response();
         }
         IdmLightProxy.clearClaimCache();
         return Response.status(200).entity(role).build();
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error putting role",se.message).response();
      }
   }

   @DELETE
   @Path("/{id}")
   public Response deleteRole(@Context UriInfo info,@PathParam("id") String id) {
      long longId=0;
      logger.info("Delete /roles/" + id);
       try {
         longId= Long.parseLong(id);
      }
      catch (NumberFormatException nfe) {
         return new IDMError(404,"invalid role id :" + id,"").response();
      }

      try {
         Role role = new Role();
         role.setRoleid((int)longId);
         role = roleStore.deleteRole(role);
         if (role==null) {
            return new IDMError(404,"role id not found :" + id,"").response();
         }
      }
      catch (StoreException se) {
         return new IDMError(500,"internal error deleting role",se.message).response();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }

}
