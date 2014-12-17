/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.auth;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.test.EqualityTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class CredentialsTest {

    @Test
    public void testConstruction() {
        Username username = Username.valueOf("username");
        Password password = Password.valueOf("password");
        
        Credentials credentials = new Credentials(username, password);
        Assert.assertEquals(username, credentials.getUsername());
        Assert.assertEquals(password, credentials.getPassword());
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidConstruction() {
        Username invalidUsername = null;
        Password validPassword = null;
        
        @SuppressWarnings("unused")
        Credentials credentials = new Credentials(invalidUsername, validPassword);
    }

    @Test
    public void testEqualsAndHashCode() {
        Username username = Username.valueOf("username");
        Password password = Password.valueOf("password");
        Username otherUsername = Username.valueOf("other-username");
        Password otherPassword = Password.valueOf("other-password");

        Credentials obj = new Credentials(username, password);
        Credentials equal1 = new Credentials(username, password);
        Credentials equal2 = new Credentials(username, password);
        Credentials unequal1 = new Credentials(otherUsername, password);
        Credentials unequal2 = new Credentials(username, otherPassword);
        Credentials unequal3 = new Credentials(username, null);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2, unequal3);
    }

    @Test
    public void testToString() {
        Username username = Username.valueOf("username");
        Password password = Password.valueOf("password");
        Credentials credentials = new Credentials(username, password);
        Assert.assertFalse(credentials.toString().isEmpty());
    }
}
