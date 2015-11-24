/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.io.File;

public class KeyStoreUtilis {

    public static String keyStorePath = "configuration/ssl/";

    public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERTIFICATE_REQUEST = "-----END CERTIFICATE REQUEST-----";
    public static final String BEGIN_CERTIFICATE_REQUEST = "-----BEGIN CERTIFICATE REQUEST-----";

    public static int defaultKeySize = 2048;
    public static int defaultValidity = 365;
    public static String defaultKeyAlg = "RSA"; //"DES"
    public static String defaultSignAlg = "SHA1WithRSAEncryption"; //MD5WithRSAEncryption

    public static String createDir(String dir) {
         File file = new File(dir);
         if(!file.exists())
             file.mkdirs();
         return file.getAbsolutePath();
    }

    public static boolean checkKeyStoreFile(String fileName) {
         File file = new File(keyStorePath+fileName);
         return file.exists();
    }
}