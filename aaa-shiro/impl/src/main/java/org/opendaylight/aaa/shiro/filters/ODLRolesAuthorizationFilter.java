/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Roles authorization filter that returns 403 Forbidden for authenticated users who lack the required role.
 * Shiro's built-in {@code RolesAuthorizationFilter} returns 401 Unauthorized in this case, which misleads
 * clients into thinking re-authentication would help. 403 correctly signals that the identity is known but
 * the permission is not granted.
 */
public class ODLRolesAuthorizationFilter extends RolesAuthorizationFilter {

    @Override
    protected boolean onAccessDenied(final ServletRequest request, final ServletResponse response) throws IOException {
        if (getSubject(request, response).getPrincipal() == null) {
            // Not authenticated — defer to Shiro's standard login redirect
            return super.onAccessDenied(request, response);
        }
        // Authenticated but missing the required role
        WebUtils.toHttp(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
