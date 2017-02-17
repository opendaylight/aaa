/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.opendaylight.aaa.idm.rest.datagetters.DomainDataGetter;
import org.opendaylight.aaa.idm.rest.datagetters.GrantDataGetter;
import org.opendaylight.aaa.idm.rest.datagetters.RoleDataGetter;
import org.opendaylight.aaa.idm.rest.datagetters.UserDataGetter;
import org.opendaylight.aaa.idm.rest.validators.DomainValidator;
import org.opendaylight.aaa.idm.rest.validators.RoleValidator;
import org.opendaylight.aaa.idm.rest.validators.UserValidator;
import org.opendaylight.aaa.idm.rest.validators.Validator;
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

    private final DomainDataGetter domainDataGetter = new DomainDataGetter();

    private final UserDataGetter userDataGetter = new UserDataGetter();

    private final RoleDataGetter roleDataGetter = new RoleDataGetter();

    private final GrantDataGetter grantDataGetter = new GrantDataGetter();

    /**
     * Extracts all domains.
     *
     * @return a response with all domains stored in the H2 database
     */
    @GET
    @Produces("application/json")
    public Response getDomains() {
        LOG.info("Get /domains");
        Domains domains;
        try {
            domains = AAAIDMLightModule.getStore().getDomains();
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
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
        Optional<Domain> domain = domainDataGetter.get(domainId);

        if (domain.isPresent()) {
            return Response.ok(domain.get()).build();
        }
        else {
            IDMError theError = domainDataGetter.getError()
                    .orElse(new IDMError(404,
                            HandlerConstants.DOMAIN_NOT_FOUND + domainId,
                            ""));
            return Response.status(theError.getCode()).entity(theError).build();
        }
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
    public Response createDomain(@Context UriInfo info, final Domain domain) {
        LOG.info("Post /domains");
        Domain writtenDomain;
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
            writtenDomain = AAAIDMLightModule.getStore().writeDomain(domain);
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error creating domain");
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }
        return Response.status(201).entity(writtenDomain).build();
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
        Domain writtenDomain;
        try {
            domain.setDomainid(domainId);
            writtenDomain = AAAIDMLightModule.getStore().updateDomain(domain);
            if (writtenDomain == null) {
                IDMError idmerror = new IDMError();
                idmerror.setMessage("Not found! Domain id :" + domainId);
                return Response.status(404).entity(idmerror).build();
            }
            IdmLightProxy.clearClaimCache();
            return Response.status(200).entity(domain).build();
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
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
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
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
        String roleId;

        Validator domainValidator = new DomainValidator(domainDataGetter);
        if (!domainValidator.validate(domainId)) {
            IDMError theError = domainValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }
        grant.setDomainid(domainId);

        Validator userValidator = new UserValidator(userDataGetter);
        if (!userValidator.validate(userId)) {
            IDMError theError = userValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }
        grant.setUserid(userId);

        // extract role id
        try {
            roleId = grant.getRoleid();
            LOG.info("roleid = {}", roleId);
        } catch (NumberFormatException nfe) {
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Invalid Role id :" + grant.getRoleid());
            return Response.status(404).entity(idmerror).build();
        }

        Validator roleValidator = new RoleValidator(roleDataGetter);
        if(!roleValidator.validate(roleId)) {
            IDMError theError = roleValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }

        // see if grant already exists for this
        Optional<Grant> existingGrant = grantDataGetter.get(domainId, userId, roleId);
        if (existingGrant.isPresent()) {
            IDMError idmerror = new IDMError(403,
                    String.format("Grant already exists for did: %s uid: %s rid: %s", domainId, userId, roleId),
                    "");
            return Response.status(idmerror.getCode()).entity(idmerror).build();
        }

        // create grant
        Grant writtenGrant;
        try {
            writtenGrant = AAAIDMLightModule.getStore().writeGrant(grant);
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage(HandlerConstants.INTERNAL_ERROR_GRANT);
            idmerror.setDetails(se.getMessage());
            return Response.status(500).entity(idmerror).build();
        }

        IdmLightProxy.clearClaimCache();
        return Response.status(201).entity(writtenGrant).build();
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
        Claim claim = new Claim();

        Validator domainValidator = new DomainValidator(domainDataGetter);
        if(!domainValidator.validate(domainId)) {
            IDMError theError = domainValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
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
            if (userList.isEmpty()) {
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
            claim.setRoles(getRoleListForUser(
                    user.getUserid(),
                    domainId));
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
            IDMError idmerror = new IDMError(500, "Internal error getting user", se.getMessage());
            return Response.status(idmerror.getCode()).entity(idmerror).build();
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
        Roles roles = new Roles();

        Validator domainValidator = new DomainValidator(domainDataGetter);
        if(!domainValidator.validate(domainId)) {
            IDMError theError = domainValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }

        Validator userValidator = new UserValidator(userDataGetter);
        if (!userValidator.validate(userId)) {
            IDMError theError = userValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }

        roles.setRoles(getRoleListForUser(userId, domainId));
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

        Validator domainValidator = new DomainValidator(domainDataGetter);
        if(!domainValidator.validate(domainId)) {
            IDMError theError = domainValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }

        Validator userValidator = new UserValidator(userDataGetter);
        if (!userValidator.validate(userId)) {
            IDMError theError = userValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }

        Validator roleValidator = new RoleValidator(roleDataGetter);
        if(!roleValidator.validate(roleId)) {
            IDMError theError = roleValidator.getError();
            return Response.status(theError.getCode()).entity(theError).build();
        }

        Optional<Grant> existingGrant = grantDataGetter.get(domainId, userId, roleId);
        if (!existingGrant.isPresent()) {
            IDMError idmerror = new IDMError(404,
                    String.format("Grant does not exist for did: %s uid: %s rid: %s", domainId, userId, roleId),
                    "");
            return Response.status(idmerror.getCode()).entity(idmerror).build();
        }
        else {
            try {
                AAAIDMLightModule.getStore().deleteGrant(existingGrant.get().getGrantid());
            } catch(IDMStoreException e) {
                LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, e);
                IDMError idmerror = new IDMError(500, "Internal error creating grant", e.getMessage());
                return Response.status(500).entity(idmerror).build();
            }
        }

        IdmLightProxy.clearClaimCache();
        return Response.status(204).build();
    }

    private List<Role> getRoleListForUser(String userId, String domainId) {
        List<Role> theRoles = new ArrayList<>();
        try {
            Grants grants = AAAIDMLightModule.getStore().getGrants(domainId, userId);
            List<Grant> grantsList = grants.getGrants();
            for (Grant grant : grantsList) {
                Role role = AAAIDMLightModule.getStore().readRole(grant.getRoleid());
                theRoles.add(role);
            }
        } catch (IDMStoreException se) {
            LOG.error(HandlerConstants.STORE_EXCEPTION_LITERAL, se);
            IDMError idmerror = new IDMError();
            idmerror.setMessage("Internal error getting Roles");
            idmerror.setDetails(se.getMessage());
            return Collections.emptyList();
        }
        return theRoles;
    }
}
