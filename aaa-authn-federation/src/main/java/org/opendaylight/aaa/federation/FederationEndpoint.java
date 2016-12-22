/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
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
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;

/**
 * An endpoint for claim-based authentication federation (in-bound).
 *
 * @author liemmn
 *
 */
@Deprecated
public class FederationEndpoint extends HttpServlet {

    private static final long serialVersionUID = -5553885846238987245L;

    /** An in-bound authentication claim */
    static final String AUTH_CLAIM = "AAA-CLAIM";

    private static final String UNAUTHORIZED = "unauthorized";

    private transient OAuthIssuer oi;

    @Override
    public void init(ServletConfig config) throws ServletException {
        oi = new OAuthIssuerImpl(new UUIDValueGenerator());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
            ServletException {
        try {
            createRefreshToken(req, resp);
        } catch (Exception e) {
            error(resp, SC_UNAUTHORIZED, e.getMessage());
        }
    }

    // Create a refresh token
    private void createRefreshToken(HttpServletRequest req, HttpServletResponse resp)
            throws OAuthSystemException, IOException {
        Claim claim = (Claim) req.getAttribute(AUTH_CLAIM);
        oauthRefreshTokenResponse(resp, claim);
    }

    // Build OAuth refresh token response from the given claim mapped and
    // injected by the external IdP
    private void oauthRefreshTokenResponse(HttpServletResponse resp, Claim claim)
            throws OAuthSystemException, IOException {
        if (claim == null) {
            throw new AuthenticationException(UNAUTHORIZED);
        }

        String userName = claim.user();
        // Need to have at least a mapped username!
        if (userName == null) {
            throw new AuthenticationException(UNAUTHORIZED);
        }

        String domain = claim.domain();
        // Need to have at least a domain!
        if (domain == null) {
            throw new AuthenticationException(UNAUTHORIZED);
        }

        String userId = userName + "@" + domain;

        // Create an unscoped ODL context from the external claim
        Authentication auth = new AuthenticationBuilder(new ClaimBuilder(claim).setUserId(userId)
                .build()).setExpiration(tokenExpiration()).build();

        // Create OAuth response
        String token = oi.refreshToken();
        OAuthResponse r = OAuthASResponse
                .tokenResponse(SC_CREATED)
                .setRefreshToken(token)
                .setExpiresIn(Long.toString(auth.expiration()))
                .setScope(
                // Use mapped domain if there is one, else list
                // all the ones that this user has access to
                        (claim.domain().isEmpty()) ? listToString(ServiceLocator.getInstance()
                                .getIdmService().listDomains(userId)) : claim.domain())
                .buildJSONMessage();
        // Cache this token...
        ServiceLocator.getInstance().getTokenStore().put(token, auth);
        write(resp, r);
    }

    // Token expiration
    private long tokenExpiration() {
        return ServiceLocator.getInstance().getTokenStore().tokenExpiration();
    }

    // Space-delimited string from a list of strings
    private String listToString(List<String> list) {
        StringBuffer sb = new StringBuffer();
        for (String s : list) {
            sb.append(s).append(" ");
        }
        return sb.toString().trim();
    }

    // Emit an error OAuthResponse with the given HTTP code
    private void error(HttpServletResponse resp, int httpCode, String error) {
        try {
            OAuthResponse r = OAuthResponse.errorResponse(httpCode).setError(error)
                    .buildJSONMessage();
            write(resp, r);
        } catch (Exception e1) {
            // Nothing to do here
        }
    }

    // Write out an OAuthResponse
    private void write(HttpServletResponse resp, OAuthResponse r) throws IOException {
        resp.setStatus(r.getResponseStatus());
        PrintWriter pw = resp.getWriter();
        pw.print(r.getBody());
        pw.flush();
        pw.close();
    }
}
