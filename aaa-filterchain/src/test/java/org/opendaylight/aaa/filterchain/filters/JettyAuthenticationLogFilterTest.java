/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filterchain.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class JettyAuthenticationLogFilterTest {
    private static JettyAuthenticationLogFilter customFilterAdapter;

    private final HttpServletRequestWrapper mockServletRequest = mock(HttpServletRequestWrapper.class);
    private final Response mockResponse = mock(Response.class);
    private final FilterChain mockFilterChain = mock(FilterChain.class);

    @BeforeAll
    public static void setUp() {
        customFilterAdapter = new JettyAuthenticationLogFilter();
    }

    @AfterAll
    public static void teardown() {
        customFilterAdapter.destroy();
    }

    @Test
    public void testSettingAuthenticationIntoJettyRequest() throws Exception {
        // Prepare environment.
        doReturn(200).when(mockResponse).getStatus();
        final var mockRequest = mock(Request.class);
        doReturn("Basic YWRtaW46YWRtaW4=").when(mockRequest).getHeader(eq("Authorization"));
        final var mockHttpSession = mock(HttpSession.class);
        doReturn(mockHttpSession).when(mockRequest).getSession();
        doReturn(mockRequest).when(mockServletRequest).getRequest();

        // Test JettyAuthenticationLogFilter with Basic authentication only.
        customFilterAdapter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        final var UserCaptor = ArgumentCaptor.forClass(UserAuthentication.class);
        verify(mockRequest).setAuthentication(UserCaptor.capture());
        final var noSessionAuthentication = UserCaptor.getValue();
        assertNotNull(noSessionAuthentication);
        assertEquals("admin", noSessionAuthentication.getUserIdentity().getUserPrincipal().getName());

        // Test JettyAuthenticationLogFilter with a session and an unauthenticated request.
        doReturn(Authentication.UNAUTHENTICATED).when(mockRequest).getAuthentication();
        customFilterAdapter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        verify(mockRequest, times(2)).setAuthentication(UserCaptor.capture());
        final var sessionAuthentication = UserCaptor.getValue();
        assertNotNull(sessionAuthentication);
        assertEquals(noSessionAuthentication.hashCode(), sessionAuthentication.hashCode());
        assertEquals(noSessionAuthentication, sessionAuthentication);

        // Test JettyAuthenticationLogFilter with a session restart due to unauthorized access.
        doReturn(401).when(mockResponse).getStatus();
        customFilterAdapter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        verify(mockRequest, times(3)).setAuthentication(UserCaptor.capture());
        final var unauthorizedUserAuthentication = UserCaptor.getValue();
        assertNotNull(unauthorizedUserAuthentication);
        assertNotSame(unauthorizedUserAuthentication, sessionAuthentication);
        assertEquals(unauthorizedUserAuthentication.getUserIdentity().getUserPrincipal().getName(),
            sessionAuthentication.getUserIdentity().getUserPrincipal().getName());
        assertEquals(1, customFilterAdapter.sessionMap().size());
    }
}
