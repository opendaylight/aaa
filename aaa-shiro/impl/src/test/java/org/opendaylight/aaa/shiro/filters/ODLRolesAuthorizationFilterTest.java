/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ODLRolesAuthorizationFilterTest {

    @AfterEach
    void unbindSubject() {
        ThreadContext.unbindSubject();
    }

    @Test
    void testAuthenticatedUserWithoutRoleGetsForbidden() throws Exception {
        final var subject = mock(Subject.class);
        when(subject.getPrincipal()).thenReturn("alice");
        ThreadContext.bind(subject);

        final var filter = new ODLRolesAuthorizationFilter();
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        assertFalse(filter.onAccessDenied(request, response));
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    /**
     * Test that redirect is called in case of not authenticated user.
     */
    @Test
    void testNotAuthenticatedUser() throws Exception {
        final var session = mock(Session.class);
        final var subject = mock(Subject.class);
        when(subject.getPrincipal()).thenReturn(null);
        when(subject.getSession()).thenReturn(session);
        ThreadContext.bind(subject);

        final var filter = new ODLRolesAuthorizationFilter();
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        assertFalse(filter.onAccessDenied(request, response));
        verify(response).sendRedirect(any());
    }
}
