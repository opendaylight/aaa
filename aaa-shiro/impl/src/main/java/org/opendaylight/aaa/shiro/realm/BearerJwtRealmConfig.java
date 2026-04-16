/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * JWT verification configuration for {@link BearerJwtRealm}.
 *
 * <p>When this bean is active with a non-blank {@code jwks.uri}, {@link BearerJwtRealm} performs
 * full JWT verification: signature, issuer, audience, expiration and not-before. Without a
 * configured JWKS URI the realm falls back to accepting any well-formed JWT without verification.
 *
 * <p>Configuration is supplied via {@code etc/org.opendaylight.aaa.shiro.bearerjwtrealm.cfg}:
 * <pre>{@code
 * jwks.uri=http(s)://keycloak.local:8080/realms/odl-realm/protocol/openid-connect/certs
 * expected.issuer=http(s)://keycloak.local:8080/realms/odl-realm
 * expected.audience=odl-application
 * allowed.algorithms=RS256
 * }</pre>
 */
public final class BearerJwtRealmConfig {
    private final @Nullable JWTProcessor<SecurityContext> jwtProcessor;

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor.
     */
    @VisibleForTesting
    BearerJwtRealmConfig(final @Nullable JWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    /**
     * Production Blueprint constructor.
     *
     * <p>When {@code jwksUri} is blank the config is treated as inactive and
     * {@link #jwtProcessor()} returns {@code null}, causing {@link BearerJwtRealm} to skip
     * verification.
     *
     * @param jwksUri URL of the JWKS endpoint; blank disables verification
     * @param expectedIssuer expected {@code iss} claim value; blank skips issuer check
     * @param expectedAudience comma-separated expected {@code aud} values; blank skips audience check
     * @param allowedAlgorithms comma-separated JWS algorithm names (e.g. {@code RS256,RS384})
     */
    @NonNullByDefault
    public BearerJwtRealmConfig(final String jwksUri, final String expectedIssuer,
            final String expectedAudience, final String allowedAlgorithms) throws Exception {
        requireNonNull(jwksUri);
        requireNonNull(expectedIssuer);
        requireNonNull(expectedAudience);
        requireNonNull(allowedAlgorithms);

        if (jwksUri.isBlank()) {
            jwtProcessor = null;
            return;
        }

        final var jwkSource = JWKSourceBuilder.create(new URI(jwksUri).toURL()).build();
        final var allowedAlgs = Arrays.stream(allowedAlgorithms.split(","))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toUnmodifiableSet());
        final var keySelector = new JWSVerificationKeySelector<>(allowedAlgs, jwkSource);
        final var processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(verifier(expectedIssuer, expectedAudience));
        jwtProcessor = processor;
    }

    private static DefaultJWTClaimsVerifier<SecurityContext> verifier(
            final String expectedIssuer, final String expectedAudience) {
        final var exactMatchBuilder = new JWTClaimsSet.Builder();
        if (!expectedIssuer.isBlank()) {
            exactMatchBuilder.issuer(expectedIssuer);
        }

        final Set<String> audience;
        if (expectedAudience.isBlank()) {
            audience = null;
        } else {
            audience = Arrays.stream(expectedAudience.split(","))
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        }

        return new DefaultJWTClaimsVerifier<>(audience, exactMatchBuilder.build(), Set.of(), null);
    }

    @Nullable JWTProcessor<SecurityContext> jwtProcessor() {
        return jwtProcessor;
    }
}
