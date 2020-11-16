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
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true)
public class OSGiAaaCertRpcServiceProvider {

    @Reference
    DataBroker dataBroker;

    @Reference
    AAAEncryptionService encryptionService;

    @Reference
    RpcProviderService rpcService;

    private ObjectRegistration<AaaCertRpcService> registration = null;

    private volatile AaaCertRpcService delegate;

    private AaaCertServiceConfig config;

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
    void bindConfig(final AaaCertServiceConfig certServiceConfig) {
        updatedConfig(certServiceConfig);
    }

    void unbindConfig(final AaaCertServiceConfig certServiceConfig) {
        delegate = null;
        registration.close();
        registration = null;
    }

    void updatedConfig(final AaaCertServiceConfig aaaCertServiceConfig) {
        if (dataBroker != null && encryptionService != null && rpcService != null) {
            delegate = new AaaCertRpcServiceImpl(aaaCertServiceConfig, dataBroker, encryptionService);
            registration = rpcService.registerRpcImplementation(AaaCertRpcService.class, delegate);
        } else {
            config = aaaCertServiceConfig;
        }
    }

    @Activate
    void init() {
        if (delegate == null && config != null) {
            delegate = new AaaCertRpcServiceImpl(config, dataBroker, encryptionService);
            registration = rpcService.registerRpcImplementation(AaaCertRpcService.class, delegate);

            config = null;
        }
    }

    @Deactivate
    void close() {
        registration.close();
    }
}
