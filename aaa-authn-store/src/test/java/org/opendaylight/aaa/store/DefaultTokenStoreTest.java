/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.opendaylight.aaa.store.DefaultTokenStore.MAX_CACHED_DISK;
import static org.opendaylight.aaa.store.DefaultTokenStore.MAX_CACHED_MEMORY;
import static org.opendaylight.aaa.store.DefaultTokenStore.SECS_TO_IDLE;
import static org.opendaylight.aaa.store.DefaultTokenStore.SECS_TO_LIVE;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.felix.dm.Component;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.osgi.service.cm.ConfigurationException;

public class DefaultTokenStoreTest {
    private static final String FOO_TOKEN = "foo_token";
    private final DefaultTokenStore dts = new DefaultTokenStore();
    private static final Dictionary<String, String> CONFIG = new Hashtable<>();

    static {
        CONFIG.put(MAX_CACHED_MEMORY, Long.toString(3));
        CONFIG.put(MAX_CACHED_DISK, Long.toString(3));
        CONFIG.put(SECS_TO_IDLE, Long.toString(1));
        CONFIG.put(SECS_TO_LIVE, Long.toString(1));
    }

    @Before
    public void setup() throws ConfigurationException {
        dts.init(mock(Component.class));
        dts.updated(CONFIG);
    }

    @After
    public void teardown() {
        dts.destroy();
    }

    @Test
    public void testCache() throws InterruptedException {
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUser("foo")
                                                                          .setUserId("1234")
                                                                          .addRole("admin").build()).build();
        dts.put(FOO_TOKEN, auth);
        assertEquals(auth, dts.get(FOO_TOKEN));
        dts.delete(FOO_TOKEN);
        assertNull(dts.get(FOO_TOKEN));
        dts.put(FOO_TOKEN, auth);
        Thread.sleep(1200);
        assertNull(dts.get(FOO_TOKEN));
    }
}
