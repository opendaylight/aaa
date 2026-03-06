/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

public class Oauth2ProxyRealmTest {
    private final Oauth2ProxyRealm realm = new Oauth2ProxyRealm();

    private static String buildJwt(final JWTClaimsSet claims) {
        return new PlainJWT(claims).serialize();
    }

    @Test
    public void testSupports_bearer() {
        final var jwt = buildJwt(new JWTClaimsSet.Builder().claim("preferred_username", "user").build());
        assertTrue(realm.supports(new BearerToken(jwt)));
    }

    @Test
    public void testSupports_usernamePassword() {
        assertTrue(!realm.supports(new UsernamePasswordToken("user", "pass")));
    }

    @Test
    public void testAuthentication_valid() {
        final var jwt = buildJwt(new JWTClaimsSet.Builder()
            .claim("preferred_username", "aadmin")
            .claim("groups", List.of("admin", "global-admin"))
            .build());

        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));

        assertNotNull(info);
        final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertEquals("aadmin", principal.getUsername());
        assertEquals("sdn", principal.getDomain());
        assertEquals("aadmin@sdn", principal.getUserId());
        assertEquals(Set.of("admin", "global-admin"), principal.getRoles());
    }

    @Test
    public void testAuthentication_noGroups() {
        final var jwt = buildJwt(new JWTClaimsSet.Builder()
            .claim("preferred_username", "aadmin")
            .build());

        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));

        assertNotNull(info);
        final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertEquals(Set.of(), principal.getRoles());
    }

    @Test
    public void testAuthentication_missingUsername() {
        final var jwt = buildJwt(new JWTClaimsSet.Builder().build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    @Test
    public void testAuthentication_blankUsername() {
        final var jwt = buildJwt(new JWTClaimsSet.Builder()
            .claim("preferred_username", "  ")
            .build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    @Test
    public void testAuthentication_malformedJwt() {
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken("not.a.valid.jwt.string")));
    }

    @Test
    public void testAuthorization_odlPrincipal() {
        final var principal = ODLPrincipalImpl.createODLPrincipal("aadmin", "sdn", "aadmin@sdn",
            Set.of("admin"));
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn(principal);

        final var info = (SimpleAuthorizationInfo) realm.doGetAuthorizationInfo(principals);

        assertEquals(Set.of("admin"), info.getRoles());
    }

    @Test
    public void testAuthorization_unknownPrincipal() {
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn("some-string-principal");

        final var info = (SimpleAuthorizationInfo) realm.doGetAuthorizationInfo(principals);

        assertNotNull(info);
        assertTrue(info.getRoles() == null || info.getRoles().isEmpty());
    }
}
