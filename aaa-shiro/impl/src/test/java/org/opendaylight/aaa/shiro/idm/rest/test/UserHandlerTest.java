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
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

@Ignore
public class UserHandlerTest extends HandlerTest {

    @Test
    public void testUserHandler() {
        // check default users
        Users users = target("/v1/users").request().get(Users.class);
        assertNotNull(users);
        List<User> usrList = users.getUsers();
        assertEquals(1, usrList.size());
        for (User usr : usrList) {
            assertTrue(usr.getName().equals("admin"));
        }

        // check existing user
        User usr = target("/v1/users/0").request().get(User.class);
        assertNotNull(usr);
        assertTrue(usr.getName().equals("admin"));

        // check not exist user
        try {
            target("/v1/users/5").request().get(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // check create user
        Map<String, String> usrData = new HashMap<>();
        usrData.put("name", "usr1");
        usrData.put("description", "test user");
        usrData.put("enabled", "true");
        usrData.put("email", "user1@usr.org");
        usrData.put("password", "ChangeZbadPa$$w0rd");
        usrData.put("domainid", "0");
        Response clientResponse = target("/v1/users").request().post(entity(usrData));
        assertEquals(201, clientResponse.getStatus());

        // check create user missing name data
        usrData.remove("name");
        try {
            clientResponse = target("/v1/users").request().post(entity(usrData));
            assertEquals(400, clientResponse.getStatus());
        } catch (WebApplicationException e) {
            assertEquals(500, e.getResponse().getStatus());
        }

        // check update user data
        usrData.put("name", "usr1Update");
        clientResponse = target("/v1/users/1").request().put(entity(usrData));
        assertEquals(200, clientResponse.getStatus());
        usr = target("/v1/users/1").request().get(User.class);
        assertNotNull(usr);
        assertTrue(usr.getName().equals("usr1Update"));

        // check delete user
        clientResponse = target("/v1/users/1").request().delete();
        assertEquals(204, clientResponse.getStatus());

        // check delete not existing user
        try {
            target("/v1/users/1").request().delete(IDMError.class);
            fail("Should fail with 404!");
        } catch (NotFoundException e) {
            // expected
        }

        // Bug 8382: if a user id is specified, 400 is returned
        usrData = new HashMap<>();
        usrData.put("name", "usr1");
        usrData.put("description", "test user");
        usrData.put("enabled", "true");
        usrData.put("email", "user1@usr.org");
        usrData.put("password", "ChangeZbadPa$$w0rd");
        usrData.put("userid", "userid");
        usrData.put("domainid", "0");
        clientResponse = target("/v1/users").request().post(entity(usrData));
        assertEquals(400, clientResponse.getStatus());
    }
}
