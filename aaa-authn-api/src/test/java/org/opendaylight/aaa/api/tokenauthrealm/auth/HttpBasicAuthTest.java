/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.tokenauthrealm.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;

public class HttpBasicAuthTest {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String DOMAIN = "sdn";
    private HttpBasicAuth auth;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        CredentialAuth<PasswordCredentials> mockCredentialAuth = mock(CredentialAuth.class);
        auth = new HttpBasicAuth(mockCredentialAuth);
        when(mockCredentialAuth.authenticate(
                new PasswordCredentialBuilder().setUserName(USERNAME).setPassword(PASSWORD).setDomain(DOMAIN).build()))
                        .thenReturn(new ClaimBuilder().setUser("admin").addRole("admin").setUserId("123").build());
        when(mockCredentialAuth.authenticate(
                new PasswordCredentialBuilder().setUserName(USERNAME).setPassword("bozo").setDomain(DOMAIN).build()))
                        .thenThrow(new AuthenticationException("barf"));
    }

    @Test
    public void testValidateOk() throws UnsupportedEncodingException {
        String data = USERNAME + ":" + PASSWORD + ":" + DOMAIN;
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Arrays.asList(
            "Basic " + new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)))));
        Claim claim = auth.validate(headers);
        assertNotNull(claim);
        assertEquals(USERNAME, claim.user());
        assertEquals("admin", claim.roles().iterator().next());
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateBadPassword() throws UnsupportedEncodingException {
        String data = USERNAME + ":bozo:" + DOMAIN;
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Arrays.asList(
            "Basic " + new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)))));
        auth.validate(headers);
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateBadPasswordNoDomain() throws UnsupportedEncodingException {
        String data = USERNAME + ":bozo";
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Arrays.asList(
            "Basic " + new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)))));
        auth.validate(headers);
    }

    @Test(expected = AuthenticationException.class)
    public void testBadHeaderFormatNoPassword() throws UnsupportedEncodingException {
        // just provide the username
        String data = USERNAME;
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Arrays.asList(
            "Basic " + new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)))));
        auth.validate(headers);
    }

    @Test(expected = AuthenticationException.class)
    public void testBadHeaderFormat() throws UnsupportedEncodingException {
        // provide username:
        String data = USERNAME + "$" + PASSWORD;
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Arrays.asList(
            "Basic " + new String(Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8)))));
        auth.validate(headers);
    }
}
