/*
 * Copyright (c) 2016 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.config.aaa.authn.hsqldb.store.rev160318;

import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.hsqldb.persistence.HSQLDBStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAHSQLDBStoreModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.hsqldb.store.rev160318.AbstractAAAHSQLDBStoreModule {
    private BundleContext bundleContext;
    private static final Logger LOG = LoggerFactory.getLogger(AAAHSQLDBStoreModule.class);

    public AAAHSQLDBStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAHSQLDBStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.hsqldb.store.rev160318.AAAHSQLDBStoreModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final HSQLDBStore HSQLDBStore = new HSQLDBStore();
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(IIDMStore.class.getName(), HSQLDBStore, null);
        LOG.info("AAA HSQLDB Store Initialized");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                serviceRegistration.unregister();
            }
        };
    }

    /**
     * @param bundleContext
     */
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * @return the bundleContext
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }

}
