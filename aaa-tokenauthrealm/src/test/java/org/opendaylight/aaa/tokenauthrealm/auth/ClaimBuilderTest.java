/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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
import java.util.HashSet;
import org.junit.Test;
import org.opendaylight.aaa.api.Claim;

/**
 * ClaimBuilder test suite.
 * @author liemmn
 *
 */
public class ClaimBuilderTest {
    @Test
    public void testBuildWithAll() {
        Claim c1 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").addRole("foo2")
                .addRoles(new HashSet<>(Arrays.asList("foo", "bar"))).build();
        assertEquals("dlux", c1.clientId());
        assertEquals("pepsi", c1.domain());
        assertEquals("john", c1.user());
        assertEquals("1234", c1.userId());
        assertTrue(c1.roles().contains("foo"));
        assertTrue(c1.roles().contains("foo2"));
        assertTrue(c1.roles().contains("bar"));
        assertEquals(3, c1.roles().size());
        Claim c2 = new ClaimBuilder(c1).build();
        assertEquals(c1, c2);
    }

    @Test
    public void testBuildWithRequired() {
        Claim c1 = new ClaimBuilder().setUser("john").setUserId("1234").addRole("foo").build();
        assertEquals("john", c1.user());
        assertEquals("1234", c1.userId());
        assertTrue(c1.roles().contains("foo"));
        assertEquals(1, c1.roles().size());
        assertEquals("", c1.domain());
        assertEquals("", c1.clientId());
    }

    @Test
    public void testBuildWithEmptyOptional() {
        Claim c1 = new ClaimBuilder().setDomain("  ").setClientId("  ").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertEquals("", c1.domain());
        assertEquals("", c1.clientId());
        assertEquals("john", c1.user());
        assertEquals("1234", c1.userId());
        assertTrue(c1.roles().contains("foo"));
        assertEquals(1, c1.roles().size());
    }

    @Test
    public void testBuildWithNullOptional() {
        Claim c1 = new ClaimBuilder().setDomain(null).setClientId(null).setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertEquals("", c1.domain());
        assertEquals("", c1.clientId());
        assertEquals("john", c1.user());
        assertEquals("1234", c1.userId());
        assertTrue(c1.roles().contains("foo"));
        assertEquals(1, c1.roles().size());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithDefault() {
        ClaimBuilder c1 = new ClaimBuilder();
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutUser() {
        ClaimBuilder c1 = new ClaimBuilder().setUserId("1234").addRole("foo");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithNullUser() {
        ClaimBuilder c1 = new ClaimBuilder().setUser(null).setUserId("1234").addRole("foo");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithEmptyUser() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("  ").setUserId("1234").addRole("foo");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutUserId() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("john").addRole("foo");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithNullUserId() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("john").setUserId(null).addRole("foo");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithEmptyUserId() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("john").setUserId("  ").addRole("foo");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithoutRole() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("john").setUserId("1234");
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithNullRole() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("john").setUserId("1234").addRole(null);
        c1.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithEmptyRole() {
        ClaimBuilder c1 = new ClaimBuilder().setUser("john").setUserId("1234").addRole("  ");
        c1.build();
    }

    @Test
    public void testEquals() {
        Claim c1 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertTrue(c1.equals(c1));
        Claim c2 = new ClaimBuilder(c1).addRole("foo").build();
        assertTrue(c1.equals(c2));
        assertTrue(c2.equals(c1));
        Claim c3 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertTrue(c1.equals(c3));
        assertTrue(c3.equals(c2));
        assertTrue(c1.equals(c2));
    }

    @Test
    public void testNotEquals() {
        Claim c1 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertFalse(c1.equals(null));
        assertFalse(c1.equals("wrong object"));
        Claim c2 = new ClaimBuilder(c1).addRoles(new HashSet<>(Arrays.asList("foo", "bar")))
                .build();
        assertEquals(2, c2.roles().size());
        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1));
        Claim c3 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertFalse(c1.equals(c2));
        assertTrue(c1.equals(c3));
        assertFalse(c2.equals(c3));
        Claim c5 = new ClaimBuilder().setUser("john").setUserId("1234").addRole("foo").build();
        assertFalse(c1.equals(c5));
        assertFalse(c5.equals(c1));
    }

    @Test
    public void testHash() {
        Claim c1 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertEquals(c1.hashCode(), c1.hashCode());
        Claim c2 = new ClaimBuilder(c1).addRole("foo").build();
        assertTrue(c1.equals(c2));
        assertEquals(c1.hashCode(), c2.hashCode());
        Claim c3 = new ClaimBuilder(c1).addRoles(new HashSet<>(Arrays.asList("foo", "bar")))
                .build();
        assertFalse(c1.equals(c3));
        assertNotEquals(c1.hashCode(), c3.hashCode());
        Claim c4 = new ClaimBuilder().setClientId("dlux").setDomain("pepsi").setUser("john")
                .setUserId("1234").addRole("foo").build();
        assertTrue(c1.equals(c4));
        assertEquals(c1.hashCode(), c4.hashCode());
        assertEquals(c2.hashCode(), c4.hashCode());
        Claim c5 = new ClaimBuilder().setUser("john").setUserId("1234").addRole("foo").build();
        assertFalse(c1.equals(c5));
        assertNotEquals(c1.hashCode(), c5.hashCode());
    }

    @Test
    public void testToString() {
        Claim c1 = new ClaimBuilder().setUser("john").setUserId("1234").addRole("foo").build();
        assertEquals("clientId:,userId:1234,userName:john,domain:,roles:[foo]", c1.toString());
        c1 = new ClaimBuilder(c1).setClientId("dlux").setDomain("pepsi").build();
        assertEquals("clientId:dlux,userId:1234,userName:john,domain:pepsi,roles:[foo]",
                c1.toString());
        c1 = new ClaimBuilder(c1).addRole("bar").build();
        assertEquals("clientId:dlux,userId:1234,userName:john,domain:pepsi,roles:[foo, bar]",
                c1.toString());
    }
}
