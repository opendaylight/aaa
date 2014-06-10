/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import static org.opendaylight.aaa.AuthConstants.AUTH_IDENTITY_CONFIRMED;
import static org.opendaylight.aaa.AuthConstants.AUTH_IDENTITY_STATUS;
import static org.opendaylight.aaa.AuthConstants.UNAUTHORIZED_EX;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenAuth;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * A {@link TokenAuth} filter for direct authentication. This must be installed
 * as the last authentication filter in the filter chain.
 *
 * @author liemmn
 *
 */
public class DirectTokenAuthFilter implements TokenAuth, ContainerRequestFilter {
    @Context
    private HttpServletRequest httpRequest;

    @Override
    public Authentication validate(String token) {
        if (token == null || token.isEmpty())
            throw UNAUTHORIZED_EX;
        Authentication auth = ServiceLocator.INSTANCE.ts.get(token);
        if (auth == null) {
            ServiceLocator.INSTANCE.as.clear();
            throw UNAUTHORIZED_EX;
        } else {
            ServiceLocator.INSTANCE.as.set(auth);
        }
        return auth;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        // Someone upstream may have validated this request...
        String authStatus = (String) httpRequest
                .getAttribute(AUTH_IDENTITY_STATUS);
        if (authStatus != null && authStatus.equals(AUTH_IDENTITY_CONFIRMED))
            return request;

        // OK, last chance to validate...
        try {
            OAuthAccessResourceRequest or = new OAuthAccessResourceRequest(
                    httpRequest, ParameterStyle.HEADER);
            validate(or.getAccessToken());
        } catch (OAuthSystemException | OAuthProblemException e) {
            throw UNAUTHORIZED_EX;
        }
        return request;
    }

}
