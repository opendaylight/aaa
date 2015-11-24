package org.opendaylight.aaa.cert;

public class KeyStoreUtilis {

    public static String keyToolCmd = "keytool";

    public static String keyStorePath = "configuration/ssl/";

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
    }
}
