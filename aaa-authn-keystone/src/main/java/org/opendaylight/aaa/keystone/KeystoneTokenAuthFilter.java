/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.keystone;

import static org.opendaylight.aaa.AuthConstants.AUTH_IDENTITY_CONFIRMED;
import static org.opendaylight.aaa.AuthConstants.AUTH_IDENTITY_STATUS;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenAuth;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * A Keystone {@link TokenAuth} filter.
 *
 * @author liemmn
 */
public class KeystoneTokenAuthFilter implements TokenAuth, ContainerRequestFilter {
    static final String TOKEN = "X-Auth-Token";

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public ContainerRequest filter(ContainerRequest req) {
        String token = req.getHeaderValue(TOKEN);
        Authentication auth = validate(token);
        if (auth != null) {
            ServiceLocator.INSTANCE.as.set(auth);
            httpRequest.setAttribute(AUTH_IDENTITY_STATUS, AUTH_IDENTITY_CONFIRMED);
        }
        return req;
    }

    @Override
    public Authentication validate(String token) {
        if (token == null || token.isEmpty())
            return null;    // Someone downstream may be able to validate...

        AuthenticationBuilder ab = new AuthenticationBuilder();
        Authentication auth;
        // TODO: Call into Keystone to get security context...
        if (token.equalsIgnoreCase("admin")) {
            auth = ab.setUserId("1234").setUserName("Bob").addRole("admin")
                    .addRole("user").setTenantId("5678")
                    .setTenantName("tenantX")
                    .setExpiration(System.currentTimeMillis() + 1000).build();
        } else {
            auth = ab.setUserId("abcd").setUserName("Alice").addRole("user")
                    .setTenantId("efgh").setTenantName("tenantY")
                    .setExpiration(System.currentTimeMillis() + 1000).build();
        }
        return auth;
    }

}
