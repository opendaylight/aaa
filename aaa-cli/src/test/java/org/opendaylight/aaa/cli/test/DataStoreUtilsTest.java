/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Test Suite for the DataStore Utils.
 * @author mserngawy
 *
 */
@RunWith(PowerMockRunner.class)
public class DataStoreUtilsTest {

    @Mock
    private IIDMStore identityStore;
    private final String domainName = "Dfoo";
    private final String usrName = "Ufoo";
    private final String roleName = "Rfoo";
    private final String grantId = "Gfoo";
    private final String dummyId = "foo";

    @Before
    public void setUp() throws Exception {
        identityStore = PowerMockito.mock(IIDMStore.class, Mockito.CALLS_REAL_METHODS);
        final Domains domains = new Domains();
        Domain domain = new Domain();
        domain.setName(domainName);
        domain.setDomainid(domainName);
        List<Domain> domainsList = new ArrayList<>();
        domainsList.add(domain);
        domains.setDomains(domainsList);
        when(identityStore.getDomains()).thenReturn(domains);

        final Users users = new Users();
        User usr = new User();
        usr.setName(usrName);
        usr.setDomainid(domainName);
        usr.setUserid(usrName);
        List<User> usersList = new ArrayList<>();
        usersList.add(usr);
        users.setUsers(usersList);
        when(identityStore.getUsers()).thenReturn(users);

        final Roles roles = new Roles();
        Role role = new Role();
        role.setName(roleName);
        role.setDomainid(domainName);
        role.setRoleid(roleName);
        List<Role> rolesList = new ArrayList<>();
        rolesList.add(role);
        roles.setRoles(rolesList);
        when(identityStore.getRoles()).thenReturn(roles);

        final Grants grants = new Grants();
        Grant grant = new Grant();
        grant.setDomainid(domainName);
        grant.setGrantid(grantId);
        grant.setRoleid(roleName);
        grant.setUserid(usrName);
        List<Grant> grantsList = new ArrayList<>();
        grantsList.add(grant);
        grants.setGrants(grantsList);
        when(identityStore.getGrants(usrName)).thenReturn(grants);
    }

    @Test
    public void testGetDomainId() throws IDMStoreException {
        final String domID = DataStoreUtils.getDomainId(identityStore, domainName);
        assertNotNull(domID);
        assertEquals(domID, domainName);
        assertNull(DataStoreUtils.getDomainId(identityStore, dummyId));
    }

    @Test
    public void testGetRoleId() throws IDMStoreException {
        final String roleID = DataStoreUtils.getRoleId(identityStore, roleName);
        assertNotNull(roleID);
        assertEquals(roleID, roleName);
        assertNull(DataStoreUtils.getRoleId(identityStore, dummyId));
    }

    @Test
    public void testGetUserId() throws IDMStoreException {
        final String usrID = DataStoreUtils.getUserId(identityStore, usrName);
        assertNotNull(usrID);
        assertEquals(usrID, usrName);
        assertNull(DataStoreUtils.getUserId(identityStore, dummyId));
    }

    @Test
    public void testGetGrantId() throws IDMStoreException {
        final String grantID = DataStoreUtils.getGrantId(identityStore, domainName, roleName, usrName);
        assertNotNull(grantID);
        assertEquals(grantID, grantId);
        assertNull(DataStoreUtils.getGrantId(identityStore, dummyId, dummyId, dummyId));
    }
}
