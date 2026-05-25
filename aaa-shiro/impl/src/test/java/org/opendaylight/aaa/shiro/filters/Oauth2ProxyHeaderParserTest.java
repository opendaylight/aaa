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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class Oauth2ProxyHeaderParserTest {
    private final Oauth2ProxyHeaderFilterConfigImpl config = new Oauth2ProxyHeaderFilterConfigImpl();

    @Mock
    private HttpServletRequest request;

    @Test
    void testNoUserHeaderReturnsNull() {
        doReturn(Collections.enumeration(List.of()))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertNull(Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testBlankUserHeaderReturnsNull() {
        doReturn(Collections.enumeration(List.of("")))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertNull(Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));

        doReturn(Collections.enumeration(List.of("   ")))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertNull(Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testMultipleUserHeadersReturnsNull() {
        doReturn(Collections.enumeration(List.of("user1", "user2")))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertNull(Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testValidUserIsReturned() {
        doReturn(Collections.enumeration(List.of("  user.name@org  ")))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertEquals("user.name@org", Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));

        // boundary: exactly MAX_USER_LENGTH (128) is accepted
        doReturn(Collections.enumeration(List.of("a".repeat(128))))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertEquals("a".repeat(128), Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testUserWithSpecialCharactersNotAccepted() {
        // \0 is a null byte, \n is newline, \t is tab, \r is carriage return.
        // All of these are ISO control characters and should be rejected.
        for (final var bad : List.of("ad\0min", "us\ner\r", "mod\terator", "<admin>", "{admin}",
            // delimiter injection semicolon and comma must not pass through
            "user;injected", "user,second")) {
            doReturn(Collections.enumeration(List.of(bad)))
                .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
            assertNull(Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
                config.allowedCharactersPattern()));
        }
    }

    @Test
    void testMaxUserLengthExceeded() {
        // MAX_USER_LENGTH is 128
        doReturn(Collections.enumeration(List.of("a".repeat(129))))
            .when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        assertNull(Oauth2ProxyHeaderParser.parseUser(request, config.maxUserLength(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testNullOrEmptyRolesReturnsEmptySet() {
        doReturn(null).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        assertTrue(Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(), config.maxRolesPerUser(),
            config.maxRoleLength(), config.headerPattern(), config.allowedCharactersPattern()).isEmpty());

        doReturn(Collections.enumeration(List.of())).when(request)
            .getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        assertTrue(Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(), config.maxRolesPerUser(),
            config.maxRoleLength(), config.headerPattern(), config.allowedCharactersPattern()).isEmpty());
    }

    @Test
    void testNullOrEmptyHeadersAreIgnored() {
        final var headers = Collections.enumeration(Arrays.asList(null, "", "   ", "admin"));
        doReturn(headers).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        assertEquals(Set.of("admin"), Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testSingleHeaderMultipleRoles() {
        final var headers = Collections.enumeration(List.of("admin, role:odl:user, role:moderator",
            "   role:admin   ,   user   ,role:org:odl:spaced-role  "));
        doReturn(headers).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        final var roles = Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern());
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
        // delimiter injection semicolons must not survive role parsing
        final var maliciousRole6 = "admin;injected";
        doReturn(Collections.enumeration(List.of(maliciousRole1, maliciousRole2, maliciousRole3, maliciousRole4,
            maliciousRole5, maliciousRole6))).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        final var roles = Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern());
        assertEquals(Set.of(), roles);
    }

    @Test
    void testMalformedRoleHeaderIsSkipped() {
        // Headers with invalid structure are rejected wholesale; valid sibling headers still processed
        final var headers = Collections.enumeration(List.of("admin", "admin;injected", "user"));
        doReturn(headers).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        assertEquals(Set.of("admin", "user"), Oauth2ProxyHeaderParser.parseRolesHeader(request,
            config.maxHeaderLength(), config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern()));
    }

    @Test
    void testMaxHeaderLengthExceeded() {
        // MAX_HEADER_LENGTH is 4096
        final var headers = Collections.enumeration(List.of("admin", "a".repeat(4097), "user"));
        doReturn(headers).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        final var roles = Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern());

        // The massive header should be skipped, but the others processed
        assertEquals(Set.of("admin", "user"), roles);
    }

    @Test
    void testMaxRoleLengthExceeded() {
        // MAX_ROLE_LENGTH is 128
        final var headers = Collections.enumeration(List.of("admin, " + "a".repeat(129), "user"));
        doReturn(headers).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
        final var roles = Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern());

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
        manyRolesHeader.append("role");
        final var headers = Collections.enumeration(List.of(manyRolesHeader.toString()));
        doReturn(headers).when(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);

        final var roles = Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern());

        // It should truncate exactly at the 200 limit
        assertEquals(200, roles.size());
        assertTrue(roles.contains("role0"));
        assertTrue(roles.contains("role199"));
        assertFalse(roles.contains("role200"));
    }
}
