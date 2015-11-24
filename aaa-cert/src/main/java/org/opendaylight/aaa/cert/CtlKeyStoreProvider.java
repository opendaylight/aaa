/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import org.opendaylight.aaa.cert.command.CreateKeyStore;
import org.opendaylight.aaa.cert.command.GetCert;
import org.opendaylight.aaa.cert.KeyToolResult;
import org.opendaylight.aaa.cert.KeyTool;

public class CtlKeyStoreProvider {

    private KeyTool keytool;

    public CtlKeyStoreProvider() {
        keytool = new KeyTool(KeyStoreUtilis.keyStorePath);
    }

    public KeyToolResult createCtlKeyStore(String keyStore, String storePasswd, String keyPasswd, String alias, String dName, String validity) {
        CreateKeyStore createKeyStore = new CreateKeyStore();
        createKeyStore.setAlias(alias);
        createKeyStore.setDName(dName);
        createKeyStore.setKeyPass(keyPasswd);
        createKeyStore.setKeyStore(keyStore);
        createKeyStore.setStorePass(storePasswd);
        createKeyStore.setValidity(validity);
        return keytool.execute(createKeyStore);
    }

    public KeyToolResult getCtlCert(String keyStore, String storePasswd, String keyPasswd, String alias) {
        GetCert getSelfSignCert = new GetCert();
        getSelfSignCert.setAlias(alias);
        getSelfSignCert.setKeyPass(keyPasswd);
        getSelfSignCert.setKeyStore(keyStore);
        getSelfSignCert.setStorePass(storePasswd);
        return keytool.execute(getSelfSignCert);
    }
}