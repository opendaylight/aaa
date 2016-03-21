/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.utils;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SSLDataUtils {

    public static InstanceIdentifier<KeyStores> getKeystoresIid() {
        return InstanceIdentifier.builder(KeyStores.class).build();
    }

    public static InstanceIdentifier<SslData> getSslDataIid() {
        return InstanceIdentifier.builder(KeyStores.class).build().child(SslData.class);
    }

    public static SslData getSslData(DataBroker dataBroker, String bundleName) {
        SslDataKey sslDataKey = new SslDataKey(bundleName);
        InstanceIdentifier<SslData> sslDataIid = InstanceIdentifier.create(KeyStores.class)
                                                .child(SslData.class, sslDataKey);
        return MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION, sslDataIid);
    }
}
