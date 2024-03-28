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
 * Unit Test for AuthenticationTokenStore.
 *
 * @author mserngawy
 */
public class AuthenticationTokenStoreTest {
    private static AuthenticationTokenStore authTokenStore;

    @After
    public void teardown() throws Exception {
        if (authTokenStore != null) {
            authTokenStore.close();
            authTokenStore.destroyPersistentFiles();
        }
    }

    @Test
    public void testTokenStore() {
        initTokenStore(36000, 3600);
        final String fooToken = "foo_token";
        Authentication auth = new AuthenticationBuilder(
                new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        authTokenStore.put(fooToken, auth);
        assertEquals(auth, authTokenStore.get(fooToken));
        authTokenStore.delete(fooToken);
        assertNull(authTokenStore.get(fooToken));
    }

    @Test
    public void testTokenStorePersistence() {
        initTokenStore(36000, 3600);
        final var fooToken = "foo_token";
        final var auth1 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        authTokenStore.put(fooToken, auth1);
        final var auth2Token = "user2_token";
        final var auth2 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("user2").setUserId("4321").addRole("admin").setDomain("domain")
                .setClientId("clientID").build())
            .setExpiration(100).build();
        authTokenStore.put(auth2Token, auth2);
        // Restart TokenStore.
        authTokenStore.close();
        initTokenStore(36000, 3600);
        // Verify the loaded persistent data.
        assertEquals(auth1, authTokenStore.get(fooToken));
        assertEquals(auth2, authTokenStore.get(auth2Token));
    }

    @Test
    public void testTokenStoreTimeToLive() throws Exception {
        initTokenStore(1, 0);
        final var fooToken = "foo_token";
        final var auth1 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        authTokenStore.put(fooToken, auth1);
        Thread.sleep(550);
        assertEquals(auth1, authTokenStore.get(fooToken));
        // Verify that after expired token time is removed.
        Thread.sleep(550);
        assertNull(authTokenStore.get(fooToken));
    }

    @Test
    public void testTokenStoreTimeToIdle() throws Exception {
        initTokenStore(0, 1);
        final var fooToken = "foo_token";
        final var auth1 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        authTokenStore.put(fooToken, auth1);
        // Verify that the token is present.
        Thread.sleep(550);
        assertEquals(auth1, authTokenStore.get(fooToken));
        // Verify that the countdown is reset after the first access.
        Thread.sleep(550);
        assertEquals(auth1, authTokenStore.get(fooToken));
        // Verify that the token is removed after the TTI is reached.
        Thread.sleep(1100);
        assertNull(authTokenStore.get(fooToken));
    }

    @Test
    public void testTokenStoreCombinedTimeToIdleAndTimeToLive() throws Exception {
        initTokenStore(2, 1);
        final var fooToken = "foo_token";
        final var auth1 = new AuthenticationBuilder(
            new ClaimBuilder().setUser("foo").setUserId("1234").addRole("admin").build()).build();
        authTokenStore.put(fooToken, auth1);
        // Verify token is present after expiring TTI, but before TTL expiration.
        Thread.sleep(1100);
        assertEquals(auth1, authTokenStore.get(fooToken));
        // Reset the TTI expiration.
        Thread.sleep(550);
        assertEquals(auth1, authTokenStore.get(fooToken));
        // Verify that the token is present after TTL has expired, but before the TTI expiration after a reset.
        Thread.sleep(550);
        assertEquals(auth1, authTokenStore.get(fooToken));
        // Verify that the data is removed after the TTI expiration.
        Thread.sleep(1100);
        assertNull(authTokenStore.get(fooToken));
    }

    private static void initTokenStore(final long timeToLive, final long timeToIdle) {
        authTokenStore = new AuthenticationTokenStore(timeToLive, timeToIdle);
    }
}
