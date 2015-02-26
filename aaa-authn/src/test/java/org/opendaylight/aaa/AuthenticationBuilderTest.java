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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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
    public void testBuildWithAll() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").addRole("foo2")
            .addRoles(new HashSet<>(Arrays.asList("foo", "bar"))).build();
        assertEquals(1, a1.expiration());
        assertEquals("dlux", a1.clientId());
        assertEquals("pepsi", a1.domain());
        assertEquals("john", a1.user());
        assertEquals("1234", a1.userId());
        assertTrue(a1.roles().contains("foo"));
        assertTrue(a1.roles().contains("foo2"));
        assertTrue(a1.roles().contains("bar"));
        assertEquals(3, a1.roles().size());
        Authentication a2 = new AuthenticationBuilder(a1).build();
        assertNotEquals(a1, a2);
        Authentication a3 = new AuthenticationBuilder(a1).setExpiration(1).build();
        assertEquals(a1, a3);
    }

    @Test
    public void testBuildWithRequired() {
        Authentication a1 = new AuthenticationBuilder().setUser("john").setUserId("1234").addRole("foo").build();
        assertEquals(0, a1.expiration());
        assertEquals("john", a1.user());
        assertEquals("1234", a1.userId());
        assertTrue(a1.roles().contains("foo"));
        assertEquals(1, a1.roles().size());
        assertEquals("", a1.domain());
        assertEquals("", a1.clientId());
    }

    @Test
    public void testBuildWithEmptyOptional() {
        Authentication a1 = new AuthenticationBuilder().setDomain("  ").setClientId("  ").setUser("john")
            .setUserId("1234").addRole("foo").build();
        assertEquals("", a1.domain());
        assertEquals("", a1.clientId());
        assertEquals(0, a1.expiration());
        assertEquals("john", a1.user());
        assertEquals("1234", a1.userId());
        assertTrue(a1.roles().contains("foo"));
        assertEquals(1, a1.roles().size());
    }

    @Test
    public void testBuildWithNullOptional() {
        Authentication a1 = new AuthenticationBuilder().setDomain(null).setClientId(null).setUser("john")
            .setUserId("1234").addRole("foo").build();
        assertEquals("", a1.domain());
        assertEquals("", a1.clientId());
        assertEquals(0, a1.expiration());
        assertEquals("john", a1.user());
        assertEquals("1234", a1.userId());
        assertTrue(a1.roles().contains("foo"));
        assertEquals(1, a1.roles().size());
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithDefault() {
        AuthenticationBuilder a1 = new AuthenticationBuilder();
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithNegativeExpiration() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setExpiration(-1).setUser("john").setUserId("1234")
            .addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithoutUser() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUserId("1234").addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithNullUser() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser(null).setUserId("1234").addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithEmptyUser() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("  ").setUserId("1234").addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithoutUserId() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("john").addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithNullUserId() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("john").setUserId(null).addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithEmptyUserId() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("john").setUserId("  ").addRole("foo");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithoutRole() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("john").setUserId("1234");
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithNullRole() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("john").setUserId("1234").addRole(null);
        a1.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testBuildWithEmptyRole() {
        AuthenticationBuilder a1 = new AuthenticationBuilder().setUser("john").setUserId("1234").addRole("  ");
        a1.build();
    }

    @Test
    public void testToString() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1).setUser("john").setUserId("1234").addRole
            ("foo").build();
        assertEquals("expiration:1,clientId:,userId:1234,userName:john,domain:,roles:[foo]", a1.toString());
    }

    @Test
    public void testEquals() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").build();
        assertTrue(a1.equals(a1));
        Authentication a2 = new AuthenticationBuilder(a1).setExpiration(1).addRole("foo").build();
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        Authentication a3 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").build();
        assertTrue(a1.equals(a3));
        assertTrue(a3.equals(a2));
        assertTrue(a1.equals(a2));
    }

    @Test
    public void testNotEquals() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").build();
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("wrong object"));
        Authentication a2 = new AuthenticationBuilder(a1).addRoles(new HashSet<>(Arrays.asList("foo", "bar"))).build();
        assertEquals(2, a2.roles().size());
        assertFalse(a1.equals(a2));
        assertFalse(a2.equals(a1));
        Authentication a3 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").build();
        assertFalse(a1.equals(a2));
        assertTrue(a1.equals(a3));
        assertFalse(a2.equals(a3));
        Authentication a4 = new AuthenticationBuilder().setExpiration(1).setUser("john").setUserId("1234")
            .addRole("foo").build();
        assertFalse(a1.equals(a4));
        assertFalse(a4.equals(a1));
        Authentication a5 = new AuthenticationBuilder(a1).setExpiration(9).build();
        assertFalse(a1.equals(a5));
        assertFalse(a5.equals(a1));
    }

    @Test
    public void testHashCode() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").build();
        assertEquals(a1.hashCode(), a1.hashCode());
        Authentication a2 = new AuthenticationBuilder(a1).setExpiration(1).addRole("foo").build();
        assertTrue(a1.equals(a2));
        assertEquals(a1.hashCode(), a2.hashCode());
        Authentication a3 = new AuthenticationBuilder(a1).addRoles(new HashSet<>(Arrays.asList("foo", "bar"))).build();
        assertFalse(a1.equals(a3));
        assertNotEquals(a1.hashCode(), a3.hashCode());
        Authentication a4 = new AuthenticationBuilder().setExpiration(1).setClientId("dlux").setDomain("pepsi")
            .setUser("john").setUserId("1234").addRole("foo").build();
        assertTrue(a1.equals(a4));
        assertEquals(a1.hashCode(), a4.hashCode());
        assertEquals(a2.hashCode(), a4.hashCode());
        Authentication a5 = new AuthenticationBuilder().setExpiration(1).setUser("john").setUserId("1234")
            .addRole("foo").build();
        assertFalse(a1.equals(a5));
        assertNotEquals(a1.hashCode(), a5.hashCode());
        Authentication a6 = new AuthenticationBuilder(a1).setExpiration(9).build();
        assertFalse(a1.equals(a6));
        assertNotEquals(a1.hashCode(), a6.hashCode());
        Authentication a7 = new AuthenticationBuilder(a1).build();
        assertFalse(a1.equals(a7));
        assertNotEquals(a1.hashCode(), a7.hashCode());
    }
}
