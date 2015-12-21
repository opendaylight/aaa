/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.basic.HttpBasicAuth;
import org.opendaylight.aaa.sts.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TokenAuthRealm is an adapter between the AAA shiro subsystem and the existing
 * <code>TokenAuth</code> mechanisms. Thus, one can enable use of
 * <code>IDMStore</code> and <code>IDMMDSALStore</code>.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class TokenAuthRealm extends AuthorizingRealm {

    /**
     * The unique identifying name for <code>TokenAuthRealm</code>
     */
    private static final String TOKEN_AUTH_REALM_DEFAULT_NAME = "TokenAuthRealm";

    /**
     * The message that is displayed if no <code>TokenAuth</code> interface is
     * available yet
     */
    private static final String AUTHENTICATION_SERVICE_UNAVAILABLE_MESSAGE = "{\"error\":\"Authentication service unavailable\"}";

    /**
     * The message that is displayed if credentials are missing or malformed
     */
    private static final String FATAL_ERROR_DECODING_CREDENTIALS = "{\"error\":\"Unable to decode credentials\"}";

    /**
     * The message that is displayed if non-Basic Auth is attempted
     */
    private static final String FATAL_ERROR_BASIC_AUTH_ONLY = "{\"error\":\"Only basic authentication is supported by TokenAuthRealm\"}";

    /**
     * The purposefully generic message displayed if <code>TokenAuth</code> is
     * unable to validate the given credentials
     */
    private static final String UNABLE_TO_AUTHENTICATE = "{\"error\":\"Could not authenticate\"}";

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthRealm.class);

    /**
     * This is a cached value to allow quick retrieval of roles between
     * <code>doGetAuthenticationInfo</code>, which is called first, and
     * <code>doGetAuthorizationInfo</code>, which is called next.
     */
    private Authentication cachedAuthenticationToken;

    public TokenAuthRealm() {
        super();
        super.setName(TOKEN_AUTH_REALM_DEFAULT_NAME);
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
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return new SimpleAuthorizationInfo(cachedAuthenticationToken.roles());
    }

    /**
     *
     * @param username
     * @param password
     * @return <code>username</code>:<code>password</code>
     */
    static String getUsernamePasswordString(final String username, final String password) {
        return username + HttpBasicAuth.AUTH_SEP + password;
    }

    /**
     *
     * @param credentialToken
     * @return Base64 encoded token
     */
    static String getEncodedToken(final String credentialToken) {
        return Base64.encodeToString(credentialToken.getBytes());
    }

    /**
     *
     * @param encodedToken
     * @return Basic <code>encodedToken</code>
     */
    static String getTokenAuthHeader(final String encodedToken) {
        return HttpBasicAuth.BASIC_PREFIX + encodedToken;
    }

    /**
     *
     * @param tokenAuthHeader
     * @return a map with the basic auth header
     */
    Map<String, List<String>> formHeadersWithToken(final String tokenAuthHeader) {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        final List<String> headerValue = new ArrayList<String>();
        headerValue.add(tokenAuthHeader);
        headers.put(HttpBasicAuth.AUTH_HEADER, headerValue);
        return headers;
    }

    /**
     * Adapter between basic authentication mechanism and existing
     * <code>TokenAuth</code> interface.
     *
     * @param username
     * @param password
     * @return input map for <code>TokenAuth.validate()</code>
     */
    Map<String, List<String>> formHeaders(final String username, final String password) {
        String usernamePasswordToken = getUsernamePasswordString(username, password);
        String encodedToken = getEncodedToken(usernamePasswordToken);
        String tokenAuthHeader = getTokenAuthHeader(encodedToken);
        return formHeadersWithToken(tokenAuthHeader);
    }

    /**
     * Adapter to check for available <code>TokenAuth<code> implementations.
     *
     * @return
     */
    boolean isTokenAuthAvailable() {
        return ServiceLocator.getInstance().getAuthenticationService() != null;
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
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {

        String username;
        String password;

        try {
            username = extractUsername(authenticationToken);
            password = extractPassword(authenticationToken);
        } catch (NullPointerException e) {
            throw new AuthenticationException(FATAL_ERROR_DECODING_CREDENTIALS, e);
        } catch (ClassCastException e) {
            throw new AuthenticationException(FATAL_ERROR_BASIC_AUTH_ONLY, e);
        }

        // check to see if there are TokenAuth implementations available
        if (!isTokenAuthAvailable()) {
            throw new AuthenticationException(AUTHENTICATION_SERVICE_UNAVAILABLE_MESSAGE);
        }

        // if the password is empty, this is an OAuth2 request, not a Basic HTTP
        // Auth request
        if (!Strings.isNullOrEmpty(password)) {
            if (ServiceLocator.getInstance().getAuthenticationService().isAuthEnabled()) {
                Map<String, List<String>> headers = formHeaders(username, password);
                // iterate over <code>TokenAuth</code> implementations and
                // attempt to
                // authentication with each one
                final List<TokenAuth> tokenAuthCollection = ServiceLocator.getInstance()
                        .getTokenAuthCollection();
                for (TokenAuth ta : tokenAuthCollection) {
                    try {
                        LOG.debug("Authentication attempt using {}", ta.getClass().getName());
                        Authentication auth = ta.validate(headers);
                        if (auth != null) {
                            LOG.debug("Authentication attempt successful");
                            ServiceLocator.getInstance().getAuthenticationService().set(auth);
                            this.cachedAuthenticationToken = auth;
                            return new SimpleAuthenticationInfo(username, password.toCharArray(),
                                    getName());
                        }
                    } catch (AuthenticationException ae) {
                        LOG.debug("Authentication attempt unsuccessful");
                        // invalidate cached token
                        cachedAuthenticationToken = null;
                        throw new AuthenticationException(UNABLE_TO_AUTHENTICATE, ae);
                    }
                }
            }
        }

        // extract the authentication token and attempt validation of the token
        final String token = extractUsername(authenticationToken);
        final Authentication auth;
        try {
            auth = validate(token);
            if (auth != null) {
                cachedAuthenticationToken = auth;
                return new SimpleAuthenticationInfo(auth.user(), "", getName());
            }
        } catch (AuthenticationException e) {
            cachedAuthenticationToken = null;
            LOG.info("Unknown OAuth2 Token Access Request", e);
        }

        LOG.debug("Authentication failed: exhausted TokenAuth resources");
        // invalidate cached token
        cachedAuthenticationToken = null;
        return null;
    }

    private Authentication validate(final String token) {
        Authentication auth = ServiceLocator.getInstance().getTokenStore().get(token);
        if (auth == null) {
            throw new AuthenticationException("Could not validate the token " + token);
        } else {
            ServiceLocator.getInstance().getAuthenticationService().set(auth);
        }
        return auth;
    }

    /**
     * extract the username from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    static String extractUsername(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        return (String) authenticationToken.getPrincipal();
    }

    /**
     * extract the password from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    static String extractPassword(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        final UsernamePasswordToken upt = (UsernamePasswordToken) authenticationToken;
        return new String(upt.getPassword());
    }
}
