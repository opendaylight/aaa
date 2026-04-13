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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Requires the requesting user to be {@link org.apache.shiro.subject.Subject#isAuthenticated() authenticated} for the
 * request to continue, and if they're not, requires oauth2 forwarded headers after successful user authorization on
 * identity provider server.
 * The {@link #onAccessDenied(ServletRequest, ServletResponse)} method will
 * only be called if the subject making the request is not
 * {@link org.apache.shiro.subject.Subject#isAuthenticated() authenticated}
 */
public class Oauth2ProxyTokenFilter extends AuthenticatingFilter {
    // ODL is set as upstream of OAuth2-Proxy thus X-Forwarded instead of X-Auth-Request headers
    private static final String PROXY_HEADER_USER = "X-Forwarded-User";
    private static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        final var httpRequest = WebUtils.toHttp(request);
        final var user = httpRequest.getHeader(PROXY_HEADER_USER);
        final var groups = httpRequest.getHeaders(PROXY_HEADER_GROUPS);
        return new Oauth2ProxyToken(groups, user);
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        final var token = ((HttpServletRequest) request).getHeader(PROXY_HEADER_USER);

        if (token != null) {
            return executeLogin(request, response);
        }

        var httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
