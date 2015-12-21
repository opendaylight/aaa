/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */

package org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031;

import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.authn.mdsal.store.AuthNStore;
import org.opendaylight.aaa.authn.mdsal.store.IDMMDSALStore;
import org.opendaylight.aaa.authn.mdsal.store.IDMStore;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class AuthNStoreModule
        extends
        org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.AbstractAuthNStoreModule {
    private BundleContext bundleContext;

    public AuthNStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AuthNStoreModule(
            org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.AuthNStoreModule oldModule,
            java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        DataBroker dataBrokerService = getDataBrokerDependency();
        final AuthNStore authNStore = new AuthNStore(dataBrokerService, getPassword());
        final IDMMDSALStore mdsalStore = new IDMMDSALStore(dataBrokerService);
        final IDMStore idmStore = new IDMStore(mdsalStore);

        authNStore.setTimeToLive(getTimeToLive());

        // Register the MD-SAL Token store with OSGI
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(
                TokenStore.class.getName(), authNStore, null);
        final ServiceRegistration<?> idmServiceRegistration = bundleContext.registerService(
                IIDMStore.class.getName(), idmStore, null);
        final class AutoCloseableStore implements AutoCloseable {

            @Override
            public void close() throws Exception {
                serviceRegistration.unregister();
                idmServiceRegistration.unregister();
                authNStore.close();
            }
        }

        return new AutoCloseableStore();

        // return authNStore;

        // throw new java.lang.UnsupportedOperationException();
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
