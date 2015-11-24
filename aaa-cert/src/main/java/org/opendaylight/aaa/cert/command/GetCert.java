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
import org.opendaylight.aaa.cert.AbstractKeyToolCommand;

public class GetCert extends AbstractKeyToolCommand {

    public GetCert() {
        basicArgumnet = KeyStoreUtilis.BasicKeyToolArguments.exportCert;
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyStore, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.storePass, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.alias, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyPass, "");
        arguments.put(KeyStoreUtilis.BooleanKeyToolArguments.rfc, "");
    }

    public void setAlias(String alias) {
        arguments.put(KeyStoreUtilis.KeyToolArguments.alias, alias);
    }

    public void setKeyStore(String keystore) {
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyStore, keystore);
    }

    public void setStorePass(String storePass) {
        arguments.put(KeyStoreUtilis.KeyToolArguments.storePass, storePass);
    }

    public void setKeyPass(String keyPass) {
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyPass, keyPass);
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