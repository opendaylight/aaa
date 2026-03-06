/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Oauth2ProxyRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyRealm.class);

    private static final ThreadLocal<RealmAuthProvider> AUTHENICATORS_TL = new ThreadLocal<>();
    private static final ThreadLocal<AuthenticationService> AUTH_SERVICE_TL = new ThreadLocal<>();

    private final RealmAuthProvider realmAuthProvider;
    private final AuthenticationService authService;

    public Oauth2ProxyRealm() {
        this(verifyLoad(AUTH_SERVICE_TL), verifyLoad(AUTHENICATORS_TL));
    }

    public Oauth2ProxyRealm(final AuthenticationService authService, final RealmAuthProvider realmAuthProvider) {
        this.authService = requireNonNull(authService);
        this.realmAuthProvider = requireNonNull(realmAuthProvider);
        super.setName("OAuth2ProxyRealm");
    }

    public static Registration prepareForLoad(final AuthenticationService authService,
        final RealmAuthProvider realmAuthProvider) {
        AUTH_SERVICE_TL.set(requireNonNull(authService));
        AUTHENICATORS_TL.set(requireNonNull(realmAuthProvider));
        return () -> {
            AUTH_SERVICE_TL.remove();
            AUTHENICATORS_TL.remove();
        };
    }

    private static <T> T verifyLoad(final ThreadLocal<T> threadLocal) {
        return verifyNotNull(threadLocal.get(), "OAuth2ProxyRealm loading not prepared");
    }

    @Override
    public boolean supports(final AuthenticationToken token) {
        return token instanceof BearerToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        final var bearerToken = (BearerToken) token;
        // TODO use values from headers
        return new SimpleAuthenticationInfo("admin", "admin", "admin");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        // TODO implement roles verification against 3rd party oauthZ system?
        return new SimpleAuthorizationInfo();
    }
}
