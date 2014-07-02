/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.opendaylight.aaa.AuthConstants.AUTH_CLAIM;

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
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.PasswordCredentialBuilder;
import org.opendaylight.aaa.api.Authentication;
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

    private static final String NOT_IMPLEMENTED = "not_implemented";
    private static final String UNAUTHORIZED = "unauthorized";

    private static final String TOKEN_GRANT_ENDPOINT = "/token";
    private static final String TOKEN_REVOKE_ENDPOINT = "/revoke";
    private static final String FEDERATION_ENDPOINT = "/federation";

    private static final String TOKEN_EXP_PARAM = "org.opendaylight.aaa.sts.TokenExpirationSecs";
    private static final int DEFAULT_TOKEN_EXP_SECS = 3600;

    private transient int exp;
    private transient OAuthIssuer oi;

    @Override
    public void init(ServletConfig config) throws ServletException {
        String s = config.getInitParameter(TOKEN_EXP_PARAM);
        exp = (s == null || s.isEmpty()) ? DEFAULT_TOKEN_EXP_SECS : Integer
                .valueOf(s);
        oi = new OAuthIssuerImpl(new UUIDValueGenerator());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (req.getServletPath().equals(FEDERATION_ENDPOINT))
            createRefreshToken(req, resp);
        else if (req.getServletPath().equals(TOKEN_GRANT_ENDPOINT))
            createAccessToken(req, resp);
        else if (req.getServletPath().equals(TOKEN_REVOKE_ENDPOINT))
            deleteAccessToken(req, resp);
    }

    // Delete an access token
    private void deleteAccessToken(HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        String token = req.getReader().readLine();
        if (token != null) {
            if (ServiceLocator.INSTANCE.ts.delete(token.trim()))
                resp.setStatus(SC_NO_CONTENT);
            else
                error(resp, SC_UNAUTHORIZED, UNAUTHORIZED);
        } else {
            error(resp, SC_UNAUTHORIZED, UNAUTHORIZED);
        }
    }

    // Create an access token
    private void createAccessToken(HttpServletRequest req,
            HttpServletResponse resp) {
        Claim claim = null;
        try {
            OAuthRequest oauthRequest = new OAuthRequest(req);
            // Credential request...
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
                    GrantType.PASSWORD.toString())) {
                PasswordCredentials pc = new PasswordCredentialBuilder()
                        .setUserName(oauthRequest.getUsername()).setPassword(
                                oauthRequest.getPassword());
                String tenant = oauthRequest.getScopes().iterator().next();
                // Authenticate...
                if (tenant != null)
                    claim = ServiceLocator.INSTANCE.da.authenticate(pc, tenant);
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(
                    GrantType.REFRESH_TOKEN.toString())) {
                // Refresh token...
                String token = oauthRequest.getRefreshToken();
                String domain = oauthRequest.getScopes().iterator().next();
                // Authenticate...
                Authentication auth = ServiceLocator.INSTANCE.ts.get(token);
                if (auth != null && domain != null) {
                    ClaimBuilder cb = new ClaimBuilder(auth);
                    cb.setDomain(domain); // scope domain
                    // Add roles for the scoped domain
                    for (String role : ServiceLocator.INSTANCE.is.listRoles(
                            auth.userId(), domain))
                        cb.addRole(role);
                    claim = cb.build();
                }
            } else {
                // Support authorization code later...
                error(resp, SC_NOT_IMPLEMENTED, NOT_IMPLEMENTED);
            }
        } catch (OAuthProblemException e) {
            error(resp, e);
        } catch (Exception e) {
            error(resp, e);
        }
        // Respond with OAuth token
        oauthAccessTokenResponse(resp, claim);
    }

    // Create a refresh token
    private void createRefreshToken(HttpServletRequest req,
            HttpServletResponse resp) {
        Claim claim = (Claim) req.getAttribute(AUTH_CLAIM);
        oauthRefreshTokenResponse(resp, claim);
    }

    // Build OAuth access token response from the given claim
    private void oauthAccessTokenResponse(HttpServletResponse resp, Claim claim) {
        if (claim == null) {
            error(resp, SC_UNAUTHORIZED, UNAUTHORIZED);
            return;
        }
        try {
            String token = oi.accessToken();
            OAuthResponse r = OAuthASResponse.tokenResponse(SC_CREATED)
                    .setAccessToken(token)
                    .setTokenType(TokenType.BEARER.toString())
                    .setExpiresIn(Integer.toString(exp)).buildJSONMessage();

            // Cache this token...
            Authentication auth = new AuthenticationBuilder(claim)
                    .setExpiration(exp);
            ServiceLocator.INSTANCE.ts.put(token, auth);
            write(resp, r);
        } catch (Exception e) {
            error(resp, e);
        }
    }

    // Build OAuth refresh token response from the given claim mapped and
    // injected by the external IdP
    private void oauthRefreshTokenResponse(HttpServletResponse resp, Claim claim) {
        // Must have a non-null mapped claim
        if (claim == null) {
            error(resp, SC_UNAUTHORIZED, UNAUTHORIZED);
            return;
        }

        String userName = claim.user();
        // Need to have at least a mapped username!
        if (userName == null) {
            error(resp, SC_UNAUTHORIZED, UNAUTHORIZED);
            return;
        }

        // Need to have a corresponding user id in ODL
        String userId = ServiceLocator.INSTANCE.is.getUserId(userName);
        if (userId == null) {
            error(resp, SC_UNAUTHORIZED, UNAUTHORIZED);
            return;
        }

        // Create an unscoped ODL context from the external claim
        Authentication auth = new AuthenticationBuilder(claim)
                .setUserId(userId)
                .setExpiration(exp);

        // Create OAuth response
        try {
            String token = oi.refreshToken();
            OAuthResponse r = OAuthASResponse
                    .tokenResponse(SC_CREATED)
                    .setRefreshToken(token)
                    .setExpiresIn(Integer.toString(exp))
                    .setScope(
                    // Use mapped domain if there is one, else list
                    // all the ones that this user has access to
                            claim.domain() != null ? claim.domain()
                                    : listToString(ServiceLocator.INSTANCE.is
                                            .listDomains(userId)))
                    .buildJSONMessage();
            // Cache this token...
            ServiceLocator.INSTANCE.ts.put(token, auth);
            write(resp, r);
        } catch (Exception e) {
            error(resp, e);
        }
    }

    // Space-delimited string from a list of strings
    private String listToString(List<String> list) {
        StringBuffer sb = new StringBuffer();
        for (String s : list)
            sb.append(s).append(" ");
        return sb.toString().trim();
    }

    // Emit an error OAuthResponse with the given HTTP code
    private void error(HttpServletResponse resp, int httpCode, String error) {
        try {
            OAuthResponse r = OAuthResponse.errorResponse(httpCode)
                    .setError(error).buildJSONMessage();
            write(resp, r);
            resp.sendError(httpCode);
        } catch (Exception e1) {
            // Nothing to do here
        }
    }

    // Emit an error OAuthResponse for the given OAuth-related exception
    private void error(HttpServletResponse resp, OAuthProblemException e) {
        try {
            OAuthResponse r = OAuthResponse.errorResponse(SC_UNAUTHORIZED)
                    .error(e).buildJSONMessage();
            write(resp, r);
            resp.sendError(SC_UNAUTHORIZED);
        } catch (Exception e1) {
            // Nothing to do here
        }
    }

    // Emit an error OAuthResponse for the given generic exception
    private void error(HttpServletResponse resp, Exception e) {
        try {
            OAuthResponse r = OAuthResponse
                    .errorResponse(SC_INTERNAL_SERVER_ERROR)
                    .setError(e.getClass().getName())
                    .setErrorDescription(e.getMessage()).buildJSONMessage();
            write(resp, r);
            resp.sendError(SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e1) {
            // Nothing to do here
        }
    }

    // Write out an OAuthResponse
    private void write(HttpServletResponse resp, OAuthResponse r)
            throws IOException {
        resp.setStatus(r.getResponseStatus());
        PrintWriter pw = resp.getWriter();
        pw.print(r.getBody());
        pw.flush();
        pw.close();
    }
}
