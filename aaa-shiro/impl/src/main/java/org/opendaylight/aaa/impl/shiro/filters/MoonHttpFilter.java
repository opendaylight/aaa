/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.opendaylight.aaa.shiro.moon.MoonPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoonHttpFilter extends ODLHttpAuthenticationFilter{

    private static final Logger LOG = LoggerFactory.getLogger(MoonHttpFilter.class);

    public MoonHttpFilter(){
        super();
        LOG.info("Creating the MoonHttpAthFilter");
    }

    protected boolean isLoginAttempt(String authzHeader) {
        LOG.info("MoonHttpFilter: isLoginAttempt");
        return super.isLoginAttempt(authzHeader);
    }

    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response,
            Object mappedValue) {
        LOG.info("MoonHttpFilter; isAccessAllowed");
        return super.isAccessAllowed(request, response, mappedValue);
    }
    
    // surcharge the method executeLogin of org.apache.shiro.web.filter.authc.AuthenticatingFilter
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {

        final HttpServletRequest req;
        try {
            req = (HttpServletRequest) request;
        } catch (final ClassCastException e) {
            LOG.debug("executeLogin() failed since the request could not be cast appropriately", e);
            throw e;
        }

        AuthenticationToken token = createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken " +
                    "must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        }
        try {
            Subject subject = getSubject(request, response);
            subject.login(token);
            MoonPrincipal principal = (MoonPrincipal)subject.getPrincipal();
            principal.setAction(req.getMethod());
            principal.setObject(req.getPathInfo());
            subject.hasRole("");
            return onLoginSuccess(token, subject, request, response);
        } catch (AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
    }
}
