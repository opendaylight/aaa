/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Test;

/**
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class ODLJndiLdapRealmTest {

    /**
     * throw-away anonymous test class
     */
    class TestNamingEnumeration implements NamingEnumeration<SearchResult> {

        /**
         * state variable
         */
        boolean first = true;

        /**
         * returned the first time <code>next()</code> or
         * <code>nextElement()</code> is called.
         */
        SearchResult searchResult = new SearchResult("testuser", null, new BasicAttributes(
                "objectClass", "engineering"));

        /**
         * returns true the first time, then false for subsequent calls
         */
        @Override
        public boolean hasMoreElements() {
            return first;
        }

        /**
         * returns <code>searchResult</code> then null for subsequent calls
         */
        @Override
        public SearchResult nextElement() {
            if (first) {
                first = false;
                return searchResult;
            }
            return null;
        }

        /**
         * does nothing because close() doesn't require any special behavior
         */
        @Override
        public void close() throws NamingException {
        }

        /**
         * returns true the first time, then false for subsequent calls
         */
        @Override
        public boolean hasMore() throws NamingException {
            return first;
        }

        /**
         * returns <code>searchResult</code> then null for subsequent calls
         */
        @Override
        public SearchResult next() throws NamingException {
            if (first) {
                first = false;
                return searchResult;
            }
            return null;
        }
    };

    /**
     * throw away test class
     *
     * @author ryan
     */
    class TestPrincipalCollection implements PrincipalCollection {
        /**
     *
     */
        private static final long serialVersionUID = -1236759619455574475L;

        Vector<String> collection = new Vector<String>();

        public TestPrincipalCollection(String element) {
            collection.add(element);
        }

        @Override
        public Iterator<String> iterator() {
            return collection.iterator();
        }

        @Override
        public List<String> asList() {
            return collection;
        }

        @Override
        public Set<String> asSet() {
            HashSet<String> set = new HashSet<String>();
            set.addAll(collection);
            return set;
        }

        @Override
        public <T> Collection<T> byType(Class<T> arg0) {
            return null;
        }

        @Override
        public Collection<String> fromRealm(String arg0) {
            return collection;
        }

        @Override
        public Object getPrimaryPrincipal() {
            return collection.firstElement();
        }

        @Override
        public Set<String> getRealmNames() {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return collection.isEmpty();
        }

        @Override
        public <T> T oneByType(Class<T> arg0) {
            // TODO Auto-generated method stub
            return null;
        }
    };

    @Test
    public void testGetUsernameAuthenticationToken() {
        AuthenticationToken authenticationToken = null;
        assertNull(ODLJndiLdapRealm.getUsername(authenticationToken));
        AuthenticationToken validAuthenticationToken = new UsernamePasswordToken("test",
                "testpassword");
        assertEquals("test", ODLJndiLdapRealm.getUsername(validAuthenticationToken));
    }

    @Test
    public void testGetUsernamePrincipalCollection() {
        PrincipalCollection pc = null;
        assertNull(new ODLJndiLdapRealm().getUsername(pc));
        TestPrincipalCollection tpc = new TestPrincipalCollection("testuser");
        String username = new ODLJndiLdapRealm().getUsername(tpc);
        assertEquals("testuser", username);
    }

    @Test
    public void testQueryForAuthorizationInfoPrincipalCollectionLdapContextFactory()
            throws NamingException {
        LdapContext ldapContext = mock(LdapContext.class);
        // emulates an ldap search and returns the mocked up test class
        when(
                ldapContext.search((String) any(), (String) any(),
                        (SearchControls) any())).thenReturn(new TestNamingEnumeration());
        LdapContextFactory ldapContextFactory = mock(LdapContextFactory.class);
        when(ldapContextFactory.getSystemLdapContext()).thenReturn(ldapContext);
        AuthorizationInfo authorizationInfo = new ODLJndiLdapRealm().queryForAuthorizationInfo(
                new TestPrincipalCollection("testuser"), ldapContextFactory);
        assertNotNull(authorizationInfo);
        assertFalse(authorizationInfo.getRoles().isEmpty());
        assertTrue(authorizationInfo.getRoles().contains("engineering"));
    }

    @Test
    public void testBuildAuthorizationInfo() {
        assertNull(ODLJndiLdapRealm.buildAuthorizationInfo(null));
        Set<String> roleNames = new HashSet<String>();
        roleNames.add("engineering");
        AuthorizationInfo authorizationInfo = ODLJndiLdapRealm.buildAuthorizationInfo(roleNames);
        assertNotNull(authorizationInfo);
        assertFalse(authorizationInfo.getRoles().isEmpty());
        assertTrue(authorizationInfo.getRoles().contains("engineering"));
    }

    @Test
    public void testGetRoleNamesForUser() throws NamingException {
        ODLJndiLdapRealm ldapRealm = new ODLJndiLdapRealm();
        LdapContext ldapContext = mock(LdapContext.class);

        // emulates an ldap search and returns the mocked up test class
        when(
                ldapContext.search((String) any(), (String) any(),
                        (SearchControls) any())).thenReturn(new TestNamingEnumeration());

        // extracts the roles for "testuser" and ensures engineering is returned
        Set<String> roles = ldapRealm.getRoleNamesForUser("testuser", ldapContext);
        assertFalse(roles.isEmpty());
        assertTrue(roles.iterator().next().equals("engineering"));
    }

    @Test
    public void testCreateSearchControls() {
        SearchControls searchControls = ODLJndiLdapRealm.createSearchControls();
        assertNotNull(searchControls);
        int expectedSearchScope = SearchControls.SUBTREE_SCOPE;
        int actualSearchScope = searchControls.getSearchScope();
        assertEquals(expectedSearchScope, actualSearchScope);
    }

}
