/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.realm.ldap.LdapUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.shiro.accounting.Accounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extended implementation of
 * <code>org.apache.shiro.realm.ldap.JndiLdapRealm</code> which includes
 * additional Authorization capabilities.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <code>org.apache.shiro.realm.ldap.JndiLdapRealm</code>
 * @see <a
 *      href="https://shiro.apache.org/static/1.2.3/apidocs/org/apache/shiro/realm/ldap/JndiLdapRealm.html">Shiro
 *      documentation</a>
 */
public class ODLJndiLdapRealm extends JndiLdapRealm {
    private static final Logger LOG = LoggerFactory.getLogger(ODLJndiLdapRealm.class);

    private static final String LDAP_CONNECTION_MESSAGE = "AAA LDAP connection from ";

    /*
     * Adds debugging information surrounding creation of ODLJndiLdapRealm
     */
    public ODLJndiLdapRealm() {
        super();
        final String DEBUG_MESSAGE = "Creating ODLJndiLdapRealm";
        LOG.debug(DEBUG_MESSAGE);
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
            final String ERROR_MESSAGE = "Couldn't service the LDAP connection";
            LOG.info(ERROR_MESSAGE, e);
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
     * @param token
     * @return
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

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        AuthorizationInfo ai = null;
        try {
            ai = this.queryForAuthorizationInfo(principals, getContextFactory());
        } catch (NamingException e) {
            LOG.error("Unable to query for AuthZ info: ", e);
        }
        return ai;
    }

    /**
     * extracts a username from <code>principals</code>
     *
     * @param principals
     * @return
     * @throws ClassCastException
     *             the PrincipalCollection contains an element that is not in
     *             username/password form (i.e., X.509 certificate)
     */
    protected String getUsername(final PrincipalCollection principals) throws ClassCastException {

        if (null == principals) {
            return null;
        }
        return (String) getAvailablePrincipal(principals);
    }

    @Override
    protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
            LdapContextFactory ldapContextFactory) throws NamingException {

        AuthorizationInfo authorizationInfo = null;
        try {
            final String username = getUsername(principals);
            LdapContext ldapContext = ldapContextFactory.getSystemLdapContext();
            Set<String> roleNames;

            try {
                roleNames = getRoleNamesForUser(username, ldapContext);
                authorizationInfo = buildAuthorizationInfo(roleNames);
            } finally {
                LdapUtils.closeContext(ldapContext);
            }
        } catch (ClassCastException e) {
            final String ERROR_MESSAGE = "Unable to extract a valid user";
            LOG.error(ERROR_MESSAGE, e);
        }
        return authorizationInfo;
    }

    public static AuthorizationInfo buildAuthorizationInfo(final Set<String> roleNames) {
        if (null == roleNames) {
            return null;
        }
        return new SimpleAuthorizationInfo(roleNames);
    }

    /**
     * extracts the Set of roles associated with a user based on the username
     * and ldap context (server).
     *
     * @param username
     * @param ldapContext
     * @return
     * @throws NamingException
     */
    protected Set<String> getRoleNamesForUser(String username, LdapContext ldapContext)
            throws NamingException {

        Set<String> roleNames = new LinkedHashSet<String>();
        SearchControls searchControls = createSearchControls();
        // set CN=username
        String SEARCH_FILTER = "(&(objectClass=*)(CN={0}))";
        Object[] SEARCH_ARGS = new Object[] { username };
        String searchBase = super.getUserDnSuffix();
        final String DEBUG_MESSAGE = searchBase = " + searchBase";
        LOG.debug(DEBUG_MESSAGE);
        NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase, SEARCH_FILTER,
                SEARCH_ARGS, searchControls);

        String MEMBER_OF = "memberOf";
        while (answer.hasMoreElements()) {
            SearchResult searchResult = answer.next();
            Attributes attrs = searchResult.getAttributes();
            if (attrs != null) {
                NamingEnumeration<? extends Attribute> ae = attrs.getAll();
                while (ae.hasMore()) {
                    Attribute attr = ae.next();
                    if (attr.getID().equals(MEMBER_OF)) {
                        Collection<String> groupNames = LdapUtils.getAllAttributeValues(attr);
                        Collection<String> rolesForGroups = groupNames;
                        roleNames.addAll(rolesForGroups);
                    }
                }
            }
        }
        return roleNames;
    }

    /**
     * A utility method to help create the search controls for the LDAP lookup
     *
     * @return
     */
    protected static SearchControls createSearchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return searchControls;
    }

    @Override
    public String getUserDnSuffix() {
        return super.getUserDnSuffix();
    }
}
