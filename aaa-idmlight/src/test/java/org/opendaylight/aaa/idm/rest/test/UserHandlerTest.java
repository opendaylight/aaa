/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.test;

import static org.junit.Assert.*;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.opendaylight.aaa.api.model.IDMError;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

public class UserHandlerTest extends HandlerTest {

    @Test
    public void testUserHandler() {
        //check default users
        Users users = resource().path("/v1/users").get(Users.class);
        assertNotNull(users);
        List<User> usrList = users.getUsers();
        assertEquals(2, usrList.size());
        for (User usr : usrList) {
            assertTrue(usr.getName().equals("admin") || usr.getName().equals("user"));
        }

        //check existing user
        User usr = resource().path("/v1/users/0").get(User.class);
        assertNotNull(usr);
        assertTrue(usr.getName().equals("admin"));

        //check not exist user
        try {
            resource().path("/v1/users/5").get(IDMError.class);
            fail("Should failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
            assertTrue(resp.getEntity(IDMError.class).getMessage().contains("user not found"));
        }

        // check create user
        Map<String, String> usrData = new HashMap<String, String>();
        usrData.put("name","usr1");
        usrData.put("description","test user");
        usrData.put("enabled","true");
        usrData.put("email","user1@usr.org");
        usrData.put("password","ChangeZbadPa$$w0rd");
        usrData.put("domainid","0");
        ClientResponse clientResponse = resource().path("/v1/users").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, usrData);
        assertEquals(201, clientResponse.getStatus());

        // check create user missing name data
        usrData.remove("name");
        try {
            clientResponse = resource().path("/v1/users").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, usrData);
            assertEquals(400, clientResponse.getStatus());
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(500, resp.getStatus());
        }

        // check update user data
        usrData.put("name","usr1Update");
        clientResponse = resource().path("/v1/users/2").type(MediaType.APPLICATION_JSON).put(ClientResponse.class, usrData);
        assertEquals(200, clientResponse.getStatus());
        usr = resource().path("/v1/users/2").get(User.class);
        assertNotNull(usr);
        assertTrue(usr.getName().equals("usr1Update"));

        // check delete user
        clientResponse = resource().path("/v1/users/2").delete(ClientResponse.class);
        assertEquals(204, clientResponse.getStatus());

        // check delete not existing user
        try {
            resource().path("/v1/users/2").delete(IDMError.class);
            fail("Should failed with 404!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(404, resp.getStatus());
            assertTrue(resp.getEntity(IDMError.class).getMessage().contains("Couldn't find user"));
        }
    }

}
