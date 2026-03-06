/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Realm implementation for Bearer header holding JWT tokens.
 *
 * <p>Implementation for Bearer authorization tokens. We are parsing user claim to create {@link AuthenticationInfo}
 * and roles claim to create {@link AuthorizationInfo}.
 *
 * <p>The expected input from the request is:
 * {@code Authorization: Bearer [JWT Token]}
 */
public final class BearerJwtRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(BearerJwtRealm.class);
    // TODO make this configurable for different identity providers
    private static final String USER_CLAIM = "preferred_username";
    private static final String ROLE_CLAIM = "groups";

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

        final JWTClaimsSet claims;
        try {
            claims = JWTParser.parse(bearerToken.getToken()).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new AuthenticationException("Failed to parse provided JWT claims", e);
        }

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

        final ODLPrincipal odlPrincipal = ODLPrincipalImpl.createODLPrincipal(username, null, username, roles);
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

    private static Set<String> parseRoles(final JWTClaimsSet claims) throws ParseException {
        final var groups = claims.getStringListClaim(ROLE_CLAIM);
        if (groups == null) {
            LOG.warn("JWT has no roles claim; granting no roles");
            return Set.of();
        }
        return Set.copyOf(groups);
    }
}
