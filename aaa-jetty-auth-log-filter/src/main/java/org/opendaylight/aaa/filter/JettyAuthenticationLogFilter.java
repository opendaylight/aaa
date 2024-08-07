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
import java.util.concurrent.ConcurrentMap;
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
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter sets the Authentication field with the user from Basic authorization to ensure the correct name
 * is printed in the NCSA logs.
 *
 * <p>To enable this filter is required to activate it in Karaf console:
 *
 * <p>scr:enable org.opendaylight.aaa.filter.JettyAuthenticationLogFilter
 */
@Component(enabled = false, property = CustomFilterAdapterConstants.FILTERCHAIN_FILTER + "=true")
public final class JettyAuthenticationLogFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(JettyAuthenticationLogFilter.class);
    private static final String BASIC = "Basic";
    private static final String BASIC_SEP = BASIC + " ";

    private final ConcurrentMap<HttpSession, Authentication> sessionMap = new ConcurrentHashMap<>();

    @Activate
    public JettyAuthenticationLogFilter() {
        LOG.info("Activation of JettyAuthenticationLogFilter");
    }

    @Override
    public void init(final FilterConfig newFilterConfig) throws ServletException {
        LOG.debug("Initializing JettyAuthenticationLogFilter");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
        if (request instanceof HttpServletRequestWrapper wrapper && wrapper.getRequest() instanceof Request unwrapped) {
            doFilter(unwrapped, useSession(response));
        }
    }

    private void doFilter(final Request request, final boolean useSession) {
        final var requestSession = request.getSession();
        if (useSession) {
            final var existingAuth = sessionMap.get(requestSession);
            if (existingAuth != null) {
                LOG.trace("Reusing existing authorization for {}", request);
                request.setAuthentication(existingAuth);
                return;
            }
        }

        final var authorization = request.getHeader("Authorization");
        if (authorization == null) {
            LOG.trace("No Authorization header present in {}", request);
            return;
        }
        if (!authorization.startsWith(BASIC_SEP)) {
            LOG.trace("Request {} does not use basic authorization", request);
            return;
        }
        if (!requestIsNullOrUnauthenticated(request)) {
            LOG.trace("Request {} is already authenticated", request);
            return;
        }

        final var userAndPassword = new String(
            Base64.getDecoder().decode(authorization.substring(BASIC_SEP.length())), StandardCharsets.UTF_8).split(":");

        final var userPrincipal = new AbstractLoginService.UserPrincipal(userAndPassword[0], null);
        final var defaultUserIdentity = new DefaultUserIdentity(null, userPrincipal, new String[0]);
        final var userAuthentication = new UserAuthentication(BASIC, defaultUserIdentity);
        request.setAuthentication(userAuthentication);
        LOG.debug("User [{}] has been set in the request authentication", userAuthentication);
        if (useSession && requestSession != null) {
            sessionMap.values().removeIf(value -> userAuthentication.equals(value));
            sessionMap.put(requestSession, userAuthentication);
        }
    }

    @Deactivate
    @Override
    public void destroy() {
        LOG.debug("Destroying JettyAuthenticationLogFilter");
        sessionMap.clear();
    }

    @VisibleForTesting
    Map<HttpSession, Authentication> sessionMap() {
        return Map.copyOf(sessionMap);
    }

    private static boolean requestIsNullOrUnauthenticated(final Request baseRequest) {
        final var auth = baseRequest.getAuthentication();
        return auth == null || auth == Authentication.UNAUTHENTICATED;
    }

    private static boolean useSession(final ServletResponse servletResponse) {
        if (servletResponse instanceof Response response) {
            final var status = response.getStatus();
            return status < 400 || status >= 500;
        }
        return true;
    }
}
