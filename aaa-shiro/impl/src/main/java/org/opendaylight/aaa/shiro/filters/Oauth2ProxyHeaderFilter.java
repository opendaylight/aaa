/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Shiro filter that authenticates requests forwarded by an upstream OAuth2-Proxy instance.
 *
 * <p>ODL is deployed as an upstream service behind OAuth2-Proxy. After the proxy authenticates
 * the user against an external identity provider, it injects {@code X-Forwarded-User} and
 * {@code X-Forwarded-Groups} headers into the proxied request. This filter reads those headers
 * and creates an {@link Oauth2ProxyHeaderToken} for the configured realm to process.
 *
 * <p>Security limits (max lengths, max roles, allowed characters) are configurable via
 * {@link Oauth2ProxyHeaderFilterConfig} ({@code org.opendaylight.aaa.shiro.oauth2proxy.cfg}).
 *
 * <p><strong>Security prerequisite:</strong> direct HTTP access to ODL that bypasses the proxy
 * must be blocked at the network level. Failure to do so allows any caller to forge these headers
 * and authenticate as an arbitrary user.
 */
@NonNullByDefault
public final class Oauth2ProxyHeaderFilter extends AuthenticatingFilter {
    private static final ThreadLocal<Oauth2ProxyHeaderFilterConfig> CONFIG_TL = new ThreadLocal<>();
    // ODL is set as upstream of OAuth2-Proxy thus X-Forwarded instead of X-Auth-Request headers
    /**
     * Proxy header containing username.
     */
    static final String PROXY_HEADER_USER = "X-Forwarded-User";
    /**
     * Proxy header containing user roles.
     */
    static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    private final Oauth2ProxyHeaderFilterConfig config;

    public Oauth2ProxyHeaderFilter() {
        this(configFromThreadLocal());
    }

    @VisibleForTesting
    Oauth2ProxyHeaderFilter(final Oauth2ProxyHeaderFilterConfig config) {
        this.config = requireNonNull(config);
    }

    private static Oauth2ProxyHeaderFilterConfig configFromThreadLocal() {
        final var config = CONFIG_TL.get();
        if (config != null) {
            return config;
        }
        return new Oauth2ProxyHeaderFilterConfigImpl();
    }

    /**
     * Prepares this class for loading by Shiro's reflection-based instantiation. Must be called
     * (and the returned {@link Registration} kept open) before Shiro calls the no-arg constructor.
     *
     * @param config the configuration to inject
     * @return a {@link Registration} that clears the thread-local when closed
     */
    public static Registration prepareForLoad(final Oauth2ProxyHeaderFilterConfig config) {
        CONFIG_TL.set(requireNonNull(config));
        return CONFIG_TL::remove;
    }

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        return new Oauth2ProxyHeaderToken(Oauth2ProxyHeaderParser.parseRolesHeader(request, config.maxHeaderLength(),
            config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
            config.allowedCharactersPattern()), Oauth2ProxyHeaderParser
                .parseUser(request, config.maxUserLength(), config.allowedCharactersPattern()));
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        final var user = Oauth2ProxyHeaderParser
            .parseUser(request, config.maxUserLength(), config.allowedCharactersPattern());
        if (user != null) {
            return executeLogin(request, response);
        }
        WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
