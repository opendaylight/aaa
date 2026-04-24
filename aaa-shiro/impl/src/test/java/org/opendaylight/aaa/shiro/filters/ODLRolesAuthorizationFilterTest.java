/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Test;

public class ODLRolesAuthorizationFilterTest {

    @After
    public void unbindSubject() {
        ThreadContext.unbindSubject();
    }

    @Test
    public void testAuthenticatedUserWithoutRoleGetsForbidden() throws Exception {
        final var subject = mock(Subject.class);
        when(subject.getPrincipal()).thenReturn("alice");
        ThreadContext.bind(subject);

        final var filter = new ODLRolesAuthorizationFilter();
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        assertFalse(filter.onAccessDenied(request, response));
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
