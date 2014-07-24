/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.aaa.store.DefaultTokenStore.MAX_CACHED;
import static org.opendaylight.aaa.store.DefaultTokenStore.SECS_TO_IDLE;
import static org.opendaylight.aaa.store.DefaultTokenStore.SECS_TO_LIVE;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.dm.Component;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.api.Authentication;

public class DefaultTokenStoreTest {
    private static final String FOO_TOKEN = "foo_token";
    private final DefaultTokenStore dts = new DefaultTokenStore();

    @Before
    public void setup() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(MAX_CACHED, Integer.toString(3));
        props.put(SECS_TO_IDLE, Integer.toString(1));
        props.put(SECS_TO_LIVE, Integer.toString(1));
        Component c = mock(Component.class);
        when(c.getServiceProperties()).thenReturn(props);
        dts.init(c);
    }

    @After
    public void teardown() {
        dts.destroy();
    }

    @Test
    public void testCache() throws InterruptedException {
        Authentication auth = new AuthenticationBuilder().setUser("foo")
                .setUserId("1234").build();
        dts.put(FOO_TOKEN, auth);
        assertEquals(auth, dts.get(FOO_TOKEN));
        dts.delete(FOO_TOKEN);
        assertNull(dts.get(FOO_TOKEN));
        dts.put(FOO_TOKEN, auth);
        Thread.sleep(1200);
        assertNull(dts.get(FOO_TOKEN));
    }

}
