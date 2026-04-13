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

public class Oauth2ProxyFilter extends AuthenticatingFilter {
    private static final String PROXY_HEADER_USER = "X-Forwarded-User";
    private static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    public record Oauth2ProxyToken(String groups, String user) implements AuthenticationToken {
        @Override
        public Object getPrincipal() {
            return groups;
        }

        @Override
        public Object getCredentials() {
            return user;
        }
    }

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String user = httpRequest.getHeader(PROXY_HEADER_USER);
        String groups = httpRequest.getHeader(PROXY_HEADER_GROUPS);
        return new Oauth2ProxyToken(groups, user);
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        String token = ((HttpServletRequest) request).getHeader(PROXY_HEADER_USER);

        if (token != null) {
            return executeLogin(request, response);
        }

        // or redirect to oauth2-proxy's?
        var httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
