/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import java.util.regex.Pattern;

import com.hp.util.common.type.SerializableValueType;

/**
 * MAC Address.
 * 
 * @author Fabiel Zuniga
 */
public final class MacAddress extends SerializableValueType<String> {
    private static final long serialVersionUID = 1L;

    private static Pattern PATTERN = Pattern.compile("^[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}:[0-9A-Fa-f]{2}$");

    private MacAddress(String value) {
        super(value);

        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid format: " + value);
        }
    }

    /**
     * Verifies whether a string represents a valid MAC Address.
     *
     * @param value value to validate
     * @return {@code true} id {@code value} represents a valid MAC address, {@code false} otherwise
     */
    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches();
    }


    /**
     * Constructs an MAC Address form the string representation.
     *
     * @param value value
     * @return a MAC Address
     */
    public static MacAddress valueOf(String value) {
        return new MacAddress(value);
    }

    /**
     * Constructs a MAC address from its octet components.
     * <p>
     * 01:23:45:67:89:AB -> octet1.octet2.octet3.octet4.octet5.octet6
     *
     * @param octet1 first octet (The most significant)
     * @param octet2 second octet
     * @param octet3 third octet
     * @param octet4 fourth octet
     * @param octet5 fifth octet
     * @param octet6 sixth octet (The least significant)
     * @return A IP address (IpV4)
     */
    public static MacAddress valueOfOctets(byte octet1, byte octet2, byte octet3, byte octet4, byte octet5, byte octet6) {
        StringBuilder str = new StringBuilder(15);

        str.append(toMacAddressOctet(octet1));
        str.append(':');
        str.append(toMacAddressOctet(octet2));
        str.append(':');
        str.append(toMacAddressOctet(octet3));
        str.append(':');
        str.append(toMacAddressOctet(octet4));
        str.append(':');
        str.append(toMacAddressOctet(octet5));
        str.append(':');
        str.append(toMacAddressOctet(octet6));

        return MacAddress.valueOf(str.toString());
    }

    private static String toMacAddressOctet(byte octet) {
        // Everything in Java is signed and bytes, ints, longs are encoded in two's complement.

        int unsignedByte = octet & (0xff);
        String strHexOctet = Integer.toHexString(unsignedByte);
        if (strHexOctet.length() < 2) {
            strHexOctet = '0' + strHexOctet;
        }

        return strHexOctet;
    }
}
