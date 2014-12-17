/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.auth;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.EqualityTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class PasswordTest {

    @Test
    public void testValueOf() {
        Password password = Password.valueOf("password");
        Assert.assertEquals("password", password.getValue());
    }

    @Test
    public void testValueOfNull() {
        Assert.assertNull(Password.valueOf(null));
    }

    @Test
    public void testToString() {
        Password password = Password.valueOf("qwerty");
        Assert.assertFalse(password.toString().isEmpty());
        // For security the password should not be revealed
        Assert.assertFalse(password.toString().contains(password.getValue()));
    }

    @Test
    public void testEqualsAndHashCode() {
        Password obj = Password.valueOf("123");
        Password equal1 = Password.valueOf("123");
        Password equal2 = Password.valueOf("123");
        Password unequal1 = Password.valueOf("1234");
        Password unequal2 = Password.valueOf("");

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2);
    }
}
