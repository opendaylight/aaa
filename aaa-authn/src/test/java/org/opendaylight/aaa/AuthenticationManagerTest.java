/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.osgi.service.cm.ConfigurationException;

public class AuthenticationManagerTest {
    @Test
    public void testAuthenticationCrudSameThread() {
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("Bob")
                .setUserId("1234").addRole("admin").addRole("guest").build()).build();
        AuthenticationService as = AuthenticationManager.instance();

        assertNotNull(as);

        as.set(auth);
        assertEquals(auth, as.get());

        as.clear();
        assertNull(as.get());
    }

    @Test
    public void testAuthenticationCrudSpawnedThread() throws InterruptedException,
            ExecutionException {
        AuthenticationService as = AuthenticationManager.instance();
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("Bob")
                .setUserId("1234").addRole("admin").addRole("guest").build()).build();

        as.set(auth);
        Future<Authentication> future = Executors.newSingleThreadExecutor().submit(new Worker());
        assertEquals(auth, future.get());

        as.clear();
        future = Executors.newSingleThreadExecutor().submit(new Worker());
        assertNull(future.get());
    }

    @Test
    public void testAuthenticationCrudSpawnedThreadPool() throws InterruptedException,
            ExecutionException {
        AuthenticationService as = AuthenticationManager.instance();
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("Bob")
                .setUserId("1234").addRole("admin").addRole("guest").build()).build();

        as.set(auth);
        List<Future<Authentication>> fs = Executors.newFixedThreadPool(2).invokeAll(
                Arrays.asList(new Worker(), new Worker()));
        for (Future<Authentication> f : fs) {
            assertEquals(auth, f.get());
        }

        as.clear();
        fs = Executors.newFixedThreadPool(2).invokeAll(Arrays.asList(new Worker(), new Worker()));
        for (Future<Authentication> f : fs) {
            assertNull(f.get());
        }
    }

    @Test
    public void testUpdatedValid() throws ConfigurationException {
        Dictionary<String, String> props = new Hashtable<>();
        AuthenticationManager as = AuthenticationManager.instance();

        assertFalse(as.isAuthEnabled());

        props.put(AuthenticationManager.AUTH_ENABLED, "TrUe");
        as.updated(props);
        assertTrue(as.isAuthEnabled());

        props.put(AuthenticationManager.AUTH_ENABLED, "FaLsE");
        as.updated(props);
        assertFalse(as.isAuthEnabled());
    }

    @Test
    public void testUpdatedNullProperty() throws ConfigurationException {
        AuthenticationManager as = AuthenticationManager.instance();

        assertFalse(as.isAuthEnabled());
        as.updated(null);
        assertFalse(as.isAuthEnabled());
    }

    @Test(expected = ConfigurationException.class)
    public void testUpdatedInvalidValue() throws ConfigurationException {
        AuthenticationManager as = AuthenticationManager.instance();
        Dictionary<String, String> props = new Hashtable<>();

        props.put(AuthenticationManager.AUTH_ENABLED, "yes");
        as.updated(props);
    }

    @Test(expected = ConfigurationException.class)
    public void testUpdatedInvalidKey() throws ConfigurationException {
        AuthenticationManager as = AuthenticationManager.instance();
        Dictionary<String, String> props = new Hashtable<>();

        props.put("Invalid Key", "true");
        as.updated(props);
    }

    private class Worker implements Callable<Authentication> {
        @Override
        public Authentication call() throws Exception {
            AuthenticationService as = AuthenticationManager.instance();
            return as.get();
        }
    }
}
