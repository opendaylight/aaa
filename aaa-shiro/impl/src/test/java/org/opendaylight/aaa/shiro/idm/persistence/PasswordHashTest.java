/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm.persistence;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;
import org.opendaylight.aaa.shiro.idm.IdmLightProxy;

/*
 * @Author - Sharon Aicler (saichler@cisco.com)
*/
public class PasswordHashTest {
    private final PasswordHashService passwordService = new DefaultPasswordHashService();

    private IIDMStore store;

    @Before
    public void before() throws IDMStoreException {
        store = mock(IIDMStore.class);
        Domain domain = new Domain();
        domain.setName("sdn");
        domain.setDomainid("sdn");

        doReturn(domain).when(store).readDomain("sdn");
        Creds creds = new Creds();

        User user = new User();
        user.setName("admin");
        user.setUserid(creds.username());
        user.setDomainid("sdn");
        user.setSalt("ABCD");
        user.setPassword(passwordService.getPasswordHash(creds.password(), user.getSalt()).getHashedPassword());
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
        doReturn(role).when(store).readRole("admin");
        doReturn(users).when(store).getUsers(creds.username(), creds.domain());
        doReturn(grants).when(store).getGrants(creds.domain(), creds.username());
    }

    @Test
    public void testPasswordHash() {
        final var proxy = new IdmLightProxy(store, passwordService);
        proxy.authenticate(new Creds());
    }

    private static final class Creds implements PasswordCredentials {
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
