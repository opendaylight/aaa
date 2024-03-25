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
    private H2TokenStore h2TokenStore = H2TokenStore.createH2TokenWithIdleExpiration(36000);

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
        final var auth1 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        h2TokenStore.put(fooToken, auth1);
        final var auth2Token = "user2_token";
        final var auth2 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("user2").setUserId("4321").addRole("admin").setDomain("domain")
                .setClientId("clientID").build())
            .setExpiration(100).build();
        h2TokenStore.put(auth2Token, auth2);

        // Restart H2TokenStore
        h2TokenStore.close();
        h2TokenStore = H2TokenStore.createH2TokenWithLiveExpiration(3600);
        // Verify loaded persistent data
        assertEquals(auth1, h2TokenStore.get(fooToken));
        assertEquals(auth2, h2TokenStore.get(auth2Token));

        // Restart H2TokenStore
        h2TokenStore.close();
        h2TokenStore = H2TokenStore.createH2Token();
        // Verify loaded persistent data
        assertEquals(auth1, h2TokenStore.get(fooToken));
        assertEquals(auth2, h2TokenStore.get(auth2Token));
    }
}
