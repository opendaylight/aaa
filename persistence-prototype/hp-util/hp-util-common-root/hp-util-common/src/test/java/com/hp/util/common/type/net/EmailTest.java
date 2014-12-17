/*
 * Copyright (c) 2011 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class EmailTest {

    private static final String[] VALID_VALUES = new String[] {"name@domain.com", "name.lastname@domain.com",
        "name-lastname@domain.com", "name@subdomain.domain.com", "name.lastname@subdomain.domain.com",
        "name-lastname@subdomain.domain.com", "name-12_@domain-125_.com"};

    private static final String[] INVALID_VALUES = new String[] {null, "name", "name@.domain.com", "name@domain.c",
        "name@.com", ".name@domain.com", "nam(e)@domain.com", "nam*e@domain.com", "nam%e@domain.com",
        "na..me@domain.com", "name.@domain.com", "name@domain@domain.com", "name@domain.1m"};

    @Test
    public void testIsValidString() {
        for (String value : VALID_VALUES) {
            Assert.assertTrue("Invalid validation for valid value: " + value, Email.isValid(value));
        }

        for (String value : INVALID_VALUES) {
            Assert.assertFalse("Invalid validation for invalid value: " + value, Email.isValid(value));
        }
    }

    @Test
    public void testGetValue() {
        for (String value : VALID_VALUES) {
            Email email = Email.valueOf(value);

            Assert.assertNotNull(email);
            Assert.assertEquals(value, email.getValue());
        }

        for (final String value : INVALID_VALUES) {

            Instruction executor = new Instruction() {
                @Override
                public void execute() throws Throwable {
                    Email.valueOf(value);
                }
            };

            ThrowableTester.testThrowsAny(Exception.class, executor);
        }
    }
}
