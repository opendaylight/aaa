/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

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
import org.opendaylight.aaa.idm.AAAIDMLightModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST application used to manipulate the H2 database roles table. The REST
 * endpoint is <code>/auth/v1/roles</code>.
 *
 * <p>
 * A wrapper script called <code>idmtool</code> is provided to manipulate AAA
 * data.
 *
 * @author peter.mellquist@hp.com
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
@Path("/v1/roles")
public class RoleHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RoleHandler.class);

    /**
     * Extracts all roles.
     *
     * @return A response with all roles in the H2 database, or internal error
     *         if one is encountered
     */
    @GET
    @Produces("application/json")
    public Response getRoles() {
        LOG.info("get /roles");
        Roles roles = null;
        try {
            roles = AAAIDMLightModule.getStore().getRoles();
        } catch (IDMStoreException e) {
            LOG.error("Internal error getting the roles", e);
            return new IDMError(500, "internal error getting roles", e.getMessage()).response();
        }
        return Response.ok(roles).build();
    }

    /**
     * Extract a specific role identified by <code>id</code>.
     *
     * @param id
     *            the String id for the role
     * @return A response with the role identified by <code>id</code>, or
     *         internal error if one is encountered
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getRole(@PathParam("id") String id) {
        LOG.info("get /roles/{}", id);
        Role role = null;

        try {
            role = AAAIDMLightModule.getStore().readRole(id);
        } catch (IDMStoreException e) {
            LOG.error("Internal error getting the role", e);
            return new IDMError(500, "internal error getting roles", e.getMessage()).response();
        }

        if (role == null) {
            return new IDMError(404, "role not found id:" + id, "").response();
        }
        return Response.ok(role).build();
    }

    /**
     * Creates a role.
     *
     * @param info
     *            passed from Jersey
     * @param role
     *            the role JSON payload
     * @return A response stating success or failure of role creation, or
     *         internal error if one is encountered
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createRole(@Context UriInfo info, Role role) {
        LOG.info("Post /roles");
        try {

            // Bug 8382: role id is an implementation detail and isn't
            // specifiable
            if (role.getRoleid() != null) {
                final String errorMessage = "do not specify roleId, it will be assigned automatically for you";
                LOG.debug(errorMessage);
                final IDMError idmError = new IDMError();
                idmError.setMessage(errorMessage);
                return Response.status(400).entity(idmError).build();
            }

            // TODO: role names should be unique!
            // name
            if (role.getName() == null) {
                return new IDMError(404, "name must be defined on role create", "").response();
            } else if (role.getName().length() > IdmLightApplication.MAX_FIELD_LEN) {
                return new IDMError(400, "role name max length is :" + IdmLightApplication.MAX_FIELD_LEN, "")
                        .response();
            }

            // domain
            if (role.getDomainid() == null) {
                return new IDMError(404, "The role's domain must be defined on role when creating a role.", "")
                        .response();
            } else if (role.getDomainid().length() > IdmLightApplication.MAX_FIELD_LEN) {
                return new IDMError(400, "role domain max length is :" + IdmLightApplication.MAX_FIELD_LEN, "")
                        .response();
            }

            // description
            if (role.getDescription() == null) {
                role.setDescription("");
            } else if (role.getDescription().length() > IdmLightApplication.MAX_FIELD_LEN) {
                return new IDMError(400, "role description max length is :" + IdmLightApplication.MAX_FIELD_LEN, "")
                        .response();
            }

            role = AAAIDMLightModule.getStore().writeRole(role);
        } catch (IDMStoreException e) {
            LOG.error("Internal error creating role", e);
            return new IDMError(500, "internal error creating role", e.getMessage()).response();
        }
        return Response.status(201).entity(role).build();
    }

    /**
     * Updates a specific role identified by <code>id</code>.
     *
     * @param info
     *            passed from Jersey
     * @param role
     *            the role JSON payload
     * @param id
     *            the String id for the role
     * @return A response stating success or failure of role update, or internal
     *         error if one occurs
     */
    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putRole(@Context UriInfo info, Role role, @PathParam("id") String id) {
        LOG.info("put /roles/{}", id);

        try {
            role.setRoleid(id);

            // name
            // TODO: names should be unique
            if ((role.getName() != null) && (role.getName().length() > IdmLightApplication.MAX_FIELD_LEN)) {
                return new IDMError(400, "role name max length is :" + IdmLightApplication.MAX_FIELD_LEN, "")
                        .response();
            }

            // description
            if ((role.getDescription() != null)
                    && (role.getDescription().length() > IdmLightApplication.MAX_FIELD_LEN)) {
                return new IDMError(400, "role description max length is :" + IdmLightApplication.MAX_FIELD_LEN, "")
                        .response();
            }

            role = AAAIDMLightModule.getStore().updateRole(role);
            if (role == null) {
                return new IDMError(404, "role id not found :" + id, "").response();
            }
            IdmLightProxy.clearClaimCache();
            return Response.status(200).entity(role).build();
        } catch (IDMStoreException e) {
            LOG.error("Internal error putting role", e);
            return new IDMError(500, "internal error putting role", e.getMessage()).response();
        }
    }

    /**
     * Delete a role.
     *
     * @param info
     *            passed from Jersey
     * @param id
     *            the String id for the role
     * @return A response stating success or failure of user deletion, or
     *         internal error if one occurs
     */
    @DELETE
    @Path("/{id}")
    public Response deleteRole(@Context UriInfo info, @PathParam("id") String id) {
        LOG.info("Delete /roles/{}", id);

        try {
            Role role = AAAIDMLightModule.getStore().deleteRole(id);
            if (role == null) {
                return new IDMError(404, "role id not found :" + id, "").response();
            }
        } catch (IDMStoreException e) {
            LOG.error("Internal error deleting role", e);
            return new IDMError(500, "internal error deleting role", e.getMessage()).response();
        }
        IdmLightProxy.clearClaimCache();
        return Response.status(204).build();
    }
}
