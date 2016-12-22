/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.opendaylight.aaa.federation.FederationEndpoint.AUTH_CLAIM;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic {@link Filter} for {@link ClaimAuth} implementations.
 * <p>
 * This filter trusts any authentication metadata bound to a request. A request
 * with fake authentication claims could be forged by an attacker and submitted
 * to one of the Connector ports the engine is listening on and we would blindly
 * accept the forged information in this filter. Therefore it is vital we only
 * accept authentication claims from a trusted proxy. It is incumbent upon the
 * site administrator to dedicate specific connector ports on which previously
 * authenticated requests from a trusted proxy will be sent to and to assure
 * only a trusted proxy can connect to that port. The site administrator must
 * enumerate those ports in the configuration. We reject any request which did
 * not originate on one of the configured secure proxy ports.
 *
 * @author liemmn
 *
 */
@Deprecated
public class ClaimAuthFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(ClaimAuthFilter.class);

    private static final String CGI_AUTH_TYPE = "AUTH_TYPE";
    private static final String CGI_PATH_INFO = "PATH_INFO";
    private static final String CGI_PATH_TRANSLATED = "PATH_TRANSLATED";
    private static final String CGI_QUERY_STRING = "QUERY_STRING";
    private static final String CGI_REMOTE_ADDR = "REMOTE_ADDR";
    private static final String CGI_REMOTE_HOST = "REMOTE_HOST";
    private static final String CGI_REMOTE_PORT = "REMOTE_PORT";
    private static final String CGI_REMOTE_USER = "REMOTE_USER";
    private static final String CGI_REMOTE_USER_GROUPS = "REMOTE_USER_GROUPS";
    private static final String CGI_REQUEST_METHOD = "REQUEST_METHOD";
    private static final String CGI_SCRIPT_NAME = "SCRIPT_NAME";
    private static final String CGI_SERVER_PROTOCOL = "SERVER_PROTOCOL";

    static final String UNAUTHORIZED_PORT_ERR = "Unauthorized proxy port";

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        Set<Integer> secureProxyPorts;
        int localPort;

        // Check to see if we are communicated over an authorized port or not
        secureProxyPorts = FederationConfiguration.instance().secureProxyPorts();
        localPort = req.getLocalPort();
        if (!secureProxyPorts.contains(localPort)) {
            ((HttpServletResponse) resp).sendError(SC_UNAUTHORIZED, UNAUTHORIZED_PORT_ERR);
            return;
        }

        // Let's do some transformation!
        List<ClaimAuth> claimAuthCollection = ServiceLocator.getInstance().getClaimAuthCollection();
        for (ClaimAuth ca : claimAuthCollection) {
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
        String name;
        Object objectValue;
        String stringValue;
        Map<String, Object> claims = new HashMap<>();

        /*
         * Tomcat has a bug/feature, not all attributes are enumerated by
         * getAttributeNames() therefore getAttributeNames() cannot be used to
         * obtain the full set of attributes. However if you know the name of
         * the attribute a priori you can call getAttribute() and obtain the
         * value. Therefore we maintain a list of attribute names
         * (httpAttributes) which will be used to call getAttribute() with so we
         * don't miss essential attributes.
         *
         * This is the Tomcat bug, note it is marked WONTFIX. Bug 25363 -
         * request.getAttributeNames() not working properly Status: RESOLVED
         * WONTFIX https://issues.apache.org/bugzilla/show_bug.cgi?id=25363
         *
         * The solution adopted by Tomcat is to document the behavior in the
         * "The Apache Tomcat Connector - Reference Guide" under the JkEnvVar
         * property where is says:
         *
         * You can retrieve the variables on Tomcat as request attributes via
         * request.getAttribute(attributeName). Note that the variables send via
         * JkEnvVar will not be listed in request.getAttributeNames().
         */

        // Capture attributes which can be enumerated ...
        @SuppressWarnings("unchecked")
        Enumeration<String> attrs = req.getAttributeNames();
        while (attrs.hasMoreElements()) {
            name = attrs.nextElement();
            objectValue = req.getAttribute(name);
            if (objectValue instanceof String) {
                // metadata might be i18n, assume UTF8 and decode
                stringValue = decodeUTF8((String) objectValue);
                objectValue = stringValue;
            }
            claims.put(name, objectValue);
        }

        // Capture specific attributes which cannot be enumerated ...
        for (String attr : FederationConfiguration.instance().httpAttributes()) {
            name = attr;
            objectValue = req.getAttribute(name);
            if (objectValue instanceof String) {
                // metadata might be i18n, assume UTF8 and decode
                stringValue = decodeUTF8((String) objectValue);
                objectValue = stringValue;
            }
            claims.put(name, objectValue);
        }

        /*
         * In general we should not utilize HTTP headers as validated security
         * assertions because they are too easy to forge. Therefore in general
         * we don't include HTTP headers, however in certain circumstances
         * specific headers may be acceptable, thus we permit an admin to
         * configure the capture of specific headers.
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
        // remote user might be i18n, assume UTF8 and decode
        claims.put(CGI_REMOTE_USER, decodeUTF8(req.getRemoteUser()));
        claims.put(CGI_REMOTE_USER_GROUPS, req.getAttribute(CGI_REMOTE_USER_GROUPS));
        claims.put(CGI_REQUEST_METHOD, req.getMethod());
        claims.put(CGI_SCRIPT_NAME, req.getServletPath());
        claims.put(CGI_SERVER_PROTOCOL, req.getProtocol());

        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimAuthFilter claims = {}", claims.toString());
        }

        return claims;
    }

    /**
     * Decode from UTF-8, return Unicode.
     *
     * If we're unable to UTF-8 decode the string the fallback is to return the
     * string unmodified and log a warning.
     *
     * Some data, especially metadata attached to a user principal may be
     * internationalized (i18n). The classic examples are the user's name,
     * location, organization, etc. We need to be able to read this metadata and
     * decode it into unicode characters so that we properly handle i18n string
     * values.
     *
     * One of the the prolems is we often don't know the encoding (i.e. charset)
     * of the string. RFC-5987 is supposed to define how non-ASCII values are
     * transmitted in HTTP headers, this is a follow on from the work in
     * RFC-2231. However at the time of this writing these RFC's are not
     * implemented in the Servlet Request classes. Not only are these RFC's
     * unimplemented but they are specific to HTTP headers, much of our metadata
     * arrives via attributes as opposed to being in a header.
     *
     * Note: ASCII encoding is a subset of UTF-8 encoding therefore any strings
     * which are pure ASCII will decode from UTF-8 just fine. However on the
     * other hand Latin-1 (ISO-8859-1) encoding is not compatible with UTF-8 for
     * code points in the range 128-255 (i.e. beyond 7-bit ascii). ISO-8859-1 is
     * the default encoding for HTTP and HTML 4, however the consensus is the
     * use of ISO-8859-1 was a mistake and Unicode with UTF-8 encoding is now
     * the norm. If a string value is transmitted encoded in ISO-8859-1
     * contaiing code points in the range 128-255 and we try to UTF-8 decode it
     * it will either not be the correct decoded string or it will throw a
     * decoding exception.
     *
     * Conventional practice at the moment is for the sending side to encode
     * internationalized values in UTF-8 with the receving end decoding the
     * value back from UTF-8. We do not expect the use of ISO-8859-1 on these
     * attributes. However due to peculiarities of the Java String
     * implementation we have to specify the raw bytes are encoded in ISO-8859-1
     * just to get back the raw bytes to be able to feed into the UTF-8 decoder.
     * This doesn't seem right but it is because we need the full 8-bit byte and
     * the only way to say "unmodified 8-bit bytes" in Java is to call it
     * ISO-8859-1. Ugh!
     *
     * @param string
     *            The input string in UTF-8 to be decoded.
     * @return Unicode string
     */
    private String decodeUTF8(String string) {
        if (string == null) {
            return null;
        }
        try {
            return new String(string.getBytes("ISO8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Unable to UTF-8 decode: ", string, e);
            return string;
        }
    }

}
