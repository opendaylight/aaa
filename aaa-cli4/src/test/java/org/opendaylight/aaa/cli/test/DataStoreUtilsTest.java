/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.test;

import static org.junit.Assert.*;
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
import org.opendaylight.aaa.api.model.*;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author mserngawy
 *
 */
@RunWith(PowerMockRunner.class)
public class DataStoreUtilsTest {

    @Mock private IIDMStore identityStore;
    private final String domainName = "Dfoo";
    private final String usrName = "Ufoo";
    private final String roleName = "Rfoo";
    private final String grantId = "Gfoo";
    private final String dummyId = "foo";

    @Before
    public void setUp() throws Exception {
        identityStore = PowerMockito.mock(IIDMStore.class, Mockito.CALLS_REAL_METHODS);
        Domains domains = new Domains();
        Domain domain = new Domain();
        domain.setName(domainName);
        domain.setDomainid(domainName);
        List<Domain> dList = new ArrayList<>();
        dList.add(domain);
        domains.setDomains(dList);
        when(identityStore.getDomains()).thenReturn(domains);

        Users users = new Users();
        User usr = new User();
        usr.setName(usrName);
        usr.setDomainid(domainName);
        usr.setUserid(usrName);
        List<User> uList = new ArrayList<>();
        uList.add(usr);
        users.setUsers(uList);
        when(identityStore.getUsers()).thenReturn(users);

        Roles roles = new Roles();
        Role role = new Role();
        role.setName(roleName);
        role.setDomainid(domainName);
        role.setRoleid(roleName);
        List<Role> rList = new ArrayList<>();
        rList.add(role);
        roles.setRoles(rList);
        when(identityStore.getRoles()).thenReturn(roles);

        Grants grants = new Grants();
        Grant grant = new Grant();
        grant.setDomainid(domainName);
        grant.setGrantid(grantId);
        grant.setRoleid(roleName);
        grant.setUserid(usrName);
        List<Grant> gList = new ArrayList<>();
        gList.add(grant);
        grants.setGrants(gList);
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
