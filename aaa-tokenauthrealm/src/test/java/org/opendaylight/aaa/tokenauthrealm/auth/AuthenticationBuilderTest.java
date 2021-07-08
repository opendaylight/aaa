/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.tokenauthrealm.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.Claim;

public class AuthenticationBuilderTest {
    private Set<String> roles = new LinkedHashSet<>(Arrays.asList("role1", "role2"));
    private Claim validClaim = new ClaimBuilder().setDomain("aName").setUserId("1")
            .setClientId("2222").setUser("bob").addRole("foo").addRoles(roles).build();

    @Test
    public void testBuildWithExpiration() {
        Authentication a1 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertEquals(1, a1.expiration());
        assertEquals("aName", a1.domain());
        assertEquals("1", a1.userId());
        assertEquals("2222", a1.clientId());
        assertEquals("bob", a1.user());
        assertTrue(a1.roles().contains("foo"));
        assertTrue(a1.roles().containsAll(roles));
        assertEquals(3, a1.roles().size());
        Authentication a2 = new AuthenticationBuilder(a1).build();
        assertNotEquals(a1, a2);
        Authentication a3 = new AuthenticationBuilder(a1).setExpiration(1).build();
        assertEquals(a1, a3);
    }

    @Test
    public void testBuildWithoutExpiration() {
        Authentication a1 = new AuthenticationBuilder(validClaim).build();
        assertEquals(0, a1.expiration());
        assertEquals("aName", a1.domain());
        assertEquals("1", a1.userId());
        assertEquals("2222", a1.clientId());
        assertEquals("bob", a1.user());
        assertTrue(a1.roles().contains("foo"));
        assertTrue(a1.roles().containsAll(roles));
        assertEquals(3, a1.roles().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithNegativeExpiration() {
        AuthenticationBuilder a1 = new AuthenticationBuilder(validClaim).setExpiration(-1);
        a1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithNullClaim() {
        AuthenticationBuilder a1 = new AuthenticationBuilder(null);
        a1.build();
    }

    @Test
    public void testToString() {
        Authentication a1 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertEquals(
                "expiration:1,clientId:2222,userId:1,userName:bob,domain:aName,roles:[foo, role1, role2]",
                a1.toString());
    }

    @Test
    public void testEquals() {
        Authentication a1 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertTrue(a1.equals(a1));
        Authentication a2 = new AuthenticationBuilder(a1).setExpiration(1).build();
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));
        Authentication a3 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertTrue(a1.equals(a3));
        assertTrue(a3.equals(a2));
        assertTrue(a1.equals(a2));
    }

    @Test
    public void testNotEquals() {
        Authentication a1 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertFalse(a1.equals(null));
        assertFalse(a1.equals("wrong object"));
        Authentication a2 = new AuthenticationBuilder(a1).build();
        assertFalse(a1.equals(a2));
        assertFalse(a2.equals(a1));
        Authentication a3 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertFalse(a1.equals(a2));
        assertTrue(a1.equals(a3));
        assertFalse(a2.equals(a3));
        Authentication a4 = new AuthenticationBuilder(validClaim).setExpiration(9).build();
        assertFalse(a1.equals(a4));
        assertFalse(a4.equals(a1));
        Authentication a5 = new AuthenticationBuilder(a1).setExpiration(9).build();
        assertFalse(a1.equals(a5));
        assertFalse(a5.equals(a1));
    }

    @Test
    public void testHashCode() {
        Authentication a1 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertEquals(a1.hashCode(), a1.hashCode());
        Authentication a2 = new AuthenticationBuilder(a1).setExpiration(1).build();
        assertTrue(a1.equals(a2));
        assertEquals(a1.hashCode(), a2.hashCode());
        Authentication a3 = new AuthenticationBuilder(validClaim).setExpiration(1).build();
        assertTrue(a1.equals(a3));
        assertEquals(a1.hashCode(), a3.hashCode());
        assertEquals(a2.hashCode(), a3.hashCode());
        Authentication a4 = new AuthenticationBuilder(a1).setExpiration(9).build();
        assertFalse(a1.equals(a4));
        assertNotEquals(a1.hashCode(), a4.hashCode());
        Authentication a5 = new AuthenticationBuilder(a1).build();
        assertFalse(a1.equals(a5));
        assertNotEquals(a1.hashCode(), a5.hashCode());
    }
}
