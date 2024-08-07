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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter sets the Authentication field with the user from Basic authorization to ensure the correct name
 * is printed in the Jetty NCSA logs.
 *
 * <p>To enable this filter is required to activate it in Karaf console:
 *
 * <p>scr:enable org.opendaylight.aaa.filter.JettyAuthenticationLogFilter
 *
 * <p>Or activate it through configuration file `etc/org.opendaylight.aaa.filterchain.cfg`:
 *
 * <p>customFilterList=org.opendaylight.aaa.filter.JettyAuthenticationLogFilter
 */
@Component(enabled = false, property = CustomFilterAdapterConstants.FILTERCHAIN_FILTER + "=true")
public final class JettyAuthenticationLogFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(JettyAuthenticationLogFilter.class);
    private static final String BASIC = "Basic";
    private static final String BASIC_SEP = BASIC + " ";

    // Maps user authentication to sessions.
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
        // Apply the filter only if processing a Jetty request.
        if (request instanceof HttpServletRequestWrapper wrapper && wrapper.getRequest() instanceof Request unwrapped) {
            doFilter(unwrapped, containValidSession(response));
        }
    }

    /**
     * Set Authentication data in the Jetty request to ensure accurate output in Jetty NCSA logs.
     *
     * @param request Jetty request
     * @param validSession true if ODL used session for authentication.
     */
    private void doFilter(final Request request, final boolean validSession) {
        // Get the session from the request, or return null if no session was used.
        final var requestSession = request.getSession(false);
        if (validSession && requestSession != null) {
            // Check if this session was already processed.
            final var existingAuth = sessionMap.get(requestSession);
            if (existingAuth != null) {
                LOG.trace("Reusing existing authorization for {}", request);
                request.setAuthentication(existingAuth);
                return;
            }
        }

        // Get the user name from the request.
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

        // Create an Authentication class for the Jetty request based on Basic Authentication.
        final var userPrincipal = new AbstractLoginService.UserPrincipal(userAndPassword[0], null);
        final var defaultUserIdentity = new DefaultUserIdentity(null, userPrincipal, new String[0]);
        final var userAuthentication = new UserAuthentication(BASIC, defaultUserIdentity);
        request.setAuthentication(userAuthentication);
        LOG.debug("User [{}] has been set in the request authentication", userAuthentication);

        // Store the session if it is available and valid.
        if (validSession && requestSession != null) {
            // Remove the old session based on Authentication in case of expiry or other conditions.
            sessionMap.values().removeIf(authentication -> userAuthentication.equals(authentication));
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

    /**
     * Return true if it is possible that ODL used a session for authentication instead of Basic authentication.
     *
     * @param servletResponse servlet response
     * @return true if the status code is not [400, 401, 403]
     */
    private static boolean containValidSession(final ServletResponse servletResponse) {
        if (servletResponse instanceof HttpServletResponse response) {
            final var status = response.getStatus();
            return status != HttpStatus.BAD_REQUEST_400 && status != HttpStatus.UNAUTHORIZED_401
                && status != HttpStatus.FORBIDDEN_403;
        }
        return false;
    }
}
