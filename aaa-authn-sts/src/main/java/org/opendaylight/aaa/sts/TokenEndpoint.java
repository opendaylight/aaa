/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.PasswordCredentialBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.PasswordCredentials;

/**
 * Secure Token Service (STS) endpoint.
 *
 * @author liemmn
 *
 */
public class TokenEndpoint extends HttpServlet {
    private static final long serialVersionUID = 8272453849539659999L;

    private static final String DOMAIN_SCOPE_REQUIRED = "Domain scope required";
    private static final String NOT_IMPLEMENTED = "not_implemented";
    private static final String UNAUTHORIZED = "unauthorized";

    static final String TOKEN_GRANT_ENDPOINT = "/token";
    static final String TOKEN_REVOKE_ENDPOINT = "/revoke";
    static final String TOKEN_VALIDATE_ENDPOINT = "/validate";

    private transient OAuthIssuer oi;

    @Override
    public void init(ServletConfig config) throws ServletException {
        oi = new OAuthIssuerImpl(new UUIDValueGenerator());
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if (req.getServletPath().equals(TOKEN_GRANT_ENDPOINT)) {
                createAccessToken(req, resp);
            } else if (req.getServletPath().equals(TOKEN_REVOKE_ENDPOINT)) {
                deleteAccessToken(req, resp);
            } else if (req.getServletPath().equals(TOKEN_VALIDATE_ENDPOINT)) {
                validateToken(req, resp);
            }
        } catch (AuthenticationException e) {
            error(resp, SC_UNAUTHORIZED, e.getMessage());
        } catch (OAuthProblemException oe) {
            error(resp, oe);
        } catch (Exception e) {
            error(resp, e);
        }
    }

    private void validateToken(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, OAuthSystemException {
        String token = req.getReader().readLine();
        if (token != null) {
            Authentication authn = ServiceLocator.getInstance().getTokenStore().get(token.trim());
            if (authn == null) {
                throw new AuthenticationException(UNAUTHORIZED);
            } else {
                ServiceLocator.getInstance().getAuthenticationService().set(authn);
                resp.setStatus(SC_OK);
            }
        } else {
            throw new AuthenticationException(UNAUTHORIZED);
        }
    }

    // Delete an access token
    private void deleteAccessToken(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String token = req.getReader().readLine();
        if (token != null) {
            if (ServiceLocator.getInstance().getTokenStore().delete(token.trim())) {
                resp.setStatus(SC_NO_CONTENT);
            } else {
                throw new AuthenticationException(UNAUTHORIZED);
            }
        } else {
            throw new AuthenticationException(UNAUTHORIZED);
        }
    }

    // Create an access token
    private void createAccessToken(HttpServletRequest req, HttpServletResponse resp)
            throws OAuthSystemException, OAuthProblemException, IOException {
        Claim claim = null;
        String clientId = null;

        OAuthRequest oauthRequest = new OAuthRequest(req);
        // Any client credentials?
        clientId = oauthRequest.getClientId();
        if (clientId != null) {
            ServiceLocator.getInstance().getClientService()
                          .validate(clientId, oauthRequest.getClientSecret());
        }

        // Credential request...
        if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
            String domain = oauthRequest.getScopes().iterator().next();
            PasswordCredentials pc = new PasswordCredentialBuilder().setUserName(
                    oauthRequest.getUsername()).setPassword(oauthRequest.getPassword())
                                                                    .setDomain(domain).build();
            if (!oauthRequest.getScopes().isEmpty()) {
                claim = ServiceLocator.getInstance().getCredentialAuth().authenticate(pc);
            }
        } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
                GrantType.REFRESH_TOKEN.toString())) {
            // Refresh token...
            String token = oauthRequest.getRefreshToken();
            if (!oauthRequest.getScopes().isEmpty()) {
                String domain = oauthRequest.getScopes().iterator().next();
                // Authenticate...
                Authentication auth = ServiceLocator.getInstance().getTokenStore().get(token);
                if (auth != null && domain != null) {
                    List<String> roles = ServiceLocator.getInstance().getIdmService()
                                                       .listRoles(auth.userId(), domain);
                    if (!roles.isEmpty()) {
                        ClaimBuilder cb = new ClaimBuilder(auth);
                        cb.setDomain(domain); // scope domain
                        // Add roles for the scoped domain
                        for (String role : roles) {
                            cb.addRole(role);
                        }
                        claim = cb.build();
                    }
                }
            } else {
                error(resp, SC_BAD_REQUEST, DOMAIN_SCOPE_REQUIRED);
            }
        } else {
            // Support authorization code later...
            error(resp, SC_NOT_IMPLEMENTED, NOT_IMPLEMENTED);
        }

        // Respond with OAuth token
        oauthAccessTokenResponse(resp, claim, clientId);
    }

    // Build OAuth access token response from the given claim
    private void oauthAccessTokenResponse(HttpServletResponse resp, Claim claim, String clientId)
            throws OAuthSystemException, IOException {
        if (claim == null) {
            throw new AuthenticationException(UNAUTHORIZED);
        }
        String token = oi.accessToken();

        // Cache this token...
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder(claim).setClientId(
                clientId).build()).setExpiration(tokenExpiration()).build();
        ServiceLocator.getInstance().getTokenStore().put(token, auth);

        OAuthResponse response = OAuthASResponse.tokenResponse(SC_CREATED).setAccessToken(token)
                                         .setTokenType(TokenType.BEARER.toString())
                                         .setExpiresIn(Long.toString(auth.expiration()))
                                         .buildJSONMessage();
        write(resp, response);
    }

    // Token expiration
    private long tokenExpiration() {
        return ServiceLocator.getInstance().getTokenStore().tokenExpiration();
    }

    // Emit an error OAuthResponse with the given HTTP code
    private void error(HttpServletResponse resp, int httpCode, String error) {
        try {
            OAuthResponse response = OAuthResponse.errorResponse(httpCode).setError(error)
                                           .buildJSONMessage();
            write(resp, response);
        } catch (IOException | OAuthSystemException e) {
            // Nothing to do here
        }
    }

    // Emit an error OAuthResponse for the given OAuth-related exception
    private void error(HttpServletResponse resp, OAuthProblemException exception) {
        try {
            OAuthResponse response = OAuthResponse.errorResponse(SC_BAD_REQUEST).error(exception)
                                           .buildJSONMessage();
            write(resp, response);
        } catch (IOException | OAuthSystemException e) {
            // Nothing to do here
        }
    }

    // Emit an error OAuthResponse for the given generic exception
    private void error(HttpServletResponse resp, Exception exception) {
        try {
            OAuthResponse response = OAuthResponse.errorResponse(SC_INTERNAL_SERVER_ERROR)
                                           .setError(exception.getClass().getName())
                                           .setErrorDescription(exception.getMessage()).buildJSONMessage();
            write(resp, response);
        } catch (IOException | OAuthSystemException e) {
            // Nothing to do here
        }
    }

    // Write out an OAuthResponse
    private void write(HttpServletResponse resp, OAuthResponse response) throws IOException {
        resp.setStatus(response.getResponseStatus());
        PrintWriter pw = resp.getWriter();
        pw.print(response.getBody());
        pw.flush();
        pw.close();
    }
}
