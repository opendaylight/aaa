/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.TreeSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A unit test for federation endpoint.
 *
 * @author liemmn
 *
 */
@Ignore
public class FederationEndpointTest {
    private static final long TOKEN_TIMEOUT_SECS = 10;
    private static final String CONTEXT = "/oauth2/federation";

    private final static ServletTester server = new ServletTester();
    private static final Claim claim = new ClaimBuilder().setUser("bob").setUserId("1234")
            .addRole("admin").build();

    @BeforeClass
    public static void init() throws Exception {
        // Set up server
        server.setContextPath(CONTEXT);

        // Add our servlet under test
        server.addServlet(FederationEndpoint.class, "/*");

        // Add ClaimAuth filter
        server.addFilter(ClaimAuthFilter.class, "/*", 0);

        // Let's do dis
        server.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        server.stop();
    }

    @Before
    public void setup() {
        mockServiceLocator();
        when(ServiceLocator.getInstance().getTokenStore().tokenExpiration()).thenReturn(
                TOKEN_TIMEOUT_SECS);
    }

    @After
    public void teardown() {
        ServiceLocator.getInstance().getClaimAuthCollection().clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFederation() throws Exception {
        when(ServiceLocator.getInstance().getClaimAuthCollection().get(0).transform(anyMap()))
                .thenReturn(claim);
        when(ServiceLocator.getInstance().getIdmService().listDomains(anyString())).thenReturn(
                Arrays.asList("pepsi", "coke"));

        // Configure secure port (of zero)
        FederationConfiguration.instance = mock(FederationConfiguration.class);
        when(FederationConfiguration.instance.secureProxyPorts()).thenReturn(
                new TreeSet<Integer>(Arrays.asList(0)));

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setURI(CONTEXT + "/");
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(201, resp.getStatus());
        String content = resp.getContent();
        assertTrue(content.contains("pepsi coke"));
    }

    @Test
    public void testFederationUnconfiguredProxyPort() throws Exception {
        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setURI(CONTEXT + "/");
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(401, resp.getStatus());
    }

    private static void mockServiceLocator() {
        ServiceLocator.getInstance().setIdmService(mock(IdMService.class));
        ServiceLocator.getInstance().setTokenStore(mock(TokenStore.class));
        ServiceLocator.getInstance().getClaimAuthCollection().add(mock(ClaimAuth.class));
    }
}
