/*
 * Copyright (c) 2015, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default values class for aaa-cert bundle.
 *
 * @author mserngawy
 */
public final class KeyStoreConstant {
    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreConstant.class);

    static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";

    static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----";
    public static final String DEFAULT_KEY_ALG = "RSA"; // DES
    public static final int DEFAULT_KEY_SIZE = 2048; // 1024
    public static final String DEFAULT_SIGN_ALG = "SHA1WithRSAEncryption"; // MD5WithRSAEncryption

    public static final int DEFAULT_VALIDITY = 365;
    static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    static final String END_CERTIFICATE_REQUEST = "-----END CERTIFICATE REQUEST-----";
    static final String KEY_STORE_PATH = "configuration" + File.separator + "ssl" + File.separator;

    private KeyStoreConstant() {

    }

    public static File toAbsoluteFile(final String fileName, final String basePath) {
        final File file = new File(fileName);
        return file.isAbsolute() ? file : new File(basePath + fileName);
    }

    public static boolean checkKeyStoreFile(final String fileName) {
        return toAbsoluteFile(fileName, KEY_STORE_PATH).exists();
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
        if (certFile != null && !certFile.isEmpty()) {
            final Path path = toAbsoluteFile(certFile, KEY_STORE_PATH).toPath();
            try {
                return Files.readString(path);
            } catch (IOException e) {
                LOG.info("Failed to read {}", path, e);
            }
        }
        return null;
    }

    public static boolean saveCert(final String fileName, final String cert) {
        if (fileName != null && !fileName.isEmpty()) {
            final Path path = toAbsoluteFile(fileName, KEY_STORE_PATH).toPath();
            try {
                Files.writeString(path, cert);
                return true;
            } catch (IOException e) {
                LOG.info("Failed to write {}", path, e);
            }
        }
        return false;
    }
}
