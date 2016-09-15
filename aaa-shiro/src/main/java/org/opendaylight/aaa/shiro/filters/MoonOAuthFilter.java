/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.TokenType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.shiro.moon.MoonPrincipal;
import org.opendaylight.aaa.sts.OAuthRequest;
import org.opendaylight.aaa.sts.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * MoonOAuthFilter filters oauth1 requests form token based authentication
 * @author Alioune BA alioune.ba@orange.com
 *
 */
public class MoonOAuthFilter extends AuthenticatingFilter{

    private static final Logger LOG = LoggerFactory.getLogger(MoonOAuthFilter.class);

    private static final String DOMAIN_SCOPE_REQUIRED = "Domain scope required";
    private static final String NOT_IMPLEMENTED = "not_implemented";
    private static final String UNAUTHORIZED = "unauthorized";
    private static final String UNAUTHORIZED_CREDENTIALS = "Unauthorized: Login/Password incorrect";

    static final String TOKEN_GRANT_ENDPOINT = "/token";
    static final String TOKEN_REVOKE_ENDPOINT = "/revoke";
    static final String TOKEN_VALIDATE_ENDPOINT = "/validate";

    @Override
    protected UsernamePasswordToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        OAuthRequest oauthRequest = new OAuthRequest(httpRequest);
        return new UsernamePasswordToken(oauthRequest.getUsername(), oauthRequest.getPassword());
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        return executeLogin(request, response);
    }

    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
            ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse= (HttpServletResponse) response;
        MoonPrincipal principal = (MoonPrincipal) subject.getPrincipals().getPrimaryPrincipal();
        Claim claim = principal.principalToClaim();
        oauthAccessTokenResponse(httpResponse, claim, "", principal.getToken());
        return true;
    }

    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
            ServletRequest request, ServletResponse response) {
        HttpServletResponse resp = (HttpServletResponse) response;
        error(resp, SC_BAD_REQUEST, UNAUTHORIZED_CREDENTIALS);
        return false;
    }

    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req= (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        try {
            if (req.getServletPath().equals(TOKEN_GRANT_ENDPOINT)) {
                UsernamePasswordToken token = createToken(request, response);
                if (token == null) {
                    String msg = "A valid non-null AuthenticationToken " +
                            "must be created in order to execute a login attempt.";
                    throw new IllegalStateException(msg);
                }
                try {
                    Subject subject = getSubject(request, response);
                    subject.login(token);
                    return onLoginSuccess(token, subject, request, response);
                } catch (AuthenticationException e) {
                    return onLoginFailure(token, e, request, response);
                }
            } else if (req.getServletPath().equals(TOKEN_REVOKE_ENDPOINT)) {
                //TODO: deleteAccessToken(req, resp);
            } else if (req.getServletPath().equals(TOKEN_VALIDATE_ENDPOINT)) {
                //TODO: validateToken(req, resp);
            }
        } catch (AuthenticationException e) {
            error(resp, SC_UNAUTHORIZED, e.getMessage());
        } catch (OAuthProblemException oe) {
            error(resp, oe);
        } catch (Exception e) {
            error(resp, e);
        }
        return false;
    }

    private void oauthAccessTokenResponse(HttpServletResponse resp, Claim claim, String clientId, String token)
            throws OAuthSystemException, IOException {
        if (claim == null) {
            throw new AuthenticationException(UNAUTHORIZED);
        }

        // Cache this token...
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder(claim).setClientId(
                clientId).build()).setExpiration(tokenExpiration()).build();
        ServiceLocator.getInstance().getTokenStore().put(token, auth);

        OAuthResponse r = OAuthASResponse.tokenResponse(SC_CREATED).setAccessToken(token)
                                         .setTokenType(TokenType.BEARER.toString())
                                         .setExpiresIn(Long.toString(auth.expiration()))
                                         .buildJSONMessage();
        write(resp, r);
    }

    private void write(HttpServletResponse resp, OAuthResponse r) throws IOException {
        resp.setStatus(r.getResponseStatus());
        PrintWriter pw = resp.getWriter();
        pw.print(r.getBody());
        pw.flush();
        pw.close();
    }

    private long tokenExpiration() {
        return ServiceLocator.getInstance().getTokenStore().tokenExpiration();
    }

    // Emit an error OAuthResponse with the given HTTP code
    private void error(HttpServletResponse resp, int httpCode, String error) {
        try {
            OAuthResponse r = OAuthResponse.errorResponse(httpCode).setError(error)
                                           .buildJSONMessage();
            write(resp, r);
        } catch (Exception e1) {
            LOG.error("Failed to write the error ", e1);
        }
    }

    private void error(HttpServletResponse resp, OAuthProblemException e) {
        try {
            OAuthResponse r = OAuthResponse.errorResponse(SC_BAD_REQUEST).error(e)
                                           .buildJSONMessage();
            write(resp, r);
        } catch (Exception e1) {
            LOG.error("Failed to write the error ", e1);
        }
    }

    private void error(HttpServletResponse resp, Exception e) {
        try {
            OAuthResponse r = OAuthResponse.errorResponse(SC_INTERNAL_SERVER_ERROR)
                                           .setError(e.getClass().getName())
                                           .setErrorDescription(e.getMessage()).buildJSONMessage();
            write(resp, r);
        } catch (Exception e1) {
            LOG.error("Failed to write the error ", e1);
        }
    }

}
