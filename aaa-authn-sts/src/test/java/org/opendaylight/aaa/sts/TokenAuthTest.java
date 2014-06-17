/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.WebAppDescriptor;

public class TokenAuthTest extends AbstractAuthTest {

    private static final String RS_PACKAGES = "org.opendaylight.aaa.sts";
    private static final String JERSEY_FILTERS = "com.sun.jersey.spi.container.ContainerRequestFilters";
    private static final String AUTH_FILTERS = TokenAuthFilter.class.getName();

    public TokenAuthTest() throws Exception {
        super(new WebAppDescriptor.Builder(RS_PACKAGES).initParam(
                JERSEY_FILTERS, AUTH_FILTERS).build());
    }

    @Test()
    public void testGetUnauthorized() {
        try {
            resource().path("test").get(String.class);
            fail("Shoulda failed with 401!");
        } catch (UniformInterfaceException e) {
            assertEquals(401, e.getResponse().getStatus());
        }
    }

    @Test
    public void testGet() {
        String resp = resource().path("test")
                .header("Authorization", "Bearer " + GOOD_TOKEN)
                .get(String.class);
        assertEquals("ok", resp);
    }

}
