/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest;

import java.util.Collection;

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

/**
 * REST application used to manipulate the H2 database users table. The REST
 * endpoint is <code>/auth/v1/users</code>.
 *
 * A wrapper script called <code>idmtool</code> is provided to manipulate AAA data.
 *
 * @author peter.mellquist@hp.com
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
@Path("/v1/users")
public class UserHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserHandler.class);

    /**
     * If a user is created through the <code>/auth/v1/users</code> rest
     * endpoint without a password, the default password is assigned to the
     * user.
     */
    private final static String DEFAULT_PWD = "changeme";

    /**
     * When an HTTP GET is performed on <code>/auth/v1/users</code>, the
     * password field is replaced with <code>REDACTED_PASSWORD</code> for
     * security reasons.
     */
    private static final String REDACTED_PASSWORD = "**********";

    /**
     * When an HTTP GET is performed on <code>/auth/v1/users</code>, the salt
     * field is replaced with <code>REDACTED_SALT</code> for security reasons.
     */
    private static final String REDACTED_SALT = "**********";

    /**
     * When creating a user, the description is optional and defaults to an
     * empty string.
     */
    private static final String DEFAULT_DESCRIPTION = "";

    /**
     * When creating a user, the email is optional and defaults to an empty
     * string.
     */
    private static final String DEFAULT_EMAIL = "";

    /**
     * Extracts all users. The password and salt fields are redacted for
     * security reasons.
     *
     * @return A response containing the users, or internal error if one occurs
     */
    @GET
    @Produces("application/json")
    public Response getUsers() {
        LOG.info("GET /auth/v1/users  (extracts all users)");

        try {
            final Users users = AAAIDMLightModule.getStore().getUsers();

            // Redact the password and salt for security purposes.
            final Collection<User> usersList = users.getUsers();
            for (User user : usersList) {
                redactUserPasswordInfo(user);
            }

            return Response.ok(users).build();
        } catch (IDMStoreException se) {
            return internalError("getting", se);
        }
    }

    /**
     * Extracts the user represented by <code>id</code>. The password and salt
     * fields are redacted for security reasons.
     *
     * @param id the unique id of representing the user account
     * @return A response with the user information, or internal error if one occurs
     */
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getUser(@PathParam("id") String id) {
        LOG.info("GET auth/v1/users/ {}  (extract user with specified id)", id);

        try {
            final User user = AAAIDMLightModule.getStore().readUser(id);

            if (user == null) {
                final String error = "user not found! id: " + id;
                return new IDMError(404, error, "").response();
            }

            // Redact the password and salt for security purposes.
            redactUserPasswordInfo(user);

            return Response.ok(user).build();
        } catch (IDMStoreException se) {
            return internalError("getting", se);
        }
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
     * If a password is not provided, please ensure you change the default
     * password ASAP for security reasons!
     *
     * @param info passed from Jersey
     * @param user the user defined in the JSON payload
     * @return A response stating success or failure of user creation
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createUser(@Context UriInfo info, User user) {
        LOG.info("POST /auth/v1/users  (create a user with the specified payload");

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
        final String userDescription = user.getDescription();
        if (userDescription == null) {
            user.setDescription(DEFAULT_DESCRIPTION);
        }
        // The "description" field has a maximum length.
        if (userDescription.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("description", IdmLightApplication.MAX_FIELD_LEN);
        }

        // The "email" field is optional and defaults to "".
        final String userEmail = user.getEmail();
        if (userEmail == null) {
            user.setEmail(DEFAULT_EMAIL);
        }
        if (userEmail.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("email", IdmLightApplication.MAX_FIELD_LEN);
        }
        // TODO add a check on email format here.

        // The "password" field is optional and defautls to "changeme".
        final String userPassword = user.getPassword();
        if (userPassword == null) {
            user.setPassword(DEFAULT_PWD);
        } else if (userPassword.length() > IdmLightApplication.MAX_FIELD_LEN) {
            return providedFieldTooLong("password", IdmLightApplication.MAX_FIELD_LEN);
        }

        try {
            // At this point, fields have been properly verified. Create the
            // user account
            final User createdUser = AAAIDMLightModule.getStore().writeUser(user);
            user.setUserid(createdUser.getUserid());
        } catch (IDMStoreException se) {
            return internalError("creating", se);
        }

        // Redact the password and salt for security reasons.
        redactUserPasswordInfo(user);
        // TODO report back to the client a warning message to change the
        // default password if none was specified.
        return Response.status(201).entity(user).build();
    }

    /**
     * REST endpoint to update a user account.
     *
     * @param info passed from Jersey
     * @param user the user defined in the JSON payload
     * @param id the unique id for the user that will be updated
     * @return A response stating success or failure of the user update
     */
    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response putUser(@Context UriInfo info, User user, @PathParam("id") String id) {

        LOG.info("PUT /auth/v1/users/{}  (Updates a user account)", id);

        try {
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

            user = AAAIDMLightModule.getStore().updateUser(user);
            if (user == null) {
                return new IDMError(404, String.format("User not found for id %s", id), "").response();
            }

            IdmLightProxy.clearClaimCache();

            // Redact the password and salt for security reasons.
            redactUserPasswordInfo(user);
            return Response.status(200).entity(user).build();
        } catch (IDMStoreException se) {
            return internalError("updating", se);
        }
    }

    /**
     * REST endpoint to delete a user account.
     *
     * @param info passed from Jersey
     * @param id the unique id of the user which is being deleted
     * @return A response stating success or failure of user deletion
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@Context UriInfo info, @PathParam("id") String id) {
        LOG.info("DELETE /auth/v1/users/{}  (Delete a user account)", id);

        try {
            final User user = AAAIDMLightModule.getStore().deleteUser(id);

            if (user == null) {
                return new IDMError(404,
                        String.format("Error deleting user.  " +
                                      "Couldn't find user with id %s", id),
                                      "").response();
            }
        } catch (IDMStoreException se) {
            return internalError("deleting", se);
        }

        // Successfully deleted the user; report success to the client.
        IdmLightProxy.clearClaimCache();
        return Response.status(204).build();
    }

    /**
     * Creates a <code>Response</code> related to an internal server error.
     *
     * @param verbal such as "creating", "deleting", "updating"
     * @param e The exception, which is propagated in the response
     * @return A response containing internal error with specific reasoning
     */
    private Response internalError(final String verbal, final Exception e) {
        LOG.error("There was an internal error {} the user", verbal, e);
        return new IDMError(500,
                String.format("There was an internal error %s the user", verbal),
                e.getMessage()).response();
    }

    /**
     * Creates a <code>Response</code> related to the user not providing a
     * required field.
     *
     * @param fieldName the name of the field which is missing
     * @return A response explaining that the request is missing a field
     */
    private Response missingRequiredField(final String fieldName) {

        return new IDMError(400,
                String.format("%s is required to create the user account.  " +
                              "Please provide a %s in your payload.", fieldName, fieldName),
                              "").response();
    }

    /**
     * Creates a <code>Response</code> related to the user providing a field
     * that is too long.
     *
     * @param fieldName the name of the field that is too long
     * @param maxFieldLength the maximum length of <code>fieldName</code>
     * @return A response containing the bad field and the maximum field length
     */
    private Response providedFieldTooLong(final String fieldName, final int maxFieldLength) {

        return new IDMError(400,
                getProvidedFieldTooLongMessage(fieldName, maxFieldLength),
                "").response();
    }

    /**
     * Creates the client-facing message related to the user providing a field
     * that is too long.
     *
     * @param fieldName the name of the field that is too long
     * @param maxFieldLength the maximum length of <code>fieldName</code>
     * @return
     */
    private static String getProvidedFieldTooLongMessage(final String fieldName,
            final int maxFieldLength) {

        return String.format("The provided %s field is too long.  " +
                             "The max length is %s.", fieldName, maxFieldLength);
    }

    /**
     * Prepares a user account for output by redacting the appropriate fields.
     * This method side-effects the <code>user</code> parameter.
     *
     * @param user the user account which will have fields redacted
     */
    private static void redactUserPasswordInfo(final User user) {
        user.setPassword(REDACTED_PASSWORD);
        user.setSalt(REDACTED_SALT);
    }

    /**
     * Validate the input field length
     *
     * @param inputField
     * @return true if input field bigger than the MAX_FIELD_LEN
     */
    private boolean checkInputFieldLength(final String inputField) {
        return inputField != null && inputField.length() > IdmLightApplication.MAX_FIELD_LEN;
    }
}
