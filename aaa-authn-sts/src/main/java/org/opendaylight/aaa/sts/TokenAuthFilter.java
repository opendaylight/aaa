/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
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

/**
 * A token-based authentication filter for resource providers.
 *
 * @author liemmn
 * @see javax.ws.rs.ContainerRequestFilter
 */
public class TokenAuthFilter implements ContainerRequestFilter {

    private final String OPTIONS = "OPTIONS";
    private final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private final String AUTHORIZATION = "authorization";

    @Context
    private HttpServletRequest httpRequest;

    /**
     * CORS access control : when browser sends cross-origin request, it first sends the OPTIONS method
     * with a list of access control request headers, which has a list of custom headers and access control method
     * such as GET. POST etc. You custom header "Authorization will not be present in request header, instead it
     * will be present as a value inside Access-Control-Request-Headers.
     * We should not do any authorization against such request.
     * for more details : https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
     */
    private boolean checkCORSOptionRequest(ContainerRequestContext request) {
        if(OPTIONS.equals(request.getMethod())) {
            String headerString = request.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);
            return headerString.toLowerCase().contains(AUTHORIZATION);
        }
        return false;
    }

    // Validate an ODL token...
    private Authentication validate(final String token) {
        Authentication auth = ServiceLocator.getInstance().getTokenStore().get(token);
        if (auth == null) {
            throw unauthorized();
        } else {
            ServiceLocator.getInstance().getAuthenticationService().set(auth);
        }
        return auth;
    }

    // Houston, we got a problem!
    private static final WebApplicationException unauthorized() {
        ServiceLocator.getInstance().getAuthenticationService().clear();
        return new UnauthorizedException();
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

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		// Do the CORS check first
	    if(checkCORSOptionRequest(request)) {
	           return;
	    }
	    // Are we up yet?
	    if (ServiceLocator.INSTANCE.as == null) {
            throw new WebApplicationException(
                    Response.status(Status.SERVICE_UNAVAILABLE)
                            .type(MediaType.APPLICATION_JSON)
                            .entity("{\"error\":\"Authentication service unavailable\"}")
                            .build());
	    }

        // Are we doing authentication or not?
        if (ServiceLocator.INSTANCE.as.isAuthEnabled()) {
            Map<String, List<String>> headers = request.getHeaders();
            // Go through and invoke other TokenAuth first...
	        for (TokenAuth ta : ServiceLocator.INSTANCE.ta) {
	            try {
	                Authentication auth = ta.validate(headers);
	                if (auth != null) {
	                    ServiceLocator.INSTANCE.as.set(auth);
	                    return;
	                }
	            } catch (AuthenticationException ae) {
	                request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access the resource.").build());
	            }
	        }

	        // OK, last chance to validate token...
	        try {
	            OAuthAccessResourceRequest or = new OAuthAccessResourceRequest(
	                    httpRequest, ParameterStyle.HEADER);
	            validate(or.getAccessToken());
                return;
	        } catch (OAuthSystemException | OAuthProblemException e) {
	            request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access the resource.").build());
	        }
            request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access the resource.").build());
	    }
    }
}
