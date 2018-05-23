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
import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;

public class RoleHandlerTest extends HandlerTest {

    @Test
    public void testRoleHandler() {
        // check default roles
        Roles roles = target("/v1/roles").request().get(Roles.class);
        assertNotNull(roles);
        List<Role> roleList = roles.getRoles();
        assertEquals(2, roleList.size());
        for (Role role : roleList) {
            assertTrue(role.getName().equals("admin") || role.getName().equals("user"));
        }

        // check existing role
        Role role = target("/v1/roles/0").request().get(Role.class);
        assertNotNull(role);
        assertTrue(role.getName().equals("admin"));

        // check not exist Role
        try {
            target("/v1/roles/5").request().get(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // check create Role
        Map<String, String> roleData = new HashMap<>();
        roleData.put("name", "role1");
        roleData.put("description", "test Role");
        roleData.put("domainid", "0");
        Response clientResponse = target("/v1/roles").request().post(entity(roleData));
        assertEquals(201, clientResponse.getStatus());

        // check create Role missing name data
        roleData.remove("name");
        try {
            clientResponse = target("/v1/roles").request().post(entity(roleData));
            assertEquals(404, clientResponse.getStatus());
        } catch (WebApplicationException e) {
            assertEquals(500, e.getResponse().getStatus());
        }

        // check update Role data
        roleData.put("name", "role1Update");
        clientResponse = target("/v1/roles/2").request().put(entity(roleData));
        assertEquals(200, clientResponse.getStatus());
        role = target("/v1/roles/2").request().get(Role.class);
        assertNotNull(role);
        assertTrue(role.getName().equals("role1Update"));

        // check delete Role
        clientResponse = target("/v1/roles/2").request().delete();
        assertEquals(204, clientResponse.getStatus());

        // check delete not existing Role
        try {
            target("/v1/roles/2").request().delete(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // Bug 8382: if a role id is specified, 400 is returned
        roleData = new HashMap<>();
        roleData.put("name", "role1");
        roleData.put("description", "test Role");
        roleData.put("domainid", "0");
        roleData.put("roleid", "roleid");
        clientResponse = target("/v1/roles").request().post(entity(roleData));
        assertEquals(400, clientResponse.getStatus());
    }
}
