/*
 * Copyright (c) 2016, 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.idm.rest.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Roles;

public class DomainHandlerTest extends HandlerTest {

    @Test
    public void testDomainHandler() {
        // check default domains
        Domains domains = target("/v1/domains").request().get(Domains.class);
        assertNotNull(domains);
        assertEquals(1, domains.getDomains().size());
        assertTrue(domains.getDomains().get(0).getName().equals("sdn"));

        // check existing domain
        Domain domain = target("/v1/domains/0").request().get(Domain.class);
        assertNotNull(domain);
        assertTrue(domain.getName().equals("sdn"));

        // check not exist domain
        try {
            target("/v1/domains/5").request().get(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // check create domain
        Map<String, String> domainData = new HashMap<>();
        domainData.put("name", "dom1");
        domainData.put("description", "test dom");
        domainData.put("enabled", "true");
        Response clientResponse = target("/v1/domains").request().post(entity(domainData));
        assertEquals(201, clientResponse.getStatus());

        // check update domain data
        domainData.put("name", "dom1Update");
        clientResponse = target("/v1/domains/1").request().put(entity(domainData));
        assertEquals(200, clientResponse.getStatus());
        domain = target("/v1/domains/1").request().get(Domain.class);
        assertNotNull(domain);
        assertTrue(domain.getName().equals("dom1Update"));

        // check create grant
        Map<String, String> grantData = new HashMap<>();
        grantData.put("roleid", "1");
        clientResponse = target("/v1/domains/1/users/0/roles").request().post(entity(grantData));
        assertEquals(201, clientResponse.getStatus());

        // check create existing grant
        clientResponse = target("/v1/domains/1/users/0/roles").request().post(entity(grantData));
        assertEquals(403, clientResponse.getStatus());

        // check create grant with invalid domain id
        clientResponse = target("/v1/domains/5/users/0/roles").request().post(entity(grantData));
        assertEquals(404, clientResponse.getStatus());

        // check validate user (admin)
        Map<String, String> usrPwdData = new HashMap<>();
        usrPwdData.put("username", "admin");
        usrPwdData.put("userpwd", "admin");
        clientResponse = target("/v1/domains/0/users/roles").request().post(entity(usrPwdData));
        assertEquals(200, clientResponse.getStatus());

        // check validate user (admin) with wrong password
        usrPwdData.put("userpwd", "1234");
        clientResponse = target("/v1/domains/0/users/roles").request().post(entity(usrPwdData));
        assertEquals(401, clientResponse.getStatus());

        // check get user (admin) roles
        Roles usrRoles = target("/v1/domains/0/users/0/roles").request().get(Roles.class);
        assertNotNull(usrRoles);
        assertTrue(usrRoles.getRoles().size() > 1);

        // check get invalid user roles
        try {
            target("/v1/domains/0/users/5/roles").request().get(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // check delete grant
        clientResponse = target("/v1/domains/0/users/0/roles/0").request().delete();
        assertEquals(204, clientResponse.getStatus());

        // check delete grant for invalid domain
        clientResponse = target("/v1/domains/3/users/0/roles/0").request().delete();
        assertEquals(404, clientResponse.getStatus());

        // check delete domain
        clientResponse = target("/v1/domains/1").request().delete();
        assertEquals(204, clientResponse.getStatus());

        // check delete not existing domain
        try {
            target("/v1/domains/1").request().delete(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // Bug 8382: if a domain id is specified, 400 is returned
        domainData = new HashMap<>();
        domainData.put("name", "dom1");
        domainData.put("description", "test dom");
        domainData.put("domainid", "dom1");
        domainData.put("enabled", "true");
        clientResponse = target("/v1/domains").request().post(entity(domainData));
        assertEquals(400, clientResponse.getStatus());

        // Bug 8382: if a grant id is specified, 400 is returned
        grantData = new HashMap<>();
        grantData.put("roleid", "1");
        grantData.put("grantid", "grantid");
        clientResponse = target("/v1/domains/1/users/0/roles").request().post(entity(grantData));
        assertEquals(400, clientResponse.getStatus());
    }
}
