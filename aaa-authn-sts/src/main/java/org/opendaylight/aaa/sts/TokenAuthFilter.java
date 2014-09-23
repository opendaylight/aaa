/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.TokenAuth;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * A token-based authentication filter for resource providers.
 *
 * @author liemmn
 *
 */
public class TokenAuthFilter implements ContainerRequestFilter {
    private static final WebApplicationException UNAUTHORIZED_EX = new UnauthorizedException();
    private static final WebApplicationException UNAVAILABLE_EX = new WebApplicationException(
            Response.status(Status.SERVICE_UNAVAILABLE)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"error\":\"Authentication service unavailable\"}")
                    .build());

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        // Are we up yet?
        if (ServiceLocator.INSTANCE.as == null)
            throw UNAVAILABLE_EX;

        // Are we doing authentication or not?
        if (ServiceLocator.INSTANCE.as.isAuthEnabled()) {
            Map<String, List<String>> headers = request.getRequestHeaders();

            // Go through and invoke other TokenAuth first...
            for (TokenAuth ta : ServiceLocator.INSTANCE.ta) {
                try {
                    Authentication auth = ta.validate(headers);
                    if (auth != null) {
                        ServiceLocator.INSTANCE.as.set(auth);
                        return request;
                    }
                } catch (AuthenticationException ae) {
                    throw unauthorized();
                }
            }

            // OK, last chance to validate token...
            try {
                OAuthAccessResourceRequest or = new OAuthAccessResourceRequest(
                        httpRequest, ParameterStyle.HEADER);
                validate(or.getAccessToken());
            } catch (OAuthSystemException | OAuthProblemException e) {
                throw unauthorized();
            }
        }

        return request;
    }

    // Validate an ODL token...
    private Authentication validate(final String token) {
        Authentication auth = ServiceLocator.INSTANCE.ts.get(token);
        if (auth == null) {
            throw unauthorized();
        } else {
            ServiceLocator.INSTANCE.as.set(auth);
        }
        return auth;
    }

    // Houston, we got a problem!
    private static final WebApplicationException unauthorized() {
        ServiceLocator.INSTANCE.as.clear();
        return UNAUTHORIZED_EX;
    }

    // A custom 401 web exception that handles http basic response as well
    static final class UnauthorizedException extends WebApplicationException {
        private static final long serialVersionUID = -1732363804773027793L;
        static final String WWW_AUTHENTICATE = "WWW-Authenticate";
        static final Object OPENDAYLIGHT = "Basic realm=\"opendaylight\"";
        private static final Response response = Response
                .status(Status.UNAUTHORIZED)
                .header(WWW_AUTHENTICATE, OPENDAYLIGHT).build();

        public UnauthorizedException() {
            super(response);
        }
    }
}
