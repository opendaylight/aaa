/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddSignedCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetCertificateRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetCertificateRequestOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements AutoCloseable, BindingAwareProvider, AaaCertMdsalRpcService{

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);

    public AaaCertMdsalProvider() {
        LOG.info("Initialized AaaCertMdsalProvider");
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService#getCertificateRequest(org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetCertificateRequestInput)
     */
    @Override
    public Future<RpcResult<GetCertificateRequestOutput>> getCertificateRequest(GetCertificateRequestInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService#addSignedCertificate(org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddSignedCertificateInput)
     */
    @Override
    public Future<RpcResult<Void>> addSignedCertificate(AddSignedCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService#getCertificate(org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.GetCertificateInput)
     */
    @Override
    public Future<RpcResult<GetCertificateOutput>> getCertificate(GetCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService#addCertificate(org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AddCertificateInput)
     */
    @Override
    public Future<RpcResult<Void>> addCertificate(AddCertificateInput input) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.controller.sal.binding.api.BindingAwareProvider#onSessionInitiated(org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext)
     */
    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

}
