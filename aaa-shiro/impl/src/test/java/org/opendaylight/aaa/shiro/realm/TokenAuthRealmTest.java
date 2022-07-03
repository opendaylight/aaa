/*
 * Copyright (c) 2015, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.Test;
import org.opendaylight.aaa.shiro.realm.util.TokenUtils;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.aaa.tokenauthrealm.auth.AuthenticationManager;
import org.opendaylight.aaa.tokenauthrealm.auth.TokenAuthenticators;

public class TokenAuthRealmTest {
    private final TokenAuthRealm testRealm = new TokenAuthRealm(new AuthenticationManager(), new TokenAuthenticators());

    @Test
    public void testTokenAuthRealm() {
        assertEquals("TokenAuthRealm", testRealm.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testDoGetAuthorizationInfoPrincipalCollectionNullCacheToken() {
        testRealm.doGetAuthorizationInfo(null);
    }

    @Test
    public void testGetUsernamePasswordDomainString() {
        final String username = "user";
        final String password = "password";
        final String domain = "domain";
        final String expectedUsernamePasswordString = "user:password:domain";
        assertEquals(expectedUsernamePasswordString, HeaderUtils.getUsernamePasswordDomainString(
                username, password, domain));
    }

    @Test
    public void testGetEncodedToken() {
        final String stringToEncode = "admin1:admin1";
        final byte[] bytesToEncode = stringToEncode.getBytes();
        final String expectedToken = org.apache.shiro.codec.Base64.encodeToString(bytesToEncode);
        assertEquals(expectedToken, HeaderUtils.getEncodedToken(stringToEncode));
    }

    @Test
    public void testGetTokenAuthHeader() {
        final String encodedCredentials = HeaderUtils.getEncodedToken(HeaderUtils.getUsernamePasswordDomainString(
                "user1", "password", "sdn"));
        final String expectedTokenAuthHeader = "Basic " + encodedCredentials;
        assertEquals(expectedTokenAuthHeader, HeaderUtils.getTokenAuthHeader(encodedCredentials));
    }

    @Test
    public void testFormHeadersWithToken() {
        final String authHeader = HeaderUtils.getEncodedToken(
                HeaderUtils.getTokenAuthHeader(
                        HeaderUtils.getUsernamePasswordDomainString(
                                "user1", "password", "sdn")));
        final Map<String, List<String>> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Authorization", Lists.newArrayList(authHeader));
        final Map<String, List<String>> actualHeaders = HeaderUtils.formHeadersWithToken(authHeader);
        List<String> value;
        for (String key : expectedHeaders.keySet()) {
            value = expectedHeaders.get(key);
            assertTrue(actualHeaders.get(key).equals(value));
        }
    }

    @Test
    public void testFormHeaders() {
        final String username = "basicUser";
        final String password = "basicPassword";
        final String domain = "basicDomain";
        final String authHeader = HeaderUtils.getTokenAuthHeader(HeaderUtils.getEncodedToken(
                HeaderUtils.getUsernamePasswordDomainString(
                        username, password, domain)));
        final Map<String, List<String>> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Authorization", Lists.newArrayList(authHeader));
        final Map<String, List<String>> actualHeaders = HeaderUtils.formHeaders(username, password, domain);
        List<String> value;
        for (String key : expectedHeaders.keySet()) {
            value = expectedHeaders.get(key);
            assertTrue(actualHeaders.get(key).equals(value));
        }
    }

    @Test(expected = org.apache.shiro.authc.AuthenticationException.class)
    public void testDoGetAuthenticationInfoAuthenticationToken() {
        testRealm.doGetAuthenticationInfo(null);
    }

    @Test
    public void testExtractUsernameNullUsername() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn(null);
        assertNull(TokenUtils.extractUsername(at));
    }

    @Test(expected = ClassCastException.class)
    public void testExtractPasswordNullPassword() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn("username");
        when(at.getCredentials()).thenReturn(null);
        TokenUtils.extractPassword(at);
    }

    @Test(expected = ClassCastException.class)
    public void testExtractUsernameBadUsernameClass() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn(1);
        TokenUtils.extractUsername(at);
    }

    @Test(expected = ClassCastException.class)
    public void testExtractPasswordBadPasswordClass() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn("username");
        when(at.getCredentials()).thenReturn(1);
        TokenUtils.extractPassword(at);
    }
}
