/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Future;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
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

/**
 * AaaCertRpcServiceImpl Implements the basic RPCs operation that add and retrieve certificates to and from the keystores.
 * These RPCs are accessible only for by the ODL's user who has the admin role and can be disabled. Check the shiro.ini file for more info.
 *
 * @author mserngawy
 *
 */
public class AaaCertRpcServiceImpl implements AaaCertRpcService {

    private static final Logger LOG = LoggerFactory.getLogger(AaaCertRpcServiceImpl.class);

    private final IAaaCertProvider aaaCertProvider;

    public AaaCertRpcServiceImpl(final AaaCertServiceConfig aaaCertServiceConfig, final DataBroker dataBroker, final AAAEncryptionService encryptionSrv) {
        if (aaaCertServiceConfig.isUseConfig()) {
            if (aaaCertServiceConfig.isUseMdsal()) {
                aaaCertProvider = new DefaultMdsalSslData(new AaaCertMdsalProvider(dataBroker, encryptionSrv), aaaCertServiceConfig.getBundleName(),
                        aaaCertServiceConfig.getCtlKeystore(), aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default mdsal SslData as aaaCertProvider");
            } else {
                aaaCertProvider = new AaaCertProvider(aaaCertServiceConfig.getCtlKeystore(), aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default keystore files as aaaCertProvider");
            }
            LOG.info("AaaCert Rpc Service has been initialized");
        } else {
            aaaCertProvider = null;
            LOG.info("AaaCert Rpc Service has not been initialized, change the initial aaa-cert-config data and restart Opendaylight");
        }
    }

    @Override
    public Future<RpcResult<GetNodeCertifcateOutput>> getNodeCertifcate(GetNodeCertifcateInput input) {
        final SettableFuture<RpcResult<GetNodeCertifcateOutput>> futureResult = SettableFuture.create();
        final String cert = aaaCertProvider.getCertificateTrustStore(input.getNodeAlias(), false);
        if (cert != null && !cert.isEmpty()) {
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
    public Future<RpcResult<Void>> setODLCertifcate(SetODLCertifcateInput input) {
        final SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();
        if (aaaCertProvider.addCertificateODLKeyStore(input.getOdlCertAlias(), input.getOdlCert())) {
            futureResult.set(RpcResultBuilder.<Void> success().build());
        } else {
            futureResult.set(RpcResultBuilder.<Void> failed().build());
            LOG.info("Error while adding ODL certificate");
        }
        return futureResult;
    }

    @Override
    public Future<RpcResult<GetODLCertificateOutput>> getODLCertificate() {
        final SettableFuture<RpcResult<GetODLCertificateOutput>> futureResult = SettableFuture.create();
        final String cert = aaaCertProvider.getODLKeyStoreCertificate(false);
        if (cert != null && !cert.isEmpty()) {
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
        final String certReq = aaaCertProvider.genODLKeyStoreCertificateReq(false);
        if (certReq != null && !certReq.isEmpty()) {
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
    public Future<RpcResult<Void>> setNodeCertifcate(SetNodeCertifcateInput input) {
        final SettableFuture<RpcResult<Void>> futureResult = SettableFuture.create();
        if (aaaCertProvider.addCertificateTrustStore(input.getNodeAlias(), input.getNodeCert())) {
            futureResult.set(RpcResultBuilder.<Void> success().build());
        } else {
            futureResult.set(RpcResultBuilder.<Void> failed().build());
            LOG.info("Error while adding the Node certificate");
        }
        return futureResult;
    }
}
