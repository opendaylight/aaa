/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.tokenauthrealm.auth.AuthenticationBuilder;
import org.opendaylight.aaa.tokenauthrealm.auth.ClaimBuilder;

/**
 * Unit Test for H2TokenStore.
 *
 * @author mserngawy
 */
public class H2TokenStoreTest {
    private H2TokenStore h2TokenStore = new H2TokenStore(36000, 3600);

    @After
    public void teardown() throws Exception {
        h2TokenStore.close();
        h2TokenStore.destroyPersistentFiles();
    }

    @Test
    public void testTokenStore() throws InterruptedException {
        final String fooToken = "foo_token";
        Authentication auth = new AuthenticationBuilder(
                new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        h2TokenStore.put(fooToken, auth);
        assertEquals(auth, h2TokenStore.get(fooToken));
        h2TokenStore.delete(fooToken);
        assertNull(h2TokenStore.get(fooToken));
    }

    @Test
    public void testTokenStorePersistence() {
        final var fooToken = "foo_token";
        final var auth = new AuthenticationBuilder(
            new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        h2TokenStore.put(fooToken, auth);
        // Restart H2TokenStore
        h2TokenStore.close();
        h2TokenStore = new H2TokenStore(36000, 3600);
        // Verify loaded persistent data
        assertEquals(auth, h2TokenStore.get(fooToken));
    }
}
