/*
 * Copyright (c) 2015, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.aaa.tokenauthrealm.auth.AuthenticationManager;

class TokenAuthRealmTest {
    private final TokenAuthRealm testRealm = new TokenAuthRealm(new AuthenticationManager(), List::of);

    @Test
    void testTokenAuthRealm() {
        assertEquals("TokenAuthRealm", testRealm.getName());
    }

    @Test
    void testDoGetAuthorizationInfoPrincipalCollectionNullCacheToken() {
        assertThrows(NullPointerException.class, () -> testRealm.doGetAuthorizationInfo(null));
    }

    @Test
    void testGetUsernamePasswordDomainString() {
        final String username = "user";
        final String password = "password";
        final String domain = "domain";
        final String expectedUsernamePasswordString = "user:password:domain";
        assertEquals(expectedUsernamePasswordString, HeaderUtils.getUsernamePasswordDomainString(
                username, password, domain));
    }

    @Test
    void testGetEncodedToken() {
        final String stringToEncode = "admin1:admin1";
        final byte[] bytesToEncode = stringToEncode.getBytes();
        final String expectedToken = org.apache.shiro.codec.Base64.encodeToString(bytesToEncode);
        assertEquals(expectedToken, HeaderUtils.getEncodedToken(stringToEncode));
    }

    @Test
    void testGetTokenAuthHeader() {
        final String encodedCredentials = HeaderUtils.getEncodedToken(HeaderUtils.getUsernamePasswordDomainString(
                "user1", "password", "sdn"));
        final String expectedTokenAuthHeader = "Basic " + encodedCredentials;
        assertEquals(expectedTokenAuthHeader, HeaderUtils.getTokenAuthHeader(encodedCredentials));
    }

    @Test
    void testFormHeadersWithToken() {
        final String authHeader = HeaderUtils.getEncodedToken(
                HeaderUtils.getTokenAuthHeader(
                        HeaderUtils.getUsernamePasswordDomainString(
                                "user1", "password", "sdn")));
        final Map<String, List<String>> actualHeaders = HeaderUtils.formHeadersWithToken(authHeader);
        assertEquals(List.of(authHeader), actualHeaders.get("Authorization"));
    }

    @Test
    void testFormHeaders() {
        final String username = "basicUser";
        final String password = "basicPassword";
        final String domain = "basicDomain";
        final String authHeader = HeaderUtils.getTokenAuthHeader(HeaderUtils.getEncodedToken(
                HeaderUtils.getUsernamePasswordDomainString(
                        username, password, domain)));
        final Map<String, List<String>> actualHeaders = HeaderUtils.formHeaders(username, password, domain);

        assertEquals(List.of(authHeader), actualHeaders.get("Authorization"));
    }

    @Test
    void testDoGetAuthenticationInfoAuthenticationToken() {
        assertThrows(AuthenticationException.class, () -> testRealm.doGetAuthenticationInfo(null));
    }
}
