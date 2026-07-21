/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Configuration for Oauth2 Proxy Header authentication. Exposed as an OSGi service and populated from
 * {@code org.opendaylight.aaa.shiro.oauth2proxy.cfg} via OSGi Configuration Admin.
 */
public interface Oauth2ProxyHeaderParserConfig {
    /**
     * Default maximum allowed length for a single header value in bytes.
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7230#section-3.2.5">RFC 7230 §3.2.5</a>
     */
    int MAX_HEADER_LENGTH_DEFAULT = 4096;
    /**
     * Default maximum allowed length for a single role name in characters.
     */
    int MAX_ROLE_LENGTH_DEFAULT = 128;
    /**
     * Default maximum allowed length for a username in characters.
     */
    int MAX_USER_LENGTH_DEFAULT = 128;
    /**
     * Default maximum number of roles a single user may carry.
     */
    int MAX_ROLES_PER_USER_DEFAULT = 200;
    /**
     * Default regex character class for whitelisted characters in usernames and role names.
     */
    String ALLOWED_CHARS_DEFAULT = "[a-zA-Z0-9_.:\\-@]";

    /**
     * {@return the maximum allowed length for a single forwarded header value in bytes}
     */
    int maxHeaderLength();

    /**
     * {@return the maximum allowed length for a single role name in characters}
     */
    int maxRoleLength();

    /**
     * {@return the maximum allowed length for a username in characters}
     */
    int maxUserLength();

    /**
     * {@return the maximum number of roles a single user may carry}
     */
    int maxRolesPerUser();

    /**
     * {@return configured character class (e.g. {@code [a-zA-Z0-9_.:\-@]})}
     */
    @NonNull String allowedChars();

    /**
     * Returns the regular expression used to whitelist characters in a single username or role name.
     *
     * <p>Anchored so the entire value must consist of one or more characters from the configured
     * character class (e.g. {@code [a-zA-Z0-9_.:\-@]}).
     *
     * <p>Callers matching this repeatedly should compile it once via {@link java.util.regex.Pattern}
     * and cache the result, rather than compiling it on every use.
     *
     * @return the regular expression
     */
    default @NonNull String allowedCharactersRegex() {
        return "^(?:" + allowedChars() + ")+$";
    }

    /**
     * Returns the regular expression used to validate an entire {@code X-Forwarded-Groups} header value.
     *
     * <p>Matches a comma-separated list of one or more roles, where each role may optionally carry a
     * {@code role:} prefix and is built from the character class of {@link #allowedCharactersRegex()}.
     * Whitespace is permitted around individual roles and around the header as a whole.
     *
     * <p>Callers matching this repeatedly should compile it once via {@link java.util.regex.Pattern}
     * and cache the result, rather than compiling it on every use.
     *
     * @return the regular expression
     */
    default @NonNull String headerRegex() {
        return "^\\s*(?:role:)?(?:" + allowedChars() + ")+(?:\\s*,\\s*(?:role:)?(?:"
            + allowedChars() + ")+)*\\s*$";
    }
}
