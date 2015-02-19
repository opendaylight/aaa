/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.junit.Assert.*;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.osgi.service.cm.ConfigurationException;

public class AuthenticationManagerTest {
    @Test
    public void testAuthenticationCrud() {
        AuthenticationService as = AuthenticationManager.instance();
        assertNotNull(as);
        Authentication auth = new AuthenticationBuilder().setUser("Bob").addRole("admin").addRole("guest").build();
        as.set(auth);
        assertEquals(auth, as.get());
        as.clear();
        assertEquals(null, as.get());
    }

    @Test
    public void testAuthEnabled() {
        AuthenticationManager as = AuthenticationManager.instance();
        assertFalse(as.isAuthEnabled());
        Dictionary<String, String> props = new Hashtable<>();
        props.put(AuthenticationManager.AUTH_ENABLED, "TrUe");
        try {
            as.updated(props);
            assertTrue(as.isAuthEnabled());
            props.put(AuthenticationManager.AUTH_ENABLED, "FaLsE");
            as.updated(props);
            assertFalse(as.isAuthEnabled());
        } catch(ConfigurationException ce) {
            fail("Unexpected exception: " + ce);
        }
    }

    @Test(expected=ConfigurationException.class)
    public void testUpdatedException() throws ConfigurationException {
        AuthenticationManager as = AuthenticationManager.instance();
        Dictionary<String, String> props = new Hashtable<>();
        props.put(AuthenticationManager.AUTH_ENABLED, "yes");
        as.updated(props);
    }
}