/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.inject.Singleton;
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

    private final int maxHeaderLength;
    private final int maxRoleLength;
    private final int maxUserLength;
    private final int maxRolesPerUser;
    private final Pattern allowedCharactersPattern;
    private final Pattern headerPattern;

    @Activate
    public Oauth2ProxyHeaderFilterConfigImpl(final Configuration config) {
        this(config.max$_$header$_$length(), config.max$_$role$_$length(), config.max$_$user$_$length(),
            config.max$_$roles$_$per$_$user(), validatePattern(config.allowed$_$chars()));
    }

    Oauth2ProxyHeaderFilterConfigImpl() {
        this(MAX_HEADER_LENGTH_DEFAULT, MAX_ROLE_LENGTH_DEFAULT, MAX_USER_LENGTH_DEFAULT, MAX_ROLES_PER_USER_DEFAULT,
            ALLOWED_CHARS_DEFAULT);
    }

    private Oauth2ProxyHeaderFilterConfigImpl(final int maxHeaderLength, final int maxRoleLength,
            final int maxUserLength, final int maxRolesPerUser, final String allowedChars) {
        this.maxHeaderLength = maxHeaderLength;
        this.maxRoleLength = maxRoleLength;
        this.maxUserLength = maxUserLength;
        this.maxRolesPerUser = maxRolesPerUser;
        allowedCharactersPattern = Pattern.compile("^(?:" + allowedChars + ")+$");
        headerPattern = Pattern.compile(
            "^\\s*(?:role:)?(?:" + allowedChars + ")+(?:\\s*,\\s*(?:role:)?(?:" + allowedChars + ")+)*\\s*$");
        LOG.debug("Oauth2ProxyHeaderFilter configuration: maxHeaderLength={}, maxRoleLength={}, "
                + "maxUserLength={}, maxRolesPerUser={}, allowedChars={}",
            maxHeaderLength, maxRoleLength, maxUserLength, maxRolesPerUser, allowedChars);
    }

    @Override
    public int maxHeaderLength() {
        return maxHeaderLength;
    }

    @Override
    public int maxRoleLength() {
        return maxRoleLength;
    }

    @Override
    public int maxUserLength() {
        return maxUserLength;
    }

    @Override
    public int maxRolesPerUser() {
        return maxRolesPerUser;
    }

    @Override
    public Pattern allowedCharactersPattern() {
        return allowedCharactersPattern;
    }

    @Override
    public Pattern headerPattern() {
        return headerPattern;
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
