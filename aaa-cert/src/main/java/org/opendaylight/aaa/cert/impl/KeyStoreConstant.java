/*
 * Copyright (c) 2015, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default values class for aaa-cert bundle.
 *
 * @author mserngawy
 *
 */
public final class KeyStoreConstant {
    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreConstant.class);

    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";

    public static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----";
    // Day time in millisecond
    public static final long DAY_TIME = 1000L * 60 * 60 * 24;
    public static final String DEFAULT_KEY_ALG = "RSA"; // DES
    public static final int DEFAULT_KEY_SIZE = 2048; // 1024
    public static final String DEFAULT_SIGN_ALG = "SHA1WithRSAEncryption"; // MD5WithRSAEncryption

    public static final int DEFAULT_VALIDITY = 365;
    public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    public static final String END_CERTIFICATE_REQUEST = "-----END CERTIFICATE REQUEST-----";
    public static final String TLS_PROTOCOL = "TLS";
    public static final String KEY_STORE_PATH = "configuration" + File.separator + "ssl" + File.separator;

    private KeyStoreConstant() {

    }

    public static boolean checkKeyStoreFile(final String fileName) {
        final File file = new File(KEY_STORE_PATH + fileName);
        return file.exists();
    }

    public static String createDir(final String dir) {
        final File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                LOG.error("Failed to create directories {}", file);
            }
        }
        return file.getAbsolutePath();
    }

    public static String readFile(final String certFile) {
        if (certFile == null || certFile.isEmpty()) {
            return null;
        }

        final String path = KEY_STORE_PATH + certFile;
        try (FileInputStream fInputStream = new FileInputStream(path)) {
            final int available = fInputStream.available();
            final byte[] certBytes = new byte[available];
            final int numRead = fInputStream.read(certBytes);
            if (numRead != available) {
                LOG.warn("Expected {} bytes read from {}, actual was {}", available, path, numRead);
            }
            return new String(certBytes, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return null;
        }
    }

    public static boolean saveCert(final String fileName, final String cert) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(KEY_STORE_PATH + fileName), StandardCharsets.UTF_8))) {
            out.write(cert);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }
}
