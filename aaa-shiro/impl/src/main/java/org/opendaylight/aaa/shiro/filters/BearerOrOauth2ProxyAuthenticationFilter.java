/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Authentication filter that accepts both OAuth2 Proxy forwarded-user headers and
 * {@code Authorization: Bearer <JWT>} in a single Shiro filter-chain step.
 *
 * <p>Execution order within a single request:
 * <ol>
 *   <li>Any {@code filterchain.cfg} filter that runs before the Shiro {@code AAAShiroFilter} may
 *       authenticate the request and bind a {@link org.apache.shiro.subject.Subject} to the
 *       thread. If authentication already succeeded, {@code isAccessAllowed} returns {@code true}
 *       and this filter passes the request through without touching any header.</li>
 *   <li>If the request carries an {@code X-Forwarded-User} header (set by OAuth2 Proxy after
 *       successful IdP authentication), this filter creates an {@link Oauth2ProxyToken}, which
 *       Shiro routes to {@link org.opendaylight.aaa.shiro.realm.Oauth2ProxyTokenRealm}.</li>
 *   <li>If the request carries {@code Authorization: Bearer …} this filter extracts the raw JWT
 *       string and creates a {@link org.apache.shiro.authc.BearerToken}, which Shiro routes to
 *       {@link org.opendaylight.aaa.shiro.realm.BearerJwtRealm}.</li>
 *   <li>If neither credential is present, or authentication fails, a {@code 401 Unauthorized}
 *       response is sent with a {@code WWW-Authenticate: Bearer} challenge.</li>
 * </ol>
 *
 * <p>Register this filter in {@code aaa-app-config.xml} under the name {@code authcBearerOrProxy}
 * and reference it in the URL patterns section instead of {@code authcBearer}. See the ODL AAA
 * user guide for a complete configuration example.
 */
public class BearerOrOauth2ProxyAuthenticationFilter extends BearerHttpAuthenticationFilter {

    private static final String PROXY_HEADER_USER = "X-Forwarded-User";
    private static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    /**
     * Returns {@code true} when the request carries an {@code X-Forwarded-User} header set by
     * OAuth2 Proxy, or an {@code Authorization: Bearer} header (case-insensitive).
     */
    @Override
    protected boolean isLoginAttempt(final ServletRequest request, final ServletResponse response) {
        return WebUtils.toHttp(request).getHeader(PROXY_HEADER_USER) != null
            || super.isLoginAttempt(request, response);
    }

    /**
     * Dispatches to the right token type based on which credential is present.
     *
     * <ul>
     *   <li>{@code X-Forwarded-User} present — returns an {@link Oauth2ProxyToken} carrying the
     *       forwarded user identity and groups; routed to {@code Oauth2ProxyTokenRealm}.</li>
     *   <li>{@code Bearer …} — returns a {@link org.apache.shiro.authc.BearerToken} containing
     *       the raw JWT string; routed to {@code BearerJwtRealm}.</li>
     * </ul>
     */
    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        final var httpRequest = WebUtils.toHttp(request);
        final var user = httpRequest.getHeader(PROXY_HEADER_USER);
        if (user != null) {
            return new Oauth2ProxyToken(httpRequest.getHeaders(PROXY_HEADER_GROUPS), user);
        }
        return super.createToken(request, response);
    }
}
