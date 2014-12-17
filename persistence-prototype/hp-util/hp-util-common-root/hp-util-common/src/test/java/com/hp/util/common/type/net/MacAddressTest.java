/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class MacAddressTest {

    private static final String[] VALID_VALUES = new String[] { "00:00:00:00:00:00", "3D:F2:C9:A6:B3:4F",
            "3d:f2:c9:a6:b3:4f" };

    private static final String[] INVALID_VALUES = new String[] {null, "3D:F2:AC9:A6:B3:4F", "3D:F2:C9:A6:B3:4F:00",
        ":F2:C9:A6:B3:4F", "F2:C9:A6:B3:4F"};

    @Test
    public void testIsValidString() {
        for (String value : VALID_VALUES) {
            Assert.assertTrue("Invalid validation for valid value: " + value, MacAddress.isValid(value));
        }

        for (String value : INVALID_VALUES) {
            Assert.assertFalse("Invalid validation for invalid value: " + value, MacAddress.isValid(value));
        }
    }

    @Test
    public void testGetValue() {
        for (String value : VALID_VALUES) {
            MacAddress macAddress = MacAddress.valueOf(value);

            Assert.assertNotNull(macAddress);
            Assert.assertEquals(value, macAddress.getValue());
        }

        for (final String value : INVALID_VALUES) {

            Instruction executor = new Instruction() {
                @Override
                public void execute() throws Throwable {
                    MacAddress.valueOf(value);
                }
            };

            ThrowableTester.testThrowsAny(Exception.class, executor);
        }
    }

    @Test
    public void testValueOfOctets() {
        for (int i = 0; i < 255; i++) {
            byte b = (byte) i;
            MacAddress.valueOfOctets(b, b, b, b, b, b);
        }
    }
}
