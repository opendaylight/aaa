/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.Locale;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Authentication filter that accepts both {@code Authorization: Bearer <JWT>} and
 * {@code Authorization: Basic <credentials>} headers in a single Shiro filter-chain step.
 *
 * <p>Execution order within a single request:
 * <ol>
 *   <li>Any {@code filterchain.cfg} filter that runs before the Shiro {@code AAAShiroFilter} may
 *       authenticate the request and bind a {@link org.apache.shiro.subject.Subject} to the
 *       thread. If authentication already succeeded, {@code isAccessAllowed} returns {@code true}
 *       and this filter passes the request through without touching the Authorization header.</li>
 *   <li>If the request carries {@code Authorization: Bearer …} this filter extracts the raw JWT
 *       string and creates a {@link BearerToken}, which Shiro routes to
 *       {@link org.opendaylight.aaa.shiro.realm.BearerJwtRealm}.</li>
 *   <li>If the request carries {@code Authorization: Basic …} this filter decodes the
 *       Base64-encoded credentials and creates a {@code UsernamePasswordToken}, which Shiro routes
 *       to {@link org.opendaylight.aaa.shiro.realm.TokenAuthRealm} (or any other realm that
 *       accepts username/password tokens).</li>
 *   <li>If neither header is present, or authentication fails, a {@code 401 Unauthorized} response
 *       is sent with two {@code WWW-Authenticate} challenges — one for Bearer and one for Basic.</li>
 * </ol>
 *
 * <p>Register this filter in {@code aaa-app-config.xml} under the name {@code authcBearerOrBasic}
 * and reference it in the URL patterns section instead of {@code authcBearer} or
 * {@code authcBasic}. See the ODL AAA user guide for a complete configuration example.
 */
public class BearerOrBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter {

    private static final String BEARER_PREFIX = "bearer ";

    /**
     * Returns {@code true} when the Authorization header starts with {@code Bearer} (case-insensitive)
     * or with {@code Basic} (handled by the parent class).
     */
    @Override
    protected boolean isLoginAttempt(final String authzHeader) {
        return authzHeader.toLowerCase(Locale.ENGLISH).startsWith(BEARER_PREFIX)
            || super.isLoginAttempt(authzHeader);
    }

    /**
     * Dispatches to the right token type based on the Authorization header scheme.
     *
     * <ul>
     *   <li>{@code Bearer …} — returns a {@link BearerToken} containing the raw JWT string.</li>
     *   <li>{@code Basic …} — delegates to the parent, which Base64-decodes the credentials and
     *       returns a {@code UsernamePasswordToken}.</li>
     *   <li>Absent / other — delegates to the parent (returns an empty-credentials token).</li>
     * </ul>
     */
    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        final var authzHeader = getAuthzHeader(request);
        if (authzHeader != null && authzHeader.toLowerCase(Locale.ENGLISH).startsWith(BEARER_PREFIX)) {
            final var token = authzHeader.substring(BEARER_PREFIX.length()).strip();
            return new BearerToken(token, request.getRemoteHost());
        }
        return super.createToken(request, response);
    }

    /**
     * Sends a {@code 401 Unauthorized} response advertising both {@code Bearer} and {@code Basic}
     * authentication schemes via two {@code WWW-Authenticate} response headers.
     */
    @Override
    protected boolean sendChallenge(final ServletRequest request, final ServletResponse response) {
        final var httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        final var realm = "\"" + getApplicationName() + "\"";
        httpResponse.setHeader(AUTHENTICATE_HEADER, "Bearer realm=" + realm);
        httpResponse.addHeader(AUTHENTICATE_HEADER, "Basic realm=" + realm);
        return false;
    }
}
