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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderFilter.class);
    // ODL is set as upstream of OAuth2-Proxy thus X-Forwarded instead of X-Auth-Request headers
    @VisibleForTesting
    static final String PROXY_HEADER_USER = "X-Forwarded-User";
    @VisibleForTesting
    static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    private static final Pattern ROLE_REGEX = Pattern.compile("^role:");
    private static final ThreadLocal<Oauth2ProxyHeaderFilterConfig> CONFIG_TL = new ThreadLocal<>();

    private final int maxHeaderLength;
    private final int maxRoleLength;
    private final int maxUserLength;
    private final int maxRolesPerUser;
    private final Pattern allowedCharactersPattern;
    private final Pattern headerPattern;

    public Oauth2ProxyHeaderFilter() {
        this(configFromThreadLocal());
    }

    @VisibleForTesting
    Oauth2ProxyHeaderFilter(final Oauth2ProxyHeaderFilterConfig config) {
        requireNonNull(config);
        maxHeaderLength = config.maxHeaderLength();
        maxRoleLength = config.maxRoleLength();
        maxUserLength = config.maxUserLength();
        maxRolesPerUser = config.maxRolesPerUser();
        final var allowedChars = config.allowedChars();
        allowedCharactersPattern = Pattern.compile("^(?:" + allowedChars + ")+$");
        headerPattern = Pattern.compile(
            "^\\s*(?:role:)?(?:" + allowedChars + ")+(?:\\s*,\\s*(?:role:)?(?:" + allowedChars + ")+)*\\s*$");
    }

    private static Oauth2ProxyHeaderFilterConfig configFromThreadLocal() {
        final var config = CONFIG_TL.get();
        if (config != null) {
            return config;
        }
        return new Oauth2ProxyHeaderFilterConfig() {
            @Override
            public int maxHeaderLength() {
                return Oauth2ProxyHeaderFilterConfig.MAX_HEADER_LENGTH_DEFAULT;
            }

            @Override
            public int maxRoleLength() {
                return Oauth2ProxyHeaderFilterConfig.MAX_ROLE_LENGTH_DEFAULT;
            }

            @Override
            public int maxUserLength() {
                return Oauth2ProxyHeaderFilterConfig.MAX_USER_LENGTH_DEFAULT;
            }

            @Override
            public int maxRolesPerUser() {
                return Oauth2ProxyHeaderFilterConfig.MAX_ROLES_PER_USER_DEFAULT;
            }

            @Override
            public String allowedChars() {
                return Oauth2ProxyHeaderFilterConfig.ALLOWED_CHARS_DEFAULT;
            }
        };
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
        final var httpRequest = WebUtils.toHttp(request);
        final var groupHeader = httpRequest.getHeaders(PROXY_HEADER_GROUPS);
        return new Oauth2ProxyHeaderToken(parseRoles(groupHeader), parseUser(request));
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        final var user = parseUser(request);
        if (user != null) {
            return executeLogin(request, response);
        }
        WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    /**
     * Parses user from {@code PROXY_HEADER_USER} header.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return A single sanitized user
     */
    @VisibleForTesting
    @Nullable String parseUser(final ServletRequest request) {
        final var users = WebUtils.toHttp(request).getHeaders(PROXY_HEADER_USER).asIterator();
        if (!users.hasNext()) {
            LOG.warn("Expected at least one user.");
            return null;
        }
        final var user = users.next();
        if (users.hasNext()) {
            LOG.warn("Expected at most one user.");
            return null;
        }
        if (user == null || user.isBlank()) {
            LOG.warn("Rejected empty user.");
            return null;
        }

        final var sanitized = user.strip();
        if (sanitized.length() > maxUserLength) {
            LOG.warn("Rejected user, exceeds maximum allowed length.");
            return null;
        }
        if (!allowedCharactersPattern.matcher(sanitized).matches()) {
            LOG.warn("Rejected malformed user during parsing.");
            return null;
        }
        return sanitized;
    }

    /**
     * Parses roles from X-Forwarded-Groups header.
     *
     * <p>Example: role:global-admin,role:odl-application:admin
     * roles are separated by "," and each role can have namespace with ":" as separator
     * we want to get role with its namespace but without "role:" at the beginning.
     *
     * @param headers A List of header strings
     * @return Set of parsed roles
     */
    @VisibleForTesting
    Set<String> parseRoles(final @Nullable Enumeration<@Nullable String> headers) {
        // Check if the headers list itself is null or empty
        if (headers == null || !headers.hasMoreElements()) {
            LOG.warn("Rejected empty role headers.");
            return Set.of();
        }

        final var parsedRoles = new HashSet<String>();
        while (headers.hasMoreElements()) {
            final var header = headers.nextElement();
            // Skip null or entirely empty headers
            if (header == null || header.isBlank()) {
                LOG.warn("Rejected empty role header during parsing.");
                continue;
            }

            // Enforce maximum acceptable header length
            if (header.length() > maxHeaderLength) {
                LOG.warn("A role header exceeds maximum allowed length. Skipping this specific header.");
                continue;
            }
            if (!headerPattern.matcher(header).matches()) {
                LOG.warn("Rejected malformed role header during parsing.");
                continue;
            }

            final var headerValues = header.split(",");
            for (final var value : headerValues) {
                if (parsedRoles.size() >= maxRolesPerUser) {
                    LOG.warn("Maximum role limit reached {}. Truncating remaining headerValues.", maxRolesPerUser);
                    return Set.copyOf(parsedRoles);
                }
                // strip from leading and trailing whitespaces and optional role pattern
                final var role = ROLE_REGEX.matcher(value.strip()).replaceFirst("");
                if (role.isBlank()) {
                    LOG.warn("Rejected empty role during parsing.");
                    continue;
                }
                // enforce maximum acceptable length of role
                if (role.length() > maxRoleLength) {
                    LOG.warn("A role exceeds maximum allowed length. Skipping this specific role.");
                    continue;
                }
                // strict Validation against allowed characters
                if (!allowedCharactersPattern.matcher(role).matches()) {
                    LOG.warn("Rejected malformed role token during parsing.");
                    continue;
                }
                parsedRoles.add(role);
            }
        }
        return Set.copyOf(parsedRoles);
    }
}
