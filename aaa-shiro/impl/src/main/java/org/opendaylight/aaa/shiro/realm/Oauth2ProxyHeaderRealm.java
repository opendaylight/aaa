/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Realm that relies on user being successfully authorized by identity provider server. Process information
 * forwarded by oauth2-proxy, authorize and authenticate user based on that.
 */
public final class Oauth2ProxyHeaderRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderRealm.class);

    public Oauth2ProxyHeaderRealm() {
        setAuthenticationTokenClass(Oauth2ProxyHeaderToken.class);
        setCredentialsMatcher(new AllowAllCredentialsMatcher());
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        if (!(token instanceof Oauth2ProxyHeaderToken(final Set<String> groups, final String user))) {
            throw new AuthenticationException("Only Oauth2ProxyHeaderToken is supported by Oauth2ProxyHeaderRealm");
        }
        if (user == null) {
            throw new AuthenticationException("Oauth2ProxyHeaderToken is missing valid user to authenticate");
        }
        final var odlPrincipal = ODLPrincipalImpl.createODLPrincipal(user, null, user, groups);
        return new SimpleAuthenticationInfo(odlPrincipal, token.getCredentials(), getName());
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
}
