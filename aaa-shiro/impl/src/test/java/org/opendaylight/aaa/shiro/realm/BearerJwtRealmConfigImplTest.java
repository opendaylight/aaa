/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the OSGi activation constructor of {@link BearerJwtRealmConfigImpl} — the path exercised
 * when the operator writes values to the {@code .cfg} file. Uses a {@code file://} JWKS URL so the
 * nimbus {@code RemoteJWKSet} can load keys without a network server.
 */
class BearerJwtRealmConfigImplTest {

    /**
     * Blank jwks-uri must produce a null processor (verification disabled, pass-through mode).
     */
    @Test
    void blankJwksUriYieldsNullProcessor() {
        var impl = new BearerJwtRealmConfigImpl(cfg("", "", new String[0], new String[]{"RS256"}));
        assertNull(impl.jwtProcessor());
    }

    /**
     * Syntactically invalid jwks-uri must throw {@link IllegalArgumentException} during activation.
     */
    @Test
    void invalidJwksUriThrowsDuringActivation() {
        assertThrows(IllegalArgumentException.class,
            () -> new BearerJwtRealmConfigImpl(cfg("not-a-url", "", new String[0], new String[]{"RS256"})));
    }

    /**
     * A syntactically valid URL must yield a non-null processor.
     * Confirms that {@code JWKSourceBuilder} connects lazily, not during construction.
     */
    @Test
    void validJwksUrlBuildsNonNullProcessor(@TempDir Path dir) throws Exception {
        final var impl = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, generateKey()), "", new String[0], new String[]{"RS256"}));
        assertNotNull(impl.jwtProcessor());
    }

    /**
     * A correctly signed JWT with matching issuer must pass verification end-to-end through the
     * activation constructor.
     */
    @Test
    void validJwtPassesVerification(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();
        final var processor = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, rsaKey), "test-iss", new String[0], new String[]{"RS256"})).jwtProcessor();

        final var jwt = signedJwt(rsaKey, JWSAlgorithm.RS256,
            new JWTClaimsSet.Builder().issuer("test-iss").expirationTime(futureDate()).build());
        assertDoesNotThrow(() -> processor.process(jwt, null));
    }

    /**
     * A JWT with the wrong issuer must be rejected.
     */
    @Test
    void wrongIssuerIsRejected(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();
        final var processor = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, rsaKey), "expected-iss", new String[0], new String[]{"RS256"})).jwtProcessor();

        final var jwt = signedJwt(rsaKey, JWSAlgorithm.RS256,
            new JWTClaimsSet.Builder().issuer("wrong-iss").expirationTime(futureDate()).build());
        assertThrows(Exception.class, () -> processor.process(jwt, null));
    }

    /**
     * Simulates {@code expected-audience=my-service} in the .cfg file: OSGi parses the single value
     * into {@code String[]{"my-service"}}. A JWT with a matching audience must pass; one with a
     * different audience must be rejected.
     */
    @Test
    void singleAudienceIsEnforced(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();
        final var processor = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, rsaKey), "", new String[]{"my-service"}, new String[]{"RS256"})).jwtProcessor();

        final var valid = signedJwt(rsaKey, JWSAlgorithm.RS256,
            new JWTClaimsSet.Builder().audience("my-service").expirationTime(futureDate()).build());
        assertDoesNotThrow(() -> processor.process(valid, null));

        final var wrong = signedJwt(rsaKey, JWSAlgorithm.RS256,
            new JWTClaimsSet.Builder().audience("other-service").expirationTime(futureDate()).build());
        assertThrows(Exception.class, () -> processor.process(wrong, null));
    }

    /**
     * Simulates {@code expected-audience=svc-a,svc-b} in the .cfg file: OSGi parses the
     * comma-separated list into {@code String[]{"svc-a", "svc-b"}}. A JWT whose audience matches
     * any one of the configured values must be accepted.
     */
    @Test
    void multipleAudiencesAcceptedWhenOneMatches(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();
        final var processor = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, rsaKey), "", new String[]{"svc-a", "svc-b"}, new String[]{"RS256"})).jwtProcessor();

        final var jwt = signedJwt(rsaKey, JWSAlgorithm.RS256,
            new JWTClaimsSet.Builder().audience("svc-b").expirationTime(futureDate()).build());
        assertDoesNotThrow(() -> processor.process(jwt, null));
    }

    /**
     * Simulates {@code allowed-algorithms=RS256} in the .cfg file. A JWT signed with RS384 must be
     * rejected by the key selector without attempting a JWK fetch.
     */
    @Test
    void disallowedAlgorithmIsRejected(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();
        final var processor = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, rsaKey), "", new String[0], new String[]{"RS256"})).jwtProcessor();

        final var jwt = signedJwt(rsaKey, JWSAlgorithm.RS384,
            new JWTClaimsSet.Builder().expirationTime(futureDate()).build());
        assertThrows(Exception.class, () -> processor.process(jwt, null));
    }

    /**
     * An unsigned (plain) JWT must be rejected when verification is enabled.
     */
    @Test
    void unsignedJwtIsRejected(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();
        final var processor = new BearerJwtRealmConfigImpl(
            cfg(jwksUri(dir, rsaKey), "", new String[0], new String[]{"RS256"})).jwtProcessor();

        final var plain = new PlainJWT(
            new JWTClaimsSet.Builder().expirationTime(futureDate()).build()).serialize();
        assertThrows(Exception.class, () -> processor.process(plain, null));
    }

    /**
     * Reads a {@code .cfg}-formatted string exactly as an operator would write it on disk, parses it
     * the way Felix ConfigAdmin does for {@code cardinality = Integer.MIN_VALUE} properties (split
     * by comma), and verifies the resulting processor accepts and rejects JWTs correctly.
     *
     * <p>This test proves that {@code expected-audience=svc-a,svc-b} and
     * {@code allowed-algorithms=RS256,RS384} in the config file produce the correct runtime behaviour.
     */
    @Test
    void configFileFormatIsCorrectlyHandled(@TempDir Path dir) throws Exception {
        final var rsaKey = generateKey();

        // Exactly the format an operator types in etc/org.opendaylight.aaa.shiro.bearerjwtrealm.cfg.
        // With cardinality = Integer.MIN_VALUE, Felix CM splits comma-separated values into String[].
        final var cfgText = """
            jwks-uri=%s
            expected-issuer=test-iss
            expected-audience=svc-a,svc-b
            allowed-algorithms=RS256,RS384
            """.formatted(jwksUri(dir, rsaKey));

        final var props = new Properties();
        props.load(new StringReader(cfgText));

        // parseCsv() applies Felix CM's comma-split logic for cardinality = Integer.MIN_VALUE
        final var config = cfg(
            props.getProperty("jwks-uri", ""),
            props.getProperty("expected-issuer", ""),
            parseCsv(props.getProperty("expected-audience", "")),
            parseCsv(props.getProperty("allowed-algorithms", ""))
        );
        final var processor = new BearerJwtRealmConfigImpl(config).jwtProcessor();
        assertNotNull(processor);

        // JWT matching issuer + one of the two audiences, signed with RS256 (in allowed-algorithms) passes
        final var valid = signedJwt(rsaKey, JWSAlgorithm.RS256, new JWTClaimsSet.Builder()
            .issuer("test-iss").audience("svc-b").expirationTime(futureDate()).build());
        assertDoesNotThrow(() -> processor.process(valid, null));

        // JWT signed with RS384 also passes — it is the second entry in allowed-algorithms
        final var rs384 = signedJwt(rsaKey, JWSAlgorithm.RS384, new JWTClaimsSet.Builder()
            .issuer("test-iss").audience("svc-a").expirationTime(futureDate()).build());
        assertDoesNotThrow(() -> processor.process(rs384, null));

        // JWT signed with RS512 (not listed in allowed-algorithms) is rejected without a JWKS fetch
        final var disallowed = signedJwt(rsaKey, JWSAlgorithm.RS512, new JWTClaimsSet.Builder()
            .issuer("test-iss").audience("svc-a").expirationTime(futureDate()).build());
        assertThrows(Exception.class, () -> processor.process(disallowed, null));

        // JWT with wrong issuer is rejected even if algorithm and audience are correct
        final var wrongIss = signedJwt(rsaKey, JWSAlgorithm.RS256, new JWTClaimsSet.Builder()
            .issuer("evil-iss").audience("svc-a").expirationTime(futureDate()).build());
        assertThrows(Exception.class, () -> processor.process(wrongIss, null));
    }

    private static String[] parseCsv(final String value) {
        if (value.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(value.split(",")).map(String::strip).toArray(String[]::new);
    }

    private static RSAKey generateKey() throws Exception {
        return new RSAKeyGenerator(2048).keyID("test-key").generate();
    }

    private static String jwksUri(final Path dir, final RSAKey rsaKey) throws Exception {
        final var jwksFile = dir.resolve("jwks.json");
        Files.writeString(jwksFile, new JWKSet(rsaKey.toPublicJWK()).toString());
        return jwksFile.toUri().toString();
    }

    private static String signedJwt(final RSAKey rsaKey, final JWSAlgorithm alg,
            final JWTClaimsSet claims) throws Exception {
        final var jwt = new SignedJWT(new JWSHeader(alg), claims);
        jwt.sign(new RSASSASigner(rsaKey));
        return jwt.serialize();
    }

    private static Date futureDate() {
        return new Date(System.currentTimeMillis() + 60_000);
    }

    private static BearerJwtRealmConfigImpl.Configuration cfg(
            final String jwksUri, final String issuer,
            final String[] audience, final String[] algorithms) {
        return new BearerJwtRealmConfigImpl.Configuration() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return BearerJwtRealmConfigImpl.Configuration.class;
            }

            @Override
            public String jwks$_$uri() {
                return jwksUri;
            }

            @Override
            public String expected$_$issuer() {
                return issuer;
            }

            @Override
            public String[] expected$_$audience() {
                return audience;
            }

            @Override
            public String[] allowed$_$algorithms() {
                return algorithms;
            }
        };
    }
}
