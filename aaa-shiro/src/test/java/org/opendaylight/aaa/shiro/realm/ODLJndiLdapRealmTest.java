/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.junit.Test;

/**
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class ODLJndiLdapRealmTest {

  @Test
  public void testDoGetAuthenticationInfoAuthenticationToken() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetUsernameAuthenticationToken() {
    fail("Not yet implemented");
  }

  @Test
  public void testDoGetAuthorizationInfoPrincipalCollection() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetUsernamePrincipalCollection() {
    fail("Not yet implemented");
  }

  @Test
  public void testQueryForAuthorizationInfoPrincipalCollectionLdapContextFactory() {
    fail("Not yet implemented");
  }

  @Test
  public void testBuildAuthorizationInfo() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetRoleNamesForUser() throws NamingException {
    ODLJndiLdapRealm ldapRealm = new ODLJndiLdapRealm();
    
    LdapContext ldapContext = mock(LdapContext.class);
    NamingEnumeration<SearchResult> value = new NamingEnumeration<SearchResult>() {
      boolean first = true;
      SearchResult searchResult = new SearchResult("testuser", 
          null, new BasicAttributes("memberOf", "engineering"));
      @Override
      public boolean hasMoreElements() {
        return first;
      }

      @Override
      public SearchResult nextElement() {
        if(first) {
          first = false;
          return searchResult;
        }
        return null;
      }

      @Override
      public void close() throws NamingException {        
      }

      @Override
      public boolean hasMore() throws NamingException {
        return first;
      }

      @Override
      public SearchResult next() throws NamingException {
        if(first) {
          first = false;
          return searchResult;
        }
        return null;
      }
      
    };
    when(ldapContext.search((String) any(),
        (String) any(), (Object[]) any(),
        (SearchControls) any()))
        .thenReturn(value);
    Set<String> roles =
        ldapRealm.getRoleNamesForUser("testuser", ldapContext);
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
