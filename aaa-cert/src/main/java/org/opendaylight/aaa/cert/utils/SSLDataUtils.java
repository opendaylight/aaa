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
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.SslData;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SSLDataUtils {

    /*public static InstanceIdentifier<SslData> getSslDataIid() {
        return InstanceIdentifier.create(SslData.class);
    }

    public static SslData getSslData(DataBroker dataBroker, String bundleName) {
        InstanceIdentifier<SslData> sslDataIid = getSslDataIid();
        return MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION, sslDataIid);
    }*/
}
