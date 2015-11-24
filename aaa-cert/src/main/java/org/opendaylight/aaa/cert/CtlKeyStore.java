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

    public KeyToolResult createCtlKeyStore() {
        try {
            CreateKeyStore cks = new CreateKeyStore();
            cks.setAlias("controller");
            cks.setDName("");
            cks.setKeyPass("opendaylight");
            cks.setKeyStore("ctl.jks");
            cks.setStorePass("opendaylight");
            return keytool.execute(cks);
        } catch (JavaToolException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        CtlKeyStore ctl = new CtlKeyStore();
        ctl.createCtlKeyStore();
    }
}
