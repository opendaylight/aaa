/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.shiro.filters.Oauth2ProxyFilter;

public final class Oauth2ProxyRealm extends AuthorizingRealm {

    @Override
    public boolean supports(final AuthenticationToken token) {
        return token instanceof Oauth2ProxyFilter.Oauth2ProxyToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        // TODO parse roles from JWT?
        // we need to configure keycloak to pass roles/groups into token
        // currently we have additional header:
        // X-Auth-Request-Groups with the value: role:global-admin,role:odl-application:admin
        final var groups = (String) principalCollection.getPrimaryPrincipal();
        final var roles = Arrays.stream(groups.split(","))
            .map(s -> s.split(":"))
            .map(s -> s[s.length - 1])
            .collect(Collectors.toSet());
        return new SimpleAuthorizationInfo(roles);
    }
}
