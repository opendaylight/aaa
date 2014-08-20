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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import javax.naming.AuthenticationException;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;

public class TokenEndpointTest {
    private static final long TOKEN_TIMEOUT_SECS = 10;
    private static final String CONTEXT = "/oauth2";
    private static final String DIRECT_AUTH = "grant_type=password&username=admin&password=odl&scope=pepsi&client_id=dlux&client_secret=secrete";
    private static final String REFRESH_TOKEN = "grant_type=refresh_token&refresh_token=whateverisgood&scope=pepsi";

    private static final Claim claim = new ClaimBuilder().setUser("bob")
            .setUserId("1234").addRole("admin").build();
    private final static ServletTester server = new ServletTester();

    @BeforeClass
    public static void init() throws Exception {
        // Set up server
        server.setContextPath(CONTEXT);

        // Add our servlet under test
        server.addServlet(TokenEndpoint.class, "/federation");
        server.addServlet(TokenEndpoint.class, "/revoke");
        server.addServlet(TokenEndpoint.class, "/token");

        // Add ClaimAuth filter
        server.addFilter(ClaimAuthFilter.class, "/federation", 0);

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
        when(ServiceLocator.INSTANCE.ts.tokenExpiration()).thenReturn(
                TOKEN_TIMEOUT_SECS);
    }

    @After
    public void teardown() {
        ServiceLocator.INSTANCE.ca.clear();
        ServiceLocator.INSTANCE.ta.clear();
    }

    @Test
    public void testFederation401() throws Exception {
        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setURI(CONTEXT + TokenEndpoint.FEDERATION_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(401, resp.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFederation() throws Exception {
        when(ServiceLocator.INSTANCE.ca.get(0).transform(anyMap())).thenReturn(
                claim);
        when(ServiceLocator.INSTANCE.is.getUserId(anyString())).thenReturn(
                "1234");
        when(ServiceLocator.INSTANCE.is.listDomains(anyString())).thenReturn(
                Arrays.asList("pepsi", "coke"));
        doThrow(AuthenticationException.class).when(ServiceLocator.INSTANCE.cs)
                .validate(isNull(String.class), isNull(String.class));

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setURI(CONTEXT + TokenEndpoint.FEDERATION_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(201, resp.getStatus());
        assertTrue(resp.getContent().contains("pepsi coke"));
    }

    @Test
    public void testCreateToken401() throws Exception {
        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent(DIRECT_AUTH);
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_GRANT_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(401, resp.getStatus());
    }

    @Test
    public void testCreateTokenWithPassword() throws Exception {
        when(
                ServiceLocator.INSTANCE.da.authenticate(
                        any(PasswordCredentials.class), anyString()))
                .thenReturn(claim);

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent(DIRECT_AUTH);
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_GRANT_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(201, resp.getStatus());
        assertTrue(resp.getContent().contains("expires_in\":10"));
        assertTrue(resp.getContent().contains("Bearer"));
    }

    @Test
    public void testCreateTokenWithRefreshToken() throws Exception {
        when(ServiceLocator.INSTANCE.ts.get(anyString())).thenReturn(
                new AuthenticationBuilder(claim).build());
        when(ServiceLocator.INSTANCE.is.listRoles(anyString(), anyString()))
                .thenReturn(Arrays.asList("admin", "user"));

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent(REFRESH_TOKEN);
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_GRANT_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(201, resp.getStatus());
        assertTrue(resp.getContent().contains("expires_in\":10"));
        assertTrue(resp.getContent().contains("Bearer"));
    }

    @Test
    public void testDeleteToken() throws Exception {
        when(ServiceLocator.INSTANCE.ts.delete("token_to_be_deleted"))
                .thenReturn(true);

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent("token_to_be_deleted");
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_REVOKE_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(server.getResponses(req.generate()));
        assertEquals(204, resp.getStatus());
    }

    @SuppressWarnings("unchecked")
    private static void mockServiceLocator() {
        ServiceLocator.INSTANCE.cs = mock(ClientService.class);
        ServiceLocator.INSTANCE.is = mock(IdMService.class);
        ServiceLocator.INSTANCE.as = mock(AuthenticationService.class);
        ServiceLocator.INSTANCE.ts = mock(TokenStore.class);
        ServiceLocator.INSTANCE.da = mock(CredentialAuth.class);
        ServiceLocator.INSTANCE.ca.add(mock(ClaimAuth.class));
        ServiceLocator.INSTANCE.ta.add(mock(TokenAuth.class));
    }
}
