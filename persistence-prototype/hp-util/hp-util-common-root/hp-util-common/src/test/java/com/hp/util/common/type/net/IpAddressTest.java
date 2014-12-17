/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import java.net.UnknownHostException;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class IpAddressTest {

    private static final String[] VALID_VALUES = new String[] { IpAddress.LOOPBACK_IPv4.getValue(),
            IpAddress.LOOPBACK_IPv6.getValue(), "1.1.1.1", "255.255.255.255", "192.168.1.1", "10.10.1.1",
            "132.254.111.10", "26.10.2.10" };

    // TODO: Verify invalid values because IPv6 allows zeros on the right to be omitted.
    /*
    private static final String[] INVALID_VALUES = new String[] {null, "10.10.10", "10.10", "10", "a.a.a.a",
        "10.0.0.a", "10.10.10.256", "222.222.2.999", "999.10.10.20", "2222.22.22.22", "22.2222.22.2", "10.10.10"};
        */
    private static final String[] INVALID_VALUES = new String[] { null, "a.a.a.a", "10.0.0.a", "10.10.10.256",
            "222.222.2.999", "999.10.10.20", "2222.22.22.22", "22.2222.22.2" };

    @Test
    public void testIsValidString() {
        for (String value : VALID_VALUES) {
            Assert.assertTrue("Invalid validation for valid value: " + value, IpAddress.isValid(value));
        }

        for (String value : INVALID_VALUES) {
            Assert.assertFalse("Invalid validation for invalid value: " + value, IpAddress.isValid(value));
        }
    }

    @Test
    public void testGetValue() {
        for (String value : VALID_VALUES) {
            IpAddress ipAddress = IpAddress.valueOf(value);

            Assert.assertNotNull(ipAddress);
            Assert.assertEquals(value, ipAddress.getValue());
        }

        for (final String value : INVALID_VALUES) {

            Instruction executor = new Instruction() {
                @Override
                public void execute() throws Throwable {
                    IpAddress.valueOf(value);
                }
            };

            ThrowableTester.testThrowsAny(Exception.class, executor);
        }
    }

    @Test
    public void testGetInetAddress() throws UnknownHostException {
        Assert.assertNotNull(IpAddress.LOOPBACK_IPv4.getInetAddress());
        Assert.assertNotNull(IpAddress.LOOPBACK_IPv6.getInetAddress());

        for (String value : VALID_VALUES) {
            IpAddress ipAddress = IpAddress.valueOf(value);
            Assert.assertNotNull(ipAddress.getInetAddress());
        }
    }
}
