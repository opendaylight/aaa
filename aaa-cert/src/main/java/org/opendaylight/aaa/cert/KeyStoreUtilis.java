/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

public class KeyStoreUtilis {

    public static String keyToolCmd = "keytool";

    public static String keyStorePath = "/Volumes/Stoarge/ODL-sources/";

    public static class KeyToolArguments {
        public static String certReq = "-certreq";
        public static String exportCert = "-exportcert";
        public static String genKey = "-genkeypair";
        public static String keyAlg = "-keyalg";
        public static String alias = "-alias";
        public static String keyStore = "-keystore";
        public static String storePass = "-storepass";
        public static String validity = "-validity";
        public static String keySize = "-keysize";
        public static String dName = "-dname";
        public static String list = "-list";
        public static String keyPass = "-keypass";
        public static String importCert = "-importcert";
        public static String file = "-file";
        public static String rfc = "-rfc";
    }
}
