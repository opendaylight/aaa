/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.tokenauthrealm.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.aaa.api.Authentication;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AuthenticationManagerTest {
    @Mock
    private AuthenticationManager.Configuration configuration;

    private final AuthenticationManager authManager = new AuthenticationManager();

    @Test
    public void testAuthenticationCrudSameThread() {
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("Bob")
                .setUserId("1234").addRole("admin").addRole("guest").build()).build();

        assertNotNull(authManager);

        authManager.set(auth);
        assertEquals(auth, authManager.get());

        authManager.clear();
        assertNull(authManager.get());
    }

    @Test
    public void testAuthenticationCrudSpawnedThread() throws InterruptedException,
            ExecutionException {
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("Bob")
                .setUserId("1234").addRole("admin").addRole("guest").build()).build();

        authManager.set(auth);
        Future<Authentication> future = Executors.newSingleThreadExecutor().submit(new Worker());
        assertEquals(auth, future.get());

        authManager.clear();
        future = Executors.newSingleThreadExecutor().submit(new Worker());
        assertNull(future.get());
    }

    @Test
    public void testAuthenticationCrudSpawnedThreadPool() throws InterruptedException,
            ExecutionException {
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("Bob")
                .setUserId("1234").addRole("admin").addRole("guest").build()).build();

        authManager.set(auth);
        List<Future<Authentication>> fs = Executors.newFixedThreadPool(2).invokeAll(
                Arrays.asList(new Worker(), new Worker()));
        for (Future<Authentication> f : fs) {
            assertEquals(auth, f.get());
        }

        authManager.clear();
        fs = Executors.newFixedThreadPool(2).invokeAll(Arrays.asList(new Worker(), new Worker()));
        for (Future<Authentication> f : fs) {
            assertNull(f.get());
        }
    }

    @Test
    public void testUpdatedValid() {
        assertFalse(authManager.isAuthEnabled());

        doReturn(true).when(configuration).authEnabled();
        authManager.modified(configuration);
        assertTrue(authManager.isAuthEnabled());

        doReturn(false).when(configuration).authEnabled();
        authManager.modified(configuration);
        assertFalse(authManager.isAuthEnabled());
    }

    private class Worker implements Callable<Authentication> {
        @Override
        public Authentication call() throws Exception {
            return authManager.get();
        }
    }
}
