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

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.EncryptedJWT;
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
import org.junit.Test;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

public class BearerJwtRealmTest {
    private static final String USER_CLAIM = "preferred_username";
    private static final String ROLE_CLAIM = "groups";

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
    public void testSupportsBearer() {
        assertTrue(realm.supports(new BearerToken("any-bearer-string")));
    }

    /**
     * Tests that we do NOT support other kinds of token.
     */
    @Test
    public void testSupportsUsernamePassword() {
        assertFalse(realm.supports(new UsernamePasswordToken("user", "pass")));
    }

    // -------------------------------------------------------------------------
    // Unverified JWT tests (BearerJwtRealmConfig NOT configured)
    // -------------------------------------------------------------------------

    /**
     * Tests that fully valid token is parsed correctly (no verification configured).
     */
    @Test
    public void testAuthenticationValid() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder()
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
    public void testAuthenticationNoRoles() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder()
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
    public void testAuthenticationMissingUsername() throws Exception {
        final var jwt = buildUnverifiedJwt(new JWTClaimsSet.Builder().build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    /**
     * Tests that blank user claim results in error.
     */
    @Test
    public void testAuthenticationBlankUsername() throws Exception {
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
    public void testAuthenticationMalformedJwt() {
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken("not.a.valid.jwt.string")));
    }

    /**
     * Tests that an unsigned (alg=none) JWT is rejected even in unverified mode (RFC 8725 §3.2).
     */
    @Test
    public void testUnverifiedPlainJwtRejected() {
        final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
            .claim(USER_CLAIM, "aadmin")
            .build());
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(jwt)));
    }

    /**
     * Tests that an encrypted JWT (JWE) is rejected in unverified mode with a clean exception
     * rather than a NullPointerException on the undecrypted payload.
     */
    @Test
    public void testUnverifiedEncryptedJwtRejected() {
        assertThrows(AuthenticationException.class,
            () -> realm.doGetAuthenticationInfo(new BearerToken(buildEncryptedJwt())));
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

    /**
     * Tests that custom user and role claim names are honored when configured.
     */
    @Test
    public void testCustomClaimNames() throws Exception {
        final var config = new BearerJwtRealmConfig(null, "sub", "groups");
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
    public void testVerifiedAuthenticationValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "test-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .claim(ROLE_CLAIM, List.of("admin"))
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            final var principal = (ODLPrincipal) info.getPrincipals().getPrimaryPrincipal();
            assertEquals("aadmin", principal.getUsername());
            assertEquals(Set.of("admin"), principal.getRoles());
        }
    }

    /**
     * Tests that an expired JWT is rejected when verification is configured.
     */
    @Test
    public void testVerifiedAuthenticationExpired() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "test-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
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
    public void testVerifiedAuthenticationWrongIssuer() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "expected-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
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
    public void testVerifiedAuthenticationUnsignedRejected() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, null, null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildPlainJwt(new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that an encrypted JWT (JWE) is rejected in verified mode with a clean exception
     * before the token reaches the JWTProcessor.
     */
    @Test
    public void testVerifiedEncryptedJwtRejected() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, "test-issuer", null))) {
            final var verifiedRealm = new BearerJwtRealm();
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(buildEncryptedJwt())));
        }
    }

    /**
     * Tests that a JWT signed with a different key (unknown key) is rejected.
     */
    @Test
    public void testVerifiedAuthenticationUnknownKey() throws Exception {
        final var configKey = newRsaKey();
        final var signingKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(configKey, null, null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(signingKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
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
    public void testVerifiedAuthenticationWrongAudience() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, null, "my-service"))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
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
    public void testVerifiedAuthenticationMultipleAlgorithmsValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, "test-issuer", null, Set.of(JWSAlgorithm.RS256, JWSAlgorithm.RS384)))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, JWSAlgorithm.RS384, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            assertEquals("aadmin",
                ((ODLPrincipal) info.getPrincipals().getPrimaryPrincipal()).getUsername());
        }
    }

    /**
     * Tests that a JWT signed with an algorithm not in the allowed set is rejected.
     */
    @Test
    public void testVerifiedAuthenticationAlgorithmNotAllowed() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(buildConfig(rsaKey, null, null))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, JWSAlgorithm.RS384, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
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
    public void testVerifiedAuthenticationMultipleAudienceValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, "test-issuer", Set.of("service-a", "service-b"), Set.of(JWSAlgorithm.RS256)))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .issuer("test-issuer")
                .audience("service-a")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            assertEquals("aadmin",
                ((ODLPrincipal) info.getPrincipals().getPrimaryPrincipal()).getUsername());
        }
    }

    /**
     * Tests that a JWT with no audience claim is rejected when audience verification is configured.
     */
    @Test
    public void testVerifiedAuthenticationMissingAudience() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfig(rsaKey, null, Set.of("my-service"), Set.of(JWSAlgorithm.RS256)))) {
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
     * Tests that the production constructor rejects a blank issuer when JWKS is configured
     * (RFC 8725 §3.8 — issuer validation is mandatory).
     */
    @Test
    public void testProductionConfigRequiresIssuer() {
        assertThrows(IllegalArgumentException.class, () -> new BearerJwtRealmConfig(
            "http://localhost:8080/certs", "", "", "RS256",
            "preferred_username", "groups", "JWT", 300, 15, false));
    }

    /**
     * Tests that a JWT without an iss claim is rejected when issuer verification is configured
     * (RFC 8725 §3.8).
     */
    @Test
    public void testVerifiedAuthenticationMissingIssuerClaim() throws Exception {
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
    public void testVerifiedAuthenticationAudPresentWhenNotConfigured() throws Exception {
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

    /**
     * Tests that a JWT with the correct typ header is accepted (RFC 8725 §3.11).
     */
    @Test
    public void testVerifiedAuthenticationTypeValid() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfigWithType(rsaKey, "test-issuer", "JWT"))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwtWithType(rsaKey, "JWT", new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            final var info = verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt));
            assertNotNull(info);
            assertEquals("aadmin",
                ((ODLPrincipal) info.getPrincipals().getPrimaryPrincipal()).getUsername());
        }
    }

    /**
     * Tests that a JWT with a mismatched typ header is rejected (RFC 8725 §3.11).
     */
    @Test
    public void testVerifiedAuthenticationTypeMismatch() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfigWithType(rsaKey, "test-issuer", "JWT"))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwtWithType(rsaKey, "JWT", new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT with no typ header is rejected when type checking is configured (RFC 8725 §3.11).
     */
    @Test
    public void testVerifiedAuthenticationTypeMissing() throws Exception {
        final var rsaKey = newRsaKey();
        try (var ignored = BearerJwtRealm.prepareForLoad(
                buildConfigWithType(rsaKey, "test-issuer", "JWT"))) {
            final var verifiedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwt(rsaKey, new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .issuer("test-issuer")
                .expirationTime(futureDate())
                .build());
            assertThrows(AuthenticationException.class,
                () -> verifiedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    /**
     * Tests that a JWT with a mismatched typ header is rejected in unverified mode (RFC 8725 §3.11).
     */
    @Test
    public void testUnverifiedAuthenticationTypeMismatch() throws Exception {
        final var config = new BearerJwtRealmConfig(null, USER_CLAIM, ROLE_CLAIM, "JWT");
        try (var ignored = BearerJwtRealm.prepareForLoad(config)) {
            final var typedRealm = new BearerJwtRealm();
            final var jwt = buildSignedJwtWithType(UNVERIFIED_KEY, "JWT", new JWTClaimsSet.Builder()
                .claim(USER_CLAIM, "aadmin")
                .build());
            assertThrows(AuthenticationException.class,
                () -> typedRealm.doGetAuthenticationInfo(new BearerToken(jwt)));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String buildPlainJwt(final JWTClaimsSet claims) {
        return new PlainJWT(claims).serialize();
    }

    /**
     * Builds a JWE-encrypted JWT using RSA-OAEP-256 + AES-128-GCM with {@link #UNVERIFIED_KEY}.
     */
    private static String buildEncryptedJwt() throws Exception {
        final var jwt = new EncryptedJWT(
            new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM),
            new JWTClaimsSet.Builder().claim(USER_CLAIM, "aadmin").build());
        jwt.encrypt(new RSAEncrypter(UNVERIFIED_KEY.toPublicJWK()));
        return jwt.serialize();
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
     * Builds a {@link BearerJwtRealmConfig} backed by a local {@link ImmutableJWKSet} for testing.
     *
     * <p>Uses the package-private constructor that accepts a pre-built processor.
     * Allows only RS256 and accepts a single optional audience string.
     */
    private static BearerJwtRealmConfig buildConfig(final RSAKey rsaKey, final String issuer,
            final String audience) {
        return buildConfig(rsaKey, issuer,
            audience != null ? Set.of(audience) : null, Set.of(JWSAlgorithm.RS256));
    }

    /**
     * Builds a {@link BearerJwtRealmConfig} with configurable allowed algorithms and audience set.
     */
    private static BearerJwtRealmConfig buildConfig(final RSAKey rsaKey, final String issuer,
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
        return new BearerJwtRealmConfig(processor, USER_CLAIM, ROLE_CLAIM);
    }

    private static String buildSignedJwtWithType(final RSAKey rsaKey, final String typ,
            final JWTClaimsSet claims) throws Exception {
        final var header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(new JOSEObjectType(typ)).build();
        final var signedJwt = new SignedJWT(header, claims);
        signedJwt.sign(new RSASSASigner(rsaKey));
        return signedJwt.serialize();
    }

    /**
     * Builds a {@link BearerJwtRealmConfig} with type checking enabled, backed by a local JWK set.
     */
    private static BearerJwtRealmConfig buildConfigWithType(final RSAKey rsaKey, final String issuer,
            final String expectedType) {
        final var jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey.toPublicJWK()));
        final var keySelector = new JWSVerificationKeySelector<>(Set.of(JWSAlgorithm.RS256), jwkSource);
        final var exactMatchBuilder = new JWTClaimsSet.Builder();
        if (issuer != null) {
            exactMatchBuilder.issuer(issuer);
        }
        final var prohibitedClaims = Set.of("aud");
        final var claimsVerifier = new DefaultJWTClaimsVerifier<>(
            null, exactMatchBuilder.build(), Set.of(), prohibitedClaims);
        final var processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(claimsVerifier);
        processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType(expectedType)));
        return new BearerJwtRealmConfig(processor, USER_CLAIM, ROLE_CLAIM, expectedType);
    }

    private static Date futureDate() {
        return new Date(System.currentTimeMillis() + 60_000);
    }
}
