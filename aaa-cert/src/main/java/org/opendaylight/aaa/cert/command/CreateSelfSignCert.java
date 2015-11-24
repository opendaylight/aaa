/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.command;

import org.apache.maven.shared.utils.cli.StreamConsumer;
import org.opendaylight.aaa.cert.KeyStoreUtilis;
import org.opendaylight.aaa.cert.abstractKeyToolCommand;

public class CreateSelfSignCert extends abstractKeyToolCommand {

    public CreateSelfSignCert() {
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyAlg, "RSA");
        arguments.put(KeyStoreUtilis.KeyToolArguments.alias, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyStore, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.dName, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.storePass, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyPass, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.keySize, "2048");
        arguments.put(KeyStoreUtilis.KeyToolArguments.validity, "360");
    }

    @Override
    public StreamConsumer getSystemOutStreamConsumer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StreamConsumer getSystemErrorStreamConsumer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSystemOutStreamConsumer(StreamConsumer systemOutStreamConsumer) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setSystemErrorStreamConsumer(StreamConsumer systemErrorStreamConsumer) {
        // TODO Auto-generated method stub
    }
}