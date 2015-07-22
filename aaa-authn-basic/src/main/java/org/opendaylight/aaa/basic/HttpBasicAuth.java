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

import javax.ws.rs.core.MultivaluedMap;

import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.PasswordCredentialBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;

import com.sun.jersey.core.util.Base64;

/**
 * An HTTP Basic authenticator.  Note that this is provided as a Hydrogen
 * backward compatible authenticator, but usage of this authenticator or
 * HTTP Basic Authentication is highly discouraged due to its vulnerability.
 *
 * @author liemmn
 *
 */
public class HttpBasicAuth implements TokenAuth {
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_SEP = ":";
    private static final String BASIC_PREFIX = "Basic ";

    volatile CredentialAuth<PasswordCredentials> ca;

    @Override
    public Authentication validate(Map<String, List<String>> headers, MultivaluedMap<String, String> queryParameters)
            throws AuthenticationException {
        if (headers.containsKey(AUTH_HEADER)) {
            final String authHeader = headers.get(AUTH_HEADER).get(0);
            if (authHeader != null && authHeader.startsWith(BASIC_PREFIX)) {
                // HTTP Basic Auth
                String[] creds = new String(Base64.base64Decode(authHeader
                        .substring(BASIC_PREFIX.length()))).split(AUTH_SEP);
                PasswordCredentials pc = new PasswordCredentialBuilder()
                        .setUserName(creds[0]).setPassword(creds[1]).build();
                String domainParameter = null;
                if (queryParameters != null) {
                   domainParameter = queryParameters.getFirst("domainname");
                }
                Claim claim = ca.authenticate(pc, domainParameter);
                return new AuthenticationBuilder(claim).build();
            }
        }
        return null;
    }

}
