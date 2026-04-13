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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Oauth2ProxyHeaderFilterTest {
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
        final var roles = Collections.enumeration(List.of("role:global-admin, role:odl-application:admin",
            "role1", "role2"));
        final var tokenRoles = Set.of("global-admin", "odl-application:admin", "role1", "role2");
        doReturn(USER).when(request).getHeader(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        doReturn(roles).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);

        final var token = filter.createToken(request, response);

        assertEquals(new Oauth2ProxyHeaderToken(tokenRoles, USER), token);
    }

    /**
     * Test unauthorized when correct header is not present.
     */
    @Test
    void testOauth2ProxyTokenFilterUnauthorized() throws Exception {
        doReturn(null).when(request).getHeader(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertFalse(filter.onAccessDenied(request, response));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Test unauthorized when malformed user.
     */
    @Test
    void testOauth2ProxyTokenFilterMalformedUser() throws Exception {
        final var malformedUser = "\u0000 \n \r \t";

        doReturn(malformedUser).when(request).getHeader(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertFalse(filter.onAccessDenied(request, response));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Test unauthorized when too long user.
     */
    @Test
    void testOauth2ProxyTokenFilterLongUser() throws Exception {
        final var longUser = "a".repeat(129);

        doReturn(longUser).when(request).getHeader(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertFalse(filter.onAccessDenied(request, response));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    // Role parsing tests

    @Test
    void testNullOrEmptyRolesReturnsEmptySet() {
        assertTrue(Oauth2ProxyHeaderFilter.parseRoles(null).isEmpty());
        assertTrue(Oauth2ProxyHeaderFilter.parseRoles(Collections.enumeration(List.of())).isEmpty());
    }

    @Test
    void testNullOrEmptyHeadersAreIgnored() {
        final var headers = Collections.enumeration(Arrays.asList(null, "", "   ", "admin"));
        assertEquals(Set.of("admin"), Oauth2ProxyHeaderFilter.parseRoles(headers));
    }

    @Test
    void testSingleHeaderMultipleRoles() {
        final var headers = Collections.enumeration(List.of("admin, role:odl:user, role:moderator",
            "   role:admin   ,   user   ,role:org:odl:spaced-role  "));
        final var roles = Oauth2ProxyHeaderFilter.parseRoles(headers);
        assertEquals(Set.of("admin", "odl:user", "moderator", "user", "org:odl:spaced-role"), roles);
    }

    @Test
    void testRolesWithSpecialCharactersNotAccepted() {
        // \u0000 is a null byte, \n is newline, \t is tab, \r is carriage return.
        // All of these are ISO control characters and should be stripped.
        final var maliciousRole1 = "ad\u0000min";
        final var maliciousRole2 = "us\ner\r";
        final var maliciousRole3 = "mod\terator";
        final var maliciousRole4 = "<moderator>";
        final var maliciousRole5 = "{moderator}";
        final var roles = Oauth2ProxyHeaderFilter.parseRoles(Collections.enumeration(List.of(maliciousRole1,
            maliciousRole2, maliciousRole3, maliciousRole4, maliciousRole5)));

        assertEquals(Set.of(), roles);
    }

    @Test
    void testMaxHeaderLengthExceeded() {
        // MAX_HEADER_LENGTH is 4096

        final var headers = List.of("admin", "a".repeat(4097), "user");
        final var roles = Oauth2ProxyHeaderFilter.parseRoles(Collections.enumeration(headers));

        // The massive header should be skipped, but the others processed
        assertEquals(Set.of("admin", "user"), roles);
    }

    @Test
    void testMaxRoleLengthExceeded() {
        // MAX_ROLE_LENGTH is 4096

        final var headers = List.of("admin, " + "a".repeat(129), "user");
        final var roles = Oauth2ProxyHeaderFilter.parseRoles(Collections.enumeration(headers));

        // The massive header should be skipped, but the others processed
        assertEquals(Set.of("admin", "user"), roles);
    }

    @Test
    void testMaxRolesPerUserExceeded() {
        // MAX_ROLES_PER_USER is 200
        final var manyRolesHeader = new StringBuilder();
        for (int i = 0; i < 210; i++) {
            manyRolesHeader.append("role").append(i).append(",");
        }

        final var roles = Oauth2ProxyHeaderFilter
            .parseRoles(Collections.enumeration(List.of(manyRolesHeader.toString())));

        // It should truncate exactly at the 100 limit
        assertEquals(200, roles.size());
        assertTrue(roles.contains("role0"));
        assertTrue(roles.contains("role199"));
        assertFalse(roles.contains("role200"));
    }
}
