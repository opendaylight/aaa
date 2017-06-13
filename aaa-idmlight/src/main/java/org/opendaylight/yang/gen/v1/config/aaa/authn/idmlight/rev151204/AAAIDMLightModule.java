package org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAIDMLightModule {

    private static final Logger LOG = LoggerFactory.getLogger(AAAIDMLightModule.class);
    private BundleContext bundleContext = null;
    private static volatile IIDMStore store = null;

    public AAAIDMLightModule(IIDMStore iidmStore) {
        store = iidmStore;
        LOG.info("AAAIDMLight has been initalized ");
    }

    public void initializeStore() {
        try {
            if (store != null) {
                LOG.info("IIDMStore service {} was found", store.getClass());
                new StoreBuilder(store).init();
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
