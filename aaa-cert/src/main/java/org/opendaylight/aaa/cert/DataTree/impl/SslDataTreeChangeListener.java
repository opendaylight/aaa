/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.DataTree.impl;

import org.opendaylight.aaa.cert.api.AaaCertDataTreeChangeListener;
import org.opendaylight.aaa.cert.api.IAaaCertMdsalProvider;
import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslDataTreeChangeListener extends AaaCertDataTreeChangeListener<SslData> {
    private static final Logger LOG = LoggerFactory.getLogger(SslDataTreeChangeListener.class);
    private final BundleContext context;
    private final ListenerRegistration<SslDataTreeChangeListener> listener;

    public SslDataTreeChangeListener(DataBroker dataBroker) {
        super(dataBroker);
        this.context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        final InstanceIdentifier<SslData> sslDataIid = KeyStoresDataUtils.getSslDataIid();
        final DataTreeIdentifier<SslData> dataTreeIid =
                new DataTreeIdentifier<>(LogicalDatastoreType.CONFIGURATION, sslDataIid);
        listener = dataBroker.registerDataTreeChangeListener(dataTreeIid, this);
        LOG.info("ssl data tree registered");
    }

    @Override
    public void close() throws Exception {
        if (listener != null){
            listener.close();
        }
    }

    @Override
    public void add(DataTreeModification<SslData> newDataObject) {
        LOG.info("SSL data added ", newDataObject.getRootNode().getDataAfter());
        SslData sslData = newDataObject.getRootNode().getDataAfter();
        ServiceReference<?> serviceReference = context.getServiceReference(IAaaCertMdsalProvider.class);
        if (serviceReference != null) {
           AaaCertMdsalProvider certProvider = (AaaCertMdsalProvider) context.getService(serviceReference);
        }
        else
        	LOG.info("serviceReference is null");
    }

    @Override
    public void remove(DataTreeModification<SslData> removedDataObject) {
        // TODO Auto-generated method stub
    }

    @Override
    public void update(DataTreeModification<SslData> modifiedDataObject) {
        // TODO Auto-generated method stub
    }

}
