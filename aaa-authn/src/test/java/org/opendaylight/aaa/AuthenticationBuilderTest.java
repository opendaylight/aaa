package org.opendaylight.aaa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;

public class AuthenticationBuilderTest {

    @Test
    public void testEquals() {
        Authentication a1 = new AuthenticationBuilder().setExpiration(1)
                .setDomain("aName").setUserId("1")
                .setUser("bob").addRole("foo").build();
        assertFalse(a1.equals(null));
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
