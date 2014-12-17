/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test.common;

import com.hp.util.common.type.net.IpAddress;
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.test.RandomDataGenerator;

/**
 * Random data generator.
 * 
 * @author Fabiel Zuniga
 */
public class RandomDataGeneratorExtended extends RandomDataGenerator {

    /**
     * Generates an IP address.
     *
     * @return an IP address
     */
    public IpAddress getIpAddress() {
        int intValue = getInt();
        byte[] intBytes = new byte[] {(byte)(intValue >>> 24), (byte)(intValue >>> 16), (byte)(intValue >>> 8),
            (byte)intValue};
        return IpAddress.valueOfIpV4(intBytes[0], intBytes[1], intBytes[2], intBytes[3]);
    }

    /**
     * Generates a MAC address.
     * 
     * @return a MAC address
     */
    public MacAddress getMacAddress() {
        int intValue = getInt();
        byte[] intBytes = new byte[] {(byte)(intValue >>> 24), (byte)(intValue >>> 16), (byte)(intValue >>> 8),
            (byte)intValue};
        return MacAddress.valueOfOctets((byte)0, (byte)0, intBytes[0], intBytes[1], intBytes[2], intBytes[3]);
    }
}
