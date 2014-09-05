/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import static org.opendaylight.aaa.AuthConstants.AUTH_CLAIM;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;

/**
 * A generic {@link Filter} for {@link ClaimAuth} implementations.
 *
 * @author liemmn
 *
 */
public class ClaimAuthFilter implements Filter {
    private static final Logger logger =
        LoggerFactory.getLogger(ClaimAuthFilter.class);

    private static final String CGI_AUTH_TYPE = "AUTH_TYPE";
    private static final String CGI_PATH_INFO = "PATH_INFO";
    private static final String CGI_PATH_TRANSLATED = "PATH_TRANSLATED";
    private static final String CGI_QUERY_STRING = "QUERY_STRING";
    private static final String CGI_REMOTE_ADDR = "REMOTE_ADDR";
    private static final String CGI_REMOTE_HOST = "REMOTE_HOST";
    private static final String CGI_REMOTE_PORT = "REMOTE_PORT";
    private static final String CGI_REMOTE_USER = "REMOTE_USER";
    private static final String CGI_REQUEST_METHOD = "REQUEST_METHOD";
    private static final String CGI_SCRIPT_NAME = "SCRIPT_NAME";
    private static final String CGI_SERVER_PROTOCOL = "SERVER_PROTOCOL";

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        for (ClaimAuth ca : ServiceLocator.INSTANCE.ca) {
            Claim claim = ca.transform(claims((HttpServletRequest) req));
            if (claim != null) {
                req.setAttribute(AUTH_CLAIM, claim);
                // No need to do further transformation since it has been done
                break;
            }
        }
        chain.doFilter(req, resp);
    }

    // Extract attributes and headers out of the request
    private Map<String, Object> claims(HttpServletRequest req) {
        Map<String, Object> claims = new HashMap<>();

        /*
         * Tomcat has a bug/feature, not all attributes are enumerated
         * by getAttributeNames() therefore getAttributeNames() cannot
         * be used to obtain the full set of attributes. However if
         * you know the name of the attribute a priori you can call
         * getAttribute() and obtain the value. Therefore we maintain
         * a list of attribute names (httpAttributes) which will be
         * used to call getAttribute() with so we don't miss essential
         * attributes.
         *
         * This is the Tomcat bug, note it is marked WONTFIX.
         * Bug 25363 - request.getAttributeNames() not working properly
         * Status: RESOLVED WONTFIX
         * https://issues.apache.org/bugzilla/show_bug.cgi?id=25363
         *
         * The solution adopted by Tomcat is to document the behavior
         * in the "The Apache Tomcat Connector - Reference Guide"
         * under the JkEnvVar property where is says:
         *
         * You can retrieve the variables on Tomcat as request
         * attributes via request.getAttribute(attributeName). Note
         * that the variables send via JkEnvVar will not be listed in
         * request.getAttributeNames().
         */

        // Capture attributes which can be enumerated ...
        @SuppressWarnings("unchecked")
        Enumeration<String> attrs = req.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
            claims.put(attr, req.getAttribute(attr));
        }

        // Capture specific attributes which cannot be enumerated ...
        for (String attr : FederationConfiguration.instance().httpAttributes()) {
            claims.put(attr, req.getAttribute(attr));
        }

        /*
         * In general we should not utilize HTTP headers as validated
         * security assertions because they are too easy to
         * forge. Therefore in general we don't include HTTP headers,
         * however in certain circumstances specific headers may be
         * acceptable, thus we permit an admin to configure the
         * capture of specific headers.
         */
        for (String header : FederationConfiguration.instance().httpHeaders()) {
            claims.put(header, req.getHeader(header));
        }

        // Capture standard CGI variables...
        claims.put(CGI_AUTH_TYPE, req.getAuthType());
        claims.put(CGI_PATH_INFO, req.getPathInfo());
        claims.put(CGI_PATH_TRANSLATED, req.getPathTranslated());
        claims.put(CGI_QUERY_STRING, req.getQueryString());
        claims.put(CGI_REMOTE_ADDR, req.getRemoteAddr());
        claims.put(CGI_REMOTE_HOST, req.getRemoteHost());
        claims.put(CGI_REMOTE_PORT, req.getRemotePort());
        claims.put(CGI_REMOTE_USER, req.getRemoteUser());
        claims.put(CGI_REQUEST_METHOD, req.getMethod());
        claims.put(CGI_SCRIPT_NAME, req.getServletPath());
        claims.put(CGI_SERVER_PROTOCOL, req.getProtocol());

        if (logger.isDebugEnabled()) {
            logger.debug("ClaimAuthFilter claims = " + claims.toString());
        }

        return claims;
    }

}
