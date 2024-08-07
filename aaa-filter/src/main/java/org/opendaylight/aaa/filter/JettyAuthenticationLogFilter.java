/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filter;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyAuthenticationLogFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(JettyAuthenticationLogFilter.class);
    private static final String BASIC = "Basic";
    private static final String BASIC_SEP = BASIC + " ";

    private final Map<HttpSession, Authentication> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void init(final FilterConfig newFilterConfig) throws ServletException {
        LOG.debug("Initializing JettyAuthenticationLogFilter");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
        if (request instanceof HttpServletRequestWrapper httpServletWrapper) {
            setRequestAuthenticationFromUserInHeader(httpServletWrapper, useSession(response));
        }
    }

    @Override
    public void destroy() {
        LOG.debug("Destroying JettyAuthenticationLogFilter");
        sessionMap.clear();
    }

    @VisibleForTesting
    Map<HttpSession, Authentication> sessionMap() {
        return Map.copyOf(sessionMap);
    }

    private void setRequestAuthenticationFromUserInHeader(final HttpServletRequestWrapper request,
            final boolean useSession) {
        if (request.getRequest() instanceof Request baseRequest) {
            final var requestSession = baseRequest.getSession();
            if (useSession && sessionMap.containsKey(requestSession)) {
                baseRequest.setAuthentication(sessionMap.get(requestSession));
            } else {
                final var authorization = baseRequest.getHeader("Authorization");
                if (authorization != null && authorization.startsWith(BASIC_SEP)
                    && requestIsNullOrUnauthenticated(baseRequest)) {
                    final var userAndPassword = new String(
                        Base64.getDecoder().decode(authorization.substring(BASIC_SEP.length())), StandardCharsets.UTF_8)
                        .split(":");

                    final var userPrincipal = new AbstractLoginService.UserPrincipal(userAndPassword[0], null);
                    final var defaultUserIdentity = new DefaultUserIdentity(null, userPrincipal, new String[0]);
                    final var userAuthentication = new UserAuthentication(BASIC, defaultUserIdentity);
                    baseRequest.setAuthentication(userAuthentication);
                    LOG.debug("User [{}] has been set in the request authentication", userAuthentication);
                    if (useSession && requestSession != null) {
                        sessionMap.entrySet().removeIf(entry -> userAuthentication.equals(entry.getValue()));
                        sessionMap.put(requestSession, userAuthentication);
                    }
                }
            }
        }
    }

    private static boolean requestIsNullOrUnauthenticated(final Request baseRequest) {
        return baseRequest.getAuthentication() == null
            || Authentication.UNAUTHENTICATED == baseRequest.getAuthentication();
    }

    private static boolean useSession(final ServletResponse servletResponse) {
        if (servletResponse instanceof Response response) {
            final var status = response.getStatus();
            return status < 400 || status >= 500;
        }
        return true;
    }
}
