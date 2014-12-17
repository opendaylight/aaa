/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.type.Port;
import com.hp.util.test.EqualityTester;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class HostTest {

    @Test
    public void testConstruction() {
        IpAddress ipAddress = IpAddress.LOOPBACK_IPv4;
        Port port = Port.valueOf(1);

        Host host = new Host(ipAddress, port);

        Assert.assertEquals(ipAddress, host.getIpAddress());
        Assert.assertEquals(port, host.getPort());
    }

    @Test
    public void testInvalidConstruction() {
        final IpAddress validIpAddress = IpAddress.LOOPBACK_IPv4;
        final IpAddress invalidIpAddress = null;

        final Port validPort = Port.valueOf(1);
        final Port invalidPort = null;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new Host(invalidIpAddress, validPort);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new Host(validIpAddress, invalidPort);
            }
        });
    }

    @Test
    public void testEqualsAndHashCode() {
        Host obj = new Host(IpAddress.LOOPBACK_IPv4, Port.valueOf(1));
        Host equal1 = new Host(IpAddress.LOOPBACK_IPv4, Port.valueOf(1));
        Host equal2 = new Host(IpAddress.LOOPBACK_IPv4, Port.valueOf(1));
        Host unequal1 = new Host(IpAddress.valueOf("15.255.123.85"), Port.valueOf(1));
        Host unequal2 = new Host(IpAddress.LOOPBACK_IPv4, Port.valueOf(2));

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2);
    }

    @Test
    public void testToString() {
        Host host = new Host(IpAddress.LOOPBACK_IPv4, Port.valueOf(1));
        Assert.assertFalse(host.toString().isEmpty());
    }
}
