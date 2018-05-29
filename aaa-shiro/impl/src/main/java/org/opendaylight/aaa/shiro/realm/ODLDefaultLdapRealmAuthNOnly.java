/*
 * Copyright (c) 2016 - 2018 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.ldap.DefaultLdapRealm;
import org.opendaylight.aaa.shiro.accounting.Accounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for <code>org.apache.shiro.realm.ldap.DefaultLdapRealm</code>.
 * This implementation disables Authorization so any LDAP user is able to access
 * server resources. This is particularly useful for quickly prototyping ODL
 * without worrying about resolving LDAP attributes (groups) to OpenDaylight
 * roles.
 */
public class ODLDefaultLdapRealmAuthNOnly extends DefaultLdapRealm {

    private static final Logger LOG = LoggerFactory.getLogger(ODLDefaultLdapRealmAuthNOnly.class);

    private static final String LDAP_CONNECTION_MESSAGE = "AAA LDAP connection from ";

    /*
     * Adds debugging information surrounding creation of ODLDefaultLdapRealm
     */
    public ODLDefaultLdapRealmAuthNOnly() {
        LOG.info("Creating {}", ODLDefaultLdapRealmAuthNOnly.class.getName());
    }

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
     * Logs an incoming LDAP connection.
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
     * Extracts the username from <code>token</code>.
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
