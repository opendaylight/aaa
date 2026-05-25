/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.Set;
import javax.servlet.ServletRequest;

/**
 * Configuration for Oauth2 Proxy Header authentication. Exposed as an OSGi service and populated from
 * {@code org.opendaylight.aaa.shiro.oauth2proxy.cfg} via OSGi Configuration Admin.
 */
public interface Oauth2ProxyHeaderFilterConfig {
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

    String PROXY_HEADER_USER = "X-Forwarded-User";

    String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    /**
     * Parses user from {@code PROXY_HEADER_USER} header.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return A single sanitized user
     */
    String parseUser(ServletRequest request);

    /**
     * Parses user from {@code PROXY_HEADER_GROUPS} header.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return Set of parsed roles
     */
    Set<String> parseRolesHeader(ServletRequest request);
}
