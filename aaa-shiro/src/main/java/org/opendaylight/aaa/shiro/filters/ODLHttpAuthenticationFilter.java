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

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends <code>BasicHttpAuthenticationFilter</code> to include ability to
 * authenticate OAuth2 tokens, which is needed for backwards compatibility
 * with <code>TokenAuthFilter</code>.
 *
 * This behavior is enabled by default for backwards compatibility.  To disable
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

	protected static final String BEARER_SCHEME = "Bearer";

	public ODLHttpAuthenticationFilter() {
		super();
		final String INFO_MSG = "Creating the ODLHttpAuthenticationFilter";
		LOG.info(INFO_MSG);
	}

	@Override
	protected String[] getPrincipalsAndCredentials(String scheme, String encoded) {
		String decoded = Base64.decodeToString(encoded);
		if(decoded.contains(":")) {
			return decoded.split(":");
		}
		return new String[]{encoded};
	}

	@Override
	protected boolean isLoginAttempt(String authzHeader) {
		String authzScheme = getAuthzScheme().toLowerCase();
		return authzHeader.toLowerCase().startsWith(authzScheme) || authzHeader.toLowerCase().startsWith(BEARER_SCHEME.toLowerCase());
	}

}
