/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.util.List;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.jupiter.api.Test;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

class BearerJwtRealmTest {
    private static final String USER_CLAIM = "preferred_username";
    private static final String ROLE_CLAIM = "groups";
    private static final String USER = "user";

    private final BearerJwtRealm realm = new BearerJwtRealm();

    /**
     * Test that we do support BearerToken.
     */
    @Test
    void testSupportsBearer() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder().claim(USER_CLAIM, USER).build());
        assertTrue(realm.supports(new BearerToken(jwt)));
    }

    /**
     * Tests that we do NOT support other kinds of token.
     */
    @Test
    void testSupportsUsernamePassword() {
        assertFalse(realm.supports(new UsernamePasswordToken(USER, "pass")));
    }

    /**
     * Tests that fully valid token is parsed correctly (no verification configured).
     */
    @Test
    void testAuthenticationValid() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, USER)
            .claim(ROLE_CLAIM, List.of("admin", "global-admin"))
            .build());
        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));
        assertNotNull(info);
        final var principal = assertInstanceOf(ODLPrincipal.class, info.getPrincipals().getPrimaryPrincipal());
        assertEquals(USER, principal.getUsername());
        assertNull(principal.getDomain());
        assertEquals(USER, principal.getUserId());
        assertEquals(Set.of("admin", "global-admin"), principal.getRoles());
    }

    /**
     * Test that missing roles claims in JWT result in no roles in application.
     */
    @Test
    void testAuthenticationNoRoles() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, USER)
            .build());
        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));
        assertNotNull(info);
        final var principal = assertInstanceOf(ODLPrincipal.class, info.getPrincipals().getPrimaryPrincipal());
        assertTrue(principal.getRoles().isEmpty());
    }

    /**
     * Tests that missing user claim results in error.
     */
    @Test
    void testAuthenticationMissingUsername() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder().build());
        final var ex = assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
        assertEquals("Required JWT user claim value is empty", ex.getMessage());
    }

    /**
     * Tests that blank user claim results in error.
     */
    @Test
    void testAuthenticationBlankUsername() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "")
            .build());
        final var ex = assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
        assertEquals("Required JWT user claim value is empty", ex.getMessage());
    }

    /**
     * Tests that malformed JWT results in error.
     */
    @Test
    void testAuthenticationMalformedJwt() {
        final var ex = assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken("not.a.valid.jwt.string")));
        assertEquals("Failed to parse provided JWT claims", ex.getMessage());
    }

    /**
     * Tests authorization of expected principal type.
     */
    @Test
    void testAuthorizationOdlPrincipal() {
        final var principal = ODLPrincipalImpl.createODLPrincipal(USER, null, USER,
            Set.of("admin"));
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn(principal);
        final var info = assertInstanceOf(SimpleAuthorizationInfo.class, realm.doGetAuthorizationInfo(principals));
        assertEquals(Set.of("admin"), info.getRoles());
    }

    /**
     * Tests that authorization of unexpected principal type results in error.
     */
    @Test
    void testAuthorizationUnknownPrincipal() {
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn("some-string-principal");
        final var info = assertInstanceOf(SimpleAuthorizationInfo.class, realm.doGetAuthorizationInfo(principals));
        assertNotNull(info);
        assertNull(info.getRoles());
    }

    private static String buildPlainJwt(final JWTClaimsSet claims) {
        return new PlainJWT(claims).serialize();
    }
}
