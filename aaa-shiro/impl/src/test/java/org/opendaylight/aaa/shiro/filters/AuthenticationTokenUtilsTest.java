/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import static org.junit.Assert.*;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import org.opendaylight.aaa.shiro.filters.AuthenticationTokenUtils;

/**
 * Tests authentication token output utilities.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class AuthenticationTokenUtilsTest {

    /**
     * A sample non-UsernamePasswordToken implementation for testing.
     */
    private final class NotUsernamePasswordToken implements AuthenticationToken {

        @Override
        public Object getPrincipal() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }
    }

    @Test
    public void testIsUsernamePasswordToken() throws Exception {
        // null test
        final AuthenticationToken nullUsernamePasswordToken = null;
        assertFalse(AuthenticationTokenUtils.isUsernamePasswordToken(nullUsernamePasswordToken));

        // alternate implementation of AuthenticationToken
        final AuthenticationToken notUsernamePasswordToken = new NotUsernamePasswordToken();
        assertFalse(AuthenticationTokenUtils.isUsernamePasswordToken(notUsernamePasswordToken));

        // positive test case
        final AuthenticationToken positiveUsernamePasswordToken = new UsernamePasswordToken();
        assertTrue(AuthenticationTokenUtils.isUsernamePasswordToken(positiveUsernamePasswordToken));

    }

    @Test
    public void testExtractUsername() throws Exception {
        // null test
        final AuthenticationToken nullAuthenticationToken = null;
        assertEquals(AuthenticationTokenUtils.DEFAULT_TOKEN,
                AuthenticationTokenUtils.extractUsername(nullAuthenticationToken));

        // non-UsernamePasswordToken test
        final AuthenticationToken notUsernamePasswordToken = new NotUsernamePasswordToken();
        assertEquals(AuthenticationTokenUtils.DEFAULT_TOKEN,
                AuthenticationTokenUtils.extractUsername(notUsernamePasswordToken));

        // null username test
        final UsernamePasswordToken nullUsername = new UsernamePasswordToken();
        nullUsername.setUsername(null);
        assertEquals(AuthenticationTokenUtils.DEFAULT_USERNAME,
                AuthenticationTokenUtils.extractUsername(nullUsername));

        // positive test
        final UsernamePasswordToken positiveUsernamePasswordToken = new UsernamePasswordToken();
        final String testUsername = "testUser1";
        positiveUsernamePasswordToken.setUsername(testUsername);
        assertEquals(testUsername, AuthenticationTokenUtils.extractUsername(positiveUsernamePasswordToken));
    }

    @Test
    public void testExtractHostname() throws Exception {
        // null test
        final AuthenticationToken nullAuthenticationToken = null;
        assertEquals(AuthenticationTokenUtils.DEFAULT_HOSTNAME,
                AuthenticationTokenUtils.extractHostname(nullAuthenticationToken));

        // non-UsernamePasswordToken test
        final AuthenticationToken notUsernamePasswordToken = new NotUsernamePasswordToken();
        assertEquals(AuthenticationTokenUtils.DEFAULT_HOSTNAME,
                AuthenticationTokenUtils.extractHostname(notUsernamePasswordToken));

        // null hostname test
        final UsernamePasswordToken nullHostname = new UsernamePasswordToken();
        nullHostname.setHost(null);
        assertEquals(AuthenticationTokenUtils.DEFAULT_HOSTNAME,
                AuthenticationTokenUtils.extractHostname(nullHostname));

        // positive test
        final UsernamePasswordToken positiveUsernamePasswordToken = new UsernamePasswordToken();
        final String testUsername = "testHostname1";
        positiveUsernamePasswordToken.setHost(testUsername);
        assertEquals(testUsername, AuthenticationTokenUtils.extractHostname(positiveUsernamePasswordToken));
    }

    @Test
    public void testGenerateUnsuccessfulAuthenticationMessage() throws Exception {
        final UsernamePasswordToken unsuccessfulToken = new UsernamePasswordToken();
        unsuccessfulToken.setUsername("unsuccessfulUser1");
        unsuccessfulToken.setHost("unsuccessfulHost1");
        assertEquals("Unsuccessful authentication attempt by unsuccessfulUser1 from unsuccessfulHost1",
                AuthenticationTokenUtils.generateUnsuccessfulAuthenticationMessage(unsuccessfulToken));
    }

    @Test
    public void testGenerateSuccessfulAuthenticationMessage() throws Exception {
        final UsernamePasswordToken successfulToken = new UsernamePasswordToken();
        successfulToken.setUsername("successfulUser1");
        successfulToken.setHost("successfulHost1");
        assertEquals("Successful authentication attempt by successfulUser1 from successfulHost1",
                AuthenticationTokenUtils.generateSuccessfulAuthenticationMessage(successfulToken));
    }
}
