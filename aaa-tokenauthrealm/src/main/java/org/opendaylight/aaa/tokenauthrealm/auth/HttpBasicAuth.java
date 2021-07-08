/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.tokenauthrealm.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP Basic authenticator. Note that this is provided as a Hydrogen
 * backward compatible authenticator, but usage of this authenticator or HTTP
 * Basic Authentication is highly discouraged due to its vulnerability.
 *
 * <p>
 * To obtain a token using the HttpBasicAuth Strategy, add a header to your HTTP
 * request in the form:
 * <code>Authorization: Basic BASE_64_ENCODED_CREDENTIALS</code>
 *
 * <p>
 * Where <code>BASE_64_ENCODED_CREDENTIALS</code> is the base 64 encoded value
 * of the user's credentials in the following form: <code>user:password</code>
 *
 * <p>
 * For example, assuming the user is "admin" and the password is "admin":
 * <code>Authorization: Basic YWRtaW46YWRtaW4=</code>
 *
 * @author liemmn
 */
public class HttpBasicAuth implements TokenAuth {

    public static final String AUTH_HEADER = "Authorization";

    public static final String AUTH_SEP = ":";

    public static final String BASIC_PREFIX = "Basic ";

    // TODO relocate this constant
    public static final String DEFAULT_DOMAIN = "sdn";

    /**
     * username and password.
     */
    private static final int NUM_HEADER_CREDS = 2;

    /**
     * username, password and domain.
     */
    private static final int NUM_TOKEN_CREDS = 3;

    private static final Logger LOG = LoggerFactory.getLogger(HttpBasicAuth.class);

    private final CredentialAuth<PasswordCredentials> credentialAuth;

    public HttpBasicAuth(CredentialAuth<PasswordCredentials> credentialAuth) {
        this.credentialAuth = credentialAuth;
    }

    private static boolean checkAuthHeaderFormat(final String authHeader) {
        return authHeader != null && authHeader.startsWith(BASIC_PREFIX);
    }

    private static String extractAuthHeader(final Map<String, List<String>> headers) {
        return headers.get(AUTH_HEADER).get(0);
    }

    private static String[] extractCredentialArray(final String authHeader) {
        return new String(Base64.getDecoder().decode(authHeader.substring(BASIC_PREFIX.length())),
                StandardCharsets.UTF_8).split(AUTH_SEP);
    }

    private static boolean verifyCredentialArray(final String[] creds) {
        return creds != null && creds.length == NUM_HEADER_CREDS;
    }

    private static String[] addDomainToCredentialArray(final String[] creds) {
        String[] newCredentialArray = new String[NUM_TOKEN_CREDS];
        System.arraycopy(creds, 0, newCredentialArray, 0, creds.length);
        newCredentialArray[2] = DEFAULT_DOMAIN;
        return newCredentialArray;
    }

    private static Authentication generateAuthentication(
            CredentialAuth<PasswordCredentials> credentialAuth, final String[] creds)
            throws ArrayIndexOutOfBoundsException {
        final PasswordCredentials pc = new PasswordCredentialBuilder().setUserName(creds[0])
                .setPassword(creds[1]).setDomain(creds[2]).build();
        final Claim claim = credentialAuth.authenticate(pc);
        return new AuthenticationBuilder(claim).build();
    }

    @Override
    public Authentication validate(final Map<String, List<String>> headers)
            throws AuthenticationException {
        if (headers.containsKey(AUTH_HEADER)) {
            final String authHeader = extractAuthHeader(headers);
            if (checkAuthHeaderFormat(authHeader)) {
                // HTTP Basic Auth
                String[] creds = extractCredentialArray(authHeader);
                // If no domain was supplied then use the default one, which is
                // "sdn".
                if (verifyCredentialArray(creds)) {
                    creds = addDomainToCredentialArray(creds);
                }
                // Assumes correct formatting in form Base64("user:password").
                // Throws an exception if an unknown format is used.
                try {
                    return generateAuthentication(this.credentialAuth, creds);
                } catch (ArrayIndexOutOfBoundsException e) {
                    final String message = "Login Attempt in Bad Format."
                            + " Please provide user:password in Base64 format.";
                    LOG.info(message);
                    throw new AuthenticationException(message, e);
                }
            }
        }
        return null;
    }
}
