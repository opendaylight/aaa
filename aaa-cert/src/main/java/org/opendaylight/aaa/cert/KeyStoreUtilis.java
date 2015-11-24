/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import java.io.File;

public class KeyStoreUtilis {

    public static String keyToolCmd = "keytool";

    public static String keyStorePath = "configuration/ssl/";

    public static String defaultKeySize = "2048";
    public static String defaultValidity = "360";
    public static String defaultKeyAlg = "RSA";

    public static class BasicKeyToolArguments {
        public static String certReq = "-certreq";
        public static String exportCert = "-exportcert";
        public static String genKeyPair = "-genkeypair";
        public static String genCert = "-gencert";
        public static String importCert = "-importcert";
        public static String list = "-list";
        public static String help = "-help";
        public static String printCert = "-printcert";
        public static String printCertReq = "-printcertreq";
    }

    public static class KeyToolArguments {
        public static String keyAlg = "-keyalg";
        public static String alias = "-alias";
        public static String keyStore = "-keystore";
        public static String storePass = "-storepass";
        public static String validity = "-validity";
        public static String keySize = "-keysize";
        public static String dName = "-dname";
        public static String keyPass = "-keypass";
        public static String file = "-file";
    }

    public static class BooleanKeyToolArguments {
        public static String rfc = "-rfc";
        public static String noPrompt = "-noprompt";
        public static String verbose = "-v";
    }

    public static String createDir(String dir) {
         File file = new File(dir);
         if(!file.exists())
             file.mkdirs();
         return file.getAbsolutePath();
    }

}

