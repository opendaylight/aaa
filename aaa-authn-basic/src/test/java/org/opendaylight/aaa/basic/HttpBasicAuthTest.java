/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.PasswordCredentialBuilder;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;

import com.sun.jersey.core.util.Base64;

public class HttpBasicAuthTest {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";
    private HttpBasicAuth auth;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        auth = new HttpBasicAuth();
        auth.ca = mock(CredentialAuth.class);
        when(
                auth.ca.authenticate(new PasswordCredentialBuilder()
                        .setUserName(USERNAME).setPassword(PASSWORD).build(),
                        null)).thenReturn(
                new ClaimBuilder().setUser("admin").addRole("admin").setUserId("123").build());
        when(
                auth.ca.authenticate(new PasswordCredentialBuilder()
                        .setUserName(USERNAME).setPassword("bozo").build(),
                        null)).thenThrow(new AuthenticationException("barf"));
    }

    @Test
    public void testValidateOk() throws UnsupportedEncodingException {
        String data = USERNAME + ":" + PASSWORD;
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(
                "Authorization",
                Arrays.asList("Basic "
                        + new String(Base64.encode(data.getBytes("utf-8")))));
        Claim claim = auth.validate(headers);
        assertNotNull(claim);
        assertEquals(USERNAME, claim.user());
        assertEquals("admin", claim.roles().iterator().next());
    }

    @Test(expected = AuthenticationException.class)
    public void testValidateBadPassword() throws UnsupportedEncodingException {
        String data = USERNAME + ":bozo";
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(
                "Authorization",
                Arrays.asList("Basic "
                        + new String(Base64.encode(data.getBytes("utf-8")))));
        auth.validate(headers);
    }
}
