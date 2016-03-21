/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126;

import org.opendaylight.aaa.cert.impl.AaaCertProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 * AaaCertProviderModule create and intialize the AaaCertProvider services
 */
public class AaaCertProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AbstractAaaCertProviderModule {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertProviderModule.class);

    public AaaCertProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier, final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AaaCertProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier, final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, final org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertProviderModule oldModule, final java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public AutoCloseable createInstance() {
        final CtlKeystore ctlKeyStore = this.getCtlKeystore();
        final TrustKeystore trust = this.getTrustKeystore();
        final AaaCertProvider aaaCertProvider = new AaaCertProvider(ctlKeyStore, trust);
        if (this.getUseConfig() && !KeyStoreConstant.checkKeyStoreFile(ctlKeyStore.getName())) {
            LOG.info("Creating keystore based on given configuration");
            aaaCertProvider.createODLKeyStore();
            aaaCertProvider.createTrustKeyStore();
        }

        getBrokerDependency().registerProvider(aaaCertProvider);
        return aaaCertProvider;
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

}
