/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.AaaCertRpcService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OSGiAaaCertRpcServiceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiAaaCertRpcServiceProvider.class);
    private final DataBroker dataBroker;
    private final AAAEncryptionService encryptionService;
    private final RpcProviderService rpcService;

    private volatile AaaCertRpcService delegate;

    private ObjectRegistration<AaaCertRpcService> registration;

    @Activate
    public OSGiAaaCertRpcServiceProvider(@Reference final DataBroker dataBroker,
            @Reference final AAAEncryptionService encryptionService, @Reference final RpcProviderService rpcService) {
        this.dataBroker = dataBroker;
        this.encryptionService = encryptionService;
        this.rpcService = rpcService;
    }

    @Reference
    void init(final AaaCertServiceConfig certServiceConfig) {
        if (dataBroker != null && encryptionService != null && rpcService != null) {
            delegate = new AaaCertRpcServiceImpl(certServiceConfig, dataBroker, encryptionService);
            if (registration != null) {
                registration.close();
            }
            registration = rpcService.registerRpcImplementation(AaaCertRpcService.class, delegate);
            LOG.info("AaaCertRpcService was successfully registered to RpcService");
        } else {
            LOG.info("AaaCertRpcService was not registered!");
        }
    }

    @Deactivate
    void close() {
        if (registration != null) {
            registration.close();
            LOG.info("AaaCertRpcService was successfully unregistered from RpcService");
        }
    }
}
