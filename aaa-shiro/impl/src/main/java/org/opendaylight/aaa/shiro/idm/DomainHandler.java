/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

import static java.util.Objects.requireNonNull;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Claim;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.UserPwd;
import org.opendaylight.aaa.api.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST application used to manipulate the H2 database domains table. The REST
 * endpoint is <code>/auth/v1/domains</code>.
 *
 * <p>
 * A wrapper script called <code>idmtool</code> is provided to manipulate AAA
 * data.
 *
 * @author peter.mellquist@hp.com
 */
@Path("/v1/domains")
public class DomainHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DomainHandler.class);

    private final IIDMStore iidMStore;
    private final ClaimCache claimCache;

    public DomainHandler(final IIDMStore iidMStore, final ClaimCache claimCache) {
        this.iidMStore = requireNonNull(iidMStore);
        this.claimCache = requireNonNull(claimCache);
    }

    /**
     * Extracts all domains.
     *
     * @return a response with all domains stored in the H2 database
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains() {
        LOG.info("Get /domains");
        final Domains domains;
        try {
            domains = iidMStore.getDomains();
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domains");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        return Response.ok(domains).build();
    }

    /**
     * Extracts the domain represented by <code>domainId</code>.
     *
     * @param domainId
     *            the string domain (i.e., "sdn")
     * @return a response with the specified domain
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomain(@PathParam("id") final String domainId) {
        LOG.info("Get /domains/{}", domainId);
        final Domain domain;
        try {
            domain = iidMStore.readDomain(domainId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }

        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! domain id :" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }
        return Response.ok(domain).build();
    }

    /**
     * Creates a domain. The name attribute is required for domain creation.
     * Enabled and description fields are optional. Optional fields default in
     * the following manner: <code>enabled</code>: <code>false</code>
     * <code>description</code>: An empty string (<code>""</code>).
     *
     * @param info
     *            passed from Jersey
     * @param domain
     *            designated by the REST payload
     * @return A response stating success or failure of domain creation.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDomain(@Context final UriInfo info, final Domain domain) {
        LOG.info("Post /domains");

        final Domain newDomain;
        try {
            // Bug 8382: domain id is an implementation detail and isn't
            // specifiable
            if (domain.getDomainid() != null) {
                final String errorMessage = "do not specify domainId, it will be assigned automatically for you";
                LOG.debug(errorMessage);
                final IDMError idmError = new IDMError();
                idmError.setMessage(errorMessage);
                return Response.status(Status.BAD_REQUEST).entity(idmError).build();
            }
            if (domain.isEnabled() == null) {
                domain.setEnabled(false);
            }
            if (domain.getName() == null) {
                domain.setName("");
            }
            if (domain.getDescription() == null) {
                domain.setDescription("");
            }
            newDomain = iidMStore.writeDomain(domain);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        return Response.status(Status.CREATED).entity(newDomain).build();
    }

    /**
     * Updates a domain.
     *
     * @param info
     *            passed from Jersey
     * @param domain
     *            the REST payload
     * @param domainId
     *            the last part of the path, containing the specified domain id
     * @return A response stating success or failure of domain update.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putDomain(@Context final UriInfo info, final Domain domain,
            @PathParam("id") final String domainId) {
        LOG.info("Put /domains/{}", domainId);

        domain.setDomainid(domainId);
        final Domain newDomain;
        try {
            newDomain = iidMStore.updateDomain(domain);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error putting domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }

        if (newDomain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id:" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }
        claimCache.clear();
        return Response.ok(newDomain).build();
    }

    /**
     * Deletes a domain.
     *
     * @param info
     *            passed from Jersey
     * @param domainId
     *            the last part of the path, containing the specified domain id
     * @return A response stating success or failure of domain deletion.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteDomain(@Context final UriInfo info, @PathParam("id") final String domainId) {
        LOG.info("Delete /domains/{}", domainId);

        final Domain domain;
        try {
            domain = iidMStore.deleteDomain(domainId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error deleting Domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }

        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id:" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }
        claimCache.clear();
        return Response.noContent().build();
    }

    /**
     * Creates a grant. A grant defines the role a particular user is given on a
     * particular domain. For example, by default, AAA installs a grant for the
     * "admin" user, granting permission to act with "admin" role on the "sdn"
     * domain.
     *
     * @param info
     *            passed from Jersey
     * @param domainId
     *            the domain the user is allowed to access
     * @param userId
     *            the user that is allowed to access the domain
     * @param grant
     *            the payload containing role access controls
     * @return A response stating success or failure of grant creation.
     */
    @POST
    @Path("/{did}/users/{uid}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGrant(@Context final UriInfo info, @PathParam("did") final String domainId,
            @PathParam("uid") final String userId, final Grant grant) {
        LOG.info("Post /domains/{}/users/{}/roles", domainId, userId);

        // Bug 8382: grant id is an implementation detail and isn't specifiable
        if (grant.getGrantid() != null) {
            final String errorMessage = "do not specify grantId, it will be assigned automatically for you";
            LOG.debug(errorMessage);
            final IDMError idmError = new IDMError();
            idmError.setMessage(errorMessage);
            return Response.status(Status.BAD_REQUEST).entity(idmError).build();
        }

        // validate domain id
        final Domain domain;
        try {
            domain = iidMStore.readDomain(domainId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! domain id :" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }
        grant.setDomainid(domainId);

        final User user;
        try {
            user = iidMStore.readUser(userId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (user == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! User id:" + userId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }
        grant.setUserid(userId);

        // validate role id
        final String roleId;
        try {
            roleId = grant.getRoleid();
        } catch (NumberFormatException nfe) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Invalid Role id:" + grant.getRoleid());
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }
        LOG.info("roleid = {}", roleId);

        final Role role;
        try {
            role = iidMStore.readRole(roleId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting role");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (role == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! role:" + grant.getRoleid());
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        // see if grant already exists for this
        final Grant existingGrant;
        try {
            existingGrant = iidMStore.readGrant(domainId, userId, roleId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating grant");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (existingGrant != null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Grant already exists for did:" + domainId + " uid:" + userId + " rid:" + roleId);
            return Response.status(Status.FORBIDDEN).entity(idmerror).build();
        }

        // create grant
        final Grant newGrant;
        try {
            newGrant = iidMStore.writeGrant(grant);
        } catch (IDMStoreException e) {
            LOG.error("StoreException: ", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating grant");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }

        claimCache.clear();
        return Response.status(Status.CREATED).entity(newGrant).build();
    }

    /**
     * Used to validate user access.
     *
     * @param info
     *            passed from Jersey
     * @param domainId
     *            the domain in question
     * @param userpwd
     *            the password attempt
     * @return A response stating success or failure of user validation.
     */
    @POST
    @Path("/{did}/users/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateUser(@Context final UriInfo info, @PathParam("did") final String domainId,
            final UserPwd userpwd) {
        LOG.info("GET /domains/{}/users", domainId);

        final Domain domain;
        try {
            domain = iidMStore.readDomain(domainId);
        } catch (IDMStoreException se) {
            LOG.error("StoreException: ", se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id:" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        // check request body for username and pwd
        String username = userpwd.getUsername();
        if (username == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("username not specfied in request body");
            return Response.status(Status.BAD_REQUEST).entity(idmerror).build();
        }
        String pwd = userpwd.getUserpwd();
        if (pwd == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("userpwd not specfied in request body");
            return Response.status(Status.BAD_REQUEST).entity(idmerror).build();
        }

        // find userid for user
        final Users users;
        try {
            users = iidMStore.getUsers(username, domainId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }

        final List<User> userList = users.getUsers();
        if (userList.isEmpty()) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("did not find username: " + username);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        User user = userList.get(0);
        String userPwd = user.getPassword();
        String reqPwd = userpwd.getUserpwd();
        if (!userPwd.equals(reqPwd)) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("password does not match for username: " + username);
            return Response.status(Status.UNAUTHORIZED).entity(idmerror).build();
        }

        List<Role> roleList = new ArrayList<>();
        try {
            for (Grant grant : iidMStore.getGrants(domainId, user.getUserid()).getGrants()) {
                roleList.add(iidMStore.readRole(grant.getRoleid()));
            }
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Roles");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }

        Claim claim = new Claim();
        claim.setDomainid(domainId);
        claim.setUsername(username);
        claim.setUserid(user.getUserid());
        claim.setRoles(roleList);

        return Response.ok(claim).build();
    }

    /**
     * Get the grants for a user on a domain.
     *
     * @param info
     *            passed from Jersey
     * @param domainId
     *            the domain in question
     * @param userId
     *            the user in question
     * @return A response containing the grants for a user on a domain.
     */
    @GET
    @Path("/{did}/users/{uid}/roles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoles(@Context final UriInfo info, @PathParam("did") final String domainId,
            @PathParam("uid") final String userId) {
        LOG.info("GET /domains/{}/users/{}/roles", domainId, userId);
        Domain domain = null;
        User user;
        List<Role> roleList = new ArrayList<>();

        try {
            domain = iidMStore.readDomain(domainId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id:" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        try {
            user = iidMStore.readUser(userId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (user == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! User id:" + userId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        try {
            for (Grant grant : iidMStore.getGrants(domainId, userId).getGrants()) {
                roleList.add(iidMStore.readRole(grant.getRoleid()));
            }
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Roles");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        Roles roles = new Roles();
        roles.setRoles(roleList);
        return Response.ok(roles).build();
    }

    /**
     * Delete a grant.
     *
     * @param info
     *            passed from Jersey
     * @param domainId
     *            the domain for the grant
     * @param userId
     *            the user for the grant
     * @param roleId
     *            the role for the grant
     * @return A response stating success or failure of the grant deletion.
     */
    @DELETE
    @Path("/{did}/users/{uid}/roles/{rid}")
    public Response deleteGrant(@Context final UriInfo info, @PathParam("did") final String domainId,
            @PathParam("uid") final String userId, @PathParam("rid") final String roleId) {
        final Domain domain;
        try {
            domain = iidMStore.readDomain(domainId);
        } catch (IDMStoreException e) {
            LOG.error("Error deleting Grant", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting domain");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (domain == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Domain id:" + domainId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        final User user;
        try {
            user = iidMStore.readUser(userId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting user");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (user == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! User id:" + userId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        final Role role;
        try {
            role = iidMStore.readRole(roleId);
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Role");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        if (role == null) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Not found! Role id:" + roleId);
            return Response.status(Status.NOT_FOUND).entity(idmerror).build();
        }

        // see if grant already exists
        try {
            Grant existingGrant = iidMStore.readGrant(domainId, userId, roleId);
            if (existingGrant == null) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Grant does not exist for did:" + domainId + " uid:" + userId + " rid:" + roleId);
                return Response.status(Status.NOT_FOUND).entity(idmerror).build();
            }
            iidMStore.deleteGrant(existingGrant.getGrantid());
        } catch (IDMStoreException e) {
            LOG.error("StoreException", e);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating grant");
            idmerror.setDetails(e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(idmerror).build();
        }
        claimCache.clear();
        return Response.noContent().build();
    }
}
