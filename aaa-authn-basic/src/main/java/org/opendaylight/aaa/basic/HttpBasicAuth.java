/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.basic;

import java.util.List;
import java.util.Map;

import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.PasswordCredentialBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.Base64;

/**
 * An HTTP Basic authenticator.  Note that this is provided as a Hydrogen
 * backward compatible authenticator, but usage of this authenticator or
 * HTTP Basic Authentication is highly discouraged due to its vulnerability.
 *
 * To obtain a token using the HttpBasicAuth Strategy, add a header to your
 * HTTP request in the form:
 * <code>Authorization: Basic BASE_64_ENCODED_CREDENTIALS</code>
 *
 * Where <code>BASE_64_ENCODED_CREDENTIALS</code> is the base 64 encoded value
 * of the user's credentials in the following form:
 * <code>user:password</code>
 *
 * For example, assuming the user is "admin" and the password is "admin":
 * <code>Authorization: Basic YWRtaW46YWRtaW4=</code>
 *
 * @author liemmn
 *
 */
public class HttpBasicAuth implements TokenAuth {

    private static final String AUTH_HEADER = "Authorization";

    private static final String AUTH_SEP = ":";

    private static final String BASIC_PREFIX = "Basic ";

    private static final Logger LOG = LoggerFactory.getLogger(HttpBasicAuth.class);

    volatile CredentialAuth<PasswordCredentials> credentialAuth;

    private static boolean checkAuthHeaderFormat(final String authHeader) {
       return (authHeader != null && authHeader.startsWith(BASIC_PREFIX));
    }

    private static String extractAuthHeader(final Map<String, List<String>> headers) {
        return headers.get(AUTH_HEADER).get(0);
    }

    private static String [] extractCredentialArray(final String authHeader) {
        return new String(Base64.base64Decode(authHeader
                .substring(BASIC_PREFIX.length()))).split(AUTH_SEP);
    }

    private static Authentication generateAuthentication(CredentialAuth<PasswordCredentials> credentialAuth, final String [] creds)
            throws ArrayIndexOutOfBoundsException, IllegalStateException {
        final PasswordCredentials pc = new PasswordCredentialBuilder()
                .setUserName(creds[0]).setPassword(creds[1]).build();
        // domain set to null;  assumes the default "sdn" domain
        final Claim claim = credentialAuth.authenticate(pc, null);
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
                // Assumes correct formatting in form Base64("user:password").
                // Throws an exception if an unknown format is used.
                try {
                    return generateAuthentication(this.credentialAuth, creds);
                } catch (ArrayIndexOutOfBoundsException e) {
                    final String message = "Login Attempt in Bad Format."
                            + " Please provide user:password in Base64 format.";
                    LOG.info(message);
                    throw new AuthenticationException(message);
                }
            }
        }
        return null;
    }

}
