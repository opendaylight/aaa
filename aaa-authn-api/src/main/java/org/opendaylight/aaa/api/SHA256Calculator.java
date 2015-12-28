/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import java.security.MessageDigest;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sharon Aicler (saichler@cisco.com)
 */
public class SHA256Calculator {

    private static final Logger LOG = LoggerFactory.getLogger(SHA256Calculator.class);

    private static MessageDigest md = null;
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static WriteLock writeLock = lock.writeLock();

    public static String generateSALT() {
        StringBuffer salt = new StringBuffer();
        for (int i = 0; i < 12; i++) {
            int random = (int) (Math.random() * 24 + 1);
            salt.append((char) (65 + random));
        }
        return salt.toString();
    }

    public static String getSHA256(byte data[], String salt) {
        byte SALT[] = salt.getBytes();
        byte temp[] = new byte[data.length + SALT.length];
        System.arraycopy(data, 0, temp, 0, data.length);
        System.arraycopy(SALT, 0, temp, data.length, SALT.length);

        if (md == null) {
            try {
                writeLock.lock();
                if (md == null) {
                    try {
                        md = MessageDigest.getInstance("SHA-256");
                    } catch (Exception err) {
                        LOG.error("Error calculating SHA-256 for SALT", err);
                    }
                }
            } finally {
                writeLock.unlock();
            }
        }

        byte by[] = null;

        try {
            writeLock.lock();
            md.update(temp);
            by = md.digest();
        } finally {
            writeLock.unlock();
        }
        return removeSpecialCharacters(new String(by));
    }

    public static String getSHA256(String password, String salt) {
        return getSHA256(password.getBytes(), salt);
    }

    public static String removeSpecialCharacters(String str) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '\'' && str.charAt(i) != 0) {
                buff.append(str.charAt(i));
            }
        }
        return buff.toString();
    }
}
