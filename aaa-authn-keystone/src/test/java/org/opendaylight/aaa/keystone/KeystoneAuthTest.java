/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.keystone;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.aaa.keystone.KeystoneTokenAuthFilter.TOKEN;

import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class KeystoneAuthTest extends JerseyTest {

    private static final String RS_PACKAGES = "org.opendaylight.aaa.keystone";
    private static final String JERSEY_FILTERS = "com.sun.jersey.spi.container.ContainerRequestFilters";
    private static final String AUTH_FILTERS = KeystoneTokenAuthFilter.class.getName();
    static {
        ServiceLocator.INSTANCE.as = new AuthenticationService() {
            ThreadLocal<Authentication> ctx = new ThreadLocal<>();

            @Override
            public Authentication get() {
                return ctx.get();
            }

            @Override
            public void set(Authentication auth) {
                ctx.set(auth);
            }

            @Override
            public void clear() {
                ctx.remove();
            }
        };
    }

    public KeystoneAuthTest() throws Exception {
        super(new WebAppDescriptor.Builder(RS_PACKAGES).initParam(
                JERSEY_FILTERS, AUTH_FILTERS).build());
    }

    @Test()
    public void testGetFailed() {
        String resp = resource().path("test").get(String.class);
        assertEquals("failed", resp);
    }

    @Test
    public void testGet() {
        String resp = resource().path("test").header(TOKEN, "admin")
                .get(String.class);
        assertEquals("ok", resp);
    }

}
