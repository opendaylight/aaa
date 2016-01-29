/*
 * Copyright (c) 2015, 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
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
import org.apache.shiro.util.Nameable;
import org.opendaylight.aaa.shiro.accounting.Accounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extended implementation of
 * <code>org.apache.shiro.realm.ldap.JndiLdapRealm</code> which includes
 * additional Authorization capabilities.  To enable this Realm, add the
 * following to <code>shiro.ini</code>:
 *
 *<code>#ldapRealm = org.opendaylight.aaa.shiro.realm.ODLJndiLdapRealmAuthNOnly
 *#ldapRealm.userDnTemplate = uid={0},ou=People,dc=DOMAIN,dc=TLD
 *#ldapRealm.contextFactory.url = ldap://<URL>:389
 *#ldapRealm.searchBase = dc=DOMAIN,dc=TLD
 *#ldapRealm.ldapAttributeForComparison = objectClass
 *# The CSV list of enabled realms.  In order to enable a realm, add it to the
 *# list below:
 * securityManager.realms = $tokenAuthRealm, $ldapRealm</code>
 *
 * The values above are specific to the deployed LDAP domain.  If the defaults
 * are not sufficient, alternatives can be derived through enabling
 * <code>TRACE</code> level logging.  To enable <code>TRACE</code> level
 * logging, issue the following command in the karaf shell:
 * <code>karaf> log:set TRACE org.opendaylight.aaa.shiro.realm.ODLJndiLdapRealm</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 * @see <code>org.apache.shiro.realm.ldap.JndiLdapRealm</code>
 * @see <a
 *      href="https://shiro.apache.org/static/1.2.3/apidocs/org/apache/shiro/realm/ldap/JndiLdapRealm.html">Shiro
 *      documentation</a>
 */
public class ODLJndiLdapRealm extends JndiLdapRealm implements Nameable {

    private static final Logger LOG = LoggerFactory.getLogger(ODLJndiLdapRealm.class);

    /**
     * When an LDAP Authorization lookup is made for a user account, a list of
     * attributes are returned.  The attributes are used to determine LDAP
     * grouping, which is equivalent to ODL role(s).  The default value is
     * set to "objectClass", which is common attribute for LDAP systems.
     * The actual value may be configured through setting
     * <code>ldapAttributeForComparison</code>.
     */
    private static final String DEFAULT_LDAP_ATTRIBUTE_FOR_COMPARISON = "objectClass";

    /**
     * The LDAP nomenclature for user ID, which is used in the authorization query process.
     */
    private static final String UID = "uid";

    /**
     * The searchBase for the ldap query, which indicates the LDAP realms to
     * search.  By default, this is set to the
     * <code>super.getUserDnSuffix()</code>.
     */
    private String searchBase = super.getUserDnSuffix();

    /**
     * When an LDAP Authorization lookup is made for a user account, a list of
     * attributes is returned.  The attributes are used to determine LDAP
     * grouping, which is equivalent to ODL role(s).  The default is set to
     * <code>DEFAULT_LDAP_ATTRIBUTE_FOR_COMPARISON</code>.
     */
    private String ldapAttributeForComparison = DEFAULT_LDAP_ATTRIBUTE_FOR_COMPARISON;

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

        // Delegates all AuthN lookup responsibility to the super class
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
        LOG.info("AAA LDAP connection from {}", username);
        Accounter.output("AAA LDAP connection from " + username);
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
            LOG.error("Unable to query for AuthZ info", e);
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

    /*
     * (non-Javadoc)
     *
     * This method is only called if doGetAuthenticationInfo(...) completes successfully AND
     * the requested endpoint has an RBAC restriction.  To add an RBAC restriction, edit the
     * etc/shiro.ini file and add a url to the url section.  E.g.,
     *
     * <code>/** = authcBasic, roles[person]</code>
     *
     * @see org.apache.shiro.realm.ldap.JndiLdapRealm#queryForAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection, org.apache.shiro.realm.ldap.LdapContextFactory)
     */
    @Override
    protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
            LdapContextFactory ldapContextFactory) throws NamingException {

        AuthorizationInfo authorizationInfo = null;
        try {
            final String username = getUsername(principals);
            final LdapContext ldapContext = ldapContextFactory.getSystemLdapContext();
            final Set<String> roleNames;

            try {
                roleNames = getRoleNamesForUser(username, ldapContext);
                authorizationInfo = buildAuthorizationInfo(roleNames);
            } finally {
                LdapUtils.closeContext(ldapContext);
            }
        } catch (ClassCastException e) {
            LOG.error("Unable to extract a valid user", e);
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
    protected Set<String> getRoleNamesForUser(final String username, final LdapContext ldapContext)
            throws NamingException {

        // Stores the role names, which are equivalent to the set of group names extracted
        // from the LDAP query.
        final Set<String> roleNames = new LinkedHashSet<String>();

        final SearchControls searchControls = createSearchControls();

        LOG.debug("Asking the configured LDAP about which groups uid=\"{}\" belongs to using "
                + "searchBase=\"{}\" ldapAttributeForComparison=\"{}\"",
                username, searchBase, ldapAttributeForComparison);
        final NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase,
                String.format("%s=%s", UID, username), searchControls);

        // Cursor based traversal over the LDAP query result
        while (answer.hasMoreElements()) {
            final SearchResult searchResult = answer.next();
            final Attributes attrs = searchResult.getAttributes();
            if (attrs != null) {
                // Extract the attributes from the LDAP search.
                // attrs.getAttr(String) was not chosen, since all attributes should be exposed
                // in trace logging should the operator wish to use an alternate attribute.
                final NamingEnumeration<? extends Attribute> ae = attrs.getAll();
                while (ae.hasMore()) {
                    final Attribute attr = ae.next();
                    LOG.trace("LDAP returned \"{}\" attribute for \"{}\"", attr.getID(), username);
                    if (attr.getID().equals(ldapAttributeForComparison)) {
                        // Stresses the point that LDAP groups are EQUIVALENT to ODL role names
                        // TODO make this configurable via a Strategy pattern so more interesting mappings can be made
                        final Collection<String> groupNamesExtractedFromLdap = LdapUtils.getAllAttributeValues(attr);
                        final Collection<String> roleNamesFromLdapGroups = groupNamesExtractedFromLdap;
                        if (LOG.isTraceEnabled()) {
                            for (String roleName : roleNamesFromLdapGroups) {
                                LOG.trace("Mapped the \"{}\" LDAP group to ODL role for \"{}\"", roleName, username);
                            }
                        }
                        roleNames.addAll(roleNamesFromLdapGroups);
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

    /**
     * Injected from <code>shiro.ini</code> configuration.
     *
     * @param searchBase The desired value for searchBase
     */
    public void setSearchBase(final String searchBase) {
        // public for injection reasons
        this.searchBase = searchBase;
    }

    /**
     * Injected from <code>shiro.ini</code> configuration.
     *
     * @param ldapAttributeForComparison The attribute from which groups are extracted
     */
    public void setLdapAttributeForComparison(final String ldapAttributeForComparison) {
        // public for injection reasons
        this.ldapAttributeForComparison = ldapAttributeForComparison;
    }
}
