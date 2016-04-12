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
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;


public class RoleHandlerTest extends HandlerTest{

    @Test
    public void testRoleHandler() {
        //check default roles
        Roles roles = resource().path("/v1/roles").get(Roles.class);
        assertNotNull(roles);
        List<Role> roleList = roles.getRoles();
        assertEquals(2, roleList.size());
        for (Role role : roleList) {
            assertTrue(role.getName().equals("admin") || role.getName().equals("user"));
        }

        //check existing role
        Role role = resource().path("/v1/roles/0").get(Role.class);
        assertNotNull(role);
        assertTrue(role.getName().equals("admin"));

        //check not exist Role
        try {
            resource().path("/v1/roles/5").get(IDMError.class);
            fail("Should failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
            assertTrue(resp.getEntity(IDMError.class).getMessage().contains("role not found"));
        }

        // check create Role
        Map<String, String> roleData = new HashMap<String, String>();
        roleData.put("name","role1");
        roleData.put("description","test Role");
        roleData.put("domainid","0");
        ClientResponse clientResponse = resource().path("/v1/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, roleData);
        assertEquals(201, clientResponse.getStatus());

        // check create Role missing name data
        roleData.remove("name");
        try {
            clientResponse = resource().path("/v1/roles").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, roleData);
            assertEquals(404, clientResponse.getStatus());
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(500, resp.getStatus());
        }

        // check update Role data
        roleData.put("name","role1Update");
        clientResponse = resource().path("/v1/roles/2").type(MediaType.APPLICATION_JSON).put(ClientResponse.class, roleData);
        assertEquals(200, clientResponse.getStatus());
        role = resource().path("/v1/roles/2").get(Role.class);
        assertNotNull(role);
        assertTrue(role.getName().equals("role1Update"));

        // check delete Role
        clientResponse = resource().path("/v1/roles/2").delete(ClientResponse.class);
        assertEquals(204, clientResponse.getStatus());

        // check delete not existing Role
        try {
            resource().path("/v1/roles/2").delete(IDMError.class);
            fail("Should failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
            assertTrue(resp.getEntity(IDMError.class).getMessage().contains("role id not found"));
        }
    }
}
