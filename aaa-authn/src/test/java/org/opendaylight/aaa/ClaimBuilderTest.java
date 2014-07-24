/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;
import org.opendaylight.aaa.api.Claim;

/**
 * 
 * @author liemmn
 * 
 */
public class ClaimBuilderTest {
    private static final Claim c1 = new ClaimBuilder().setClientId("dlux")
            .setDomain("pepsi").setUser("john").setUserId("1234")
            .addRole("foo").build();

    @Test
    public void testClone() {
        Claim c2 = new ClaimBuilder(c1).build();
        assertEquals(c1, c2);
        Claim c3 = new ClaimBuilder(c1).addRoles(
                new HashSet<String>(Arrays.asList("foo", "bar"))).build();
        assertFalse(c1.equals(c3));
        assertEquals(2, c3.roles().size());
    }

    @Test
    public void testHash() {
        HashSet<Claim> claims = new HashSet<>();
        claims.add(c1);
        claims.add(c1);
        assertEquals(1, claims.size());

        Claim c2 = new ClaimBuilder(c1).addRoles(
                new HashSet<String>(Arrays.asList("foo", "bar"))).build();
        claims.add(c2);
        assertEquals(2, claims.size());
    }

    @Test
    public void testToString() {
        assertTrue(c1.toString().contains("foo"));
    }

}
