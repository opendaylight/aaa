/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filter;

import static org.apache.shiro.subject.support.DefaultSubjectContext.PRINCIPALS_SESSION_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.security.Principal;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
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
    private static final String SESSION_USER = "sessionUser";
    private static final String BASIC_USER = "admin";

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
    @Mock
    private Principal mockPrincipal;
    @Mock
    private SimplePrincipalCollection mockSimplePrincipal;
    @Captor
    private ArgumentCaptor<UserAuthentication> userCaptor;

    private final JettyAuthenticationLogFilter logFilter = new JettyAuthenticationLogFilter();

    @BeforeEach
    void beforeEach() {
        // Prepare environment.
        doReturn(mockRequest).when(mockServletRequest).getRequest();
    }

    @AfterEach
    void teardown() {
        logFilter.destroy();
    }

    @Test
    void testFilterWithBasicAuth() throws Exception {
        // Setup environment only for Basic Auth without session.
        doReturn("Basic YWRtaW46YWRtaW4=").when(mockRequest).getHeader(eq("Authorization"));

        // Execute filter.
        logFilter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        // Verify correct Authentication user.
        verify(mockRequest).setAuthentication(userCaptor.capture());
        final var noSessionAuthentication = userCaptor.getValue();
        assertNotNull(noSessionAuthentication);
        assertEquals(BASIC_USER, noSessionAuthentication.getUserIdentity().getUserPrincipal().getName());
    }

    @Test
    void testFilterWithUnauthenticatedRequestWithBasicAuth() throws Exception {
        // Setup environment only for Basic Auth without session and Request with Unauthenticated value.
        doReturn("Basic YWRtaW46YWRtaW4=").when(mockRequest).getHeader(eq("Authorization"));
        doReturn(Authentication.UNAUTHENTICATED).when(mockRequest).getAuthentication();

        // Execute filter.
        logFilter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        // Verify correct Authentication user.
        verify(mockRequest).setAuthentication(userCaptor.capture());
        final var sessionAuthentication = userCaptor.getValue();
        assertNotNull(sessionAuthentication);
        assertEquals(BASIC_USER, sessionAuthentication.getUserIdentity().getUserPrincipal().getName());
    }

    @Test
    void testFilterWithSession() throws Exception {
        // Setup environment only for Session without Basic Authentication.
        doReturn(mockHttpSession).when(mockRequest).getSession(eq(false));
        doReturn(mockSimplePrincipal).when(mockHttpSession).getAttribute(eq(PRINCIPALS_SESSION_KEY));
        doReturn(mockPrincipal).when(mockSimplePrincipal).getPrimaryPrincipal();
        doReturn(SESSION_USER).when(mockPrincipal).getName();

        // Execute filter.
        logFilter.doFilter(mockServletRequest, mockResponse, mockFilterChain);

        // Verify correct Authentication user.
        verify(mockRequest).setAuthentication(userCaptor.capture());
        final var sessionAuthentication = userCaptor.getValue();
        assertNotNull(sessionAuthentication);
        assertEquals(SESSION_USER, sessionAuthentication.getUserIdentity().getUserPrincipal().getName());
    }
}
