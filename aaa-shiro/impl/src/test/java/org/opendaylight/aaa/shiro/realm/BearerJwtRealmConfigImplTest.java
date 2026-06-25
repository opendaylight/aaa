/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
     * Loads {@code example-bearer-jwt-realm.cfg} from test resources — the file an operator would
     * deploy on disk. Asserts that each property is parsed to the correct value, then verifies
     * end-to-end JWT behaviour using a local {@code file://} JWKS (substituted for the example URL).
     *
     * <p>Felix ConfigAdmin splits comma-separated values into {@code String[]} for properties whose
     * {@link org.osgi.service.metatype.annotations.AttributeDefinition} uses
     * {@code cardinality = Integer.MIN_VALUE}. {@link #parseCsv} replicates that logic so the test
     * exercises exactly the values an operator writes in the file.
     */
    @Test
    void configFileValuesAreParsedAndVerified(@TempDir Path dir) throws Exception {
        final var props = new Properties();
        try (var stream = getClass().getResourceAsStream("example-bearer-jwt-realm.cfg")) {
            assertNotNull(stream, "example-bearer-jwt-realm.cfg must be present in test resources");
            props.load(stream);
        }

        // Assert each raw property value read from the file
        assertEquals(
            "https://keycloak.example.com/realms/odl/protocol/openid-connect/certs",
            props.getProperty("jwks-uri"));
        assertEquals(
            "https://keycloak.example.com/realms/odl",
            props.getProperty("expected-issuer"));
        assertEquals("odl-app,odl-admin", props.getProperty("expected-audience"));
        assertEquals("RS256,RS384",        props.getProperty("allowed-algorithms"));

        // Apply Felix CM's comma-split (cardinality = Integer.MIN_VALUE)
        final var audience   = parseCsv(props.getProperty("expected-audience", ""));
        final var algorithms = parseCsv(props.getProperty("allowed-algorithms", ""));

        assertArrayEquals(new String[]{"odl-app", "odl-admin"}, audience);
        assertArrayEquals(new String[]{"RS256", "RS384"},       algorithms);

        // End-to-end: substitute a local file:// JWKS for the example URL, then verify JWT behaviour
        final var rsaKey = generateKey();
        final var config = cfg(
            jwksUri(dir, rsaKey),
            props.getProperty("expected-issuer", ""),
            audience,
            algorithms
        );
        final var processor = new BearerJwtRealmConfigImpl(config).jwtProcessor();
        assertNotNull(processor);

        // RS256 + audience "odl-admin" (one of two) + correct issuer → accepted
        final var valid = signedJwt(rsaKey, JWSAlgorithm.RS256, new JWTClaimsSet.Builder()
            .issuer(props.getProperty("expected-issuer"))
            .audience("odl-admin")
            .expirationTime(futureDate())
            .build());
        assertDoesNotThrow(() -> processor.process(valid, null));

        // RS384 (second entry in allowed-algorithms) + audience "odl-app" → accepted
        final var rs384 = signedJwt(rsaKey, JWSAlgorithm.RS384, new JWTClaimsSet.Builder()
            .issuer(props.getProperty("expected-issuer"))
            .audience("odl-app")
            .expirationTime(futureDate())
            .build());
        assertDoesNotThrow(() -> processor.process(rs384, null));

        // RS512 is not in allowed-algorithms → rejected without a JWKS fetch
        final var disallowed = signedJwt(rsaKey, JWSAlgorithm.RS512, new JWTClaimsSet.Builder()
            .issuer(props.getProperty("expected-issuer"))
            .audience("odl-app")
            .expirationTime(futureDate())
            .build());
        assertThrows(Exception.class, () -> processor.process(disallowed, null));

        // Wrong issuer → rejected even with a valid algorithm and audience
        final var wrongIss = signedJwt(rsaKey, JWSAlgorithm.RS256, new JWTClaimsSet.Builder()
            .issuer("https://evil.example.com/realms/odl")
            .audience("odl-app")
            .expirationTime(futureDate())
            .build());
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
