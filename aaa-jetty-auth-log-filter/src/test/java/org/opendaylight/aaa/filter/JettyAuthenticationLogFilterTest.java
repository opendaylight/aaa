/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JettyAuthenticationLogFilterTest {
    @Mock
    private HttpServletRequestWrapper mockServletRequest;
    @Mock
    private Response mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    @Mock
    private HttpSession mockHttpSession;
    @Mock
    private Request mockRequest;
    @Captor
    private ArgumentCaptor<UserAuthentication> userCaptor;

    private final JettyAuthenticationLogFilter logFilter = new JettyAuthenticationLogFilter();

    @BeforeEach
    void beforeEach() {
        // Prepare environment.
        doReturn(200).when(mockResponse).getStatus();
        doReturn("Basic YWRtaW46YWRtaW4=").when(mockRequest).getHeader(eq("Authorization"));
        doReturn(mockHttpSession).when(mockRequest).getSession(eq(false));
        doReturn(mockRequest).when(mockServletRequest).getRequest();
    }

    @AfterEach
    public void teardown() {
        logFilter.destroy();
    }

    @Test
    void testSettingAuthenticationIntoJettyRequest() throws Exception {
        // Test JettyAuthenticationLogFilter with Basic authentication only.
        logFilter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        verify(mockRequest).setAuthentication(userCaptor.capture());
        final var noSessionAuthentication = userCaptor.getValue();
        assertNotNull(noSessionAuthentication);
        assertEquals("admin", noSessionAuthentication.getUserIdentity().getUserPrincipal().getName());

        // Test JettyAuthenticationLogFilter with a session and an unauthenticated request.
        doReturn(Authentication.UNAUTHENTICATED).when(mockRequest).getAuthentication();
        logFilter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        verify(mockRequest, times(2)).setAuthentication(userCaptor.capture());
        final var sessionAuthentication = userCaptor.getValue();
        assertNotNull(sessionAuthentication);
        assertEquals(noSessionAuthentication.hashCode(), sessionAuthentication.hashCode());
        assertEquals(noSessionAuthentication, sessionAuthentication);

        // Test JettyAuthenticationLogFilter with a session restart due to unauthorized access.
        doReturn(401).when(mockResponse).getStatus();
        logFilter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        verify(mockRequest, times(3)).setAuthentication(userCaptor.capture());
        final var unauthorizedUserAuthentication = userCaptor.getValue();
        assertNotNull(unauthorizedUserAuthentication);
        assertNotSame(unauthorizedUserAuthentication, sessionAuthentication);
        assertEquals(unauthorizedUserAuthentication.getUserIdentity().getUserPrincipal().getName(),
            sessionAuthentication.getUserIdentity().getUserPrincipal().getName());
        assertEquals(1, logFilter.sessionMap().size());
    }
}
