/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static java.util.Objects.requireNonNull;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Utility methods for forming audit trail output based on an <code>AuthenticationToken</code>.
 */
public final class AuthenticationTokenUtils {

    /**
     * default value used in messaging when the "user" field is unparsable from the HTTP REST request.
     */
    static final String DEFAULT_USERNAME = "an unknown user";

    /**
     * default value used in messaging when the "user" field is not present in the HTTP REST request, implying
     * a different implementation of <code>AuthenticationToken</code> such as <code>CasToken</code>.
     */
    static final String DEFAULT_TOKEN = "an un-parsable token type";

    /**
     * default value used in messaging when the "host" field cannot be determined.
     */
    static final String DEFAULT_HOSTNAME = "an unknown host";

    private AuthenticationTokenUtils() {
        // private to prevent instantiation
    }

    /**
     * Determines whether the supplied <code>Token</code> is a <code>UsernamePasswordToken</code>.
     *
     * @param token A generic <code>Token</code>, which might be a <code>UsernamePasswordToken</code>
     * @return Whether the supplied <code>Token</code> is a <code>UsernamePasswordToken</code>
     */
    public static boolean isUsernamePasswordToken(final AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    /**
     * Extracts the username if possible.  If the supplied token is a <code>UsernamePasswordToken</code>
     * and the username field is not set, <code>DEFAULT_USERNAME</code> is returned.  If the supplied
     * token is not a <code>UsernamePasswordToken</code> (i.e., a <code>CasToken</code> or other
     * implementation of <code>AuthenticationToken</code>), then <code>DEFAULT_TOKEN</code> is
     * returned.
     *
     * @param token An <code>AuthenticationToken</code>, possibly a <code>UsernamePasswordToken</code>
     * @return the username, <code>DEFAULT_USERNAME</code> or <code>DEFAULT_TOKEN</code> depending on input
     */
    public static String extractUsername(final AuthenticationToken token) {
        if (token instanceof UsernamePasswordToken upt) {
            return extractField(upt.getUsername(), DEFAULT_USERNAME);
        }
        return DEFAULT_TOKEN;
    }

    /**
     * Extracts the hostname if possible.  If the supplied token is a <code>UsernamePasswordToken</code>
     * and the hostname field is not set, <code>DEFAULT_HOSTNAME</code> is returned.  If the supplied
     * token is not a <code>UsernamePasswordToken</code> (i.e., a <code>CasToken</code> or other
     * implementation of <code>AuthenticationToken</code>), then <code>DEFAULT_HOSTNAME</code> is
     * returned.
     *
     * @param token An <code>AuthenticationToken</code>, possibly a <code>UsernamePasswordToken</code>
     * @return the hostname, or <code>DEFAULT_USERNAME</code> depending on input
     */
    public static String extractHostname(final AuthenticationToken token) {
        if (token instanceof UsernamePasswordToken upt) {
            return extractField(upt.getHost(), DEFAULT_HOSTNAME);
        }
        return DEFAULT_HOSTNAME;
    }

    /**
     * Utility method to generate a generic message indicating Authentication was unsuccessful.
     *
     * @param token An <code>AuthenticationToken</code>, possibly a <code>UsernamePasswordToken</code>
     * @return A message indicating authentication was unsuccessful
     */
    public static String generateUnsuccessfulAuthenticationMessage(final AuthenticationToken token) {
        final String username = extractUsername(token);
        final String remoteHostname = extractHostname(token);
        return String.format("Unsuccessful authentication attempt by %s from %s", username, remoteHostname);
    }

    /**
     * Utility method to generate a generic message indicating Authentication was successful.
     *
     * @param token An <code>AuthenticationToken</code>, possibly a <code>UsernamePasswordToken</code>
     * @return A message indicating authentication was successful
     */
    public static String generateSuccessfulAuthenticationMessage(final AuthenticationToken token) {
        final String username = extractUsername(token);
        final String remoteHostname = extractHostname(token);
        return String.format("Successful authentication attempt by %s from %s", username, remoteHostname);
    }

    /**
     * Utility method that returns <code>field</code>, or <code>defaultValue</code> if <code>field</code> is null.
     *
     * @param field A generic string, which is possibly null.
     * @param defaultValue A non-null value returned if <code>field</code> is null
     * @return <code>field</code> or <code>defaultValue</code> if field is null
     * @throws NullPointerException If <code>defaultValue</code> is null
     */
    private static String extractField(final String field, final String defaultValue) {
        final String def = requireNonNull(defaultValue, "defaultValue can't be null");
        return field != null ? field : def;
    }
}
