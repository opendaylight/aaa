/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.persistence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.idm.AaaIdmLight;
import org.opendaylight.aaa.idm.IdmLightProxy;

/*
 * @Author - Sharon Aicler (saichler@cisco.com)
*/
public class PasswordHashTest {

    @Before
    public void before() throws IDMStoreException {
        IIDMStore store = Mockito.mock(IIDMStore.class);
        AaaIdmLight.setStore(store);
        Domain domain = new Domain();
        domain.setName("sdn");
        domain.setDomainid("sdn");

        Mockito.when(store.readDomain("sdn")).thenReturn(domain);
        Creds creds = new Creds();

        User user = new User();
        user.setName("admin");
        user.setUserid(creds.username());
        user.setDomainid("sdn");
        user.setSalt("ABCD");
        user.setPassword(SHA256Calculator.getSHA256(creds.password(), user.getSalt()));
        List<User> lu = new LinkedList<>();
        lu.add(user);
        Users users = new Users();
        users.setUsers(lu);

        Grant grant = new Grant();
        List<Grant> listOfGrants = new ArrayList<>();
        listOfGrants.add(grant);
        grant.setDomainid("sdn");
        grant.setRoleid("admin");
        grant.setUserid("admin");
        Grants grants = new Grants();
        grants.setGrants(listOfGrants);
        Role role = new Role();
        role.setRoleid("admin");
        role.setName("admin");
        Mockito.when(store.readRole("admin")).thenReturn(role);
        Mockito.when(store.getUsers(creds.username(), creds.domain())).thenReturn(users);
        Mockito.when(store.getGrants(creds.domain(), creds.username())).thenReturn(grants);
    }

    @Test
    public void testPasswordHash() {
        IdmLightProxy proxy = new IdmLightProxy();
        proxy.authenticate(new Creds());
    }

    private static class Creds implements PasswordCredentials {
        @Override
        public String username() {
            return "admin";
        }

        @Override
        public String password() {
            return "admin";
        }

        @Override
        public String domain() {
            return "sdn";
        }
    }
}
