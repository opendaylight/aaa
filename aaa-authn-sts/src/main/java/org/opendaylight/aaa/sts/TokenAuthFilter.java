/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
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

/**
 * A token-based authentication filter for resource providers.
 *
 * <p>
 * Deprecated: Use <code>AAAFilter</code> instead.
 *
 * @author liemmn
 *
 */
@Deprecated
public class TokenAuthFilter implements ContainerRequestFilter {

    private static final String OPTIONS = "OPTIONS";
    private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String AUTHORIZATION = "authorization";

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public ContainerRequest filter(ContainerRequest request) {

        // Do the CORS check first
        if (checkCORSOptionRequest(request)) {
            return request;
        }

        // Are we up yet?
        if (ServiceLocator.getInstance().getAuthenticationService() == null) {
            throw new WebApplicationException(
                    Response.status(Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON)
                            .entity("{\"error\":\"Authentication service unavailable\"}").build());
        }

        // Are we doing authentication or not?
        if (ServiceLocator.getInstance().getAuthenticationService().isAuthEnabled()) {
            Map<String, List<String>> headers = request.getRequestHeaders();

            // Go through and invoke other TokenAuth first...
            List<TokenAuth> tokenAuthCollection = ServiceLocator.getInstance().getTokenAuthCollection();
            for (TokenAuth ta : tokenAuthCollection) {
                try {
                    Authentication auth = ta.validate(headers);
                    if (auth != null) {
                        ServiceLocator.getInstance().getAuthenticationService().set(auth);
                        return request;
                    }
                } catch (AuthenticationException ae) {
                    throw unauthorized();
                }
            }

            // OK, last chance to validate token...
            try {
                OAuthAccessResourceRequest or = new OAuthAccessResourceRequest(httpRequest, ParameterStyle.HEADER);
                validate(or.getAccessToken());
            } catch (OAuthSystemException | OAuthProblemException e) {
                throw unauthorized();
            }
        }

        return request;
    }

    /**
     * CORS access control : when browser sends cross-origin request, it first
     * sends the OPTIONS method with a list of access control request headers,
     * which has a list of custom headers and access control method such as GET.
     * POST etc. You custom header "Authorization will not be present in request
     * header, instead it will be present as a value inside
     * Access-Control-Request-Headers. We should not do any authorization
     * against such request. for more details :
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS
     */

    private boolean checkCORSOptionRequest(ContainerRequest request) {
        if (OPTIONS.equals(request.getMethod())) {
            List<String> headerList = request.getRequestHeader(ACCESS_CONTROL_REQUEST_HEADERS);
            if (headerList != null && !headerList.isEmpty()) {
                String header = headerList.get(0);
                if (header != null && header.toLowerCase().contains(AUTHORIZATION)) {
                    return true;
                }
            }
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
    private static WebApplicationException unauthorized() {
        ServiceLocator.getInstance().getAuthenticationService().clear();
        return new UnauthorizedException();
    }

    // A custom 401 web exception that handles http basic RESPONSE as well
    static final class UnauthorizedException extends WebApplicationException {
        private static final long serialVersionUID = -1732363804773027793L;
        static final String WWW_AUTHENTICATE = "WWW-Authenticate";
        static final Object OPENDAYLIGHT = "Basic realm=\"opendaylight\"";
        private static final Response RESPONSE = Response.status(Status.UNAUTHORIZED)
                .header(WWW_AUTHENTICATE, OPENDAYLIGHT).build();

        UnauthorizedException() {
            super(RESPONSE);
        }
    }
}
