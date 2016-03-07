/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import static org.junit.Assert.*;

import ch.qos.logback.classic.spi.LoggingEvent;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import org.opendaylight.aaa.shiro.TestAppender;
import org.opendaylight.aaa.shiro.filters.AuthenticationListener;

/**
 * Test AuthenticationListener, which is responsible for logging Accounting events.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class AuthenticationListenerTest {

    @Test
    public void testOnSuccess() throws Exception {
        // sets up a successful authentication attempt
        final AuthenticationListener authenticationListener = new AuthenticationListener();
        final UsernamePasswordToken authenticationToken = new UsernamePasswordToken();
        authenticationToken.setUsername("successfulUser1");
        authenticationToken.setHost("successfulHost1");
        final SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo();
        // the following call produces accounting output
        authenticationListener.onSuccess(authenticationToken, simpleAuthenticationInfo);

        // grab the latest log output and make sure it is in line with what is expected
        final List<LoggingEvent> loggingEvents = TestAppender.getCurrentInstance().getEvents();
        // the latest logging event is the one we need to inspect
        final int whichLoggingEvent = loggingEvents.size() - 1;
        final LoggingEvent latestLoggingEvent = loggingEvents.get(whichLoggingEvent);
        final String latestLogMessage = latestLoggingEvent.getMessage();
        assertEquals("Successful authentication attempt by successfulUser1 from successfulHost1",
                latestLogMessage);
    }

    @Test
    public void testOnFailure() throws Exception {
        // variables for an unsucessful authentication attempt
        final AuthenticationListener authenticationListener = new AuthenticationListener();
        final UsernamePasswordToken authenticationToken = new UsernamePasswordToken();
        authenticationToken.setUsername("unsuccessfulUser1");
        authenticationToken.setHost("unsuccessfulHost1");
        final AuthenticationException authenticationException =
                new AuthenticationException("test auth exception");
        // produces unsuccessful authentication attempt output
        authenticationListener.onFailure(authenticationToken, authenticationException);

        // grab the latest log output and ensure it is in line with what is expected
        final List<LoggingEvent> loggingEvents = TestAppender.getCurrentInstance().getEvents();
        final int whichLoggingEvent = loggingEvents.size() - 1;
        final LoggingEvent latestLoggingEvent = loggingEvents.get(whichLoggingEvent);
        final String latestLogMessage = latestLoggingEvent.getMessage();
        assertEquals("Unsuccessful authentication attempt by unsuccessfulUser1 from unsuccessfulHost1",
                latestLogMessage);
    }
}