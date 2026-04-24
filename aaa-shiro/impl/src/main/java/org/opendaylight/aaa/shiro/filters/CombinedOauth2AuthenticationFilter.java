/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static java.util.Objects.requireNonNull;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication filter that accepts both OAuth2 Proxy forwarded-user headers and
 * {@code Authorization: Bearer <JWT>} in a single Shiro filter-chain step.
 *
 * <p>Execution order within a single request:
 * <ol>
 *   <li>Any {@code filterchain.cfg} filter that runs before the Shiro {@code AAAShiroFilter} may
 *       authenticate the request and bind a {@link org.apache.shiro.subject.Subject} to the
 *       thread. If authentication already succeeded, {@code isAccessAllowed} returns {@code true}
 *       and this filter passes the request through without touching any header.</li>
 *   <li>If the request carries an {@code X-Forwarded-User} header (set by OAuth2 Proxy after
 *       successful IdP authentication), this filter creates an {@link Oauth2ProxyHeaderToken}, which
 *       Shiro routes to {@link org.opendaylight.aaa.shiro.realm.Oauth2ProxyHeaderRealm}.</li>
 *   <li>If the request carries {@code Authorization: Bearer …} this filter extracts the raw JWT
 *       string and creates a {@link org.apache.shiro.authc.BearerToken}, which Shiro routes to
 *       {@link org.opendaylight.aaa.shiro.realm.BearerJwtRealm}.</li>
 *   <li>If neither credential is present, or authentication fails, a {@code 401 Unauthorized}
 *       response is sent with a {@code WWW-Authenticate: Bearer} challenge.</li>
 * </ol>
 *
 * <p><strong>Network-level security requirement for forwarded-header authentication:</strong>
 * This filter accepts {@code X-Forwarded-User} and {@code X-Forwarded-Groups} without
 * cryptographic verification — it trusts whoever set them. This is safe only when the application
 * is reachable exclusively through a trusted OAuth2 Proxy instance that:
 * <ul>
 *   <li>strips any client-supplied {@code X-Forwarded-User} and {@code X-Forwarded-Groups} headers
 *       before forwarding the request, and</li>
 *   <li>sets those headers itself only after successful IdP authentication.</li>
 * </ul>
 * If clients can reach the application directly on any interface or port that bypasses the proxy,
 * they can impersonate arbitrary users by injecting these headers. Enforce the network boundary
 * at the infrastructure level (firewall rules, Kubernetes {@code NetworkPolicy}, etc.) — this
 * filter alone cannot substitute for it.
 *
 * <p>Register this filter in {@code aaa-app-config.xml} under the name {@code authcCombinedOauth2}
 * and reference it in the URL patterns section instead of {@code authcBasic}. See the ODL AAA
 * user guide for a complete configuration example.
 */
@NonNullByDefault
public final class CombinedOauth2AuthenticationFilter extends BearerHttpAuthenticationFilter {
    private static final Logger LOG = LoggerFactory.getLogger(CombinedOauth2AuthenticationFilter.class);

    private static final ThreadLocal<Oauth2ProxyHeaderFilterConfig> CONFIG_TL = new ThreadLocal<>();

    private final Oauth2ProxyHeaderFilterConfig config;

    public CombinedOauth2AuthenticationFilter() {
        this.config = configFromThreadLocal();
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
    protected boolean isAccessAllowed(final ServletRequest request, final ServletResponse response,
            final Object mappedValue) {
        final boolean allowed = super.isAccessAllowed(request, response, mappedValue);
        if (allowed) {
            LOG.debug("Request passed through: subject already authenticated via filterchain");
        }
        return allowed;
    }

    /**
     * Returns {@code true} when the request carries an {@code X-Forwarded-User} header set by
     * OAuth2 Proxy, or an {@code Authorization: Bearer} header (case-insensitive).
     */
    @Override
    protected boolean isLoginAttempt(final ServletRequest request, final ServletResponse response) {
        final var user = Oauth2ProxyHeaderParser
            .parseUser(request, config.maxUserLength(), config.allowedCharactersPattern());
        return (user != null)
            || super.isLoginAttempt(request, response);
    }

    /**
     * Dispatches to the right token type based on which credential is present.
     *
     * <ul>
     *   <li>{@code X-Forwarded-User} present — returns an {@link Oauth2ProxyHeaderToken} carrying the
     *       forwarded user identity and groups; routed to {@code Oauth2ProxyHeaderRealm}.</li>
     *   <li>{@code Bearer …} — returns a {@link org.apache.shiro.authc.BearerToken} containing
     *       the raw JWT string; routed to {@code BearerJwtRealm}.</li>
     * </ul>
     */
    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        final var user = Oauth2ProxyHeaderParser
            .parseUser(request, config.maxUserLength(), config.allowedCharactersPattern());
        if (user != null) {
            LOG.debug("Authenticating via X-Forwarded-User header");
            return new Oauth2ProxyHeaderToken(Oauth2ProxyHeaderParser.parseRolesHeader(request,
                config.maxHeaderLength(), config.maxRolesPerUser(), config.maxRoleLength(), config.headerPattern(),
                config.allowedCharactersPattern()), user);
        }
        LOG.debug("Authenticating via Authorization: Bearer header");
        return super.createToken(request, response);
    }
}
