/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

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
 * <p><strong>Security prerequisite:</strong> direct HTTP access to ODL that bypasses the proxy
 * must be blocked at the network level. Failure to do so allows any caller to forge these headers
 * and authenticate as an arbitrary user.
 */
public final class Oauth2ProxyHeaderFilter extends AuthenticatingFilter {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderFilter.class);
    // ODL is set as upstream of OAuth2-Proxy thus X-Forwarded instead of X-Auth-Request headers
    @VisibleForTesting
    static final String PROXY_HEADER_USER = "X-Forwarded-User";
    @VisibleForTesting
    static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    // Maximum allowed length for a single header
    private static final int MAX_HEADER_LENGTH = 4096;
    // Maximum allowed length for a single role
    private static final int MAX_ROLE_LENGTH = 128;
    // Maximum allowed length for a username
    private static final int MAX_USER_LENGTH = 128;
    // Maximum number of roles a single user is allowed to have
    private static final int MAX_ROLES_PER_USER = 200;
    // Allowed username role characters pattern: Alphanumeric, dots, dashes, underscores, colons, @.
    private static final Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("^[a-zA-Z0-9_.:\\-@]+$");
    private static final Pattern ROLE_REGEX = Pattern.compile("^role:");

    @Override
    protected AuthenticationToken createToken(final ServletRequest request, final ServletResponse response) {
        final var httpRequest = WebUtils.toHttp(request);
        final var groupHeader = httpRequest.getHeaders(PROXY_HEADER_GROUPS);
        return new Oauth2ProxyHeaderToken(parseRoles(groupHeader), parseUser(request));
    }

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws Exception {
        final var user = parseUser(request);
        // check if user is valid
        if (user != null) {
            return executeLogin(request, response);
        }
        WebUtils.toHttp(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private static String parseUser(final ServletRequest request) {
        var user = WebUtils.toHttp(request).getHeader(PROXY_HEADER_USER);
        if (user == null || user.isBlank()) {
            return null;
        }
        user = user.strip();
        // return sanitized username
        if (user.length() > MAX_USER_LENGTH) {
            LOG.warn("Rejected user, exceeds maximum allowed length.");
            return null;
        }
        if (ALLOWED_CHARACTERS_PATTERN.matcher(user).matches()) {
            return user;
        } else {
            LOG.warn("Rejected malformed user during parsing.");
            return null;
        }
    }

    /**
     * Parse roles from X-Forwarded-Groups header. example: role:global-admin,role:odl-application:admin
     * roles are separated by "," and each role can have namespace with ":" as separator
     * we want to get role with its namespace but without "role:" at the beginning.
     *
     * @param headers A List of header strings
     * @return set of parsed roles
     */
    @VisibleForTesting
    static Set<String> parseRoles(final Enumeration<String> headers) {
        // Check if the list itself is null or empty
        if (headers == null) {
            return Set.of();
        }

        final var parsedRoles = new HashSet<String>();
        // Iterate through each header provided
        while (headers.hasMoreElements()) {
            final var headerValue = headers.nextElement();
            // Skip null or entirely empty headers
            if (headerValue == null || headerValue.isBlank()) {
                continue;
            }

            // Enforce maximum acceptable header length
            if (headerValue.length() > MAX_HEADER_LENGTH) {
                LOG.warn("An X-Forwarded-Groups header exceeds maximum allowed length. Skipping this specific header.");
                continue;
            }

            // Split header by comma
            final var roles = headerValue.split(",");
            for (final var token : roles) {
                // Enforce maximum acceptable number of roles
                if (parsedRoles.size() >= MAX_ROLES_PER_USER) {
                    LOG.warn("Maximum role limit reached {}. Truncating remaining roles.", MAX_ROLES_PER_USER);
                    return Set.copyOf(parsedRoles);
                }
                // Trim leading/trailing whitespace
                var role = token.strip();
                // Strip optional "role:" prefix
                role = role.replaceFirst(ROLE_REGEX.pattern(), "");
                // Check emptiness
                if (role.isBlank()) {
                    continue;
                }
                // Enforce maximum acceptable length of role
                if (role.length() > MAX_ROLE_LENGTH) {
                    LOG.warn("A role exceeds maximum allowed length. Skipping this specific role.");
                    continue;
                }
                // Strict Validation against allowed characters
                if (ALLOWED_CHARACTERS_PATTERN.matcher(role).matches()) {
                    parsedRoles.add(role);
                } else {
                    LOG.trace("Rejected malformed role token during parsing.");
                }
            }
        }
        // Return immutable set
        return Set.copyOf(parsedRoles);
    }
}
