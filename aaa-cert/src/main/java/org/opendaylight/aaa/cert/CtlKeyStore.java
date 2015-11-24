/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import org.apache.maven.shared.utils.cli.javatool.JavaToolException;
import org.opendaylight.aaa.cert.command.CreateKeyStore;
import org.opendaylight.aaa.cert.KeyToolResult;
import org.opendaylight.aaa.cert.KeyTool;

public class CtlKeyStore {

    private KeyTool keytool;

    public CtlKeyStore() {
        keytool = new KeyTool(KeyStoreUtilis.keyStorePath);
    }

    public KeyToolResult createCtlKeyStore(String keyStore, String storePasswd, String keyPasswd, String alias, String dName) {
        CreateKeyStore cks = new CreateKeyStore();
        cks.setAlias(alias);
        cks.setDName(dName);
        cks.setKeyPass(keyPasswd);
        cks.setKeyStore(keyStore);
        cks.setStorePass(storePasswd);
        return keytool.execute(cks);
    }

}