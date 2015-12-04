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
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/roles")
public class RoleHandler {
   private static Logger logger = LoggerFactory.getLogger(RoleHandler.class);

   @GET
   @Produces("application/json")
   public Response getRoles() {
      logger.info("get /roles");
      Roles roles=null;
      try {
         roles = AAAIDMLightModule.getStore().getRoles();
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error getting roles",se.getMessage()).response();
      }
      return Response.ok(roles).build();
   }

   @GET
   @Path("/{id}")
   @Produces("application/json")
   public Response getRole(@PathParam("id") String id)  {
      logger.info("get /roles/" + id);
      Role role=null;

      try {
         role = AAAIDMLightModule.getStore().readRole(id);
      }
      catch(IDMStoreException se) {
         return new IDMError(500,"internal error getting roles",se.getMessage()).response();
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
         else if (role.getName().length()> IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"role name max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // domain
         if (role.getDomainid()==null) {
            return new IDMError(404,"The role's domain must be defined on role when creating a role.","").response();
         }
         else if (role.getDomainid().length()>IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"role domain max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // description
         if (role.getDescription()==null) {
            role.setDescription("");
         }
         else if (role.getDescription().length()>IdmLightApplication.MAX_FIELD_LEN) {
            return new IDMError(400,"role description max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         role = AAAIDMLightModule.getStore().writeRole(role);
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error creating role",se.getMessage()).response();
      }

      return Response.status(201).entity(role).build();
   }

   @PUT
   @Path("/{id}")
   @Consumes("application/json")
   @Produces("application/json")
   public Response putRole(@Context UriInfo info,Role role,@PathParam("id") String id) {
      logger.info("put /roles/" + id);

      try {
         role.setRoleid(id);

         // name
         // TODO: names should be unique
         if ((role.getName()!=null) && (role.getName().length()>IdmLightApplication.MAX_FIELD_LEN)) {
            return new IDMError(400,"role name max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         // description
         if ((role.getDescription()!=null) && (role.getDescription().length()>IdmLightApplication.MAX_FIELD_LEN)) {
            return new IDMError(400,"role description max length is :" + IdmLightApplication.MAX_FIELD_LEN,"").response();
         }

         role = AAAIDMLightModule.getStore().updateRole(role);
         if (role==null) {
            return new IDMError(404,"role id not found :" + id,"").response();
         }
         IdmLightProxy.clearClaimCache();
         return Response.status(200).entity(role).build();
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error putting role",se.getMessage()).response();
      }
   }

   @DELETE
   @Path("/{id}")
   public Response deleteRole(@Context UriInfo info,@PathParam("id") String id) {
      logger.info("Delete /roles/" + id);

      try {
         Role role = AAAIDMLightModule.getStore().deleteRole(id);
         if (role==null) {
            return new IDMError(404,"role id not found :" + id,"").response();
         }
      }
      catch (IDMStoreException se) {
         return new IDMError(500,"internal error deleting role",se.getMessage()).response();
      }
      IdmLightProxy.clearClaimCache();
      return Response.status(204).build();
   }

}
