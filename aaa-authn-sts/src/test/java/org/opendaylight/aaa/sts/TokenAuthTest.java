/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.sts.TokenAuthFilter.UnauthorizedException;

public class TokenAuthTest extends JerseyTest {

    private static final String RS_PACKAGES = "org.opendaylight.aaa.sts";
    private static final String JERSEY_FILTERS = "com.sun.jersey.spi.container.ContainerRequestFilters";
    private static final String AUTH_FILTERS = TokenAuthFilter.class.getName();

    private static Authentication auth = new AuthenticationBuilder(new ClaimBuilder().setUserId(
            "1234").setUser("Bob").addRole("admin").addRole("user").setDomain("tenantX").build()).setExpiration(
            System.currentTimeMillis() + 1000).build();

    private static final String GOOD_TOKEN = "9b01b7cf-8a49-346d-8c47-6a61193e2b60";
    private static final String BAD_TOKEN = "9b01b7cf-8a49-346d-8c47-6a611badbeef";

    public TokenAuthTest() throws Exception {
        super(new WebAppDescriptor.Builder(RS_PACKAGES).initParam(JERSEY_FILTERS, AUTH_FILTERS)
                                                       .build());
    }

    @BeforeClass
    public static void init() {
        ServiceLocator.getInstance().setAuthenticationService(mock(AuthenticationService.class));
        ServiceLocator.getInstance().setTokenStore(mock(TokenStore.class));
        when(ServiceLocator.getInstance().getTokenStore().get(GOOD_TOKEN)).thenReturn(auth);
        when(ServiceLocator.getInstance().getTokenStore().get(BAD_TOKEN)).thenReturn(null);
        when(ServiceLocator.getInstance().getAuthenticationService().isAuthEnabled()).thenReturn(
                Boolean.TRUE);
    }

    @Test()
    public void testGetUnauthorized() {
        try {
            resource().path("test").get(String.class);
            fail("Shoulda failed with 401!");
        } catch (UniformInterfaceException e) {
            ClientResponse resp = e.getResponse();
            assertEquals(401, resp.getStatus());
            assertTrue(resp.getHeaders().get(UnauthorizedException.WWW_AUTHENTICATE)
                           .contains(UnauthorizedException.OPENDAYLIGHT));
        }
    }

    @Test
    public void testGet() {
        String resp = resource().path("test").header("Authorization", "Bearer " + GOOD_TOKEN)
                                .get(String.class);
        assertEquals("ok", resp);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetWithValidator() {
        try {
            // Mock a laxed tokenauth...
            TokenAuth ta = mock(TokenAuth.class);
            when(ta.validate(anyMap())).thenReturn(auth);
            ServiceLocator.getInstance().getTokenAuthCollection().add(ta);
            testGet();
        } finally {
            ServiceLocator.getInstance().getTokenAuthCollection().clear();
        }
    }

}
