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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public final class Oauth2ProxyHeaderFilter extends AuthenticatingFilter {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderFilter.class);
    // ODL is set as upstream of OAuth2-Proxy thus X-Forwarded instead of X-Auth-Request headers
    @VisibleForTesting
    static final String PROXY_HEADER_USER = "X-Forwarded-User";
    @VisibleForTesting
    static final String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    /**
     * Maximum allowed length for a single header value in bytes.
     *
     * <p>Aligns with the de-facto industry default of 4–8 KB per header used by nginx and Apache
     * HTTP Server. RFC 7230 §3.2.5 requires servers to respond with an appropriate 4xx when a
     * received header field exceeds the size they are willing to process.
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7230#section-3.2.5">RFC 7230 §3.2.5</a>
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html">OWASP Input Validation Cheat Sheet</a>
     */
    private static final int MAX_HEADER_LENGTH = 4096;
    /**
     * Maximum allowed length for a single role name.
     *
     * <p>Chosen conservatively to cover LDAP/OIDC group names in practice (LDAP {@code cn}
     * attributes are typically ≤64 chars; OIDC group claim values have no mandated upper bound).
     * Enforced per OWASP input validation guidance on bounding string length.
     *
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html">OWASP Input Validation Cheat Sheet</a>
     */
    private static final int MAX_ROLE_LENGTH = 128;
    /**
     * Maximum allowed length for a username.
     *
     * <p>Chosen conservatively to cover email-style usernames forwarded by OAuth2-Proxy. RFC 5321
     * caps the local part of an email address at 64 characters; the full address including domain
     * comfortably fits within 128 characters in practice.
     *
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html">OWASP Input Validation Cheat Sheet</a>
     */
    private static final int MAX_USER_LENGTH = 128;
    /**
     * Maximum number of roles a single user may carry.
     *
     * <p>A hard cap on the number of roles processed per request. This is a DoS safeguard: without
     * it, a crafted or misbehaving proxy header could cause unbounded memory allocation. The value
     * is a conservative heuristic; no RFC or OIDC specification mandates a specific limit.
     *
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html">OWASP Input Validation Cheat Sheet</a>
     */
    private static final int MAX_ROLES_PER_USER = 200;
    /**
     * Whitelist of characters permitted in usernames and role names: alphanumeric, {@code .},
     * {@code _}, {@code :}, {@code -}, {@code @}.
     *
     * <p>Whitelist-based character validation is the recommended first line of defence against
     * injection attacks. The set covers email-style usernames and namespaced role identifiers
     * (e.g. {@code namespace:role}) as emitted by OAuth2-Proxy.
     *
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html">OWASP Input Validation Cheat Sheet</a>
     */
    private static final String ALLOWED_CHARS = "[a-zA-Z0-9_.:\\-@]";
    private static final Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("^" + ALLOWED_CHARS + "+$");
    private static final Pattern ROLE_REGEX = Pattern.compile("^role:");
    /**
     * Validates a full role header value: a comma-separated list of tokens, each token optionally
     * prefixed with {@code role:} and composed entirely of {@link #ALLOWED_CHARS}.
     */
    private static final Pattern HEADER_PATTERN = Pattern.compile(
        "^\\s*(role:)?" + ALLOWED_CHARS + "+(\\s*,\\s*(role:)?" + ALLOWED_CHARS + "+)*\\s*$");

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
    static @Nullable String parseUser(final ServletRequest request) {
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
        if (sanitized.length() > MAX_USER_LENGTH) {
            LOG.warn("Rejected user, exceeds maximum allowed length.");
            return null;
        }
        if (!ALLOWED_CHARACTERS_PATTERN.matcher(sanitized).matches()) {
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
    static Set<String> parseRoles(final @Nullable Enumeration<@Nullable String> headers) {
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
            if (header.length() > MAX_HEADER_LENGTH) {
                LOG.warn("A role header exceeds maximum allowed length. Skipping this specific header.");
                continue;
            }
            if (!HEADER_PATTERN.matcher(header).matches()) {
                LOG.warn("Rejected malformed role header during parsing.");
                continue;
            }

            final var headerValues = header.split(",");
            for (final var value : headerValues) {
                if (parsedRoles.size() >= MAX_ROLES_PER_USER) {
                    LOG.warn("Maximum role limit reached {}. Truncating remaining headerValues.", MAX_ROLES_PER_USER);
                    return Set.copyOf(parsedRoles);
                }
                // strip from leading and trailing whitespaces and optional role pattern
                final var role = ROLE_REGEX.matcher(value.strip()).replaceFirst("");
                if (role.isBlank()) {
                    LOG.warn("Rejected empty role during parsing.");
                    continue;
                }
                // enforce maximum acceptable length of role
                if (role.length() > MAX_ROLE_LENGTH) {
                    LOG.warn("A role exceeds maximum allowed length. Skipping this specific role.");
                    continue;
                }
                // strict Validation against allowed characters
                if (!ALLOWED_CHARACTERS_PATTERN.matcher(role).matches()) {
                    LOG.warn("Rejected malformed role token during parsing.");
                    continue;
                }
                parsedRoles.add(role);
            }
        }
        return Set.copyOf(parsedRoles);
    }
}
