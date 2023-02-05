/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
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
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
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
 */
final class AaaCertRpcServiceImpl implements AaaCertRpcService {
    private static final Logger LOG = LoggerFactory.getLogger(AaaCertRpcServiceImpl.class);

    private final IAaaCertProvider aaaCertProvider;

    AaaCertRpcServiceImpl(final @NonNull IAaaCertProvider aaaCertProvider) {
        this.aaaCertProvider = requireNonNull(aaaCertProvider);
    }

    @Override
    public ListenableFuture<RpcResult<GetNodeCertificateOutput>> getNodeCertificate(
            final GetNodeCertificateInput input) {
        final String cert = aaaCertProvider.getCertificateTrustStore(input.getNodeAlias(), false);
        if (Strings.isNullOrEmpty(cert)) {
            return RpcResultBuilder.<GetNodeCertificateOutput>failed()
                .withRpcError(RpcResultBuilder.newError(ErrorType.APPLICATION, ErrorTag.DATA_MISSING,
                    "getNodeCertificate does not fetch certificate for the alias " + input.getNodeAlias()))
                .buildFuture();
        }

        return RpcResultBuilder.success(new GetNodeCertificateOutputBuilder().setNodeCert(cert).build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<SetODLCertificateOutput>> setODLCertificate(final SetODLCertificateInput input) {
        if (aaaCertProvider.addCertificateODLKeyStore(input.getOdlCertAlias(), input.getOdlCert())) {
            return RpcResultBuilder.success(new SetODLCertificateOutputBuilder().build()).buildFuture();
        }
        LOG.info("Error while adding ODL certificate");
        return RpcResultBuilder.<SetODLCertificateOutput>failed().buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<GetODLCertificateOutput>> getODLCertificate(final GetODLCertificateInput input) {
        final String cert = aaaCertProvider.getODLKeyStoreCertificate(false);
        if (Strings.isNullOrEmpty(cert)) {
            return RpcResultBuilder.<GetODLCertificateOutput>failed().buildFuture();
        }
        return RpcResultBuilder.success(new GetODLCertificateOutputBuilder().setOdlCert(cert).build()).buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<GetODLCertificateReqOutput>> getODLCertificateReq(
            final GetODLCertificateReqInput input) {
        final String certReq = aaaCertProvider.genODLKeyStoreCertificateReq(false);
        if (Strings.isNullOrEmpty(certReq)) {
            return RpcResultBuilder.<GetODLCertificateReqOutput>failed().buildFuture();
        }
        return RpcResultBuilder.success(new GetODLCertificateReqOutputBuilder().setOdlCertReq(certReq).build())
            .buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<SetNodeCertificateOutput>> setNodeCertificate(
           final SetNodeCertificateInput input) {
        if (aaaCertProvider.addCertificateTrustStore(input.getNodeAlias(), input.getNodeCert())) {
            return RpcResultBuilder.success(new SetNodeCertificateOutputBuilder().build()).buildFuture();
        }
        LOG.info("Error while adding the Node certificate");
        return RpcResultBuilder.<SetNodeCertificateOutput>failed().buildFuture();
    }
}
