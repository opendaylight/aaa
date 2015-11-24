/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import org.opendaylight.aaa.cert.command.CreateKeyStore;
import org.opendaylight.aaa.cert.command.GetSelfSignCert;
import org.opendaylight.aaa.cert.KeyToolResult;
import org.opendaylight.aaa.cert.KeyTool;

public class CtlKeyStore {

    private KeyTool keytool;

    public CtlKeyStore() {
        keytool = new KeyTool(KeyStoreUtilis.keyStorePath);
    }

    public KeyToolResult createCtlKeyStore(String keyStore, String storePasswd, String keyPasswd, String alias, String dName) {
        CreateKeyStore createKeyStore = new CreateKeyStore();
        createKeyStore.setAlias(alias);
        createKeyStore.setDName(dName);
        createKeyStore.setKeyPass(keyPasswd);
        createKeyStore.setKeyStore(keyStore);
        createKeyStore.setStorePass(storePasswd);
        return keytool.execute(createKeyStore);
    }

    public KeyToolResult getCtlCert(String keyStore, String storePasswd, String keyPasswd, String alias) {
        GetSelfSignCert getSelfSignCert = new GetSelfSignCert();
        getSelfSignCert.setAlias(alias);
        getSelfSignCert.setKeyPass(keyPasswd);
        getSelfSignCert.setKeyStore(keyStore);
        getSelfSignCert.setStorePass(storePasswd);
        return keytool.execute(getSelfSignCert);
    }
}