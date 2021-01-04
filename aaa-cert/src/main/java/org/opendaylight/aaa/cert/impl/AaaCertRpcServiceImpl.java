/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.AaaCertRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertificateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertificateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertificateOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AaaCertRpcServiceImpl Implements the basic RPCs operation that add and
 * retrieve certificates to and from the keystores. These RPCs are accessible
 * only for by the ODL's user who has the admin role and can be disabled. Check
 * the shiro.ini file for more info.
 *
 * @author mserngawy
 *
 */
public class AaaCertRpcServiceImpl implements AaaCertRpcService {

    private static final Logger LOG = LoggerFactory.getLogger(AaaCertRpcServiceImpl.class);

    private final IAaaCertProvider aaaCertProvider;

    public AaaCertRpcServiceImpl(final AaaCertServiceConfig aaaCertServiceConfig, final DataBroker dataBroker,
            final AAAEncryptionService encryptionSrv) {
        if (aaaCertServiceConfig.getUseConfig()) {
            if (aaaCertServiceConfig.getUseMdsal()) {
                aaaCertProvider = new DefaultMdsalSslData(new AaaCertMdsalProvider(dataBroker, encryptionSrv),
                        aaaCertServiceConfig.getBundleName(), aaaCertServiceConfig.getCtlKeystore(),
                        aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default mdsal SslData as aaaCertProvider");
            } else {
                aaaCertProvider = new AaaCertProvider(aaaCertServiceConfig.getCtlKeystore(),
                        aaaCertServiceConfig.getTrustKeystore());
                LOG.debug("Using default keystore files as aaaCertProvider");
            }
            LOG.info("AaaCert Rpc Service has been initialized");
        } else {
            aaaCertProvider = null;
            LOG.info(
                    "AaaCert Rpc Service has not been initialized,"
                    + "change the initial aaa-cert-config data and restart Opendaylight");
        }
    }

    @Override
    public ListenableFuture<RpcResult<GetNodeCertificateOutput>> getNodeCertificate(
           final GetNodeCertificateInput input) {
        final SettableFuture<RpcResult<GetNodeCertificateOutput>> futureResult = SettableFuture.create();
        final String cert = aaaCertProvider.getCertificateTrustStore(input.getNodeAlias(), false);
        if (!Strings.isNullOrEmpty(cert)) {
            final GetNodeCertificateOutput nodeCertOutput = new GetNodeCertificateOutputBuilder().setNodeCert(cert)
                    .build();
            futureResult.set(RpcResultBuilder.success(nodeCertOutput).build());
        } else {
            String errorMsg = "getNodeCertificate does not fetch certificate for the alias " + input.getNodeAlias();
            futureResult.set(RpcResultBuilder.<GetNodeCertificateOutput>failed().withRpcError(RpcResultBuilder
                .newError(RpcError.ErrorType.APPLICATION, "", errorMsg)).build());
        }
        return futureResult;
    }

    @Override
    public ListenableFuture<RpcResult<SetODLCertificateOutput>> setODLCertificate(final SetODLCertificateInput input) {
        final SettableFuture<RpcResult<SetODLCertificateOutput>> futureResult = SettableFuture.create();
        if (aaaCertProvider.addCertificateODLKeyStore(input.getOdlCertAlias(), input.getOdlCert())) {
            futureResult.set(RpcResultBuilder.success(new SetODLCertificateOutputBuilder().build()).build());
        } else {
            futureResult.set(RpcResultBuilder.<SetODLCertificateOutput>failed().build());
            LOG.info("Error while adding ODL certificate");
        }
        return futureResult;
    }

    @Override
    public ListenableFuture<RpcResult<GetODLCertificateOutput>> getODLCertificate(final GetODLCertificateInput input) {
        final SettableFuture<RpcResult<GetODLCertificateOutput>> futureResult = SettableFuture.create();
        final String cert = aaaCertProvider.getODLKeyStoreCertificate(false);
        if (!Strings.isNullOrEmpty(cert)) {
            final GetODLCertificateOutput odlCertOutput = new GetODLCertificateOutputBuilder().setOdlCert(cert).build();
            futureResult.set(RpcResultBuilder.success(odlCertOutput).build());
        } else {
            futureResult.set(RpcResultBuilder.<GetODLCertificateOutput>failed().build());
        }
        return futureResult;
    }

    @Override
    public ListenableFuture<RpcResult<GetODLCertificateReqOutput>> getODLCertificateReq(
            final GetODLCertificateReqInput input) {
        final SettableFuture<RpcResult<GetODLCertificateReqOutput>> futureResult = SettableFuture.create();
        final String certReq = aaaCertProvider.genODLKeyStoreCertificateReq(false);
        if (!Strings.isNullOrEmpty(certReq)) {
            final GetODLCertificateReqOutput odlCertReqOutput = new GetODLCertificateReqOutputBuilder()
                    .setOdlCertReq(certReq).build();
            futureResult.set(RpcResultBuilder.success(odlCertReqOutput).build());
        } else {
            futureResult.set(RpcResultBuilder.<GetODLCertificateReqOutput>failed().build());
        }
        return futureResult;
    }

    @Override
    public ListenableFuture<RpcResult<SetNodeCertificateOutput>> setNodeCertificate(
           final SetNodeCertificateInput input) {
        final SettableFuture<RpcResult<SetNodeCertificateOutput>> futureResult = SettableFuture.create();
        if (aaaCertProvider.addCertificateTrustStore(input.getNodeAlias(), input.getNodeCert())) {
            futureResult.set(RpcResultBuilder.success(new SetNodeCertificateOutputBuilder().build()).build());
        } else {
            futureResult.set(RpcResultBuilder.<SetNodeCertificateOutput>failed().build());
            LOG.info("Error while adding the Node certificate");
        }
        return futureResult;
    }
}
