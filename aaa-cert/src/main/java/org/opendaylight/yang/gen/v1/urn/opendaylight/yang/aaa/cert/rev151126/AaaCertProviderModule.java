/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126;

import java.util.List;

import org.opendaylight.aaa.cert.api.AaaCertProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreUtilis;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaaCertProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AbstractAaaCertProviderModule {

    private final Logger LOG = LoggerFactory.getLogger(AaaCertProviderModule.class);

    public AaaCertProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AaaCertProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public AutoCloseable createInstance() {
        /*List<SwitchConnectionProvider> listSwitchConnectionProvider = this.getOpenflowSwitchConnectionDependency();
        for (SwitchConnectionProvider switchConnProvide : listSwitchConnectionProvider) {
             if (switchConnProvide.getConfiguration() != null) {
                 if (switchConnProvide.getConfiguration().getTlsConfiguration() != null) {
                     TlsConfiguration tlsConfig = switchConnProvide.getConfiguration().getTlsConfiguration();
                     // needed to retrive the ctl.jks info and create the keystore
                 }
                 else {
                     ConnectionConfiguration connConfig = switchConnProvide.getConfiguration();
                     // her will implement the ConnectionConfiguration to set the TlsConfiguration
                     // and then restart the connection
                     switchConnProvide.shutdown();
                     switchConnProvide.setConfiguration(connConfig);
                     switchConnProvide.startup();
                 }
             }
        }*/
        CtlKeystore ctlKeyStore = this.getCtlKeystore();
        final AaaCertProvider aaaCertProvider = new AaaCertProvider(ctlKeyStore, this.getTrustKeystore());
        if (this.getUseConfig() && !KeyStoreUtilis.checkKeyStoreFile(ctlKeyStore.getName())) {
            LOG.info("Creating keystore based on given configuration");
            aaaCertProvider.createODLKeyStore();
            aaaCertProvider.createTrustKeyStore();
        }
        getBrokerDependency().registerProvider(aaaCertProvider);
        return aaaCertProvider;
    }

}
