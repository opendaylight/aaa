/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Authentication;

/**
 * Unit Test for H2TokenStore.
 *
 * @author mserngawy
 */
public class H2TokenStoreTest {

    private final H2TokenStore h2TokenStore = new H2TokenStore();

    @After
    public void teardown() throws Exception {
        h2TokenStore.close();
    }

    @Test
    public void testTokenStore() throws InterruptedException {
        final String fooToken = "foo_token";
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder()
                                                            .setUser("foo")
                                                            .setUserId("1234")
                                                            .addRole("admin").build()).build();
        h2TokenStore.put(fooToken, auth);
        assertEquals(auth, h2TokenStore.get(fooToken));
        h2TokenStore.delete(fooToken);
        assertNull(h2TokenStore.get(fooToken));
        Map<String, Object> configParameters = new HashMap<>();
        configParameters.put(h2TokenStore.SECS_TO_LIVE, Long.toString(2));
        h2TokenStore.updateConfigParameter(configParameters);
        h2TokenStore.put(fooToken, auth);
        Thread.sleep(3000);
        assertNull(h2TokenStore.get(fooToken));
    }

    /**
     * Non-regression for NPE.
     *
     * <code>java.lang.NullPointerException
     *   at org.opendaylight.aaa.h2.persistence.H2TokenStore.updateConfigParameter(H2TokenStore.java:85)
     *   at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)[:1.8.0_121]
     *   at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)[:1.8.0_121]
     *   at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)[:1.8.0_121]
     *   at java.lang.reflect.Method.invoke(Method.java:498)[:1.8.0_121]
     *   at org.apache.aries.blueprint.compendium.cm.CmManagedProperties.inject(CmManagedProperties.java:258)[13:org.apache.aries.blueprint.cm:1.0.8]
     *   at org.apache.aries.blueprint.compendium.cm.CmManagedProperties.updated(CmManagedProperties.java:167)[13:org.apache.aries.blueprint.cm:1.0.8]
     *   at org.apache.aries.blueprint.compendium.cm.CmManagedProperties.updated(CmManagedProperties.java:157)[13:org.apache.aries.blueprint.cm:1.0.8]
     *   at org.apache.aries.blueprint.compendium.cm.ManagedObjectManager$ConfigurationWatcher$1.run(ManagedObjectManager.java:81)[13:org.apache.aries.blueprint.cm:1.0.8]</code>
     */
    @Test
    public void updateConfigParameter_nullArg() {
        h2TokenStore.updateConfigParameter(null);
    }

}
