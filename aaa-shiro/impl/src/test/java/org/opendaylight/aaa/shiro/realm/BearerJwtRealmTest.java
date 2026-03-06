/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
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
import org.junit.Test;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

public class BearerJwtRealmTest {
    private static final String USER_CLAIM = "preferred_username";
    private static final String ROLE_CLAIM = "groups";

    private final BearerJwtRealm realm = new BearerJwtRealm();

    /**
     * Test that we do support BearerToken.
     */
    @Test
    public void testSupportsBearer() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder().claim(USER_CLAIM, "user").build());
        assertTrue(realm.supports(new BearerToken(jwt)));
    }

    /**
     * Tests that we do NOT support other kinds of token.
     */
    @Test
    public void testSupportsUsernamePassword() {
        assertFalse(realm.supports(new UsernamePasswordToken("user", "pass")));
    }

    /**
     * Tests that fully valid token is parsed correctly (no verification configured).
     */
    @Test
    public void testAuthenticationValid() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "aadmin")
            .claim(ROLE_CLAIM, List.of("admin", "global-admin"))
            .build());
        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));
        assertNotNull(info);
        final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertEquals("aadmin", principal.getUsername());
        assertNull(principal.getDomain());
        assertEquals("aadmin", principal.getUserId());
        assertEquals(Set.of("admin", "global-admin"), principal.getRoles());
    }

    /**
     * Test that missing roles claims in JWT result in no roles in application.
     */
    @Test
    public void testAuthenticationNoRoles() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "aadmin")
            .build());
        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));
        assertNotNull(info);
        final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertTrue(principal.getRoles().isEmpty());
    }

    /**
     * Tests that missing user claim results in error.
     */
    @Test
    public void testAuthenticationMissingUsername() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder().build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    /**
     * Tests that blank user claim results in error.
     */
    @Test
    public void testAuthenticationBlankUsername() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "")
            .build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    /**
     * Tests that malformed JWT results in error.
     */
    @Test
    public void testAuthenticationMalformedJwt() {
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken("not.a.valid.jwt.string")));
    }

    /**
     * Tests authorization of expected principal type.
     */
    @Test
    public void testAuthorizationOdlPrincipal() {
        final var principal = ODLPrincipalImpl.createODLPrincipal("aadmin", null, "aadmin",
            Set.of("admin"));
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn(principal);
        final var info = (SimpleAuthorizationInfo) realm.doGetAuthorizationInfo(principals);
        assertEquals(Set.of("admin"), info.getRoles());
    }

    /**
     * Tests that authorization of unexpected principal type results in error.
     */
    @Test
    public void testAuthorizationUnknownPrincipal() {
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn("some-string-principal");
        final var info = (SimpleAuthorizationInfo) realm.doGetAuthorizationInfo(principals);
        assertNotNull(info);
        assertNull(info.getRoles());
    }

    private static String buildPlainJwt(final JWTClaimsSet claims) {
        return new PlainJWT(claims).serialize();
    }
}
