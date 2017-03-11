/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A unit test for token endpoint.
 *
 * @author liemmn
 *
 */
@Ignore
public class TokenEndpointTest {
    private static final long TOKEN_TIMEOUT_SECS = 10;
    private static final String CONTEXT = "/oauth2";
    private static final String DIRECT_AUTH =
            "grant_type=password&username=admin&password=admin&scope=pepsi&client_id=dlux&client_secret=secrete";
    private static final String REFRESH_TOKEN = "grant_type=refresh_token&refresh_token=whateverisgood&scope=pepsi";

    private static final Claim CLAIM = new ClaimBuilder().setUser("bob").setUserId("1234")

                                                         .addRole("admin").build();
    private static final ServletTester SERVER = new ServletTester();

    @BeforeClass
    public static void init() throws Exception {
        // Set up SERVER
        SERVER.setContextPath(CONTEXT);

        // Add our servlet under test
        SERVER.addServlet(TokenEndpoint.class, "/revoke");
        SERVER.addServlet(TokenEndpoint.class, "/token");

        // Let's do dis
        SERVER.start();
    }

    @AfterClass
    public static void shutdown() throws Exception {
        SERVER.stop();
    }

    @Before
    public void setup() {
        mockServiceLocator();
        when(ServiceLocator.getInstance().getTokenStore().tokenExpiration()).thenReturn(
                TOKEN_TIMEOUT_SECS);
    }

    @After
    public void teardown() {
        ServiceLocator.getInstance().getTokenAuthCollection().clear();
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
        resp.parse(SERVER.getResponses(req.generate()));
        assertEquals(401, resp.getStatus());
    }

    @Test
    public void testCreateTokenWithPassword() throws Exception {
        when(
                ServiceLocator.getInstance().getCredentialAuth()
                              .authenticate(any(PasswordCredentials.class))).thenReturn(CLAIM);

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent(DIRECT_AUTH);
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_GRANT_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(SERVER.getResponses(req.generate()));
        assertEquals(201, resp.getStatus());
        assertTrue(resp.getContent().contains("expires_in\":10"));
        assertTrue(resp.getContent().contains("Bearer"));
    }

    @Test
    public void testCreateTokenWithRefreshToken() throws Exception {
        when(ServiceLocator.getInstance().getTokenStore().get(anyString())).thenReturn(
                new AuthenticationBuilder(CLAIM).build());
        when(ServiceLocator.getInstance().getIdmService().listRoles(anyString(), anyString())).thenReturn(
                Arrays.asList("admin", "user"));

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent(REFRESH_TOKEN);
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_GRANT_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(SERVER.getResponses(req.generate()));
        assertEquals(201, resp.getStatus());
        assertTrue(resp.getContent().contains("expires_in\":10"));
        assertTrue(resp.getContent().contains("Bearer"));
    }

    @Test
    public void testDeleteToken() throws Exception {
        when(ServiceLocator.getInstance().getTokenStore().delete("token_to_be_deleted")).thenReturn(
                true);

        HttpTester req = new HttpTester();
        req.setMethod("POST");
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setContent("token_to_be_deleted");
        req.setURI(CONTEXT + TokenEndpoint.TOKEN_REVOKE_ENDPOINT);
        req.setVersion("HTTP/1.0");

        HttpTester resp = new HttpTester();
        resp.parse(SERVER.getResponses(req.generate()));
        assertEquals(204, resp.getStatus());
    }

    @SuppressWarnings("unchecked")
    private static void mockServiceLocator() {
        ServiceLocator.getInstance().setClientService(mock(ClientService.class));
        ServiceLocator.getInstance().setIdmService(mock(IdMService.class));
        ServiceLocator.getInstance().setAuthenticationService(mock(AuthenticationService.class));
        ServiceLocator.getInstance().setTokenStore(mock(TokenStore.class));
        ServiceLocator.getInstance().setCredentialAuth(mock(CredentialAuth.class));
        ServiceLocator.getInstance().getTokenAuthCollection().add(mock(TokenAuth.class));
    }
}
