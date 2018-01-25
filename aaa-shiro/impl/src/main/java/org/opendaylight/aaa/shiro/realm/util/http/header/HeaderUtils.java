/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm.util.http.header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.shiro.codec.Base64;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.HttpBasicAuth;

/**
 * Utilities for HTTP header manipulation.
 */
public class HeaderUtils {

    public static final String USERNAME_DOMAIN_SEPARATOR = "@";

    /**
     *
     * @param credentialToken
     * @return Base64 encoded token
     */
    public static String getEncodedToken(final String credentialToken) {
        return Base64.encodeToString(credentialToken.getBytes());
    }

    /**
     * Bridge new to old style <code>TokenAuth</code> interface.
     *
     * @param username The request username
     * @param password The request password
     * @param domain The request domain
     * @return <code>username:password:domain</code>
     */
    public static String getUsernamePasswordDomainString(final String username, final String password,
                                                         final String domain) {
        return username + HttpBasicAuth.AUTH_SEP + password  + HttpBasicAuth.AUTH_SEP + domain;
    }

    /**
     *
     * @param encodedToken
     * @return Basic <code>encodedToken</code>
     */
    public static String getTokenAuthHeader(final String encodedToken) {
        return HttpBasicAuth.BASIC_PREFIX + encodedToken;
    }

    /**
     *
     * @param tokenAuthHeader
     * @return a map with the basic auth header
     */
    public static Map<String, List<String>> formHeadersWithToken(final String tokenAuthHeader) {
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        final List<String> headerValue = new ArrayList<String>();
        headerValue.add(tokenAuthHeader);
        headers.put(HttpBasicAuth.AUTH_HEADER, headerValue);
        return headers;
    }

    /**
     * Adapter between basic authentication mechanism and existing
     * <code>TokenAuth</code> interface.
     *
     * @param username Username from the request
     * @param password Password from the request
     * @param domain Domain from the request
     * @return input map for <code>TokenAuth.validate()</code>
     */
    public static Map<String, List<String>> formHeaders(final String username, final String password,
                                                        final String domain) {
        String usernamePasswordToken = getUsernamePasswordDomainString(username, password, domain);
        String encodedToken = getEncodedToken(usernamePasswordToken);
        String tokenAuthHeader = getTokenAuthHeader(encodedToken);
        return formHeadersWithToken(tokenAuthHeader);
    }

    /**
     * Extract username from the form <code>user</code> or <code>user@domain</code>
     *
     * @param possiblyQualifiedUsername <code>user</code> or <code>user@domain</code>
     * @return username
     */
    public static String extractUsername(final String possiblyQualifiedUsername) {
        if (possiblyQualifiedUsername.contains(USERNAME_DOMAIN_SEPARATOR)) {
            final String [] qualifiedUserArray = possiblyQualifiedUsername.split(USERNAME_DOMAIN_SEPARATOR);
            return qualifiedUserArray[0];
        }
        return possiblyQualifiedUsername;
    }

    /**
     * Extract domain from the form <code>user</code> or <code>user@domain</code>
     *
     * @param possiblyQualifiedUsername <code>user</code> or <code>user@domain</code>
     * @return the domain or <code>HttpBasicAuth.DEFAULT_DOMAIN</code>
     */
    public static String extractDomain(final String possiblyQualifiedUsername) {
        if (possiblyQualifiedUsername.contains(USERNAME_DOMAIN_SEPARATOR)) {
            final String [] qualifiedUserArray = possiblyQualifiedUsername.split(USERNAME_DOMAIN_SEPARATOR);
            return qualifiedUserArray[1];
        }
        return HttpBasicAuth.DEFAULT_DOMAIN;
    }
}
