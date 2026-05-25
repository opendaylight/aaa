/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Oauth2ProxyHeaderParser {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderParser.class);

    private static final Pattern ROLE_REGEX = Pattern.compile("^role:");

    private Oauth2ProxyHeaderParser() {
        // Stateless parser
    }

    /**
     * Parses user from {@code PROXY_HEADER_USER} header.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return A single sanitized user
     */
    static @Nullable String parseUser(final ServletRequest request, final int maxUserLength,
            final Pattern allowedCharactersPattern) {
        final var users = WebUtils.toHttp(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_USER);
        if (users == null) {
            LOG.warn("Expected at least one user.");
            return null;
        }
        if (!users.hasMoreElements()) {
            LOG.warn("Expected at least one user.");
            return null;
        }
        final var user = users.nextElement();

        if (users.hasMoreElements()) {
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
     * Parses roles from {@code PROXY_HEADER_GROUPS} header.
     *
     * <p>Example: role:global-admin,role:odl-application:admin
     * roles are separated by "," and each role can have namespace with ":" as separator
     * we want to get role with its namespace but without "role:" at the beginning.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return Set of parsed roles
     */
    @VisibleForTesting
    static Set<String> parseRolesHeader(final ServletRequest request, final int maxHeaderLength,
            final int maxRolesPerUser, final int maxRoleLength, final Pattern headerPattern,
            final Pattern allowedCharactersPattern) {
        // Extract headers from request
        final var headers = WebUtils.toHttp(request).getHeaders(Oauth2ProxyHeaderFilter.PROXY_HEADER_GROUPS);
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
