package org.opendaylight.aaa.cert;

import org.opendaylight.aaa.cert.command.CreateKeyStore;
import org.opendaylight.aaa.cert.command.GetCert;
import org.opendaylight.aaa.cert.command.ImportCert;

public class TrustKeystoreProvider {
    private KeyTool keytool;

    public TrustKeystoreProvider() {
        keytool = new KeyTool(KeyStoreUtilis.keyStorePath);
    }

    public KeyToolResult importCertificate(String keyStore, String storePasswd, String certPasswd, String alias, String certificate) {
        ImportCert importCert = new ImportCert();
        return keytool.execute(importCert);
    }

    public KeyToolResult getOVSCert(String keyStore, String storePasswd, String certPasswd, String alias) {
        GetCert getOVSCert = new GetCert();
        getOVSCert.setAlias(alias);
        getOVSCert.setKeyStore(keyStore);
        getOVSCert.setKeyPass(certPasswd);
        getOVSCert.setStorePass(storePasswd);
        return keytool.execute(getOVSCert);
    }
}
