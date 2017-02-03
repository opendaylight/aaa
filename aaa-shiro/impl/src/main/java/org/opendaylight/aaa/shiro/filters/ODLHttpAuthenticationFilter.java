/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends <code>BasicHttpAuthenticationFilter</code> to include ability to
 * authenticate OAuth2 tokens, which is needed for backwards compatibility with
 * <code>TokenAuthFilter</code>.
 *
 * This behavior is enabled by default for backwards compatibility. To disable
 * OAuth2 functionality, just comment out the following line from the
 * <code>etc/shiro.ini</code> file:
 * <code>authcBasic = org.opendaylight.aaa.shiro.filters.ODLHttpAuthenticationFilter</code>
 * then restart the karaf container.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class ODLHttpAuthenticationFilter extends BasicHttpAuthenticationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ODLHttpAuthenticationFilter.class);

    // defined in lower-case for more efficient string comparison
    protected static final String BEARER_SCHEME = "bearer";

    protected static final String OPTIONS_HEADER = "OPTIONS";

    public ODLHttpAuthenticationFilter() {
        LOG.info("Creating the ODLHttpAuthenticationFilter");
    }

    @Override
    protected String[] getPrincipalsAndCredentials(String scheme, String encoded) {
        final String decoded = Base64.decodeToString(encoded);
        // attempt to decode username/password; otherwise decode as token
        if (decoded.contains(":")) {
            return decoded.split(":");
        }
        return new String[] { encoded };
    }

    @Override
    protected boolean isLoginAttempt(String authzHeader) {
        final String authzScheme = getAuthzScheme().toLowerCase();
        final String authzHeaderLowerCase = authzHeader.toLowerCase();
        return authzHeaderLowerCase.startsWith(authzScheme)
                || authzHeaderLowerCase.startsWith(BEARER_SCHEME);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response,
            Object mappedValue) {
        final HttpServletRequest httpRequest = WebUtils.toHttp(request);
        final String httpMethod = httpRequest.getMethod();
        if (OPTIONS_HEADER.equalsIgnoreCase(httpMethod)) {
            return true;
        } else {
            return super.isAccessAllowed(httpRequest, response, mappedValue);
        }
    }
}
