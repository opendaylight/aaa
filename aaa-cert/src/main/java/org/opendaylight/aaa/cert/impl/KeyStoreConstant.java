/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Default values class for aaa-cert bundle
 *
 * @author mserngawy
 *
 */
public class KeyStoreConstant {

    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";

    public static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----";
    // Day time in millisecond
    public static final long DAY_TIME = 1000L * 60 * 60 * 24;
    public static final String DEFAULT_KEY_ALG = "RSA"; //DES
    public static final int DEFAULT_KEY_SIZE = 2048; //1024
    public static final String DEFAULT_SIGN_ALG = "SHA1WithRSAEncryption"; //MD5WithRSAEncryption

    public static final int DEFAULT_VALIDITY = 365;
    public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    public static final String END_CERTIFICATE_REQUEST = "-----END CERTIFICATE REQUEST-----";
    public static final String TLS_PROTOCOL = "TLS";
    public static String KEY_STORE_PATH = "configuration" + File.separator + "ssl" + File.separator;

    public static boolean checkKeyStoreFile(final String fileName) {
        final File file = new File(KEY_STORE_PATH + fileName);
        return file.exists();
    }

    public static String createDir(final String dir) {
        final File file = new File(dir);
        if(!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String readFile(final String certFile) {
        if (certFile == null || certFile.isEmpty()) {
            return null;
        }

        try {
            final FileInputStream fInputStream = new FileInputStream(KEY_STORE_PATH + certFile);
            final byte[] certBytes = new byte[fInputStream.available()];
            fInputStream.read(certBytes);
            fInputStream.close();
            final String cert = new String(certBytes, StandardCharsets.UTF_8);
            return cert;
        } catch (final IOException e) {
            return null;
        }
    }

    public static boolean saveCert(final String fileName, final String cert) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(KEY_STORE_PATH + fileName));
            out.write(cert);
            out.close();
            return true;
        } catch (final IOException e) {
            return false;
        }
    }
}