/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.Collections;
import java.util.List;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
public class Oauth2ProxyHeaderFilter extends AuthenticatingFilter {
    // ODL is set as upstream of OAuth2-Proxy thus X-Forwarded instead of X-Auth-Request headers
    private static final String PROXY_HEADER_USER = "X-Forwarded-User";
    private static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        final var httpRequest = WebUtils.toHttp(request);
        final var user = httpRequest.getHeader(PROXY_HEADER_USER);
        final var groupHeader = httpRequest.getHeaders(PROXY_HEADER_GROUPS);
        final var groups = groupHeader != null ? Collections.list(groupHeader) : List.<String>of();
        return new Oauth2ProxyHeaderToken(groups, user);
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        final var token = WebUtils.toHttp(request).getHeader(PROXY_HEADER_USER);

        if (token != null && !token.isEmpty()) {
            return executeLogin(request, response);
        }

        final var httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
