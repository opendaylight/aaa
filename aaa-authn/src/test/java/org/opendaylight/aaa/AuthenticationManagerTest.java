/*
 * Copyright (c) 2014-2015 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;

public class AuthenticationManagerTest {
    private static final AuthenticationService as = AuthenticationManager
            .instance();

    @Before
    public void tearDown() {
        as.set(null);
    }

    @Test
    public void testSameThread() {
        assertEquals(setAuth(), as.get());
    }

    @Test
    public void testSpawnedThread() throws InterruptedException,
            ExecutionException {
        Authentication auth = setAuth();
        Future<Authentication> f = Executors.newSingleThreadExecutor().submit(
                new Worker());
        assertEquals(auth, f.get());
    }

    @Test
    public void testSpawnedThreadPool() throws InterruptedException,
            ExecutionException {
        Authentication auth = setAuth();
        List<Future<Authentication>> fs = Executors.newFixedThreadPool(2)
                .invokeAll(Arrays.asList(new Worker(), new Worker()));
        for (Future<Authentication> f : fs)
            assertEquals(auth, f.get());
    }

    private class Worker implements Callable<Authentication> {
        @Override
        public Authentication call() throws Exception {
            return as.get();
        }
    }

    private Authentication setAuth(String name) {
        Authentication auth = new AuthenticationBuilder().setUser(name).setUserId("123")
                .addRole("admin").addRole("guest").build();
        as.set(auth);
        return auth;
    }

    private Authentication setAuth() {
        return setAuth("Bob");
    }

}
