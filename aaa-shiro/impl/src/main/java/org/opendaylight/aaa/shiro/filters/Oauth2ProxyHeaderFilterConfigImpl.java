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
import java.util.regex.PatternSyntaxException;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import org.apache.shiro.web.util.WebUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link Oauth2ProxyHeaderFilterConfig}.
 *
 * <p>Configuration is supplied via
 * {@code etc/org.opendaylight.aaa.shiro.oauth2proxyheaderfilter.cfg}:
 * <pre>{@code
 * max-header-length = 4096
 * max-role-length = 128
 * max-user-length = 128
 * max-roles-per-user = 200
 * allowed-chars = [a-zA-Z0-9_.:\-@]
 * }</pre>
 */
@Singleton
@Component(service = Oauth2ProxyHeaderFilterConfig.class,
           configurationPid = "org.opendaylight.aaa.shiro.oauth2proxyheaderfilter")
@Designate(ocd = Oauth2ProxyHeaderFilterConfigImpl.Configuration.class)
public final class Oauth2ProxyHeaderFilterConfigImpl implements Oauth2ProxyHeaderFilterConfig {
    private static final Logger LOG = LoggerFactory.getLogger(Oauth2ProxyHeaderFilterConfigImpl.class);

    @ObjectClassDefinition
    public @interface Configuration {
        @AttributeDefinition(description = """
            Maximum allowed length of an X-Forwarded-User or X-Forwarded-Groups header value in bytes (DoS \
            safeguard).""")
        int max$_$header$_$length() default MAX_HEADER_LENGTH_DEFAULT;

        @AttributeDefinition(description = """
            Maximum allowed length of a single role name in characters.""")
        int max$_$role$_$length() default MAX_ROLE_LENGTH_DEFAULT;

        @AttributeDefinition(description = """
            Maximum allowed length of a username in characters.""")
        int max$_$user$_$length() default MAX_USER_LENGTH_DEFAULT;

        @AttributeDefinition(description = """
            Maximum number of roles per user (DoS safeguard against unbounded allocation).""")
        int max$_$roles$_$per$_$user() default MAX_ROLES_PER_USER_DEFAULT;

        @AttributeDefinition(description = """
            Regex character class for whitelisted characters in usernames and role names.""")
        String allowed$_$chars() default ALLOWED_CHARS_DEFAULT;
    }

    private static final Pattern ROLE_REGEX = Pattern.compile("^role:");

    private final int maxHeaderLength;
    private final int maxRoleLength;
    private final int maxUserLength;
    private final int maxRolesPerUser;
    private final Pattern allowedCharactersPattern;
    private final Pattern headerPattern;

    @Activate
    public Oauth2ProxyHeaderFilterConfigImpl(final Configuration configuration) {
        maxHeaderLength = configuration.max$_$header$_$length();
        maxRoleLength = configuration.max$_$role$_$length();
        maxUserLength = configuration.max$_$user$_$length();
        maxRolesPerUser = configuration.max$_$roles$_$per$_$user();
        String allowedChars = validatePattern(configuration.allowed$_$chars());
        allowedCharactersPattern = Pattern.compile("^" + allowedChars + "+$");
        headerPattern = Pattern.compile(
            "^\\s*(role:)?" + allowedChars + "+(\\s*,\\s*(role:)?" + allowedChars + "+)*\\s*$");
        LOG.debug("Oauth2ProxyHeaderFilter configuration: maxHeaderLength={}, maxRoleLength={}, "
            + "maxUserLength={}, maxRolesPerUser={}, allowedChars={}",
            maxHeaderLength, maxRoleLength, maxUserLength, maxRolesPerUser, allowedChars);
    }

    public Oauth2ProxyHeaderFilterConfigImpl() {
        maxHeaderLength = MAX_HEADER_LENGTH_DEFAULT;
        maxRoleLength = MAX_ROLE_LENGTH_DEFAULT;
        maxUserLength = MAX_USER_LENGTH_DEFAULT;
        maxRolesPerUser = MAX_ROLES_PER_USER_DEFAULT;
        String allowedChars = validatePattern(ALLOWED_CHARS_DEFAULT);
        allowedCharactersPattern = Pattern.compile("^" + allowedChars + "+$");
        headerPattern = Pattern.compile(
            "^\\s*(role:)?" + allowedChars + "+(\\s*,\\s*(role:)?" + allowedChars + "+)*\\s*$");
        LOG.debug("Oauth2ProxyHeaderFilter configuration: maxHeaderLength={}, maxRoleLength={}, "
                + "maxUserLength={}, maxRolesPerUser={}, allowedChars={}",
            maxHeaderLength, maxRoleLength, maxUserLength, maxRolesPerUser, allowedChars);
    }

    @Override
    public String parseUser(final ServletRequest request) {
        final var users = WebUtils.toHttp(request).getHeaders(PROXY_HEADER_USER).asIterator();
        final String user;
        if (!users.hasNext()) {
            LOG.warn("Expected at least one user.");
            return null;
        } else {
            user = users.next();
        }
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

    @Override
    public Set<String> parseRolesHeader(ServletRequest request) {
        final var roles = WebUtils.toHttp(request).getHeaders(PROXY_HEADER_GROUPS);
        return parseRoles(roles);
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

    private static String validatePattern(final String value) {
        try {
            Pattern.compile("^" + value + "+$");
            return value;
        } catch (PatternSyntaxException e) {
            LOG.warn("Invalid regex pattern for allowed-chars: '{}', using default '{}'",
                value, ALLOWED_CHARS_DEFAULT);
            return ALLOWED_CHARS_DEFAULT;
        }
    }
}
