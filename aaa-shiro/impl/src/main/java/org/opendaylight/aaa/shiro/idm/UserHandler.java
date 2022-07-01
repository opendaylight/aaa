/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

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
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST application used to manipulate the H2 database users table. The REST endpoint is <code>/auth/v1/users</code>.
 *
 * <p>
 * A wrapper script called <code>idmtool</code> is provided to manipulate AAA data.
 *
 * @author peter.mellquist@hp.com
 */
@Path("/v1/users")
public class UserHandler {
    private static final Logger LOG = LoggerFactory.getLogger(UserHandler.class);

    private final IIDMStore iidMStore;
    private final ClaimCache claimCache;

    public UserHandler(final IIDMStore iidMStore, final ClaimCache claimCache) {
        this.iidMStore = iidMStore;
        this.claimCache = claimCache;
    }

    /**
     * Extracts all users. The password and salt fields are redacted for
     * security reasons.
     *
     * @return A response containing the users, or internal error if one occurs
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        LOG.info("GET /auth/v1/users  (extracts all users)");

        final Users users;
        try {
            users = iidMStore.getUsers();
        } catch (IDMStoreException se) {
            return internalError("getting", se);
        }

        // Redact the password and salt for security purposes.
        users.getUsers().forEach(UserHandler::redactUserPasswordInfo);

        return Response.ok(users).build();
    }

    /**
     * Extracts the user represented by <code>id</code>. The password and salt
     * fields are redacted for security reasons.
     *
     * @param id
     *            the unique id of representing the user account
     * @return A response with the user information, or internal error if one
     *         occurs
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") final String id) {
        LOG.info("GET auth/v1/users/ {}  (extract user with specified id)", id);

        final User user;
        try {
            user = iidMStore.readUser(id);
        } catch (IDMStoreException se) {
            return internalError("getting", se);
        }

        if (user == null) {
            final String error = "user not found! id: " + id;
            return new IDMError(404, error, "").response();
        }

        // Redact the password and salt for security purposes.
        redactUserPasswordInfo(user);

        return Response.ok(user).build();
    }

    /**
     * REST endpoint to create a user. Name and domain are required attributes,
     * and all other fields (description, email, password, enabled) are
     * optional. Optional fields default in the following manner:
     * <code>description</code>: An empty string (<code>""</code>).
     * <code>email</code>: An empty string (<code>""</code>).
     * <code>password</code>: <code>changeme</code> <code>enabled</code>:
     * <code>true</code>
     *
     * <p>
     * If a password is not provided, please ensure you change the default
     * password ASAP for security reasons!
     *
     * @param info
     *            passed from Jersey
     * @param user
     *            the user defined in the JSON payload
     * @return A response stating success or failure of user creation
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@Context final UriInfo info, final User user) {
        LOG.info("POST /auth/v1/users  (create a user with the specified payload");

        // Bug 8382: user id is an implementation detail and isn't specifiable
        if (user.getUserid() != null) {
            final String errorMessage = "do not specify userId, it will be assigned automatically for you";
            LOG.debug(errorMessage);
            final IDMError idmError = new IDMError();
            idmError.setMessage(errorMessage);
            return Response.status(Status.BAD_REQUEST).entity(idmError).build();
        }

        // The "enabled" field is optional, and defaults to true.
        if (user.isEnabled() == null) {
            user.setEnabled(true);
        }

        // The "name" field is required.
        final String userName = user.getName();
        if (userName == null) {
            return missingRequiredField("name");
        }
        // The "name" field has a maximum length.
        if (userName.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("name", IdmLightApplication.MAX_FIELD_LEN);
        }

        // The "domain field is required.
        final String domainId = user.getDomainid();
        if (domainId == null) {
            return missingRequiredField("domain");
        }
        // The "domain" field has a maximum length.
        if (domainId.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("domain", IdmLightApplication.MAX_FIELD_LEN);
        }

        // The "description" field is optional and defaults to "".
        String userDescription = user.getDescription();
        if (userDescription == null) {
            user.setDescription("");
        } else  if (userDescription.length() > IdmLightApplication.MAX_FIELD_LEN) {
            // The "description" field has a maximum length.
            return providedFieldTooLong("description", IdmLightApplication.MAX_FIELD_LEN);
        }

        // The "email" field is optional and defaults to "".
        final String userEmail = user.getEmail();
        if (userEmail == null) {
            user.setEmail("");
        } else if (userEmail.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("email", IdmLightApplication.MAX_FIELD_LEN);
        }
        // TODO add a check on email format here.

        // The "password" field is optional and defaults to "changeme".
        final String userPassword = user.getPassword();
        if (userPassword == null) {
            user.setPassword("changeme");
        } else if (userPassword.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("password", IdmLightApplication.MAX_FIELD_LEN);
        }

        final User createdUser;
        try {
            // At this point, fields have been properly verified. Create the user account
            createdUser = iidMStore.writeUser(user);
        } catch (IDMStoreException se) {
            return internalError("creating", se);
        }

        user.setUserid(createdUser.getUserid());

        // Redact the password and salt for security reasons.
        redactUserPasswordInfo(user);
        // FIXME: report back to the client a warning message to change the default password if none was specified.
        return Response.status(Status.CREATED).entity(user).build();
    }

    /**
     * REST endpoint to update a user account.
     *
     * @param info
     *            passed from Jersey
     * @param user
     *            the user defined in the JSON payload
     * @param id
     *            the unique id for the user that will be updated
     * @return A response stating success or failure of the user update
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response putUser(@Context final UriInfo info, final User user, @PathParam("id") final String id) {
        LOG.info("PUT /auth/v1/users/{}  (Updates a user account)", id);

        user.setUserid(id);

        if (checkInputFieldLength(user.getPassword())) {
            return providedFieldTooLong("password", IdmLightApplication.MAX_FIELD_LEN);
        }

        if (checkInputFieldLength(user.getName())) {
            return providedFieldTooLong("name", IdmLightApplication.MAX_FIELD_LEN);
        }

        if (checkInputFieldLength(user.getDescription())) {
            return providedFieldTooLong("description", IdmLightApplication.MAX_FIELD_LEN);
        }

        if (checkInputFieldLength(user.getEmail())) {
            return providedFieldTooLong("email", IdmLightApplication.MAX_FIELD_LEN);
        }

        if (checkInputFieldLength(user.getDomainid())) {
            return providedFieldTooLong("domain", IdmLightApplication.MAX_FIELD_LEN);
        }

        final User newUser;
        try {
            newUser = iidMStore.updateUser(user);
        } catch (IDMStoreException se) {
            return internalError("updating", se);
        }

        if (newUser == null) {
            return new IDMError(404, String.format("User not found for id %s", id), "").response();
        }

        claimCache.clear();

        // Redact the password and salt for security reasons.
        redactUserPasswordInfo(newUser);
        return Response.ok(newUser).build();
    }

    /**
     * REST endpoint to delete a user account.
     *
     * @param info
     *            passed from Jersey
     * @param id
     *            the unique id of the user which is being deleted
     * @return A response stating success or failure of user deletion
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@Context final UriInfo info, @PathParam("id") final String id) {
        LOG.info("DELETE /auth/v1/users/{}  (Delete a user account)", id);

        final User user;
        try {
            user = iidMStore.deleteUser(id);
        } catch (IDMStoreException se) {
            return internalError("deleting", se);
        }

        if (user == null) {
            return new IDMError(404, String.format("Error deleting user.  " + "Couldn't find user with id %s", id), "")
                .response();
        }

        // Successfully deleted the user; report success to the client.
        claimCache.clear();
        return Response.noContent().build();
    }

    /**
     * Creates a <code>Response</code> related to an internal server error.
     *
     * @param verbal
     *            such as "creating", "deleting", "updating"
     * @param ex
     *            The exception, which is logged locally
     * @return A response containing internal error with specific reasoning
     */
    private static Response internalError(final String verbal, final Exception ex) {
        LOG.error("There was an internal error {} the user", verbal, ex);
        return new IDMError(500, String.format("There was an internal error %s the user", verbal)).response();
    }

    /**
     * Creates a <code>Response</code> related to the user not providing a
     * required field.
     *
     * @param fieldName
     *            the name of the field which is missing
     * @return A response explaining that the request is missing a field
     */
    private static Response missingRequiredField(final String fieldName) {
        return new IDMError(400, String.format(
            "%s is required to create the user account.  Please provide a %s in your payload.", fieldName, fieldName),
            "").response();
    }

    /**
     * Creates a <code>Response</code> related to the user providing a field
     * that is too long.
     *
     * @param fieldName
     *            the name of the field that is too long
     * @param maxFieldLength
     *            the maximum length of <code>fieldName</code>
     * @return A response containing the bad field and the maximum field length
     */
    private static Response providedFieldTooLong(final String fieldName, final int maxFieldLength) {
        return new IDMError(400, getProvidedFieldTooLongMessage(fieldName, maxFieldLength), "").response();
    }

    /**
     * Creates the client-facing message related to the user providing a field
     * that is too long.
     *
     * @param fieldName
     *            the name of the field that is too long
     * @param maxFieldLength
     *            the maximum length of <code>fieldName</code>
     * @return a response containing the too long field and its length
     */
    private static String getProvidedFieldTooLongMessage(final String fieldName, final int maxFieldLength) {
        return String.format("The provided %s field is too long.  The max length is %s.", fieldName, maxFieldLength);
    }

    /**
     * Prepares a user account for output by redacting the appropriate fields.
     * This method side-effects the <code>user</code> parameter.
     *
     * @param user the user account which will have fields redacted
     */
    private static void redactUserPasswordInfo(final User user) {
        user.setPassword("**********");
        user.setSalt("**********");
    }

    /**
     * Validate the input field length.
     *
     * @param inputField
     *            the field to check
     * @return true if input field bigger than the MAX_FIELD_LEN
     */
    private static boolean checkInputFieldLength(final String inputField) {
        return inputField != null && inputField.length() > IdmLightApplication.MAX_FIELD_LEN;
    }
}
