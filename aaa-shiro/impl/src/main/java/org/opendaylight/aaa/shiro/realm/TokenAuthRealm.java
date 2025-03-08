/*
 * Copyright (c) 2015 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;
import org.opendaylight.aaa.shiro.realm.util.TokenUtils;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TokenAuthRealm is an adapter between the AAA shiro subsystem and the existing {@code TokenAuth} mechanisms. Thus, one
 * can enable use of {@code IDMStore} and {@code IDMMDSALStore}.
 */
public class TokenAuthRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthRealm.class);
    private static final ThreadLocal<RealmAuthProvider> AUTHENICATORS_TL = new ThreadLocal<>();
    private static final ThreadLocal<AuthenticationService> AUTH_SERVICE_TL = new ThreadLocal<>();

    private final RealmAuthProvider realmAuthProvider;
    private final AuthenticationService authService;

    public TokenAuthRealm() {
        this(verifyLoad(AUTH_SERVICE_TL), verifyLoad(AUTHENICATORS_TL));
    }

    public TokenAuthRealm(final AuthenticationService authService, final RealmAuthProvider realmAuthProvider) {
        this.authService = requireNonNull(authService);
        this.realmAuthProvider = requireNonNull(realmAuthProvider);
        super.setName("TokenAuthRealm");
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
        return verifyNotNull(threadLocal.get(), "TokenAuthRealm loading not prepared");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Roles are derived from {@code TokenAuth.authenticate()}. Shiro roles are identical to existing IDM roles.
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final var primaryPrincipal = principalCollection.getPrimaryPrincipal();
        if (primaryPrincipal instanceof ODLPrincipal odlPrincipal) {
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        }

        LOG.error("Could not decode authorization request: {} is not a known principal type", primaryPrincipal);
        return new SimpleAuthorizationInfo();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Authenticates against any {@code TokenAuth} registered with the {@code ServiceLocator}.
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {
        if (authenticationToken == null) {
            throw new AuthenticationException("{\"error\":\"Unable to decode credentials\"}");
        }

        final String username;
        final String password;
        final String domain;

        try {
            final String possiblyQualifiedUser = TokenUtils.extractUsername(authenticationToken);
            username = HeaderUtils.extractUsername(possiblyQualifiedUser);
            domain = HeaderUtils.extractDomain(possiblyQualifiedUser);
            password = TokenUtils.extractPassword(authenticationToken);
        } catch (ClassCastException e) {
            throw new AuthenticationException(
                "{\"error\":\"Only basic authentication is supported by TokenAuthRealm\"}", e);
        }

        if (!Strings.isNullOrEmpty(password)) {
            final var headers = HeaderUtils.formHeaders(username, password, domain);
            // iterate over <code>TokenAuth</code> implementations and
            // attempt to
            // authentication with each one
            for (var ta : realmAuthProvider.tokenAuthenticators()) {
                try {
                    LOG.debug("Authentication attempt using {}", ta.getClass().getName());
                    final Authentication auth = ta.validate(headers);
                    if (auth != null) {
                        LOG.debug("Authentication attempt successful");
                        authService.set(auth);
                        final ODLPrincipal odlPrincipal = ODLPrincipalImpl.createODLPrincipal(auth);
                        return new SimpleAuthenticationInfo(odlPrincipal, password.toCharArray(), getName());
                    }
                } catch (AuthenticationException ae) {
                    LOG.debug("Authentication attempt unsuccessful", ae);
                    // Purposefully generic message
                    throw new AuthenticationException("{\"error\":\"Could not authenticate\"}", ae);
                }
            }
        }

        LOG.debug("Authentication failed: exhausted TokenAuth resources");
        return null;
    }
}
