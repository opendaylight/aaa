/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.direct;

import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;

/**
 * A direct authentication mechanism that uses the IdMLight server for
 * authentication.
 *
 */
public class IdmLightProxy implements CredentialAuth<PasswordCredentials> {

    @Override
    public Claim authenticate(PasswordCredentials creds, String tenant) {
        // TODO Authenticate with Peter's IdM server via REST
        if (creds.username().equalsIgnoreCase("admin")
                && creds.password().equalsIgnoreCase("odl")) {
            ClaimBuilder claim = new ClaimBuilder();
            claim.setUserId("1234").setUserName(creds.username())
                    .addRole("admin").addRole("user").setTenantId("5678")
                    .setTenantName("tenantX").build();
            return claim;
        }
        throw new AuthenticationException("Bad username or password");
    }
}
