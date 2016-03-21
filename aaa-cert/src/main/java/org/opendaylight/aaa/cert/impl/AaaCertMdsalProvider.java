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
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddOdlSignedCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddOdlSignedCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.CreateSslDataInput;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements AutoCloseable, BindingAwareProvider, IAaaCertMdsalProvider {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);

    public AaaCertMdsalProvider() {
        LOG.info("AaaCertMdsalProvider Initialized");
    }

    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public Future<RpcResult<GetSslDataOutput>> getSslData(GetSslDataInput input) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
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
