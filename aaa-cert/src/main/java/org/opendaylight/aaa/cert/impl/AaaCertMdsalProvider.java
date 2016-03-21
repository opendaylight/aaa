/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;
import java.util.concurrent.Future;

import org.opendaylight.aaa.cert.api.IAaaCertMdsalProvider;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.aaa.cert.utils.SSLDataUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddOdlSignedCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddOdlSignedCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.CreateSslDataInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.CreateSslDataInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.CreateSslDataOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.DeleteSslDataInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.DeleteSslDataOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetOdlCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetOdlCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetOdlCertificateRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetOdlCertificateRequestOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetSslDataInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetSslDataOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.ImportSslDataInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.ImportSslDataOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.UpdateSslDataInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.UpdateSslDataOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStoresBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements AutoCloseable, BindingAwareProvider, IAaaCertMdsalProvider {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);
    private ServiceRegistration<IAaaCertMdsalProvider> aaaCertMdsalServiceRegisteration;
    private DataBroker dataBroker;

    public AaaCertMdsalProvider() {
        LOG.info("AaaCertMdsalProvider Initialized");
    }

    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        LOG.info("Aaa Certificate Mdsal Service Session Initiated");
        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        aaaCertMdsalServiceRegisteration = context.registerService(IAaaCertMdsalProvider.class, this, null);

        // Retrieve the data broker to create transactions
        dataBroker =  arg0.getSALService(DataBroker.class);
        KeyStores keyStoreData = new KeyStoresBuilder().setId("KeyStores:1").build();
        MdsalUtils.initalizeDatastore(LogicalDatastoreType.CONFIGURATION, dataBroker, SSLDataUtils.getKeystoresIid(), keyStoreData);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Aaa Certificate Mdsal Service Closed");
        aaaCertMdsalServiceRegisteration.unregister();
    }

    @Override
    public Future<RpcResult<GetSslDataOutput>> getSslData(GetSslDataInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<ImportSslDataOutput>> importSslData(ImportSslDataInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<AddNodeCertificateOutput>> addNodeCertificate(AddNodeCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetOdlCertificateRequestOutput>> getOdlCertificateRequest(
            GetOdlCertificateRequestInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetNodeCertificateOutput>> getNodeCertificate(GetNodeCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<CreateSslDataOutput>> createSslData(CreateSslDataInput input) {
        return null;
    }

    @Override
    public Future<RpcResult<UpdateSslDataOutput>> updateSslData(UpdateSslDataInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<DeleteSslDataOutput>> deleteSslData(DeleteSslDataInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<AddOdlSignedCertificateOutput>> addOdlSignedCertificate(
            AddOdlSignedCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<RpcResult<GetOdlCertificateOutput>> getOdlCertificate(GetOdlCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStore getODLKeyStore(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyStore getTrustKeyStore(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getCipherSuites(String bundleName) {
        // TODO Auto-generated method stub
        return null;
    }

}
