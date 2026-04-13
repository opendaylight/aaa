/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.aaa.shiro.filters.Oauth2ProxyHeaderToken;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

class Oauth2ProxyHeaderRealmTest {
    private static final String USER = "user";
    private static final Set<String> ROLES = Set.of("global-admin", "odl-application:admin", "role1", "role2");

    private final Oauth2ProxyHeaderRealm realm = new Oauth2ProxyHeaderRealm();

    /**
     * Test if Oauth2ProxyHeaderRealm produce correct AuthenticationInfo and AuthorizationInfo.
     */
    @Test
    void testOauth2ProxyHeaderRealm() {
        final var roles = List.of("role:global-admin,role:odl-application:admin", "role1", "role2");
        final var token = new Oauth2ProxyHeaderToken(roles, USER);
        final var authenticationInfo = realm.doGetAuthenticationInfo(token);
        assertNull(authenticationInfo.getCredentials());
        final var principal = assertInstanceOf(ODLPrincipalImpl.class,
            authenticationInfo.getPrincipals().getPrimaryPrincipal());
        assertEquals(ROLES, principal.getRoles());
        assertEquals(USER, principal.getUsername());
        assertEquals(USER, principal.getUserId());
        assertNull(principal.getDomain());

        final var authorizationInfo = realm.doGetAuthorizationInfo(authenticationInfo.getPrincipals());
        assertEquals(ROLES, authorizationInfo.getRoles());
    }

    // Role parsing tests

    @Test
    void testNullOrEmptyRolesReturnsEmptySet() {
        assertTrue(Oauth2ProxyHeaderRealm.parseRoles(null).isEmpty());
        assertTrue(Oauth2ProxyHeaderRealm.parseRoles(List.of()).isEmpty());
    }

    @Test
    void testNullOrEmptyHeadersAreIgnored() {
        final var headers = Arrays.asList(null, "", "   ", "admin");
        assertEquals(Set.of("admin"), Oauth2ProxyHeaderRealm.parseRoles(headers));
    }

    @Test
    void testSingleHeaderMultipleRoles() {
        final var headers = List.of("admin, role:odl:user, role:moderator",
            "   role:admin   ,   user   ,role:org:odl:spaced-role  ");
        Set<String> roles = Oauth2ProxyHeaderRealm.parseRoles(headers);
        assertEquals(Set.of("admin", "odl:user", "moderator", "user", "org:odl:spaced-role"), roles);
    }
}
