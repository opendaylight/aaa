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
import java.util.List;
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

public final class Oauth2ProxyRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyRealm.class);
    private static final String SDN_DOMAIN = "sdn";

    @Override
    public boolean supports(final AuthenticationToken token) {
        return token instanceof BearerToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        if (!(token instanceof BearerToken bearerToken)) {
            throw new AuthenticationException("Only BearerToken is supported by Oauth2ProxyRealm");
        }

        final JWTClaimsSet claims;
        try {
            claims = JWTParser.parse(bearerToken.getToken()).getJWTClaimsSet();
        } catch (ParseException e) {
            throw new AuthenticationException("Failed to parse JWT token", e);
        }

        final String username;
        try {
            username = claims.getStringClaim("preferred_username");
        } catch (ParseException e) {
            throw new AuthenticationException("Invalid preferred_username claim in JWT", e);
        }
        if (username == null || username.isBlank()) {
            throw new AuthenticationException("JWT missing required claim: preferred_username");
        }

        final Set<String> roles;
        try {
            roles = parseRoles(claims);
        } catch (ParseException e) {
            throw new AuthenticationException("Invalid groups claim in JWT", e);
        }

        final String userId = username + "@" + SDN_DOMAIN;
        final ODLPrincipal odlPrincipal = ODLPrincipalImpl.createODLPrincipal(username, SDN_DOMAIN, userId, roles);
        return new SimpleAuthenticationInfo(odlPrincipal, bearerToken.getToken(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final var primary = principalCollection.getPrimaryPrincipal();
        if (primary instanceof ODLPrincipal odlPrincipal) {
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        }
        LOG.error("Unsupported principal {}", primary);
        return new SimpleAuthorizationInfo();
    }

    private static Set<String> parseRoles(final JWTClaimsSet claims) throws ParseException {
        final List<String> groups = claims.getStringListClaim("groups");
        if (groups == null) {
            LOG.warn("JWT has no 'groups' claim; granting no roles");
            return Set.of();
        }
        return Set.copyOf(groups);
    }
}
