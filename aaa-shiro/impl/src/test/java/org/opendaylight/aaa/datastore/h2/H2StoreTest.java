/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import java.io.File;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;

public class H2StoreTest {

    @BeforeClass
    public static void start() {
        File file = new File("idmlight.db.mv.db");
        if (file.exists()) {
            file.delete();
        }
        file = new File("idmlight.db.trace.db");
        if (file.exists()) {
            file.delete();
        }
    }

    @AfterClass
    public static void end() {
        File file = new File("idmlight.db.mv.db");
        if (file.exists()) {
            file.delete();
        }
        file = new File("idmlight.db.trace.db");
        if (file.exists()) {
            file.delete();
        }
    }

    private H2Store h2Store;
    private PasswordHashService passwordService = new DefaultPasswordHashService();

    @Before
    public void before() throws StoreException, SQLException {
        IdmLightSimpleConnectionProvider dbConnectionFactory = new IdmLightSimpleConnectionProvider(
                new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build());
        UserStore us = new UserStore(dbConnectionFactory, passwordService);
        us.dbClean();
        DomainStore ds = new DomainStore(dbConnectionFactory);
        ds.dbClean();
        RoleStore rs = new RoleStore(dbConnectionFactory);
        rs.dbClean();
        GrantStore gs = new GrantStore(dbConnectionFactory);
        gs.dbClean();

        h2Store = new H2Store("foo", "bar", passwordService);
    }

    @Test
    public void testCreateDefaultDomain() throws StoreException {
        Domain domain = new Domain();
        Assert.assertEquals(true, domain != null);
        DomainStore ds = new DomainStore(
                new IdmLightSimpleConnectionProvider(new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build()));
        domain.setName(IIDMStore.DEFAULT_DOMAIN);
        domain.setEnabled(true);
        domain = ds.createDomain(domain);
        Assert.assertEquals(true, domain != null);
    }

    @Test
    public void testCreateTempRole() throws StoreException {
        Role role = h2Store.createRole("temp", "temp domain", "Temp Testing role");
        Assert.assertEquals(true, role != null);
    }

    @Test
    public void testCreateUser() throws StoreException {
        User user = h2Store.createUser("test", "pass", "domain", "desc",
                "email",true, "SALT");
        Assert.assertEquals(true, user != null);
    }

    @Test
    public void testCreateGrant() throws StoreException {
        Domain domain = h2Store.createDomain("sdn", true);
        Role role = h2Store.createRole("temp", "temp domain", "Temp Testing role");
        User user = h2Store.createUser("test", "pass", "domain", "desc",
                "email", true, "SALT");
        Grant grant = h2Store.createGrant(domain.getDomainid(), user.getUserid(), role.getRoleid());
        Assert.assertEquals(true, grant != null);
    }

    @Test
    public void testUpdatingUserEmail() throws StoreException {
        UserStore us = new UserStore(
                new IdmLightSimpleConnectionProvider(
                        new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build()), passwordService);
        Domain domain = h2Store.createDomain("sdn", true);
        User user = h2Store.createUser("test", "pass", domain.getDomainid(), "desc",
                "email", true, "SALT");

        user.setName("test");
        user = us.putUser(user);
        Assert.assertEquals(true, user != null);

        user.setEmail("Test@Test.com");
        user = us.putUser(user);

        user = new User();
        user.setName("test");
        user.setDomainid(domain.getDomainid());
        user = us.getUser(IDMStoreUtil.createUserid(user.getName(), user.getDomainid()));

        Assert.assertEquals("Test@Test.com", user.getEmail());
    }

    /*
     * @Test public void testCreateUserViaAPI() throws StoreException { Domain d
     * = StoreBuilder.createDomain("sdn",true);
     *
     * User user = new User(); user.setName("Hello"); user.setPassword("Hello");
     * user.setDomainid(d.getDomainid()); UserHandler h = new UserHandler();
     * h.createUser(null, user);
     *
     * User u = new User(); u.setName("Hello"); u.setDomainid(d.getDomainid());
     * UserStore us = new UserStore(); u =
     * us.getUser(IDMStoreUtil.createUserid(u.getName(),u.getDomainid()));
     *
     * Assert.assertEquals(true, u != null); }
     *
     * @Test public void testUpdateUserViaAPI() throws StoreException { Domain d
     * = StoreBuilder.createDomain("sdn",true);
     *
     * User user = new User(); user.setName("Hello"); user.setPassword("Hello");
     * user.setDomainid(d.getDomainid()); UserHandler h = new UserHandler();
     * h.createUser(null, user);
     *
     * user.setEmail("Hello@Hello.com"); user.setPassword("Test123");
     * h.putUser(null, user, "" + user.getUserid());
     *
     * UserStore us = new UserStore();
     *
     * User u = new User(); u.setName("Hello"); u.setDomainid(d.getDomainid());
     * u = us.getUser(IDMStoreUtil.createUserid(u.getName(),u.getDomainid()));
     *
     * Assert.assertEquals("Hello@Hello.com", u.getEmail());
     *
     * String hash = SHA256Calculator.getSHA256("Test123", u.getSalt());
     * Assert.assertEquals(u.getPassword(), hash); }
     *
     * @Test public void testUpdateUserRoleViaAPI() throws StoreException {
     * Domain d = StoreBuilder.createDomain("sdn",true); Role role1 =
     * StoreBuilder.createRole("temp1",d.getDomainid(),"Temp Testing role");
     * Role role2 =
     * StoreBuilder.createRole("temp2",d.getDomainid(),"Temp Testing role");
     *
     * User user = new User(); user.setName("Hello"); user.setPassword("Hello");
     * user.setDomainid(d.getDomainid());
     *
     * UserHandler h = new UserHandler(); h.createUser(null, user);
     *
     * user.setEmail("Hello@Hello.com"); user.setPassword("Test123");
     * h.putUser(null, user, user.getUserid());
     *
     * Grant g = new Grant(); g.setUserid(user.getUserid());
     * g.setDomainid(d.getDomainid()); g.setRoleid(role1.getRoleid());
     * GrantStore gs = new GrantStore(); g = gs.createGrant(g);
     *
     * Assert.assertEquals(true, g != null); Assert.assertEquals(g.getRoleid(),
     * role1.getRoleid());
     *
     * g = gs.deleteGrant(IDMStoreUtil.createGrantid(user.getUserid(),
     * d.getDomainid(), role1.getRoleid())); g.setRoleid(role2.getRoleid()); g =
     * gs.createGrant(g);
     *
     * Assert.assertEquals(true, g != null); Assert.assertEquals(g.getRoleid(),
     * role2.getRoleid());
     *
     * User u = new User(); u.setName("Hello"); u.setDomainid(d.getDomainid());
     * UserStore us = new UserStore(); u =
     * us.getUser(IDMStoreUtil.createUserid(u.getName(),u.getDomainid()));
     *
     * Assert.assertEquals("Hello@Hello.com", u.getEmail());
     *
     * String hash = SHA256Calculator.getSHA256("Test123", u.getSalt());
     * Assert.assertEquals(true, hash.equals(u.getPassword())); }
     */
}
