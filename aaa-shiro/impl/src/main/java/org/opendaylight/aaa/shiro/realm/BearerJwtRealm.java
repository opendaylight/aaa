/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.JWTProcessor;
import java.text.ParseException;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Realm implementation for Bearer header holding JWT tokens.
 *
 * <p>Implementation for Bearer authorization headers. We parse the user claim to create
 * {@link AuthenticationInfo} and the roles claim to create {@link AuthorizationInfo}.
 *
 * <p>When a {@link BearerJwtRealmConfig} is available, the JWT is fully verified: signature
 * (via JWKS), issuer, audience, expiration and not-before. Without configuration the realm
 * falls back to accepting any well-formed JWT without verification — a WARN is emitted for each
 * such token.
 *
 * <p>The expected input from the request is:
 * {@code Authorization: Bearer [JWT Token]}
 */
public final class BearerJwtRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(BearerJwtRealm.class);
    // TODO make this configurable for different identity providers
    private static final String USER_CLAIM = "preferred_username";
    private static final String ROLE_CLAIM = "groups";
    private static final ThreadLocal<JWTProcessor<SecurityContext>> PROCESSOR_TL = new ThreadLocal<>();

    private final JWTProcessor<SecurityContext> jwtProcessor;

    public BearerJwtRealm() {
        this.jwtProcessor = PROCESSOR_TL.get();
    }

    /**
     * Prepare this realm for loading by Shiro's {@code ReflectionBuilder}.
     *
     * <p>Must be called before
     * {@code configure()} and the returned {@link Registration} closed afterwards.
     *
     * @param config optional JWT verification configuration; {@code null} disables verification
     * @return a {@link Registration} that cleans up the thread-local when closed
     */
    public static Registration prepareForLoad(final BearerJwtRealmConfig config) {
        PROCESSOR_TL.set(config != null ? config.jwtProcessor() : null);
        return PROCESSOR_TL::remove;
    }

    @Override
    public boolean supports(final AuthenticationToken token) {
        return token instanceof BearerToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        if (!(token instanceof BearerToken bearerToken)) {
            throw new AuthenticationException("Token is not BearerToken: " + token.getClass());
        }

        final var claims = parseClaims(bearerToken.getToken());

        final String username;
        try {
            username = claims.getStringClaim(USER_CLAIM);
        } catch (ParseException e) {
            throw new AuthenticationException("Invalid JWT user claim data", e);
        }
        if (username == null || username.isBlank()) {
            throw new AuthenticationException("Required JWT user claim value is empty");
        }

        final Set<String> roles;
        try {
            roles = parseRoles(claims);
        } catch (ParseException e) {
            throw new AuthenticationException("Invalid JWT groups claim data", e);
        }

        final var odlPrincipal = ODLPrincipalImpl.createODLPrincipal(username, null, username, roles);
        return new SimpleAuthenticationInfo(odlPrincipal, bearerToken.getToken(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final var primary = principalCollection.getPrimaryPrincipal();
        if (primary instanceof ODLPrincipal odlPrincipal) {
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        }
        LOG.error("Unsupported principal {}", primary.getClass());
        return new SimpleAuthorizationInfo();
    }

    private JWTClaimsSet parseClaims(final String token) {
        if (jwtProcessor != null) {
            try {
                return jwtProcessor.process(JWTParser.parse(token), null);
            } catch (ParseException e) {
                throw new AuthenticationException("Failed to parse JWT", e);
            } catch (BadJOSEException e) {
                throw new AuthenticationException("JWT verification failed: " + e.getMessage(), e);
            } catch (JOSEException e) {
                throw new AuthenticationException("JWT processing error", e);
            }
        }

        LOG.warn("No JWT verification configured — accepting unverified Bearer token");
        try {
            return JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new AuthenticationException("Failed to parse provided JWT claims", e);
        }
    }

    private static Set<String> parseRoles(final JWTClaimsSet claims) throws ParseException {
        final var groups = claims.getStringListClaim(ROLE_CLAIM);
        if (groups == null) {
            LOG.warn("JWT has no roles claim; granting no roles");
            return Set.of();
        }
        return Set.copyOf(groups);
    }
}
