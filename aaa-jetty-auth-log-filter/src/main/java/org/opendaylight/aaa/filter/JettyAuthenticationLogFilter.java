/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filter;

import static org.apache.shiro.subject.support.DefaultSubjectContext.PRINCIPALS_SESSION_KEY;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.shiro.subject.PrincipalCollection;
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
 * {@code scr:enable org.opendaylight.aaa.filter.JettyAuthenticationLogFilter}
 *
 * <p>Alternativate way to activate it activate it through {@code etc/org.opendaylight.aaa.filterchain.cfg} via setting
 * {@code customFilterList=org.opendaylight.aaa.filter.JettyAuthenticationLogFilter}.
 */
@Component(enabled = false, property = CustomFilterAdapterConstants.FILTERCHAIN_FILTER + "=true")
public final class JettyAuthenticationLogFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(JettyAuthenticationLogFilter.class);
    private static final String BASIC = "Basic";
    private static final String BASIC_SEP = BASIC + " ";

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
            doFilter(unwrapped);
        }
    }

    /**
     * Set Authentication data in the Jetty request to ensure accurate output in Jetty NCSA logs.
     *
     * @param request Jetty request
     */
    private void doFilter(final Request request) {
        if (!requestIsNullOrUnauthenticated(request)) {
            LOG.trace("Request {} is already authenticated", request);
            return;
        }

        // Get the session from the request, or return null if no session was used.
        final var requestSession = request.getSession(false);
        if (requestSession != null) {
            // Set Authentication from Principal provided in the Session.
            final var attribute = requestSession.getAttribute(PRINCIPALS_SESSION_KEY);
            if (attribute instanceof PrincipalCollection collection
                    && collection.getPrimaryPrincipal() instanceof Principal principal) {
                final var defaultUserIdentity = new DefaultUserIdentity(null, principal, new String[0]);
                final var userAuthentication = new UserAuthentication(BASIC, defaultUserIdentity);
                request.setAuthentication(userAuthentication);
                LOG.debug("Session user {} has been set in the request authentication", userAuthentication);
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
        final var userAndPassword = new String(
            Base64.getDecoder().decode(authorization.substring(BASIC_SEP.length())), StandardCharsets.UTF_8).split(":");

        // Create an Authentication class for the Jetty request based on Basic Authentication.
        final var userPrincipal = new AbstractLoginService.UserPrincipal(userAndPassword[0], null);
        final var defaultUserIdentity = new DefaultUserIdentity(null, userPrincipal, new String[0]);
        final var userAuthentication = new UserAuthentication(BASIC, defaultUserIdentity);
        request.setAuthentication(userAuthentication);
        LOG.debug("Basic authentication user {} has been set in the request authentication", userAuthentication);
    }

    @Deactivate
    @Override
    public void destroy() {
        LOG.debug("Destroying JettyAuthenticationLogFilter");
    }

    private static boolean requestIsNullOrUnauthenticated(final Request baseRequest) {
        final var auth = baseRequest.getAuthentication();
        return auth == null || auth == Authentication.UNAUTHENTICATED;
    }
}
