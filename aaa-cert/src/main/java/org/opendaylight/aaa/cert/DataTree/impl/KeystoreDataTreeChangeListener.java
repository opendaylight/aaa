/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.DataTree.impl;

import org.opendaylight.aaa.cert.api.AaaCertDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.Keystore;

public class KeystoreDataTreeChangeListener extends AaaCertDataTreeChangeListener<Keystore> {

    public KeystoreDataTreeChangeListener(DataBroker dataBroker) {
        super(dataBroker);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void add(DataTreeModification<Keystore> newDataObject) {
        // TODO Auto-generated method stub
    }

    @Override
    public void remove(DataTreeModification<Keystore> removedDataObject) {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(DataTreeModification<Keystore> modifiedDataObject) {
        // TODO Auto-generated method stub
    }

}
