/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filterchain.filters;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recreates the Chain of Responsibility pattern for <code>javax.servlet.Filter</code>(s). This Adapter adds the
 * capability to dynamically insert links into the filter chain.
 *
 * <code>CustomFilterAdapter.doFilter(...)</code> calls
 * <code>AAAFilterChain.doFilter(...)</code>, which honors the injected filter
 * chain links, and then continues the original filter chain.
 *
 * <p>
 * This code was designed specifically to work with the common, generic
 * <code>javax.servlet.Filter</code> interface; thus, certain choices, such as
 * creating a new <code>AAAFilterChain</code> per request, were necessary to
 * preserve the existing API contracts (i.e., the injected chain is stored as a
 * local variable in <code>AAAFilterChain</code> so it may be used in existing
 * methods (could not be passed as a parameter), and if a new chain was not
 * spawned each time, there is a risk that the existingChain changes in the
 * middle of requests, causing inconsistent behavior.
 */
public class CustomFilterAdapter implements Filter, CustomFilterAdapterListener {
    private static final Logger LOG = LoggerFactory.getLogger(CustomFilterAdapter.class);
    private static final String BASIC = "Basic";
    private static final String BASIC_SEP = BASIC + " ";

    private final CustomFilterAdapterConfiguration customFilterAdapterConfig;
    private final Map<HttpSession, Authentication> sessionMap;

    private FilterConfig filterConfig;

    /**
     * Stores the injected filter chain.
     */
    private volatile ImmutableList<Filter> injectedFilterChain = ImmutableList.of();

    public CustomFilterAdapter(final CustomFilterAdapterConfiguration customFilterAdapterConfig) {
        this.customFilterAdapterConfig = customFilterAdapterConfig;
        sessionMap = new HashMap<>();
    }

    @Override
    public void destroy() {
        LOG.info("Destroying CustomFilterAdapter");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        // chain is the existing chain of responsibility, and filterChain
        // contains the new links to inject into the existing chain. Since
        // Jersey spawns <code>chain</code> for each request, a new chain
        final var localFilterChain = injectedFilterChain;
        if (localFilterChain.isEmpty()) {
            chain.doFilter(request, response);
        } else {
            AAAFilterChain.createAAAFilterChain().doFilter(request, response, chain, localFilterChain);
        }
        if (request instanceof HttpServletRequestWrapper requestWrapper) {
            setRequestAuthenticationFromUserInHeader(requestWrapper, useSession(response));
        }
    }

    @Override
    public void init(final FilterConfig newFilterConfig) throws ServletException {
        LOG.info("Initializing CustomFilterAdapter");

        filterConfig = newFilterConfig;

        // register as a listener for config admin changes
        customFilterAdapterConfig.registerCustomFilterAdapterConfigurationListener(this);
    }

    @Override
    public void updateInjectedFilters(final List<Filter> injectedFilters) {
        injectedFilterChain = ImmutableList.copyOf(injectedFilters);
        if (LOG.isInfoEnabled()) {
            LOG.info("Injecting a new filter chain with {} Filters: {}", injectedFilters.size(),
                injectedFilterChain.stream().map(i -> i.getClass().getSimpleName()).collect(Collectors.joining(",")));
        }
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
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
                    if (useSession && requestSession != null) {
                        removeOldSessionForUser(userAuthentication);
                        sessionMap.put(requestSession, userAuthentication);
                    }
                }
            }
        }
    }

    private void removeOldSessionForUser(final UserAuthentication userAuthentication) {
        sessionMap.entrySet().stream()
            .filter(entry -> userAuthentication.toString().equals(entry.getValue().toString()))
            .map(Map.Entry::getKey)
            .toList()
            .forEach(sessionMap::remove);
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
