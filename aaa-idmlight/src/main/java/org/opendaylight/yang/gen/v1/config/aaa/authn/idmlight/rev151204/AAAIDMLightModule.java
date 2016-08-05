package org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.idm.StoreBuilder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//public class AAAIDMLightModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AbstractAAAIDMLightModule {
public class AAAIDMLightModule {

    private static final Logger LOG = LoggerFactory.getLogger(AAAIDMLightModule.class);
    private BundleContext bundleContext = null;
    private static volatile IIDMStore store = null;

    /*public AAAIDMLightModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAIDMLightModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }*/

    public AAAIDMLightModule(IIDMStore iidmStore) {
        store = iidmStore;
        LOG.info("AAAIDMLight has been initalized ");
    }
    // @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    /* @Override
    public java.lang.AutoCloseable createInstance() {
        final WaitingServiceTracker<IIDMStore> tracker = WaitingServiceTracker.create(IIDMStore.class, bundleContext);
        store = tracker.waitForService(WaitingServiceTracker.FIVE_MINUTES);
        LOG.info("IIDMStore service {} was found", store.getClass());
        try {
            StoreBuilder.init(store);
        } catch (IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }

        return NoopAutoCloseable.INSTANCE;
    }*/

    public void initializeStore() {
        try {
            if (store != null) {
                LOG.info("IIDMStore service {} was found", store.getClass());
                StoreBuilder.init(store);
            }
        } catch (IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }
    }

    public void setBundleContext(BundleContext b){
        this.bundleContext = b;
    }

    public static final IIDMStore getStore(){
        return store;
    }

    public static final void setStore(IIDMStore s){
        store = s;
    }
}
