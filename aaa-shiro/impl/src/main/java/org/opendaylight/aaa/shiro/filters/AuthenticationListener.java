/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Follows the event-listener pattern;  the <code>Authenticator</code> notifies this class about
 * authentication attempts.  <code>AuthenticationListener</code> logs successful and unsuccessful
 * authentication attempts appropriately.  Log messages are emitted at the <code>DEBUG</code> log
 * level.  To enable the messages out of the box, use the following command from karaf:
 * <code>log:set DEBUG org.opendaylight.aaa.shiro.authc.AuthenicationListener</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class AuthenticationListener implements org.apache.shiro.authc.AuthenticationListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationListener.class);

    @Override
    public void onSuccess(final AuthenticationToken authenticationToken, final AuthenticationInfo authenticationInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(AuthenticationTokenUtils.generateSuccessfulAuthenticationMessage(authenticationToken));
        }
    }

    @Override
    public void onFailure(final AuthenticationToken authenticationToken, final AuthenticationException e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(AuthenticationTokenUtils.generateUnsuccessfulAuthenticationMessage(authenticationToken));
        }
    }

    @Override
    public void onLogout(final PrincipalCollection principalCollection) {
        // Do nothing;  AAA is aimed at RESTCONF, which stateless by definition.
        // Including this output would very quickly pollute the log.
    }
}
