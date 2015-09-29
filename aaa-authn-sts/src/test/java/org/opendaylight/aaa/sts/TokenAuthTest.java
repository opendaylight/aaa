/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;

public class TokenAuthTest extends JerseyTest {

    private static Authentication auth = new AuthenticationBuilder(new ClaimBuilder()
            .setUserId("1234").setUser("Bob").addRole("admin").addRole("user")
            .setDomain("tenantX").build())
            .setExpiration(System.currentTimeMillis() + 1000).build();

    private static final String GOOD_TOKEN = "9b01b7cf-8a49-346d-8c47-6a61193e2b60";
    private static final String BAD_TOKEN = "9b01b7cf-8a49-346d-8c47-6a611badbeef";

    public TokenAuthTest() {
    }

    @Path("/test")
    public static class JerseySpringResource {
        @GET
        @Path("/ok")
        public Response getOk()
        {
            return Response.ok().build();
        }
    }

    @Override
    protected DeploymentContext configureDeployment() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(JerseySpringResource.class);
        resourceConfig.register(TokenAuthFilter.class);
        return ServletDeploymentContext.forServlet(
                new ServletContainer(resourceConfig)).build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @BeforeClass
    public static void init() {
        ServiceLocator.INSTANCE.as = mock(AuthenticationService.class);
        ServiceLocator.INSTANCE.ts = mock(TokenStore.class);
        when(ServiceLocator.INSTANCE.ts.get(GOOD_TOKEN)).thenReturn(auth);
        when(ServiceLocator.INSTANCE.ts.get(BAD_TOKEN)).thenReturn(null);
        when(ServiceLocator.INSTANCE.as.isAuthEnabled()).thenReturn(
                Boolean.TRUE);
    }

    @Test
    public void testGetUnauthorized() {
        Response resp = target("test/ok")
                .request()
                .get();
        assertNotNull(resp);
        assertEquals(401, resp.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetWithValidator() {
        try {
            // Mock a laxed tokenauth...
            TokenAuth ta = mock(TokenAuth.class);
            when(ta.validate(anyMap())).thenReturn(auth);
            ServiceLocator.INSTANCE.ta.add(ta);
            Response resp = target("test/ok")
                    .request()
                    .header("Authorization", "Bearer " + GOOD_TOKEN)
                    .get();
            assertEquals(Response.Status.Family.SUCCESSFUL, resp.getStatusInfo().getFamily());
        } finally {
            ServiceLocator.INSTANCE.ta.clear();
        }
    }

}
