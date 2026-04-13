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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Oauth2ProxyHeaderFilterTest {
    private static final String PROXY_HEADER_USER = "X-Forwarded-User";
    private static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";
    private static final String USER = "user";

    private final Oauth2ProxyHeaderFilter filter = new Oauth2ProxyHeaderFilter();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    /**
     * Test if Oauth2ProxyHeaderFilter extract headers correctly.
     */
    @Test
    void testOauth2ProxyTokenFilter() {
        final var roles = Collections.enumeration(Stream.of("role:global-admin,role:odl-application:admin",
                "role1", "role2")
            .collect(Collectors.toList()));
        final var tokenRoles = List.of("role:global-admin,role:odl-application:admin", "role1", "role2");
        doReturn(USER).when(request).getHeader(PROXY_HEADER_USER);
        doReturn(roles).when(request).getHeaders(PROXY_HEADER_GROUPS);

        final var token = filter.createToken(request, response);

        assertEquals(new Oauth2ProxyHeaderToken(tokenRoles, USER), token);
    }

    /**
     * Test access forbidden when correct header is not present.
     */
    @Test
    void testOauth2ProxyTokenFilterForbidden() throws Exception {
        doReturn(null).when(request).getHeader(PROXY_HEADER_USER);
        assertFalse(filter.onAccessDenied(request, response));
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
