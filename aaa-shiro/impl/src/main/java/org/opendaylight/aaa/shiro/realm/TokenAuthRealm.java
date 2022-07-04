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
import java.util.List;
import java.util.Map;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;
import org.opendaylight.aaa.shiro.realm.util.TokenUtils;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.aaa.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TokenAuthRealm is an adapter between the AAA shiro subsystem and the existing {@code TokenAuth} mechanisms. Thus, one
 * can enable use of {@code IDMStore} and {@code IDMMDSALStore}.
 */
public class TokenAuthRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthRealm.class);
    private static final ThreadLocal<TokenAuthenticators> AUTHENICATORS_TL = new ThreadLocal<>();
    private static final ThreadLocal<AuthenticationService> AUTH_SERVICE_TL = new ThreadLocal<>();
    private static final ThreadLocal<TokenStore> TOKEN_STORE_TL = new ThreadLocal<>();

    private final TokenAuthenticators authenticators;
    private final AuthenticationService authService;
    private final TokenStore tokenStore;

    public TokenAuthRealm() {
        this(verifyLoad(AUTH_SERVICE_TL), verifyLoad(AUTHENICATORS_TL), TOKEN_STORE_TL.get());
    }

    public TokenAuthRealm(final AuthenticationService authService, final TokenAuthenticators authenticators) {
        this(authService, authenticators, null);
    }

    public TokenAuthRealm(final AuthenticationService authService, final TokenAuthenticators authenticators,
            final @Nullable TokenStore tokenStore) {
        this.authService = requireNonNull(authService);
        this.authenticators = requireNonNull(authenticators);
        this.tokenStore = tokenStore;
        super.setName("TokenAuthRealm");
    }

    public static Registration prepareForLoad(final AuthenticationService authService,
            final TokenAuthenticators authenticators, final @Nullable TokenStore tokenStore) {
        AUTH_SERVICE_TL.set(requireNonNull(authService));
        AUTHENICATORS_TL.set(requireNonNull(authenticators));
        TOKEN_STORE_TL.set(tokenStore);
        return () -> {
            AUTH_SERVICE_TL.remove();
            AUTHENICATORS_TL.remove();
            TOKEN_STORE_TL.remove();
        };
    }

    private static <T> T verifyLoad(final ThreadLocal<T> threadLocal) {
        return verifyNotNull(threadLocal.get(), "TokenAuthRealm loading not prepared");
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Roles are derived from {@code TokenAuth.authenticate()}. Shiro roles are identical to existing IDM roles.
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
     * <p>
     * Authenticates against any {@code TokenAuth} registered with the {@code ServiceLocator}.
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

        // if the password is empty, this is an OAuth2 request, not a Basic HTTP Auth request
        if (!Strings.isNullOrEmpty(password)) {
            Map<String, List<String>> headers = HeaderUtils.formHeaders(username, password, domain);
            // iterate over <code>TokenAuth</code> implementations and
            // attempt to
            // authentication with each one
            for (TokenAuth ta : authenticators.getTokenAuthCollection()) {
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

        // extract the authentication token and attempt validation of the token
        final String token = TokenUtils.extractUsername(authenticationToken);
        try {
            final Authentication auth = validate(token);
            final ODLPrincipal odlPrincipal = ODLPrincipalImpl.createODLPrincipal(auth);
            return new SimpleAuthenticationInfo(odlPrincipal, "", getName());
        } catch (AuthenticationException e) {
            LOG.debug("Unknown OAuth2 Token Access Request", e);
        }

        LOG.debug("Authentication failed: exhausted TokenAuth resources");
        return null;
    }

    private Authentication validate(final String token) {
        if (tokenStore == null) {
            throw new AuthenticationException("Token store not available, could not validate the token " + token);
        }

        final Authentication auth = tokenStore.get(token);
        if (auth == null) {
            throw new AuthenticationException("Could not validate the token " + token);
        }
        authService.set(auth);
        return auth;
    }
}
