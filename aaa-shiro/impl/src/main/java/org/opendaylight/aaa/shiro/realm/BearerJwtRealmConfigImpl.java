/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

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
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

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
@Singleton
@Component(service = BearerJwtRealmConfig.class, configurationPid = "org.opendaylight.aaa.shiro.bearerjwtrealm")
@Designate(ocd = BearerJwtRealmConfigImpl.Configuration.class)
public final class BearerJwtRealmConfigImpl implements BearerJwtRealmConfig {
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
        String allowed$_$algorithms() default "RS256, RS384, RS512, ES256";
    }

    private final @Nullable JWTProcessor<SecurityContext> jwtProcessor;

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor.
     */
    @VisibleForTesting
    BearerJwtRealmConfigImpl(final @Nullable JWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    /**
     * Production Blueprint constructor.
     *
     * <p>When {@code jwksUri} is blank the config is treated as inactive and
     * {@link #jwtProcessor()} returns {@code null}, causing {@link BearerJwtRealm} to skip
     * verification.
     */
    @Activate
    public BearerJwtRealmConfigImpl(final Configuration configuration) throws Exception {
        if (configuration.jwks$_$uri().isBlank()) {
            jwtProcessor = null;
            return;
        }

        final var jwkSource = JWKSourceBuilder.create(new URI(configuration.jwks$_$uri()).toURL()).build();
        final var allowedAlgs = Arrays.stream(configuration.allowed$_$algorithms().split(","))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toUnmodifiableSet());
        final var keySelector = new JWSVerificationKeySelector<>(allowedAlgs, jwkSource);
        final var processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(verifier(configuration.expected$_$issuer(),
            configuration.expected$_$audience()));
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

    @Override
    public @Nullable JWTProcessor<SecurityContext> jwtProcessor() {
        return jwtProcessor;
    }
}
