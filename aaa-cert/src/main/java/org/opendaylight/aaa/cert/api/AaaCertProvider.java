/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;

import java.security.KeyStore;
import java.util.concurrent.Future;

import org.opendaylight.aaa.cert.impl.KeyStoreUtilis;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.AaaCertRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertifcateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertifcateInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class AaaCertProvider implements AutoCloseable, IAaaCertProvider, BindingAwareProvider, AaaCertRpcService {

    private final Logger LOG = LoggerFactory.getLogger(AaaCertProvider.class);
    private ServiceRegistration<IAaaCertProvider> aaaCertServiceRegisteration;
    private ServiceRegistration<AaaCertRpcService> aaaCertRpcServiceRegisteration;
    private ODLKeyTool odlKeyTool;
    private CtlKeystore ctlKeyStore;
    private TrustKeystore trustKeyStore;

    public AaaCertProvider(CtlKeystore ctlKeyStore, TrustKeystore trustKeyStore) {
        LOG.info("aaa Certificate Service Initalized");
        odlKeyTool = new ODLKeyTool();
        this.ctlKeyStore = ctlKeyStore;
        this.trustKeyStore = trustKeyStore;
    }

    public void createTrustKeyStore() {
        odlKeyTool.createKeyStoreImportCert(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                trustKeyStore.getCertFile(), trustKeyStore.getAlias());
    }

    public void createODLKeyStore() {
        createODLKeyStore(ctlKeyStore.getName(),ctlKeyStore.getStorePassword(), ctlKeyStore.getAlias(),
                  ctlKeyStore.getDname(), ctlKeyStore.getValidity());
    }

    // BindingAwareProvider implementation
    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("aaa Certificate Service Session Initiated");
        // Register the aaa OSGi
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        aaaCertServiceRegisteration = context.registerService(IAaaCertProvider.class, this, null);
        aaaCertRpcServiceRegisteration = context.registerService(AaaCertRpcService.class, this, null);
    }

    @Override
    public void close() throws Exception {
        LOG.info("aaa Certificate Service Closed");
        aaaCertServiceRegisteration.unregister();
        aaaCertRpcServiceRegisteration.unregister();
    }

    // IAaaCertProvider implementation
    @Override
    public String createTrustKeyStore(String keyStore, String storePasswd, String alias) {
        trustKeyStore.setAlias(alias);
        trustKeyStore.setName(keyStore);
        trustKeyStore.setStorePassword(storePasswd);
        if(odlKeyTool.createKeyStoreImportCert(keyStore, storePasswd, trustKeyStore.getCertFile(), alias))
            return keyStore + " Keystore created.";
        else
            return "Failed to create keystore " + keyStore;
    }

    @Override
    public String createODLKeyStore(String keyStore, String storePasswd, String alias,
            String dName, int validity) {
        ctlKeyStore.setAlias(alias);
        ctlKeyStore.setDname(dName);
        ctlKeyStore.setName(keyStore);
        ctlKeyStore.setStorePassword(storePasswd);
        ctlKeyStore.setValidity(validity);
        if(odlKeyTool.createKeyStoreWithSelfSignCert(keyStore, storePasswd, dName, alias, validity))
            return keyStore + " Keystore created.";
        else
            return "Failed to create keystore " + keyStore;
    }

    @Override
    public String getODLKeyStorCertificate(String storePasswd, String alias) {
        return odlKeyTool.getCertificate(ctlKeyStore.getName(), storePasswd, alias, true);
    }

    @Override
    public String genODLKeyStorCertificateReq(String storePasswd, String alias) {
        return odlKeyTool.generateCertificateReq(ctlKeyStore.getName(), storePasswd,
                     alias,KeyStoreUtilis.defaultSignAlg, true);
    }

    @Override
    public boolean addCertificateTrustStore(String storePasswd, String alias, String certificate) {
        return odlKeyTool.addCertificate(trustKeyStore.getName(), storePasswd, certificate, alias);
    }

    @Override
    public String getCertificateTrustStore(String storePasswd, String aliase) {
        return odlKeyTool.getCertificate(trustKeyStore.getName(), storePasswd, aliase, true);
    }

    @Override
    public boolean addCertificateODLKeyStore(String storePasswd, String alias, String certificate) {
        return odlKeyTool.addCertificate(ctlKeyStore.getName(), storePasswd, certificate, alias);
    }


    @Override
    public KeyStore getTrustKeyStore() {
        return odlKeyTool.getKeyStore(trustKeyStore.getName(), trustKeyStore.getStorePassword());
    }

    @Override
    public KeyStore getODLKeyStore() {
        return odlKeyTool.getKeyStore(ctlKeyStore.getName(), ctlKeyStore.getStorePassword());
    }

    // AaaCertRpcService RPC implementation
    @Override
    public Future<RpcResult<Void>> setODLCertifcate(SetODLCertifcateInput input) {
        LOG.info(input.getOdlCert() + " Certificate will be add to ODL keystore.");
        final SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();
        if (odlKeyTool.addCertificate(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                input.getOdlCert(), ctlKeyStore.getAlias())) {
            futureResult.set(RpcResultBuilder.<Void> success().build());
        }
        else
            futureResult.set(RpcResultBuilder.<Void> failed().build());
        return futureResult;
    }

    @Override
    public Future<RpcResult<GetODLCertificateOutput>> getODLCertificate() {
        final SettableFuture<RpcResult<GetODLCertificateOutput>> futureResult = SettableFuture.create();
        String cert = odlKeyTool.getCertificate(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                                 ctlKeyStore.getAlias(), true);
        if (cert != null) {
            GetODLCertificateOutput odlCertOutput = new GetODLCertificateOutputBuilder()
                                                        .setOdlCert(cert)
                                                        .build();
            futureResult.set(RpcResultBuilder.<GetODLCertificateOutput> success(odlCertOutput).build());
        }
        else
            futureResult.set(RpcResultBuilder.<GetODLCertificateOutput> failed().build());
        return futureResult;
    }

    @Override
    public Future<RpcResult<GetNodeCertifcateOutput>> getNodeCertifcate(GetNodeCertifcateInput input) {
        final SettableFuture<RpcResult<GetNodeCertifcateOutput>> futureResult = SettableFuture.create();
        String cert = odlKeyTool.getCertificate(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                                 input.getNodeAlias(), true);
        if (cert != null) {
            GetNodeCertifcateOutput nodeCertOutput = new GetNodeCertifcateOutputBuilder()
                                                        .setNodeCert(cert)
                                                        .build();
            futureResult.set(RpcResultBuilder.<GetNodeCertifcateOutput> success(nodeCertOutput).build());
        }
        else
            futureResult.set(RpcResultBuilder.<GetNodeCertifcateOutput> failed().build());
        return futureResult;
    }

    @Override
    public Future<RpcResult<GetODLCertificateReqOutput>> getODLCertificateReq() {
        final SettableFuture<RpcResult<GetODLCertificateReqOutput>> futureResult = SettableFuture.create();
        String certReq = odlKeyTool.generateCertificateReq(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                                 ctlKeyStore.getAlias(), KeyStoreUtilis.defaultSignAlg, true);
        if (certReq != null) {
            GetODLCertificateReqOutput odlCertReqOutput = new GetODLCertificateReqOutputBuilder()
                                                        .setOdlCertReq(certReq)
                                                        .build();
            futureResult.set(RpcResultBuilder.<GetODLCertificateReqOutput> success(odlCertReqOutput).build());
        }
        else
            futureResult.set(RpcResultBuilder.<GetODLCertificateReqOutput> failed().build());
        return futureResult;
    }

    @Override
    public Future<RpcResult<Void>> setNodeCertifcate(SetNodeCertifcateInput input) {
        LOG.info(input.getNodeAlias() + " Certificate will be add to trust keystore.");
        final SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();
        if (odlKeyTool.addCertificate(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                input.getNodeCert(), input.getNodeAlias())) {
            futureResult.set(RpcResultBuilder.<Void> success().build());
        }
        else
            futureResult.set(RpcResultBuilder.<Void> failed().build());
        return futureResult;
    }
}
