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
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
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
import org.eclipse.jdt.annotation.NonNull;
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
 * user.claim=preferred_username
 * role.claim=roles
 * network.retrying=false
 * expected.type=JWT
 * }</pre>
 */
public final class BearerJwtRealmConfig {
    private final @Nullable JWTProcessor<SecurityContext> jwtProcessor;
    private final @NonNull String userClaim;
    private final @NonNull String roleClaim;
    private final @NonNull String expectedType;

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor and custom claim names.
     * Type checking is disabled (no {@code typ} header validation).
     */
    @VisibleForTesting
    BearerJwtRealmConfig(final @Nullable JWTProcessor<SecurityContext> jwtProcessor, final @NonNull String userClaim,
            final @NonNull String roleClaim) {
        this(jwtProcessor, userClaim, roleClaim, "at+jwt");
    }

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor, custom claim names
     * and an expected {@code typ} header value.
     */
    @VisibleForTesting
    BearerJwtRealmConfig(final @Nullable JWTProcessor<SecurityContext> jwtProcessor, final @NonNull String userClaim,
            final @NonNull String roleClaim, final @NonNull String expectedType) {
        this.jwtProcessor = jwtProcessor;
        this.userClaim = requireNonNull(userClaim);
        this.roleClaim = requireNonNull(roleClaim);
        this.expectedType = expectedType;
    }

    /**
     * Production Blueprint constructor.
     *
     * <p>When {@code jwksUri} is blank the config is treated as inactive and
     * {@link #jwtProcessor()} returns {@code null}, causing {@link BearerJwtRealm} to skip
     * verification.
     *
     * @param jwksUri URL of the JWKS endpoint; blank disables verification
     * @param expectedIssuer expected {@code iss} claim value; required when JWKS is active
     * @param expectedAudience comma-separated expected {@code aud} values; blank skips audience check
     * @param allowedAlgorithms comma-separated JWS algorithm names (e.g. {@code RS256,RS384})
     * @param userClaim JWT claim name used to extract the username
     * @param roleClaim JWT claim name used to extract the list of roles
     * @param expectedType expected {@code typ} JOSE header value (e.g. {@code at+jwt}); blank disables check
     * @param timeToLive how long the fetched JWK set is considered valid in seconds
     * @param cacheRefreshTimeout how early before expiry a background refresh is triggered in seconds
     * @param retrying retry (just once) to overcome network error
     */
    @NonNullByDefault
    public BearerJwtRealmConfig(final String jwksUri, final String expectedIssuer, final String expectedAudience,
            final String allowedAlgorithms, final String userClaim, final String roleClaim,
            final String expectedType, final long timeToLive, final long cacheRefreshTimeout,
            final boolean retrying) throws Exception {
        requireNonNull(jwksUri);
        requireNonNull(expectedIssuer);
        requireNonNull(expectedAudience);
        requireNonNull(allowedAlgorithms);

        this.userClaim = requireNonNull(userClaim);
        this.roleClaim = requireNonNull(roleClaim);
        this.expectedType = requireNonNull(expectedType);

        if (jwksUri.isBlank()) {
            jwtProcessor = null;
            return;
        }

        // RFC 8725 §3.8: issuer validation is mandatory when JWT verification is active
        if (expectedIssuer.isBlank()) {
            throw new IllegalArgumentException("expected.issuer must be configured when jwks.uri is set");
        }

        final var timeToLiveMillis = timeToLive * 1000;
        final var cacheRefreshTimeoutMillis = cacheRefreshTimeout * 1000;
        final var jwkSource = JWKSourceBuilder.create(new URI(jwksUri).toURL())
            .cache(timeToLiveMillis, cacheRefreshTimeoutMillis)
            .retrying(retrying)
            .build();
        final var allowedAlgs = Arrays.stream(allowedAlgorithms.split(","))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toUnmodifiableSet());
        final var keySelector = new JWSVerificationKeySelector<>(allowedAlgs, jwkSource);
        final var processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(verifier(expectedIssuer, expectedAudience));
        // RFC 8725 §3.11: validate the typ header to prevent token type confusion
        if (this.expectedType != null && !expectedType.isBlank()) {
            processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType(this.expectedType)));
        }
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

        // RFC 8725 §3.9: when audience verification is disabled, prohibit tokens that carry an
        // aud claim — they are scoped to a specific recipient and should not be accepted here.
        final Set<String> prohibitedClaims = audience == null ? Set.of("aud") : Set.of();
        return new DefaultJWTClaimsVerifier<>(audience, exactMatchBuilder.build(), Set.of(), prohibitedClaims);
    }

    @Nullable JWTProcessor<SecurityContext> jwtProcessor() {
        return jwtProcessor;
    }

    @NonNull String userClaim() {
        return userClaim;
    }

    @NonNull String roleClaim() {
        return roleClaim;
    }

    @NonNull String expectedType() {
        return expectedType;
    }
}
