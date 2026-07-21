/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * {@link Oauth2ProxyHeaderParserConfig} ({@code org.opendaylight.aaa.shiro.oauth2proxyheaderparser.cfg}).
 * The injected {@link Oauth2ProxyHeaderParser} is the live OSGi service (accessed through the damped
 * Blueprint reference proxy), so configuration changes apply to new requests without a restart.
 *
 * <p>The {@code parser} field is captured once, in the constructor, and kept for the life of this
 * filter instance — safe only because the value flowing in through {@link #prepareForLoad} originates
 * from the Blueprint {@code <reference>} in {@code impl-blueprint.xml}, i.e. it already is the damped
 * proxy, not a plain service instance. Holding a plain OSGi Declarative Services {@literal @Reference}
 * the same way would not get the same freshness; see {@link Oauth2ProxyHeaderParser} for that
 * distinction and for how such consumers can obtain an equivalent reference proxy of their own.
 *
 * <p><strong>Security prerequisite:</strong> direct HTTP access to ODL that bypasses the proxy
 * must be blocked at the network level. Failure to do so allows any caller to forge these headers
 * and authenticate as an arbitrary user.
 */
@NonNullByDefault
public final class Oauth2ProxyHeaderFilter extends AuthenticatingFilter {
    private static final ThreadLocal<@Nullable Oauth2ProxyHeaderParser> PARSER_TL = new ThreadLocal<>();

    private final Oauth2ProxyHeaderParser parser;

    public Oauth2ProxyHeaderFilter() {
        this(requireNonNull(PARSER_TL.get()));
    }

    /**
     * Prepares this class for loading by Shiro's reflection-based instantiation. Must be called
     * (and the returned {@link Registration} kept open) before Shiro calls the no-arg constructor.
     *
     * @param parser the parser to inject, if provided value is {@code null} a new parser with default configuration
     *               is set instead
     * @return a {@link Registration} that clears the thread-local when closed
     */
    public static Registration prepareForLoad(final @Nullable Oauth2ProxyHeaderParser parser) {
        PARSER_TL.set(requireNonNullElseGet(parser,
            () -> new Oauth2ProxyHeaderParserImpl(new Oauth2ProxyHeaderParserConfigImpl())));
        return PARSER_TL::remove;
    }

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        return parser.parseToken(request);
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        final var user = parser.parseUser(request);
        if (user != null) {
            return executeLogin(request, response);
        }
        WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
