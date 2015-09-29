/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.basic;

import java.util.List;
import java.util.Map;

import org.glassfish.jersey.internal.util.Base64;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.PasswordCredentialBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;

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
    public Authentication validate(Map<String, List<String>> headers)
            throws AuthenticationException {
        if (headers.containsKey(AUTH_HEADER)) {
            final String authHeader = headers.get(AUTH_HEADER).get(0);
            if (authHeader != null && authHeader.startsWith(BASIC_PREFIX)) {
                // HTTP Basic Auth
                String[] creds = new String(Base64.decode(authHeader
                        .substring(BASIC_PREFIX.length()).getBytes())).split(AUTH_SEP);
                // If no domain was supplied then use the default one, which is "sdn".
                if(creds!=null && creds.length==2){
                    String temp[] = new String[3];
                    System.arraycopy(creds, 0, temp, 0, creds.length);
                    temp[2] = "sdn";
                    creds = temp;
                }
                PasswordCredentials pc = new PasswordCredentialBuilder()
                        .setUserName(creds[0]).setPassword(creds[1]).setDomain(creds[2]).build();
                Claim claim = ca.authenticate(pc);
                return new AuthenticationBuilder(claim).build();
            }
        }
        return null;
    }

}
