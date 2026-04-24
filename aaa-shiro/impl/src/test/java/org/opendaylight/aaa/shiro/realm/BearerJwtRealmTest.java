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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.util.Date;
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

    // Shared key for unverified-mode tests: the realm accepts signed JWTs without
    // verifying the signature, so any key works — we just need a non-plain JWT.
    private static final RSAKey UNVERIFIED_KEY;

    static {
        try {
            UNVERIFIED_KEY = new RSAKeyGenerator(2048).keyID("unverified-test-key").generate();
        } catch (JOSEException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final BearerJwtRealm realm = new BearerJwtRealm();

    /**
     * Test that we do support BearerToken.
     */
    @Test
    void testSupportsBearer() {
        assertTrue(realm.supports(new BearerToken("any-bearer-string")));
    }

    /**
     * Tests that we do NOT support other kinds of token.
     */
    @Test
    void testSupportsUsernamePassword() {
        assertFalse(realm.supports(new UsernamePasswordToken(USER, "pass")));
    }

    // -------------------------------------------------------------------------
    // Unverified JWT tests (BearerJwtRealmConfig NOT configured)
    // -------------------------------------------------------------------------

    /**
     * Tests that fully valid token is parsed correctly (no verification configured).
     */
    @Test
    void testAuthenticationValid() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, USER)
            .claim(ROLE_CLAIM, List.of("admin", "global-admin"))
            .build());
        final var info = realm.doGetAuthenticationInfo(new BearerToken(jwt));
        assertNotNull(info);
        final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
        assertEquals(USER, principal.getUsername());
        assertNull(principal.getDomain());
        assertEquals(USER, principal.getUserId());
        assertEquals(Set.of("admin", "global-admin"), principal.getRoles());
    }

    /**
     * Test that missing roles claims in JWT result in no roles in application.
     */
    @Test
    void testAuthenticationNoRoles() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, USER)
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
    void testAuthenticationMissingUsername() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder().build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    /**
     * Tests that blank user claim results in error.
     */
    @Test
    void testAuthenticationBlankUsername() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "")
            .build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    /**
     * Tests that malformed JWT results in error.
     */
    @Test
    void testAuthenticationMalformedJwt() {
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken("not.a.valid.jwt.string")));
    }

    /**
     * Tests that an unsigned (alg=none) JWT is rejected even in unverified mode (RFC 8725 §3.2).
     */
    @Test
    void testUnverifiedPlainJwtRejected() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "aadmin")
            .build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
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
        final var info = (SimpleAuthorizationInfo) realm.doGetAuthorizationInfo(principals);
        assertEquals(Set.of("admin"), info.getRoles());
    }

    /**
     * Tests that authorization of unexpected principal type results in error.
     */
    @Test
    void testAuthorizationUnknownPrincipal() {
        final var principals = mock(PrincipalCollection.class);
        when(principals.getPrimaryPrincipal()).thenReturn("some-string-principal");
        final var info = (SimpleAuthorizationInfo) realm.doGetAuthorizationInfo(principals);
        assertNotNull(info);
        assertNull(info.getRoles());
    }

    /**
     * Tests that custom user and role claim names are honored when configured.
     */
    @Test
    void testCustomClaimNames() throws Exception {
        final var config = new BearerJwtRealmConfigImpl(null, "sub", "groups");
        try (var ignored = BearerJwtRealm.prepareForLoad(config)) {
            final var customRealm = new BearerJwtRealm();
            final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder()
                .claim("sub", "custom-user")
                .claim("groups", List.of("admin", "viewer"))
                .build());
            final var info = customRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
            assertEquals("custom-user", principal.getUsername());
            assertEquals(Set.of("admin", "viewer"), principal.getRoles());
        }
    }

    // -------------------------------------------------------------------------
    // Verified JWT tests (BearerJwtRealmConfig configured)
    // -------------------------------------------------------------------------

    /**
     * Tests that a correctly signed JWT with valid claims passes verification.
     */
    @Test
    void testVerifiedAuthenticationValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "test-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .claim(ROLE_CLAIM, List.of("admin"))
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
            assertEquals(USER, principal.getUsername());
            assertEquals(Set.of("admin"), principal.getRoles());
        }
    }

    /**
     * Tests that an expired JWT is rejected when verification is configured.
     */
    @Test
    void testVerifiedAuthenticationExpired() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "test-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .issuer("test-issuer")
                .expirationTime(new Date(System.currentTimeMillis() - 60_000))
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT with the wrong issuer is rejected.
     */
    @Test
    void testVerifiedAuthenticationWrongIssuer() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "expected-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .issuer("wrong-issuer")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that an unsigned (plain) JWT is rejected when verification is configured.
     */
    @Test
    void testVerifiedAuthenticationUnsignedRejected() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, null, null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT signed with a different key (unknown key) is rejected.
     */
    @Test
    void testVerifiedAuthenticationUnknownKey() throws Exception {
        final var configKey = newRsaKey();
        final var signingKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(configKey, null, null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(signingKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT with missing required audience is rejected.
     */
    @Test
    void testVerifiedAuthenticationWrongAudience() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, null, "my-service"))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .audience("other-service")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT signed with one of multiple configured algorithms is accepted.
     */
    @Test
    void testVerifiedAuthenticationMultipleAlgorithmsValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, "test-issuer", null, Set.of(JWSAlgorithm.RS256, JWSAlgorithm.RS384)))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, JWSAlgorithm.RS384, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            assertEquals(USER,
                ((ODLPrincipal) info.getPrincipals().getPrimaryPrincipal()).getUsername());
        }
    }

    /**
     * Tests that a JWT signed with an algorithm not in the allowed set is rejected.
     */
    @Test
    void testVerifiedAuthenticationAlgorithmNotAllowed() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, null, null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, JWSAlgorithm.RS384, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT whose audience matches one of multiple configured audience values is accepted.
     */
    @Test
    void testVerifiedAuthenticationMultipleAudienceValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, "test-issuer", Set.of("service-a", "service-b"), Set.of(JWSAlgorithm.RS256)))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .issuer("test-issuer")
                .audience("service-a")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            assertEquals(USER,
                ((ODLPrincipal) info.getPrincipals().getPrimaryPrincipal()).getUsername());
        }
    }

    /**
     * Tests that a JWT with no audience claim is rejected when audience verification is configured.
     */
    @Test
    void testVerifiedAuthenticationMissingAudience() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, null, Set.of("my-service"), Set.of(JWSAlgorithm.RS256)))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, USER)
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT without an iss claim is rejected when issuer verification is configured
     * (RFC 8725 §3.8).
     */
    @Test
    void testVerifiedAuthenticationMissingIssuerClaim() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "test-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT carrying an aud claim is rejected when audience verification is not
     * configured (RFC 8725 §3.9 — prevents confused-deputy attacks).
     */
    @Test
    void testVerifiedAuthenticationAudPresentWhenNotConfigured() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, null, null, Set.of(JWSAlgorithm.RS256)))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .audience("some-service")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String buildPlainJwt(final JWTClaimsSet claims) {
        return new PlainJWT(claims).serialize();
    }

    /**
     * Builds a signed JWT using the shared {@link #UNVERIFIED_KEY} for unverified-mode tests.
     */
    private static String buildUnverifiedJwt(final JWTClaimsSet claims) throws Exception {
        return buildSignedJwt(UNVERIFIED_KEY, claims);
    }

    private static RSAKey newRsaKey() throws Exception {
        return new RSAKeyGenerator(2048).keyID("test-key-" + System.nanoTime()).generate();
    }

    private static String buildSignedJwt(final RSAKey rsaKey, final JWTClaimsSet claims) throws Exception {
        return buildSignedJwt(rsaKey, JWSAlgorithm.RS256, claims);
    }

    private static String buildSignedJwt(final RSAKey rsaKey, final JWSAlgorithm alg,
            final JWTClaimsSet claims) throws Exception {
        final var signedJwt = new SignedJWT(new JWSHeader(alg), claims);
        signedJwt.sign(new RSASSASigner(rsaKey));
        return signedJwt.serialize();
    }

    /**
     * Builds a {@link BearerJwtRealmConfigImpl} backed by a local {@link ImmutableJWKSet} for testing.
     *
     * <p>Uses the package-private constructor that accepts a pre-built processor.
     * Allows only RS256 and accepts a single optional audience string.
     */
    private static BearerJwtRealmConfigImpl buildConfig(final RSAKey rsaKey, final String issuer,
            final String audience) {
        return buildConfig(rsaKey, issuer,
            audience != null ? Set.of(audience) : null, Set.of(JWSAlgorithm.RS256));
    }

    /**
     * Builds a {@link BearerJwtRealmConfigImpl} with configurable allowed algorithms and audience set.
     */
    private static BearerJwtRealmConfigImpl buildConfig(final RSAKey rsaKey, final String issuer,
            final Set<String> audiences, final Set<JWSAlgorithm> algorithms) {
        final var jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey.toPublicJWK()));
        final var keySelector = new JWSVerificationKeySelector<>(algorithms, jwkSource);
        final var exactMatchBuilder = new JWTClaimsSet.Builder();
        if (issuer != null) {
            exactMatchBuilder.issuer(issuer);
        }
        final var prohibitedClaims = audiences == null ? Set.of("aud") : Set.of();
        final var claimsVerifier = new DefaultJWTClaimsVerifier<>(
            audiences, exactMatchBuilder.build(), Set.of(), (Set<String>) prohibitedClaims);
        final var processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(claimsVerifier);
        return new BearerJwtRealmConfigImpl(processor, USER_CLAIM, ROLE_CLAIM);
    }

    private static Date futureDate() {
        return new Date(System.currentTimeMillis() + 60_000);
    }
}
