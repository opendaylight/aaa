/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.ctlkeystore.CipherSuites;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

/**
 * AaaCertProvider use to manage the certificates manipulation operations add, revoke and update
 *
 * @author mserngawy
 *
 */
public class AaaCertProvider implements IAaaCertProvider, AaaCertRpcService {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertProvider.class);
    private CtlKeystore ctlKeyStore;
    private final ODLKeyTool odlKeyTool;
    private TrustKeystore trustKeyStore;

    public AaaCertProvider(final AaaCertServiceConfig aaaCertServiceConfig) {
        odlKeyTool = new ODLKeyTool();
        this.ctlKeyStore = aaaCertServiceConfig.getCtlKeystore();
        this.trustKeyStore = aaaCertServiceConfig.getTrustKeystore();
        if (aaaCertServiceConfig.isUseConfig() && !KeyStoreConstant.checkKeyStoreFile(ctlKeyStore.getName())) {
            LOG.info("Creating keystore based on given configuration");
            this.createODLKeyStore();
            this.createTrustKeyStore();
        }
        LOG.info("aaa Certificate Service Initalized");
    }

    public TrustKeystore getTrustKeyStoreInfo() {
        return trustKeyStore;
    }

    public CtlKeystore getOdlKeyStoreInfo() {
        return ctlKeyStore;
    }

    @Override
    public boolean addCertificateODLKeyStore(final String storePasswd, final String alias, final String certificate) {
        return odlKeyTool.addCertificate(ctlKeyStore.getName(), storePasswd, certificate, alias);
    }

    public boolean addCertificateODLKeyStore(final String alias, final String certificate) {
        return addCertificateODLKeyStore(ctlKeyStore.getStorePassword(), alias, certificate);
    }

    @Override
    public boolean addCertificateTrustStore(final String storePasswd, final String alias, final String certificate) {
        return odlKeyTool.addCertificate(trustKeyStore.getName(), storePasswd, certificate, alias);
    }

    public boolean addCertificateTrustStore(final String alias, final String certificate) {
        return addCertificateTrustStore(trustKeyStore.getStorePassword(), alias, certificate);
    }

    public void createODLKeyStore() {
        createODLKeyStore(ctlKeyStore.getName(),ctlKeyStore.getStorePassword(), ctlKeyStore.getAlias(),
                  ctlKeyStore.getDname(), ctlKeyStore.getValidity());
    }

    @Override
    public String createODLKeyStore(final String keyStore, final String storePasswd, final String alias,
            final String dName, final int validity) {
        ctlKeyStore = new CtlKeystoreBuilder().setAlias(alias)
                                              .setDname(dName)
                                              .setName(keyStore)
                                              .setStorePassword(storePasswd)
                                              .setValidity(validity)
                                              .build();
        if(odlKeyTool.createKeyStoreWithSelfSignCert(keyStore, storePasswd, dName, alias, validity)) {
            return keyStore + " Keystore created.";
        } else {
            return "Failed to create keystore " + keyStore;
        }
    }

    public void createTrustKeyStore() {
        odlKeyTool.createKeyStoreImportCert(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                trustKeyStore.getCertFile(), trustKeyStore.getAlias());
    }

    @Override
    public String createTrustKeyStore(final String keyStore, final String storePasswd, final String alias) {
        trustKeyStore = new TrustKeystoreBuilder().setAlias(alias)
                                                  .setName(keyStore)
                                                  .setStorePassword(storePasswd)
                                                  .build();
        if(odlKeyTool.createKeyStoreImportCert(keyStore, storePasswd, trustKeyStore.getCertFile(), alias)) {
            return keyStore + " Keystore created.";
        } else {
            return "Failed to create keystore " + keyStore;
        }
    }

    @Override
    public String genODLKeyStorCertificateReq(final String storePasswd, final String alias) {
        return odlKeyTool.generateCertificateReq(ctlKeyStore.getName(), storePasswd,
                     alias, KeyStoreConstant.DEFAULT_SIGN_ALG, true);
    }

    public String genODLKeyStorCertificateReq(final String alias) {
        return genODLKeyStorCertificateReq(ctlKeyStore.getStorePassword(), alias);
    }

    @Override
    public String getCertificateTrustStore(final String storePasswd, final String aliase) {
        return odlKeyTool.getCertificate(trustKeyStore.getName(), storePasswd, aliase, true);
    }

    public String getCertificateTrustStore(final String aliase) {
        return getCertificateTrustStore(trustKeyStore.getStorePassword(), aliase);
    }

    @Override
    public Future<RpcResult<GetNodeCertifcateOutput>> getNodeCertifcate(final GetNodeCertifcateInput input) {
        final SettableFuture<RpcResult<GetNodeCertifcateOutput>> futureResult = SettableFuture.create();
        final String cert = odlKeyTool.getCertificate(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                                 input.getNodeAlias(), true);
        if (cert != null) {
            final GetNodeCertifcateOutput nodeCertOutput = new GetNodeCertifcateOutputBuilder()
                                                        .setNodeCert(cert)
                                                        .build();
            futureResult.set(RpcResultBuilder.<GetNodeCertifcateOutput> success(nodeCertOutput).build());
        } else {
            futureResult.set(RpcResultBuilder.<GetNodeCertifcateOutput> failed().build());
        }
        return futureResult;
    }

    @Override
    public Future<RpcResult<GetODLCertificateOutput>> getODLCertificate() {
        final SettableFuture<RpcResult<GetODLCertificateOutput>> futureResult = SettableFuture.create();
        final String cert = odlKeyTool.getCertificate(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                                 ctlKeyStore.getAlias(), true);
        if (cert != null) {
            final GetODLCertificateOutput odlCertOutput = new GetODLCertificateOutputBuilder()
                                                        .setOdlCert(cert)
                                                        .build();
            futureResult.set(RpcResultBuilder.<GetODLCertificateOutput> success(odlCertOutput).build());
        } else {
            futureResult.set(RpcResultBuilder.<GetODLCertificateOutput> failed().build());
        }
        return futureResult;
    }

    @Override
    public Future<RpcResult<GetODLCertificateReqOutput>> getODLCertificateReq() {
        final SettableFuture<RpcResult<GetODLCertificateReqOutput>> futureResult = SettableFuture.create();
        final String certReq = odlKeyTool.generateCertificateReq(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                                 ctlKeyStore.getAlias(), KeyStoreConstant.DEFAULT_SIGN_ALG, true);
        if (certReq != null) {
            final GetODLCertificateReqOutput odlCertReqOutput = new GetODLCertificateReqOutputBuilder()
                                                        .setOdlCertReq(certReq)
                                                        .build();
            futureResult.set(RpcResultBuilder.<GetODLCertificateReqOutput> success(odlCertReqOutput).build());
        } else {
            futureResult.set(RpcResultBuilder.<GetODLCertificateReqOutput> failed().build());
        }
        return futureResult;
    }

    @Override
    public String getODLKeyStorCertificate(final String storePasswd, final String alias) {
        return odlKeyTool.getCertificate(ctlKeyStore.getName(), storePasswd, alias, true);
    }

    @Override
    public KeyStore getODLKeyStore() {
        return odlKeyTool.getKeyStore(ctlKeyStore.getName(), ctlKeyStore.getStorePassword());
    }

    @Override
    public KeyStore getTrustKeyStore() {
        return odlKeyTool.getKeyStore(trustKeyStore.getName(), trustKeyStore.getStorePassword());
    }

    @Override
    public String[] getCipherSuites() {
        List<String> suites = new ArrayList<>();
        if (ctlKeyStore.getCipherSuites() != null && !ctlKeyStore.getCipherSuites().isEmpty()) {
            for (CipherSuites cipherSuite : ctlKeyStore.getCipherSuites()) {
                suites.add(cipherSuite.getSuiteName());
            }
        }
        return (String[]) suites.toArray();
    }

    @Override
    public Future<RpcResult<Void>> setNodeCertifcate(final SetNodeCertifcateInput input) {
        LOG.info("{} Certificate will be added to the trust keystore.", input.getNodeAlias());
        final SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();
        if (odlKeyTool.addCertificate(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                input.getNodeCert(), input.getNodeAlias())) {
            futureResult.set(RpcResultBuilder.<Void> success().build());
        } else {
            futureResult.set(RpcResultBuilder.<Void> failed().build());
        }
        return futureResult;
    }

    @Override
    public Future<RpcResult<Void>> setODLCertifcate(final SetODLCertifcateInput input) {
        LOG.info("Certificate will be add to ODL keystore.");
        final SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();
        //adding ca to the alias of signed certificate by Certificate Authority.
        //can not have 2 certifciate under the same alias.
        ctlKeyStore = new CtlKeystoreBuilder(ctlKeyStore).setAlias("ca" + ctlKeyStore.getAlias()).build();
        if (odlKeyTool.addCertificate(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                input.getOdlCert(), ctlKeyStore.getAlias())) {
            futureResult.set(RpcResultBuilder.<Void> success().build());
        } else {
            futureResult.set(RpcResultBuilder.<Void> failed().build());
        }
        return futureResult;
    }
}