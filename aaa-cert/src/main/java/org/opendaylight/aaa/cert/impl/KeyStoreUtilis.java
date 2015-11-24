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

public class KeyStoreUtilis {

    public static String keyStorePath = "configuration/ssl/";

    public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERTIFICATE_REQUEST = "-----END CERTIFICATE REQUEST-----";
    public static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----";

    public static int defaultKeySize = 2048; //1024
    public static int defaultValidity = 365;
    public static String defaultKeyAlg = "RSA"; //DES
    public static String defaultSignAlg = "SHA1WithRSAEncryption"; //MD5WithRSAEncryption

    public static String createDir(String dir) {
         File file = new File(dir);
         if(!file.exists())
             file.mkdirs();
         return file.getAbsolutePath();
    }

    public static boolean checkKeyStoreFile(String fileName) {
         File file = new File(keyStorePath + fileName);
         return file.exists();
    }

    public static String readFile(String certFile) {
        if (certFile == null || certFile.isEmpty())
            return null;

        try {
            final FileInputStream fInputStream = new FileInputStream(keyStorePath + certFile);
            byte[] certBytes = new byte[fInputStream.available()];
            fInputStream.read(certBytes);
            fInputStream.close();
            String cert = new String(certBytes, "UTF-8");
            return cert;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean saveCert(String fileName, String cert) {
        if (fileName == null || fileName.isEmpty())
            return false;

        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(keyStorePath + fileName));
            out.write(cert);
            out.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}