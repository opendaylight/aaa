/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationToken;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class TokenAuthRealmTest extends TokenAuthRealm {

    private TokenAuthRealm testRealm = new TokenAuthRealm();

    @Test
    public void testTokenAuthRealm() {
        assertEquals("TokenAuthRealm", testRealm.getName());
    }

    @Test (expected = NullPointerException.class)
    public void testDoGetAuthorizationInfoPrincipalCollectionNullCacheToken() {
        testRealm.doGetAuthorizationInfo(null);
    }

    @Test
    public void testGetUsernamePasswordString() {
        final String username = "user";
        final String password = "password";
        final String expectedUsernamePasswordString = "user:password";
        assertEquals(expectedUsernamePasswordString, getUsernamePasswordString(username,password));
    }

    @Test
    public void testGetEncodedToken() {
        final String stringToEncode = "admin1:admin1";
        final byte [] bytesToEncode = stringToEncode.getBytes();
        final String expectedToken = org.apache.shiro.codec.Base64.encodeToString(bytesToEncode);
        assertEquals(expectedToken, getEncodedToken(stringToEncode));
    }

    @Test
    public void testGetTokenAuthHeader() {
        final String encodedCredentials = getEncodedToken(
                getUsernamePasswordString("user1","password"));
        final String expectedTokenAuthHeader = "Basic " + encodedCredentials;
        assertEquals(expectedTokenAuthHeader, getTokenAuthHeader(encodedCredentials));
    }

    @Test
    public void testFormHeadersWithToken() {
        final String authHeader = getEncodedToken(
                getTokenAuthHeader(getUsernamePasswordString("user1","password")));
        final Map<String, List<String>> expectedHeaders = new HashMap<String, List<String>>();
        expectedHeaders.put("Authorization", Lists.newArrayList(authHeader));
        final Map<String, List<String>> actualHeaders = formHeadersWithToken(authHeader);
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
        final String authHeader = getTokenAuthHeader(
                getEncodedToken(getUsernamePasswordString(username, password)));
        final Map<String, List<String>> expectedHeaders = new HashMap<String, List<String>>();
        expectedHeaders.put("Authorization", Lists.newArrayList(authHeader));
        final Map<String, List<String>> actualHeaders = formHeaders(username, password);
        List<String> value;
        for (String key : expectedHeaders.keySet()) {
            value = expectedHeaders.get(key);
            assertTrue(actualHeaders.get(key).equals(value));
        }
    }

    @Test
    public void testIsTokenAuthAvailable() {
        assertFalse(testRealm.isTokenAuthAvailable());
    }

    @Test (expected = org.apache.shiro.authc.AuthenticationException.class)
    public void testDoGetAuthenticationInfoAuthenticationToken() {
        testRealm.doGetAuthenticationInfo(null);
    }

    @Test
    public void testExtractUsernameNullUsername() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn(null);
        assertNull(extractUsername(at));
    }

    @Test (expected = ClassCastException.class)
    public void testExtractPasswordNullPassword() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn("username");
        when(at.getCredentials()).thenReturn(null);
        extractPassword(at);
    }

    @Test (expected = ClassCastException.class)
    public void testExtractUsernameBadUsernameClass() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn(new Integer(1));
        extractUsername(at);
    }

    @Test (expected = ClassCastException.class)
    public void testExtractPasswordBadPasswordClass() {
        AuthenticationToken at = mock(AuthenticationToken.class);
        when(at.getPrincipal()).thenReturn("username");
        when(at.getCredentials()).thenReturn(new Integer(1));
        extractPassword(at);
    }
}
