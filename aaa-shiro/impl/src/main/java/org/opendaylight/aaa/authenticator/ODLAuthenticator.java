/*
 * Copyright © 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authenticator;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.jolokia.osgi.security.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * AAA hook for <code>odl-jolokia</code> configured w/ <code>org.jolokia.authMode=service-all</code>.
 */
public class ODLAuthenticator implements Authenticator {
    private static final Logger LOG = LoggerFactory.getLogger(ODLAuthenticator.class);

    @Override
    public boolean authenticate(HttpServletRequest httpServletRequest) {
        LOG.trace("Incoming Jolokia Authentication Attempt");

        try {
            final String authorization = httpServletRequest.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Basic")) {
                final String base64Creds = authorization.substring("Basic".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(base64Creds),
                        Charset.forName("UTF-8"));
                final String[] values = credentials.split(":", 2);
                final Subject subject = SecurityUtils.getSubject();
                UsernamePasswordToken upt = new UsernamePasswordToken();
                upt.setUsername(values[0]);
                upt.setPassword(values[1].toCharArray());
                subject.login(upt);
                return true;
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            LOG.trace("Jolokia Authentication attempt unsuccessful; formatting issue basic auth credentials");
        } catch(final AuthenticationException e) {
            LOG.trace("Jolokia Authentication attempt unsuccessful; Couldn't authenticate the subject");
        }
        return false;
    }
}
