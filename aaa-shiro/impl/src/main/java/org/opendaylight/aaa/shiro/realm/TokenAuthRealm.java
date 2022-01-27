/*
 * Copyright (c) 2015 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;
import org.opendaylight.aaa.shiro.realm.util.TokenUtils;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.aaa.shiro.web.env.ThreadLocals;
import org.opendaylight.aaa.tokenauthrealm.auth.TokenAuthenticators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TokenAuthRealm is an adapter between the AAA shiro subsystem and the existing
 * <code>TokenAuth</code> mechanisms. Thus, one can enable use of
 * <code>IDMStore</code> and <code>IDMMDSALStore</code>.
 */
public class TokenAuthRealm extends AuthorizingRealm {

    /**
     * The unique identifying name for <code>TokenAuthRealm</code>.
     */
    private static final String TOKEN_AUTH_REALM_DEFAULT_NAME = "TokenAuthRealm";

    /**
     * The message that is displayed if no <code>TokenAuth</code> interface is available yet.
     */
    private static final String AUTHENTICATION_SERVICE_UNAVAILABLE_MESSAGE =
            "{\"error\":\"Authentication service unavailable\"}";

    /**
     * The message that is displayed if credentials are missing or malformed.
     */
    private static final String FATAL_ERROR_DECODING_CREDENTIALS = "{\"error\":\"Unable to decode credentials\"}";

    /**
     * The message that is displayed if non-Basic Auth is attempted.
     */
    private static final String FATAL_ERROR_BASIC_AUTH_ONLY
            = "{\"error\":\"Only basic authentication is supported by TokenAuthRealm\"}";

    /**
     * The purposefully generic message displayed if <code>TokenAuth</code> is
     * unable to validate the given credentials.
     */
    private static final String UNABLE_TO_AUTHENTICATE = "{\"error\":\"Could not authenticate\"}";

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthRealm.class);

    private final AuthenticationService authenticationService;
    private final TokenStore tokenStore;
    private final TokenAuthenticators tokenAuthenticators;

    public TokenAuthRealm() {
        super.setName(TOKEN_AUTH_REALM_DEFAULT_NAME);
        authenticationService = Objects.requireNonNull(ThreadLocals.AUTH_SETVICE_TL.get());
        tokenStore = ThreadLocals.TOKEN_STORE_TL.get();
        tokenAuthenticators = Objects.requireNonNull(ThreadLocals.TOKEN_AUTHENICATORS_TL.get());
    }

    /*
     * (non-Javadoc)
     *
     * Roles are derived from <code>TokenAuth.authenticate()</code>. Shiro roles
     * are identical to existing IDM roles.
     *
     * @see
     * org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache
     * .shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final Object primaryPrincipal = principalCollection.getPrimaryPrincipal();
        final ODLPrincipal odlPrincipal;
        try {
            odlPrincipal = (ODLPrincipal) primaryPrincipal;
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        } catch (ClassCastException e) {
            LOG.error("Couldn't decode authorization request", e);
        }
        return new SimpleAuthorizationInfo();
    }

    /*
     * (non-Javadoc)
     *
     * Authenticates against any <code>TokenAuth</code> registered with the
     * <code>ServiceLocator</code>
     *
     * @see
     * org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org
     * .apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {
        if (authenticationToken == null) {
            throw new AuthenticationException(FATAL_ERROR_DECODING_CREDENTIALS);
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
            throw new AuthenticationException(FATAL_ERROR_BASIC_AUTH_ONLY, e);
        }

        // if the password is empty, this is an OAuth2 request, not a Basic HTTP
        // Auth request
        if (!Strings.isNullOrEmpty(password)) {
            Map<String, List<String>> headers = HeaderUtils.formHeaders(username, password, domain);
            // iterate over <code>TokenAuth</code> implementations and
            // attempt to
            // authentication with each one
            for (TokenAuth ta : tokenAuthenticators.getTokenAuthCollection()) {
                try {
                    LOG.debug("Authentication attempt using {}", ta.getClass().getName());
                    final Authentication auth = ta.validate(headers);
                    if (auth != null) {
                        LOG.debug("Authentication attempt successful");
                        authenticationService.set(auth);
                        final ODLPrincipal odlPrincipal = ODLPrincipalImpl.createODLPrincipal(auth);
                        return new SimpleAuthenticationInfo(odlPrincipal, password.toCharArray(), getName());
                    }
                } catch (AuthenticationException ae) {
                    LOG.debug("Authentication attempt unsuccessful");
                    throw new AuthenticationException(UNABLE_TO_AUTHENTICATE, ae);
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
        authenticationService.set(auth);
        return auth;
    }
}
