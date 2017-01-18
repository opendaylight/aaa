/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.opendaylight.aaa.shiro.accounting.Accounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for <code>org.apache.shiro.realm.ldap.JndiLdapRealm</code>.
 * This implementation disables Authorization so any LDAP user is able to access
 * server resources. This is particularly useful for quickly prototyping ODL
 * without worrying about resolving LDAP attributes (groups) to OpenDaylight
 * roles.
 *
 * The motivation for subclassing Shiro's implementation is two-fold: 1) Enhance
 * the default logging of Shiro. This allows us to more easily log incoming
 * connections, providing some security auditing. 2) Provide a common package in
 * the classpath for ODL supported Realm implementations (i.e.,
 * <code>org.opendaylight.aaa.shiro.realm</code>), which consolidates the number
 * of <code>Import-Package</code> statements consumers need to enumerate. For
 * example, the netconf project only needs to import
 * <code>org.opendaylight.aaa.shiro.realm</code>, and does not need to worry
 * about importing Shiro packages.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class ODLJndiLdapRealmAuthNOnly extends JndiLdapRealm {

    private static final Logger LOG = LoggerFactory.getLogger(ODLJndiLdapRealmAuthNOnly.class);

    private static final String LDAP_CONNECTION_MESSAGE = "AAA LDAP connection from ";

    /*
     * Adds debugging information surrounding creation of ODLJndiLdapRealm
     */
    public ODLJndiLdapRealmAuthNOnly() {
        LOG.debug("Creating ODLJndiLdapRealmAuthNOnly");
    }

    /*
     * (non-Javadoc) Overridden to expose important audit trail information for
     * accounting.
     *
     * @see
     * org.apache.shiro.realm.ldap.JndiLdapRealm#doGetAuthenticationInfo(org
     * .apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {

        try {
            final String username = getUsername(token);
            logIncomingConnection(username);
            return super.doGetAuthenticationInfo(token);
        } catch (ClassCastException e) {
            LOG.info("Couldn't service the LDAP connection", e);
        }
        return null;
    }

    /**
     * Logs an incoming LDAP connection
     *
     * @param username
     *            the requesting user
     */
    protected void logIncomingConnection(final String username) {
        final String message = LDAP_CONNECTION_MESSAGE + username;
        LOG.info(message);
        Accounter.output(message);
    }

    /**
     * Extracts the username from <code>token</code>
     *
     * @param token Which possibly contains a username
     * @return the username if it can be extracted
     * @throws ClassCastException
     *             The incoming token is not username/password (i.e., X.509
     *             certificate)
     */
    public static String getUsername(AuthenticationToken token) throws ClassCastException {
        if (null == token) {
            return null;
        }
        return (String) token.getPrincipal();
    }
}
