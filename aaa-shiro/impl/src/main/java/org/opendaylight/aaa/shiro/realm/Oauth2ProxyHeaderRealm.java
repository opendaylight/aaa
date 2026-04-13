/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.filters.Oauth2ProxyHeaderToken;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

/**
 * Realm that relies on user being successfully authorized by identity provider server. Process information
 * forwarded by oauth2-proxy, authorize and authenticate user based on that.
 */
public final class Oauth2ProxyHeaderRealm extends AuthorizingRealm {
    public Oauth2ProxyHeaderRealm() {
        setAuthenticationTokenClass(Oauth2ProxyHeaderToken.class);
        setCredentialsMatcher(new AllowAllCredentialsMatcher());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        if (!(token instanceof Oauth2ProxyHeaderToken(final List<String> groups, final String user))) {
            throw new AuthenticationException("Only Oauth2ProxyHeaderToken is supported by Oauth2ProxyHeaderRealm");
        }
        final var odlPrincipal = ODLPrincipalImpl.createODLPrincipal(user, null, user, parseRoles(groups));
        return new SimpleAuthenticationInfo(odlPrincipal, token.getCredentials(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final var primary = principalCollection.getPrimaryPrincipal();
        if (primary instanceof ODLPrincipal odlPrincipal) {
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        }
        return new SimpleAuthorizationInfo();
    }

    /**
     * Parse roles from X-Forwarded-Groups header. example: role:global-admin,role:odl-application:admin
     * roles are separated by "," and each role can have namespace with ":" as separator
     * we want to get role with its namespace but without "role:" at the beginning
     *
     * @param headers A List of header strings
     * @return set of parsed roles
     */
    @VisibleForTesting
    static Set<String> parseRoles(final List<String> headers) {
        // Check if the list itself is null or empty
        if (headers == null || headers.isEmpty()) {
            return Set.of();
        }
        final var parsedRoles = new HashSet<String>();
        // Iterate through each header string provided
        for (var headerValue : headers) {
            // Skip null or entirely empty headers
            if (headerValue == null || headerValue.trim().isEmpty()) {
                continue;
            }
            // Remove control characters
            headerValue = CharMatcher.javaIsoControl().removeFrom(headerValue);
            // Split by comma
            final var roles = headerValue.split(",");
            for (var possibleRole : roles) {
                // Strip leading/trailing whitespace
                var role = possibleRole.strip();
                // Strip optional "role:" prefix
                role = role.replaceFirst("^role:", "");
                // Check emptiness
                if (role.isEmpty()) {
                    continue;
                }
                parsedRoles.add(role);
            }
        }
        // Return immutable set
        return Collections.unmodifiableSet(parsedRoles);
    }
}
