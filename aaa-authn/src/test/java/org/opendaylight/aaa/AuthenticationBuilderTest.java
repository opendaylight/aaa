/*
 * Copyright (c) 2015. Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.Claim;

public class AuthenticationBuilderTest {

    @Test
    public void testBuild() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        Authentication a1 = new AuthenticationBuilder().setExpiration(1)
            .setDomain("aName").setUserId("1").setClientId("aClient")
            .setUser("bob").addRole("foo").addRoles(roles).build();
        assertEquals(1, a1.expiration());
        assertEquals("aName", a1.domain());
        assertEquals("1", a1.userId());
        assertEquals("aClient", a1.clientId());
        assertEquals("bob", a1.user());
        assertTrue(a1.roles().contains("foo"));
        assertTrue(a1.roles().containsAll(roles));
        assertEquals(3, a1.roles().size());
    }

    @Test
    public void testBuildWithClaim() {
        Set<String> roles = new HashSet<>();
        roles.add("role1");
        roles.add("role2");
        Claim claim = new ClaimBuilder().setDomain("aName").setUserId("1")
            .setUser("bob").setClientId("aClient").addRole("foo")
            .addRoles(roles).build();
        Authentication a1 = new AuthenticationBuilder(claim).build();
        assertEquals(0, a1.expiration());
        assertEquals("aName", a1.domain());
        assertEquals("1", a1.userId());
        assertEquals("aClient", a1.clientId());
        assertEquals("bob", a1.user());
        assertTrue(a1.roles().contains("foo"));
        assertTrue(a1.roles().containsAll(roles));
        assertEquals(3, a1.roles().size());
    }

    @Test
    public void testToString() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1).build();
        assertEquals("expiration:1,roles:[]", a1.toString());
    }

        @Test
    public void testEquals() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").build();
        assertFalse(a1.equals(null));
        assertTrue(a1.equals(a1));
        Authentication a2 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("bar").build();
        assertFalse(a1.equals(a2));
        a2 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").build();
        assertTrue(a1.equals(a2));
        a2 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").addRole("bar").build();
        assertFalse(a1.equals(a2));
    }

    @Test
    public void testHashCode() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").build();
        Authentication a2 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("bar").build();
        assertFalse(a1.hashCode() == a2.hashCode());
        a2 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").build();
        assertTrue(a1.hashCode() == a2.hashCode());
        a2 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").addRole("bar").build();
        assertFalse(a1.hashCode() == a2.hashCode());
    }
}
