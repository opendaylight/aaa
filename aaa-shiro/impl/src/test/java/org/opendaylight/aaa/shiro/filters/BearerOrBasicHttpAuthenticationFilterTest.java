/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Test;

public class BearerOrBasicHttpAuthenticationFilterTest {

    private static final String FAKE_JWT = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.sig";

    private final BearerOrBasicHttpAuthenticationFilter filter = new BearerOrBasicHttpAuthenticationFilter();

    @After
    public void cleanUp() {
        ThreadContext.unbindSubject();
    }

    // ---- isLoginAttempt(String) -------------------------------------------------

    @Test
    public void testBearerHeaderIsLoginAttempt() {
        assertTrue(filter.isLoginAttempt("Bearer " + FAKE_JWT));
    }

    @Test
    public void testBearerHeaderCaseInsensitiveIsLoginAttempt() {
        assertTrue(filter.isLoginAttempt("BEARER " + FAKE_JWT));
        assertTrue(filter.isLoginAttempt("bearer " + FAKE_JWT));
    }

    @Test
    public void testBasicHeaderIsLoginAttempt() {
        final var encoded = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        assertTrue(filter.isLoginAttempt("Basic " + encoded));
    }

    @Test
    public void testUnknownSchemeIsNotLoginAttempt() {
        assertFalse(filter.isLoginAttempt("Digest abc123"));
    }

    @Test
    public void testNonAuthorizationValueIsNotLoginAttempt() {
        assertFalse(filter.isLoginAttempt("randomvalue"));
    }

    // ---- createToken -----------------------------------------------------------

    @Test
    public void testCreateTokenReturnsBearerTokenForBearerHeader() {
        final var request = mockRequestWithHeader("Bearer " + FAKE_JWT);
        final var response = mock(HttpServletResponse.class);

        final var token = filter.createToken(request, response);

        assertTrue(token instanceof BearerToken);
        assertEquals(FAKE_JWT, ((BearerToken) token).getToken());
    }

    @Test
    public void testCreateTokenBearerTrimsLeadingAndTrailingWhitespace() {
        final var request = mockRequestWithHeader("Bearer  " + FAKE_JWT + " ");
        final var response = mock(HttpServletResponse.class);

        final var token = filter.createToken(request, response);

        assertTrue(token instanceof BearerToken);
        assertEquals(FAKE_JWT, ((BearerToken) token).getToken());
    }

    @Test
    public void testCreateTokenReturnsUsernamePasswordTokenForBasicHeader() {
        final var encoded = Base64.getEncoder().encodeToString("alice:secret".getBytes(StandardCharsets.UTF_8));
        final var request = mockRequestWithHeader("Basic " + encoded);
        final var response = mock(HttpServletResponse.class);

        final var token = filter.createToken(request, response);

        assertTrue(token instanceof UsernamePasswordToken);
        final var upToken = (UsernamePasswordToken) token;
        assertEquals("alice", upToken.getUsername());
        assertEquals("secret", new String(upToken.getPassword()));
    }

    // ---- sendChallenge ---------------------------------------------------------

    @Test
    public void testSendChallengeSetsBothWwwAuthenticateHeaders() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        final var result = filter.sendChallenge(request, response);

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setHeader("WWW-Authenticate", "Bearer realm=\"application\"");
        verify(response).addHeader("WWW-Authenticate", "Basic realm=\"application\"");
    }

    // ---- pre-authenticated pass-through ----------------------------------------

    @Test
    public void testAlreadyAuthenticatedSubjectPassesThrough() throws Exception {
        final var subject = mock(Subject.class);
        when(subject.isAuthenticated()).thenReturn(true);
        ThreadContext.bind(subject);

        final var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        final var response = mock(HttpServletResponse.class);

        assertTrue(filter.onPreHandle(request, response, new String[0]));
    }

    @Test
    public void testUnauthenticatedSubjectDenied() throws Exception {
        final var subject = mock(Subject.class);
        when(subject.isAuthenticated()).thenReturn(false);
        ThreadContext.bind(subject);

        final var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        final var response = mock(HttpServletResponse.class);

        assertFalse(filter.onPreHandle(request, response, new String[0]));
    }

    // ---- helpers ---------------------------------------------------------------

    private static HttpServletRequest mockRequestWithHeader(final String headerValue) {
        final var request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(headerValue);
        return request;
    }
}
