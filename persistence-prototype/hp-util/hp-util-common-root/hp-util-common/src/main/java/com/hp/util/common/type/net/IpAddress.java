/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import com.hp.util.common.SpecialConsideration;
import com.hp.util.common.type.SerializableValueType;

/**
 * IP Address.
 * 
 * @author Fabiel Zuniga
 */
public final class IpAddress extends SerializableValueType<String> {
    private static final long serialVersionUID = 1L;

    /*
     * Regular expression taken from:
     * http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression
     *
     * ^                    # start of the line
     *  (                   # start of group #1
     *    [01]?\\d\\d?      # Can be one or two digits. If three digits appear, it must start either 0 or 1
     *                      # e.g ([0-9], [0-9][0-9],[0-1][0-9][0-9])
     *    |                 # ...or
     *    2[0-4]\\d         # start with 2, follow by 0-4 and end with any digit (2[0-4][0-9])
     *    |                 # ...or
     *    25[0-5]           # start with 2, follow by 5 and end with 0-5 (25[0-5])
     *  )                   # end of group #1
     *  \.                  # follow by a dot "."
     *  ....                # repeat with 3 time (3x)
     * $                    # end of the line
     */
    private static Pattern IPv4_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Loop-back address.
     */
    public static final IpAddress LOOPBACK_IPv4 = IpAddress.valueOf("127.0.0.1");
    /**
     * Loop-back address.
     */
    // public static final IpAddress LOOPBACK_IPv6 = IpAddress.valueOf("0:0:0:0:0:0:0:1");
    public static final IpAddress LOOPBACK_IPv6 = IpAddress.valueOf("::1");

    private transient volatile InetAddress inetAddress;

    private IpAddress(String value) {
        super(value);

        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid format: " + value);
        }
    }

    private IpAddress(InetAddress inetAddress) {
        super(inetAddress.getHostAddress());
        this.inetAddress = inetAddress;
    }

    /**
     * Verifies whether a string represents a valid IP Address.
     *
     * @param value value to validate
     * @return {@code true} id {@code value} represents a valid IP address, {@code false} otherwise
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        boolean valid = IPv4_PATTERN.matcher(value).matches();
        if (!valid) {
            // TODO: Find a better way to validate an IPv6 address.
            try {
                InetAddress.getByName(value);
                valid = true;
            }
            catch (Exception e) {

            }
        }
        return valid;
    }

    /**
     * Constructs an IP Address form the string representation.
     *
     * @param value value
     * @return an IP Address
     */
    public static IpAddress valueOf(String value) {
        return new IpAddress(value);
    }

    /**
     * Constructs an IP Address form the {@link InetAddress}.
     *
     * @param inetAddress value
     * @return an IP Address
     */
    public static IpAddress valueOf(InetAddress inetAddress) {
        if (inetAddress == null) {
            throw new NullPointerException("inetAddress cannot be null");
        }
        return new IpAddress(inetAddress);
    }

    /**
     * Constructs an IP address from its octet components.
     * <p>
     * 255.255.255.255 -> octet1.octet2.octet3.octet4
     *
     * @param octet1 first octet (The most significant)
     * @param octet2 second octet
     * @param octet3 third octet
     * @param octet4 fourth octet (The least significant)
     * @return A IP address (IpV4)
     */
    public static IpAddress valueOfIpV4(byte octet1, byte octet2, byte octet3, byte octet4) {
        StringBuilder str = new StringBuilder(15);

        // Everything in Java is signed and bytes, ints, longs are encoded in two's complement.
        // int unsignedByte = signedByte & (0xff);

        str.append(octet1 & (0xff));
        str.append('.');
        str.append(octet2 & (0xff));
        str.append('.');
        str.append(octet3 & (0xff));
        str.append('.');
        str.append(octet4 & (0xff));

        return IpAddress.valueOf(str.toString());
    }

    /**
     * Gets the {@link InetAddress}.
     *
     * @return the {@link InetAddress}
     * @throws UnknownHostException if no IP Address is found
     */
    public InetAddress getInetAddress() throws UnknownHostException {
        /*
         * Synchronization on this method is not used on purpose. Not a big deal if multiple
         * instances of InetAddress are created in a multi-thread environment.
         */
        if (this.inetAddress == null) {

            @SuppressWarnings("unused")
            SpecialConsideration specialConsideration = new SpecialConsideration() {
                {
                    /*
                     * Attackers can spoof DNS entries. Do not rely on DNS names for security.
                     */
                    /*
                     * Many DNS servers are susceptible to spoofing attacks, so you should assume
                     * that your software will someday run in an environment with a compromised DNS
                     * server. If attackers are allowed to make DNS updates (sometimes called DNS
                     * cache poisoning), they can route your network traffic through their machines
                     * or make it appear as if their IP addresses are part of your domain. Do not
                     * base the security of your system on DNS names.
                     */
                    /*
                     * Example: The following code uses a DNS lookup to determine whether an inbound
                     * request is from a trusted host. If an attacker can poison the DNS cache, they
                     * can gain trusted status.
                     */
                    /*
                     * String ip = request.getRemoteAddr();
                     * InetAddress addr = InetAddress.getByName(ip);
                     * if (addr.getCanonicalHostName().endsWith("trustme.com")) {
                     *     trusted = true;
                     * }
                     */
                    /*
                     * IP addresses are more reliable than DNS names, but they can also be spoofed.
                     * Attackers can easily forge the source IP address of the packets they send,
                     * but response packets will return to the forged IP address. To see the
                     * response packets, the attacker has to sniff the traffic between the victim
                     * machine and the forged IP address. In order to accomplish the required
                     * sniffing, attackers typically attempt to locate themselves on the same subnet
                     * as the victim machine. Attackers may be able to circumvent this requirement
                     * by using source routing, but source routing is disabled across much of the
                     * Internet today. In summary, IP address verification can be a useful part of
                     * an authentication scheme, but it should not be the single factor required for
                     * authentication.
                     */
                    /*
                     * Recommendation
                     */
                    /*
                     * You can increase confidence in a domain name lookup if you check to make sure
                     * that the host's forward and backward DNS entries match. Attackers will not be
                     * able to spoof both the forward and the reverse DNS entries without
                     * controlling the nameservers for the target domain. This is not a foolproof
                     * approach however: attackers may be able to convince the domain registrar to
                     * turn over the domain to a malicious nameserver. Basing authentication on DNS
                     * entries is simply a risky proposition
                     */
                    /*
                     * While no authentication mechanism is foolproof, there are better alternatives
                     * than host-based authentication. Password systems offer decent security, but
                     * are susceptible to bad password choices, insecure password transmission, and
                     * bad password management. A cryptographic scheme like SSL is worth
                     * considering, but such schemes are often so complex that they bring with them
                     * the risk of significant implementation errors, and key material can always be
                     * stolen. In many situations, multi-factor authentication including a physical
                     * token offers the most security available at a reasonable price.
                     */

                    // TODO: Do not use getByName. Find a way to get an IpAddress for V6.
                    IpAddress.this.inetAddress = InetAddress.getByName(getValue());
                }
            };
        }
        return this.inetAddress;
    }
}
