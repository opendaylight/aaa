/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest;

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
import org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST application used to manipulate the H2 database domains table. The REST
 * endpoint is <code>/auth/v1/domains</code>.
 *
 * A wrapper script called <code>idmtool</code> is provided to manipulate AAA data.
 *
 * @author peter.mellquist@hp.com
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
@Path("/v1/domains")
public class DomainHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DomainHandler.class);

    /**
     * Extracts all domains.
     *
     * @return a response with all domains stored in the H2 database
     */
    @GET
    @Produces("application/json")
    public Response getDomains() {
        LOG.info("Get /domains");
        Domains domains = null;
        try {
            domains = AAAIDMLightModule.getStore().getDomains();
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domains");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        return Response.ok(domains).build();
    }

    /**
     * Extracts the domain represented by <code>domainId</code>.
     *
     * @param domainId the string domain (i.e., "sdn")
     * @return a response with the specified domain
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getDomain(@PathParam("id") String domainId) {
        LOG.info("Get /domains/{}", domainId);
        Domain domain = null;
        try {
            domain = AAAIDMLightModule.getStore().readDomain(domainId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }

        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
        }
        return Response.ok(domain).build();
    }

    /**
     * Creates a domain.  The name attribute is required for domain creation.
     * Enabled and description fields are optional.  Optional fields default
     * in the following manner:
     * <code>enabled</code>: <code>false</code>
     * <code>description</code>: An empty string (<code>""</code>).
     *
     * @param info passed from Jersey
     * @param domain designated by the REST payload
     * @return A response stating success or failure of domain creation.
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createDomain(@Context UriInfo info, Domain domain) {
        LOG.info("Post /domains");
        try {
            if (domain.isEnabled() == null) {
                domain.setEnabled(false);
            }
            if (domain.getName() == null) {
                domain.setName("");
            }
            if (domain.getDescription() == null) {
                domain.setDescription("");
            }
            domain = AAAIDMLightModule.getStore().writeDomain(domain);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        return Response.status(201).entity(domain).build();
    }

    /**
     * Updates a domain.
     *
     * @param info passed from Jersey
     * @param domain the REST payload
     * @param domainId the last part of the path, containing the specified domain id
     * @return A response stating success or failure of domain update.
     */
    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putDomain(@Context UriInfo info, Domain domain, @PathParam("id") String domainId) {
        LOG.info("Put /domains/{}", domainId);
        try {
            domain.setDomainid(domainId);
            domain = AAAIDMLightModule.getStore().updateDomain(domain);
            if (domain == null) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Not found! Domain id :" + domainId);
                return Response.status(404).entity(idmerror).build();
            }
            IdmLightProxy.clearClaimCache();
            return Response.status(200).entity(domain).build();
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error putting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
    }

    /**
     * Deletes a domain.
     *
     * @param info passed from Jersey
     * @param domainId the last part of the path, containing the specified domain id
     * @return A response stating success or failure of domain deletion.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteDomain(@Context UriInfo info, @PathParam("id") String domainId) {
        LOG.info("Delete /domains/{}", domainId);

        try {
            Domain domain = AAAIDMLightModule.getStore().deleteDomain(domainId);
            if (domain == null) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Not found! Domain id :" + domainId);
                return Response.status(404).entity(idmerror).build();
            }
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error deleting Domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        IdmLightProxy.clearClaimCache();
        return Response.status(204).build();
    }

    /**
     * Creates a grant.  A grant defines the role a particular user is given on
     * a particular domain.  For example, by default, AAA installs a grant for
     * the "admin" user, granting permission to act with "admin" role on the
     * "sdn" domain.
     *
     * @param info passed from Jersey
     * @param domainId the domain the user is allowed to access
     * @param userId the user that is allowed to access the domain
     * @param grant the payload containing role access controls
     * @return A response stating success or failure of grant creation.
     */
    @POST
    @Path("/{did}/users/{uid}/roles")
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGrant(@Context UriInfo info, @PathParam("did") String domainId,
            @PathParam("uid") String userId, Grant grant) {
        LOG.info("Post /domains/{}/users/{}/roles", domainId, userId);
        Domain domain = null;
        User user = null;
        Role role = null;
        String roleId = null;

        // validate domain id
        try {
            domain = AAAIDMLightModule.getStore().readDomain(domainId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
        }
        grant.setDomainid(domainId);

        try {
            user = AAAIDMLightModule.getStore().readUser(userId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (user == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! User id :" + userId);
            return Response.status(404).entity(idmerror).build();
        }
        grant.setUserid(userId);

        // validate role id
        try {
            roleId = grant.getRoleid();
            LOG.info("roleid = {}", roleId);
        } catch (NumberFormatException nfe) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Invalid Role id :" + grant.getRoleid());
            return Response.status(404).entity(idmerror).build();
        }
        try {
            role = AAAIDMLightModule.getStore().readRole(roleId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting role");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (role == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! role :" + grant.getRoleid());
            return Response.status(404).entity(idmerror).build();
        }

        // see if grant already exists for this
        try {
            Grant existingGrant = AAAIDMLightModule.getStore().readGrant(domainId, userId, roleId);
            if (existingGrant != null) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Grant already exists for did:" + domainId + " uid:" + userId
                        + " rid:" + roleId);
                return Response.status(403).entity(idmerror).build();
            }
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating grant");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }

        // create grant
        try {
            grant = AAAIDMLightModule.getStore().writeGrant(grant);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating grant");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }

        IdmLightProxy.clearClaimCache();
        return Response.status(201).entity(grant).build();
    }

    /**
     * Used to validate user access.
     *
     * @param info passed from Jersey
     * @param domainId the domain in question
     * @param userpwd the password attempt
     * @return A response stating success or failure of user validation.
     */
    @POST
    @Path("/{did}/users/roles")
    @Consumes("application/json")
    @Produces("application/json")
    public Response validateUser(@Context UriInfo info, @PathParam("did") String domainId,
            UserPwd userpwd) {

        LOG.info("GET /domains/{}/users", domainId);
        Domain domain = null;
        Claim claim = new Claim();
        List<Role> roleList = new ArrayList<Role>();

        try {
            domain = AAAIDMLightModule.getStore().readDomain(domainId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
        }

        // check request body for username and pwd
        String username = userpwd.getUsername();
        if (username == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("username not specfied in request body");
            return Response.status(400).entity(idmerror).build();
        }
        String pwd = userpwd.getUserpwd();
        if (pwd == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("userpwd not specfied in request body");
            return Response.status(400).entity(idmerror).build();
        }

        // find userid for user
        try {
            Users users = AAAIDMLightModule.getStore().getUsers(username, domainId);
            List<User> userList = users.getUsers();
            if (userList.size() == 0) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("did not find username: " + username);
                return Response.status(404).entity(idmerror).build();
            }
            User user = userList.get(0);
            String userPwd = user.getPassword();
            String reqPwd = userpwd.getUserpwd();
            if (!userPwd.equals(reqPwd)) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("password does not match for username: " + username);
                return Response.status(401).entity(idmerror).build();
            }
            claim.setDomainid(domainId);
            claim.setUsername(username);
            claim.setUserid(user.getUserid());
            try {
                Grants grants = AAAIDMLightModule.getStore().getGrants(domainId, user.getUserid());
                List<Grant> grantsList = grants.getGrants();
                for (int i = 0; i < grantsList.size(); i++) {
                    Grant grant = grantsList.get(i);
                    Role role = AAAIDMLightModule.getStore().readRole(grant.getRoleid());
                    roleList.add(role);
                }
            } catch (IDMStoreException se) {
                LOG.error("StoreException: ", se);
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Internal error getting Roles");
                idmerror.setDetails(se.getMessage());
                return Response.status(500).entity(idmerror).build();
            }
            claim.setRoles(roleList);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }

        return Response.ok(claim).build();
    }

    /**
     * Get the grants for a user on a domain.
     *
     * @param info passed from Jersey
     * @param domainId the domain in question
     * @param userId the user in question
     * @return A response containing the grants for a user on a domain.
     */
    @GET
    @Path("/{did}/users/{uid}/roles")
    @Produces("application/json")
    public Response getRoles(@Context UriInfo info, @PathParam("did") String domainId,
            @PathParam("uid") String userId) {
        LOG.info("GET /domains/{}/users/{}/roles", domainId, userId);
        Domain domain = null;
        User user = null;
        Roles roles = new Roles();
        List<Role> roleList = new ArrayList<Role>();

        try {
            domain = AAAIDMLightModule.getStore().readDomain(domainId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
        }

        try {
            user = AAAIDMLightModule.getStore().readUser(userId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (user == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! User id :" + userId);
            return Response.status(404).entity(idmerror).build();
        }

        try {
            Grants grants = AAAIDMLightModule.getStore().getGrants(domainId, userId);
            List<Grant> grantsList = grants.getGrants();
            for (int i = 0; i < grantsList.size(); i++) {
                Grant grant = grantsList.get(i);
                Role role = AAAIDMLightModule.getStore().readRole(grant.getRoleid());
                roleList.add(role);
            }
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Roles");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }

        roles.setRoles(roleList);
        return Response.ok(roles).build();
    }

    /**
     * Delete a grant.
     *
     * @param info passed from Jersey
     * @param domainId the domain for the grant
     * @param userId the user for the grant
     * @param roleId the role for the grant
     * @return A response stating success or failure of the grant deletion.
     */
    @DELETE
    @Path("/{did}/users/{uid}/roles/{rid}")
    public Response deleteGrant(@Context UriInfo info, @PathParam("did") String domainId,
            @PathParam("uid") String userId, @PathParam("rid") String roleId) {
        Domain domain = null;
        User user = null;
        Role role = null;

        try {
            domain = AAAIDMLightModule.getStore().readDomain(domainId);
        } catch (IDMStoreException se) {
            LOG.error("Error deleting Grant  : ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id :" + domainId);
            return Response.status(404).entity(idmerror).build();
        }

        try {
            user = AAAIDMLightModule.getStore().readUser(userId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException : ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (user == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! User id :" + userId);
            return Response.status(404).entity(idmerror).build();
        }

        try {
            role = AAAIDMLightModule.getStore().readRole(roleId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Role");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        if (role == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Role id :" + roleId);
            return Response.status(404).entity(idmerror).build();
        }

        // see if grant already exists
        try {
            Grant existingGrant = AAAIDMLightModule.getStore().readGrant(domainId, userId, roleId);
            if (existingGrant == null) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Grant does not exist for did:" + domainId + " uid:" + userId
                        + " rid:" + roleId);
                return Response.status(404).entity(idmerror).build();
            }
            existingGrant = AAAIDMLightModule.getStore().deleteGrant(existingGrant.getGrantid());
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating grant");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        IdmLightProxy.clearClaimCache();
        return Response.status(204).build();
    }

}
