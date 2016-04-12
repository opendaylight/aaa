/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Roles;

public class DomainHandlerTest extends HandlerTest{

    @Test
    public void testDomainHandler() {
        //check default domains
        Domains domains = resource().path("/v1/domains").get(Domains.class);
        assertNotNull(domains);
        assertEquals(1, domains.getDomains().size());
        assertTrue(domains.getDomains().get(0).getName().equals("sdn"));

        //check existing domain
        Domain domain = resource().path("/v1/domains/0").get(Domain.class);
        assertNotNull(domain);
        assertTrue(domain.getName().equals("sdn"));

        //check not exist domain
        try {
            resource().path("/v1/domains/5").get(IDMError.class);
            fail("Should failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
            assertTrue(resp.getEntity(IDMError.class).getMessage().contains("Not found! domain id"));
        }

        // check create domain
        Map<String, String> domainData = new HashMap<String, String>();
        domainData.put("name","dom1");
        domainData.put("description","test dom");
        domainData.put("domainid","1");
        domainData.put("enabled","true");
        ClientResponse clientResponse = resource().path("/v1/domains").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, domainData);
        assertEquals(201, clientResponse.getStatus());

        // check update domain data
        domainData.put("name","dom1Update");
        clientResponse = resource().path("/v1/domains/1").type(MediaType.APPLICATION_JSON).put(ClientResponse.class, domainData);
        assertEquals(200, clientResponse.getStatus());
        domain = resource().path("/v1/domains/1").get(Domain.class);
        assertNotNull(domain);
        assertTrue(domain.getName().equals("dom1Update"));

        // check create grant
        Map<String, String> grantData = new HashMap<String, String>();
        grantData.put("roleid","1");
        clientResponse = resource().path("/v1/domains/1/users/0/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, grantData);
        assertEquals(201, clientResponse.getStatus());

        // check create existing grant
        clientResponse = resource().path("/v1/domains/1/users/0/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, grantData);
        assertEquals(403, clientResponse.getStatus());

        // check create grant with invalid domain id
        clientResponse = resource().path("/v1/domains/5/users/0/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, grantData);
        assertEquals(404, clientResponse.getStatus());

        // check validate user (admin)
        Map<String, String> usrPwdData = new HashMap<String, String>();
        usrPwdData.put("username","admin");
        usrPwdData.put("userpwd","admin");
        clientResponse = resource().path("/v1/domains/0/users/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, usrPwdData);
        assertEquals(200, clientResponse.getStatus());

        // check validate user (admin) with wrong password
        usrPwdData.put("userpwd","1234");
        clientResponse = resource().path("/v1/domains/0/users/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, usrPwdData);
        assertEquals(401, clientResponse.getStatus());

        // check get user (admin) roles
        Roles usrRoles = resource().path("/v1/domains/0/users/0/roles").get(Roles.class);
        assertNotNull(usrRoles);
        assertTrue(usrRoles.getRoles().size() > 1);

        // check get invalid user roles
        try {
            resource().path("/v1/domains/0/users/5/roles").get(IDMError.class);
            fail("Should failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
        }

        // check delete grant
        clientResponse = resource().path("/v1/domains/0/users/0/roles/0").delete(ClientResponse.class);
        assertEquals(204, clientResponse.getStatus());

        // check delete grant for invalid domain
        clientResponse = resource().path("/v1/domains/3/users/0/roles/0").delete(ClientResponse.class);
        assertEquals(404, clientResponse.getStatus());

        // check delete domain
        clientResponse = resource().path("/v1/domains/1").delete(ClientResponse.class);
        assertEquals(204, clientResponse.getStatus());

        // check delete not existing domain
        try {
            resource().path("/v1/domains/1").delete(IDMError.class);
            fail("Shoulda failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
            assertTrue(resp.getEntity(IDMError.class).getMessage().contains("Not found! Domain id"));
        }
    }
}
