/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import java.util.Arrays;
import java.util.List;

import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;

/**
 * An OSGi proxy for the IdmLight server.
 *
 * @author liemmn
 */
public class IdmLightProxy implements CredentialAuth<PasswordCredentials>,
        IdMService {

    @Override
    public Claim authenticate(PasswordCredentials creds, String tenant) {
        // TODO Get this from the database/REST
        if (creds.username().equalsIgnoreCase("admin")
                && creds.password().equalsIgnoreCase("odl")) {
            ClaimBuilder claim = new ClaimBuilder();
            claim.setUserId("1234").setUser(creds.username()).addRole("admin")
                    .addRole("user").setDomain("sdn").build();
            return claim.build();
        }
        throw new AuthenticationException("Bad username or password");
    }

    @Override
    public String getUserId(String userName) {
        // TODO Get this from the database/REST
        return "1234";
    }

    @Override
    public List<String> listDomains(String userId) {
        // TODO Get this from the database/REST
        return Arrays.asList("coke");
    }

    @Override
    public List<String> listRoles(String userId, String domain) {
        // TODO Get this from the database/REST
        return Arrays.asList("admin", "user");
    }
}
