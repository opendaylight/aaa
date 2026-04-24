/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.util.WebUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

@ExtendWith(MockitoExtension.class)
class CombinedOauth2AuthenticationFilterTest {
    private static final String FAKE_JWT = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.sig";

    private final CombinedOauth2AuthenticationFilter filter = new CombinedOauth2AuthenticationFilter();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Subject subject;

    @AfterEach
    void cleanUp() {
        ThreadContext.unbindSubject();
    }

    /**
     * Tests that combined filter recognizes an {@code Authorization: Bearer} JWT token as a login attempt.
     */
    @Test
    void testBearerHeaderIsLoginAttempt() {
        doReturn(null).when(request).getHeader("X-Forwarded-User");
        doReturn("Bearer " + FAKE_JWT).when(request).getHeader("Authorization");
        assertTrue(filter.isLoginAttempt(request, response));
    }

    /**
     * Tests that the filter recognizes an {@code X-Forwarded-User} header as a login attempt.
     */
    @Test
    void testProxyHeaderIsLoginAttempt() {
        doReturn("alice").when(request).getHeader("X-Forwarded-User");
        assertTrue(filter.isLoginAttempt(request, response));
    }

    /**
     * Tests that a request carrying neither {@code X-Forwarded-User} nor {@code Authorization: Bearer}
     * is not treated as a login attempt.
     */
    @Test
    void testNoCredentialIsNotLoginAttempt() {
        doReturn(null).when(request).getHeader("Authorization");
        doReturn(null).when(request).getHeader("X-Forwarded-User");
        assertFalse(filter.isLoginAttempt(request, response));
    }

    /**
     * Tests that {@code createToken} returns a {@link BearerToken} containing the raw JWT when
     * only an {@code Authorization: Bearer} header is present.
     */
    @Test
    void testCreateTokenReturnsBearerTokenForBearerHeader() {
        doReturn(null).when(request).getHeader("X-Forwarded-User");
        doReturn("Bearer " + FAKE_JWT).when(request).getHeader("Authorization");

        final var token = filter.createToken(request, response);

        assertInstanceOf(BearerToken.class, token);
        assertEquals(FAKE_JWT, ((BearerToken) token).getToken());
    }

    /**
     * Tests that {@code createToken} returns an {@link Oauth2ProxyHeaderToken} carrying the
     * forwarded user and groups when {@code X-Forwarded-User} and {@code X-Forwarded-Groups}
     * headers are present.
     */
    @Test
    void testCreateTokenReturnsOauth2ProxyTokenForProxyHeaders() {
        final var roles = List.of("role:admin", "role:viewer");
        final var groups = Collections.enumeration(roles);
        doReturn("alice").when(request).getHeader("X-Forwarded-User");
        doReturn(groups).when(request).getHeaders("X-Forwarded-Groups");

        final var token = filter.createToken(request, response);

        assertInstanceOf(Oauth2ProxyHeaderToken.class, token);
        final var proxyToken = (Oauth2ProxyHeaderToken) token;
        assertEquals("alice", proxyToken.user());
        assertEquals(roles, proxyToken.groups());
    }

    /**
     * Tests that unauthenticated responses carry a {@code WWW-Authenticate: Bearer} challenge
     * header with the correct realm value.
     */
    @Test
    void testSendChallengeSetsBearerWwwAuthenticate() {
        final var httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final var authcHeader = filter.getAuthcScheme() + " realm=\"" + filter.getApplicationName() + "\"";
        httpResponse.setHeader("WWW-Authenticate", authcHeader);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setHeader("WWW-Authenticate", "Bearer realm=\"application\"");
    }

    /**
     * Tests that a request whose {@link Subject} is already authenticated passes through without
     * re-authentication ({@code onPreHandle} returns {@code true}).
     */
    @Test
    void testAlreadyAuthenticatedSubjectPassesThrough() throws Exception {
        doReturn(true).when(subject).isAuthenticated();
        doReturn(ODLPrincipalImpl.createODLPrincipal("admin", "odl", "admin"))
            .when(subject).getPrincipal();
        ThreadContext.bind(subject);
        doReturn("GET").when(request).getMethod();
        assertTrue(filter.onPreHandle(request, response, new String[0]));
    }

    /**
     * Tests that a request whose {@link Subject} is not authenticated is denied
     * ({@code onPreHandle} returns {@code false}).
     */
    @Test
    void testUnauthenticatedSubjectDenied() throws Exception {
        doReturn(false).when(subject).isAuthenticated();
        ThreadContext.bind(subject);
        doReturn("GET").when(request).getMethod();
        assertFalse(filter.onPreHandle(request, response, new String[0]));
    }
}
