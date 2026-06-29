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
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link BearerJwtRealmConfig}.
 *
 * <p>When this component is active with a non-blank {@code jwks.uri}, {@link BearerJwtRealm} performs
 * full JWT verification: signature, issuer, audience, expiration and not-before. Without a
 * configured JWKS URI the realm falls back to accepting any well-formed JWT without verification.
 *
 * <p>Configuration is supplied via {@code etc/org.opendaylight.aaa.shiro.bearerjwtrealm.cfg}:
 * <pre>{@code
 * jwks-uri=http(s)://keycloak.local:8080/realms/odl-realm/protocol/openid-connect/certs
 * expected-issuer=http(s)://keycloak.local:8080/realms/odl-realm
 * expected-audience=odl-application
 * allowed-algorithms=RS256
 * user-claim=preferred_username
 * role-claim=groups
 * cache-timetolive-seconds=300
 * cache-refreshtimeout-seconds=15
 * retry-jwks-retrieval=false
 * expected-type=JWT
 * max-clock-skew-seconds=60
 * }</pre>
 */
@Component(service = BearerJwtRealmConfig.class, configurationPid = "org.opendaylight.aaa.shiro.bearerjwtrealm")
@Designate(ocd = BearerJwtRealmConfigImpl.Configuration.class)
public final class BearerJwtRealmConfigImpl implements BearerJwtRealmConfig {
    private static final Logger LOG = LoggerFactory.getLogger(BearerJwtRealmConfigImpl.class);

    @ObjectClassDefinition
    public @interface Configuration {
        @AttributeDefinition(description = """
            URL of the JSON Web Key Set endpoint used to fetch public keys for JWT signature
            verification (e.g. https://keycloak.local:8080/realms/odl-realm/protocol/openid-connect/certs).
            Keys are cached using nimbus JWKSource.
            Required to enable JWT verification. Leave blank to disable.""")
        String jwks$_$uri() default "";

        @AttributeDefinition(description = """
            Expected value of the 'iss' JWT claim. Leave empty to skip issuer verification.""")
        String expected$_$issuer() default "";

        @AttributeDefinition(description = """
            Expected value(s) of the 'aud' JWT claim (comma-separated).
            Leave empty to skip audience verification.""")
        String expected$_$audience() default "";

        @AttributeDefinition(description = """
            Allowed JWS signing algorithms (comma-separated, e.g. RS256, RS384, RS512, ES256).""")
        String[] allowed$_$algorithms() default {"RS256", "RS384", "RS512", "ES256"};

        @AttributeDefinition(description = """
            JWT claim name used to extract the username.""")
        String user$_$claim() default BearerJwtRealm.DEFAULT_USER_CLAIM;

        @AttributeDefinition(description = """
            JWT claim name used to extract the list of roles.""")
        String role$_$claim() default BearerJwtRealm.DEFAULT_ROLE_CLAIM;

        @AttributeDefinition(description = """
            Expected value of the JOSE typ header (e.g. at+jwt for OAuth 2.0 access tokens per RFC 9068).
            Leave blank to skip typ header validation.""")
        String expected$_$type() default "at+jwt";

        @AttributeDefinition(description = """
            How long the fetched JWK set is considered valid (seconds).""", min = "1")
        long cache$_$timetolive$_$seconds() default 300L;

        @AttributeDefinition(description = """
            How early before cache expiry a background refresh of the JWK set is triggered (seconds).""", min = "1")
        long cache$_$refreshtimeout$_$seconds() default 15L;

        @AttributeDefinition(description = """
            Retry to contact IdP to overcome intermittent network failure.
            When enabled, a single automatic retry is attempted if the initial JWKS
            fetch fails due to a transient network error.""")
        boolean retry$_$jwks$_$retrieval() default false;

        @AttributeDefinition(description = """
            Maximum clock skew to tolerate when verifying the exp and nbf JWT claims, in seconds.
            A non-zero value absorbs minor drift between the token issuer's clock and
            this host. Per RFC 7519 §4.1.4 / §4.1.5, a small leeway is permitted; keep this value
            as small as possible to minimise the token-replay window. Set to 0 to enforce
            strict time checking.""",
            min = "0")
        int max$_$clock$_$skew$_$seconds() default 60;
    }

    private final @Nullable JWTProcessor<SecurityContext> jwtProcessor;
    private final @NonNull String userClaim;
    private final @NonNull String roleClaim;
    private final @NonNull String expectedType;

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor.
     * Type checking is enabled with default value "at+jwt".
     */
    @VisibleForTesting
    BearerJwtRealmConfigImpl(final @Nullable JWTProcessor<SecurityContext> jwtProcessor,
            final @NonNull String userClaim, final @NonNull String roleClaim) {
        this(jwtProcessor, userClaim, roleClaim, "at+jwt");
    }

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor, custom claim names
     * and an expected {@code typ} header value.
     */
    @VisibleForTesting
    BearerJwtRealmConfigImpl(final @Nullable JWTProcessor<SecurityContext> jwtProcessor,
            final @NonNull String userClaim, final @NonNull String roleClaim, final @NonNull String expectedType) {
        this.jwtProcessor = jwtProcessor;
        this.userClaim = requireNonNull(userClaim);
        this.roleClaim = requireNonNull(roleClaim);
        this.expectedType = expectedType;
    }

    /**
     * OSGi constructor. Uses values from supplied {@link Configuration}.
     *
     * <p>When jwks-uri is blank the config is treated as inactive and
     * {@link #jwtProcessor()} returns {@code null}, causing {@link BearerJwtRealm} to skip
     * verification.
     */
    @Activate
    public BearerJwtRealmConfigImpl(final Configuration configuration) {
        userClaim = configuration.user$_$claim();
        roleClaim = configuration.role$_$claim();
        expectedType = configuration.expected$_$type();

        if (configuration.jwks$_$uri().isBlank()) {
            jwtProcessor = null;
            return;
        }

        // RFC 8725 §3.8: issuer validation is mandatory when JWT verification is active
        if (configuration.expected$_$issuer().isBlank()) {
            throw new IllegalArgumentException("expected-issuer must be configured when jwks-uri is set");
        }

        final var timeToLiveMillis = configuration.cache$_$timetolive$_$seconds() * 1000;
        final var cacheRefreshTimeoutMillis = configuration.cache$_$refreshtimeout$_$seconds() * 1000;
        final JWKSource<SecurityContext> jwkSource;
        try {
            jwkSource = JWKSourceBuilder.create(new URI(configuration.jwks$_$uri()).toURL())
            .cache(timeToLiveMillis, cacheRefreshTimeoutMillis)
            .retrying(configuration.retry$_$jwks$_$retrieval())
            .build();
        } catch (final MalformedURLException | URISyntaxException e) {
            LOG.error("Malformed JWKS URL {} could not be correctly parsed", configuration.jwks$_$uri(), e);
            throw new IllegalArgumentException("jwks-uri must be empty or contain valid URL", e);
        }
        final var allowedAlgs = Arrays.stream(configuration.allowed$_$algorithms())
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toUnmodifiableSet());
        final var keySelector = new JWSVerificationKeySelector<>(allowedAlgs, jwkSource);
        final var processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(verifier(configuration.expected$_$issuer(),
            configuration.expected$_$audience(), configuration.max$_$clock$_$skew$_$seconds()));
        // RFC 8725 §3.11: validate the typ header to prevent token type confusion
        if (!expectedType.isBlank()) {
            processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType(this.expectedType)));
        }
        jwtProcessor = processor;
    }

    private static DefaultJWTClaimsVerifier<SecurityContext> verifier(
            final String expectedIssuer, final String expectedAudience, final int maxClockSkewSeconds) {
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
            if (audience.isEmpty()) {
                LOG.error("Malformed expected-audience {} , blank audience is not valid", expectedAudience);
                throw new IllegalArgumentException("Malformed expected-audience, blank audience is not valid");
            }
        }

        // RFC 8725 §3.9: when audience verification is disabled, prohibit tokens that carry an
        // aud claim — they are scoped to a specific recipient and should not be accepted here.
        final Set<String> prohibitedClaims = audience == null ? Set.of("aud") : Set.of();
        final var claimsVerifier = new DefaultJWTClaimsVerifier<>(
            audience, exactMatchBuilder.build(), Set.of(), prohibitedClaims);
        // RFC 7519 §4.1.4 / §4.1.5: permitted leeway for exp/nbf; keep small to minimise replay window.
        claimsVerifier.setMaxClockSkew(maxClockSkewSeconds);
        return claimsVerifier;
    }

    @Override
    public JWTProcessor<SecurityContext> jwtProcessor() {
        return jwtProcessor;
    }

    @Override
    public String userClaim() {
        return userClaim;
    }

    @Override
    public String roleClaim() {
        return roleClaim;
    }

    @Override
    public String expectedType() {
        return expectedType;
    }
}
