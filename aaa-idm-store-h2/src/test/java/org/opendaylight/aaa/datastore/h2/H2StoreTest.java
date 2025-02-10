/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;

class H2StoreTest {
    private static final List<Path> FILE_PATHS = List.of(Path.of("idmlight.db.mv.db"), Path.of("idmlight.db.trace.db"));

    private final PasswordHashService passwordService = new DefaultPasswordHashService();
    private H2Store h2Store;

    @BeforeAll
    static void beforeAll() throws Exception {
        cleanupFiles();
    }

    @AfterAll
    static void afterAll() throws Exception {
        cleanupFiles();
    }

    private static void cleanupFiles() throws Exception {
        for (var path : FILE_PATHS) {
            Files.deleteIfExists(path);
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
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
    void testCreateDefaultDomain() throws Exception {
        Domain domain = new Domain();
        DomainStore ds = new DomainStore(
                new IdmLightSimpleConnectionProvider(new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build()));
        domain.setName(IIDMStore.DEFAULT_DOMAIN);
        domain.setEnabled(true);
        domain = ds.createDomain(domain);
        assertNotNull(domain);
    }

    @Test
    void testCreateTempRole() throws Exception {
        final var role = h2Store.createRole("temp", "temp domain", "Temp Testing role");
        assertNotNull(role);
    }

    @Test
    void testCreateUser() throws Exception {
        final var user = h2Store.createUser("test", "pass", "domain", "desc", "email",true, "SALT");
        assertNotNull(user);
    }

    @Test
    void testCreateGrant() throws Exception {
        Domain domain = h2Store.createDomain("sdn", true);
        Role role = h2Store.createRole("temp", "temp domain", "Temp Testing role");
        User user = h2Store.createUser("test", "pass", "domain", "desc",
                "email", true, "SALT");
        Grant grant = h2Store.createGrant(domain.getDomainid(), user.getUserid(), role.getRoleid());
        assertNotNull(grant);
    }

    @Test
    void testUpdatingUserEmail() throws Exception {
        UserStore us = new UserStore(
                new IdmLightSimpleConnectionProvider(
                        new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build()), passwordService);
        Domain domain = h2Store.createDomain("sdn", true);
        User user = h2Store.createUser("test", "pass", domain.getDomainid(), "desc",
                "email", true, "SALT");

        user.setName("test");
        user = us.putUser(user);
        assertNotNull(user);

        user.setEmail("Test@Test.com");
        user = us.putUser(user);

        user = new User();
        user.setName("test");
        user.setDomainid(domain.getDomainid());
        user = us.getUser(IDMStoreUtil.createUserid(user.getName(), user.getDomainid()));

        assertEquals("Test@Test.com", user.getEmail());
    }
}
