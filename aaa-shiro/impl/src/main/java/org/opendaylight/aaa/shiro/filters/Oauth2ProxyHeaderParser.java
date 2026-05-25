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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses {@code X-Forwarded-User}/{@code X-Forwarded-Groups} proxy headers into an
 * {@link Oauth2ProxyHeaderToken}, applying the limits and character whitelist from a given
 * {@link Oauth2ProxyHeaderFilterConfig}.
 *
 * <p>Used by {@link Oauth2ProxyHeaderFilter}, and safe for other consumers of the same proxy headers
 * (e.g. other filters, or other Karaf features fronted by the same OAuth2-Proxy) to reuse instead of
 * duplicating the validation/sanitization logic. Callers should build this from the
 * {@link Oauth2ProxyHeaderFilterConfig} OSGi service so limits stay consistent with what is actually
 * configured, rather than constructing their own.
 */
@NonNullByDefault
public final class Oauth2ProxyHeaderParser {
    /**
     * Proxy header containing username.
     *
     * <p>ODL is set as upstream of OAuth2-Proxy thus X-Forwarded-User instead of X-Auth-Request-User header
     */
    @VisibleForTesting
    static final String PROXY_HEADER_USER = "X-Forwarded-User";
    /**
     * Proxy header containing user roles.
     *
     * <p>ODL is set as upstream of OAuth2-Proxy thus X-Forwarded-Groups instead of X-Auth-Request-Groups header
     */
    @VisibleForTesting
    static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderParser.class);
    private static final Pattern ROLE_REGEX = Pattern.compile("^role:");

    private final int maxHeaderLength;
    private final int maxRoleLength;
    private final int maxUserLength;
    private final int maxRolesPerUser;
    private final Pattern allowedCharactersPattern;
    private final Pattern headerPattern;

    public Oauth2ProxyHeaderParser(final Oauth2ProxyHeaderFilterConfig config) {
        requireNonNull(config);
        maxHeaderLength = config.maxHeaderLength();
        maxRoleLength = config.maxRoleLength();
        maxUserLength = config.maxUserLength();
        maxRolesPerUser = config.maxRolesPerUser();
        allowedCharactersPattern = config.allowedCharactersPattern();
        headerPattern = config.headerPattern();
    }

    /**
     * Parses both proxy headers of the given request into a single token.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return An {@link Oauth2ProxyHeaderToken} built from the request's proxy headers
     */
    public Oauth2ProxyHeaderToken parseToken(final ServletRequest request) {
        return new Oauth2ProxyHeaderToken(parseRolesHeader(request), parseUser(request));
    }

    /**
     * Parses user from {@code PROXY_HEADER_USER} header.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return A single sanitized user
     */
    public @Nullable String parseUser(final ServletRequest request) {
        final var users = WebUtils.toHttp(request).getHeaders(PROXY_HEADER_USER);
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
    public Set<String> parseRolesHeader(final ServletRequest request) {
        // Extract headers from request
        final var headers = WebUtils.toHttp(request).getHeaders(PROXY_HEADER_GROUPS);
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
