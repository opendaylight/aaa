package org.opendaylight.yang.gen.v1.config.aaa.authn.h2.store.rev151128;

import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.h2.persistence.H2Store;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAH2StoreModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.h2.store.rev151128.AbstractAAAH2StoreModule {

    private BundleContext bundleContext;
    private static final Logger LOG = LoggerFactory.getLogger(AAAH2StoreModule.class);

    public AAAH2StoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAH2StoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.h2.store.rev151128.AAAH2StoreModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final H2Store h2Store = new H2Store();
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(IIDMStore.class.getName(), h2Store, null);
        LOG.info("AAA H2 Store Initialized");
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
