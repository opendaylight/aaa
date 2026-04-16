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
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi DS component providing JWT verification configuration for {@link BearerJwtRealm}.
 *
 * <p>When this component is active (i.e. a {@code org.opendaylight.aaa.shiro.bearerjwtrealm.cfg} file is
 * deployed), {@link BearerJwtRealm} performs full JWT verification: signature, issuer, audience,
 * expiration and not-before. Without this component the realm falls back to accepting any
 * well-formed JWT without verification.
 *
 * <p>Example {@code etc/org.opendaylight.aaa.shiro.bearerjwtrealm.cfg}:
 * <pre>
 * jwks.uri=https://keycloak.example.com/realms/master/protocol/openid-connect/certs
 * expected.issuer=https://keycloak.example.com/realms/master
 * expected.audience=odl-controller
 * allowed.algorithms=RS256
 * </pre>
 */
@Component(configurationPid = "org.opendaylight.aaa.shiro.bearerjwtrealm",
           service = BearerJwtRealmConfig.class)
@Designate(ocd = BearerJwtRealmConfig.Config.class)
public final class BearerJwtRealmConfig {
    @ObjectClassDefinition(name = "OpenDaylight AAA Bearer JWT Realm Configuration")
    public @interface Config {
        @AttributeDefinition(name = "JWKS URI",
            description = """
                URL of the JSON Web Key Set endpoint used to fetch public keys for JWT signature
                verification (e.g. https://keycloak.example.com/realms/master/protocol/openid-connect/certs).
                Keys are cached using nimbus RemoteJWKSet DefaultJWKSetCache.""")
        String jwks_uri();

        @AttributeDefinition(name = "Expected Issuer",
            description = "Expected value of the 'iss' JWT claim. Leave empty to skip issuer verification.")
        String expected_issuer() default "";

        @AttributeDefinition(name = "Expected Audience",
            description = "Expected value(s) of the 'aud' JWT claim. Leave empty to skip audience verification.")
        String[] expected_audience() default {};

        @AttributeDefinition(name = "Allowed Algorithms",
            description = "Allowed JWS signing algorithms (e.g. RS256, RS384, RS512, ES256).")
        String[] allowed_algorithms() default { "RS256" };
    }

    private final JWTProcessor<SecurityContext> jwtProcessor;

    /**
     * Package-private constructor for use in unit tests, accepting a pre-built processor.
     */
    @VisibleForTesting
    BearerJwtRealmConfig(final JWTProcessor<SecurityContext> jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Activate
    public BearerJwtRealmConfig(final Config config) throws Exception {
        final JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(config.jwks_uri()));

        final Set<JWSAlgorithm> allowedAlgs = Arrays.stream(config.allowed_algorithms())
            .map(JWSAlgorithm::parse)
            .collect(Collectors.toUnmodifiableSet());

        final var keySelector = new JWSVerificationKeySelector<SecurityContext>(allowedAlgs, jwkSource);

        final var exactMatchBuilder = new JWTClaimsSet.Builder();
        final String issuer = config.expected_issuer();
        if (!issuer.isBlank()) {
            exactMatchBuilder.issuer(issuer);
        }

        final String[] audienceArr = config.expected_audience();
        final Set<String> audience = audienceArr.length > 0 ? Set.of(audienceArr) : null;

        final var claimsVerifier = new DefaultJWTClaimsVerifier<SecurityContext>(
            audience,
            exactMatchBuilder.build(),
            Set.of(),
            null);

        final var processor = new DefaultJWTProcessor<SecurityContext>();
        processor.setJWSKeySelector(keySelector);
        processor.setJWTClaimsSetVerifier(claimsVerifier);

        this.jwtProcessor = processor;
    }

    JWTProcessor<SecurityContext> getJwtProcessor() {
        return jwtProcessor;
    }
}
