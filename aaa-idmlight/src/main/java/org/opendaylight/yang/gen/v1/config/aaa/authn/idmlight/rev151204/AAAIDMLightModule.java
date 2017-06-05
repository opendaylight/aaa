package org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activates h2 based data store for AAA through Aries Blueprint.
 */
public class AAAIDMLightModule {

    private static final Logger LOG = LoggerFactory.getLogger(AAAIDMLightModule.class);

    private static volatile IIDMStore store = null;

    public AAAIDMLightModule(final IIDMStore iidmStore) {
        store = iidmStore;
        LOG.info("AAAIDMLight initialized with class of type {}", store.getClass().getName());
    }

    /**
     * Called by blueprint to for store initialization (i.e., add initial IDM data).
     */
    public void initializeStore() {
        try {
            if (store != null) {
                LOG.info("IIDMStore service {} was found", store.getClass().getName());
                new StoreBuilder(store).init();
            }
        } catch (final IDMStoreException e) {
            LOG.error("Failed to initialize data in store", e);
        }
    }

    public static final IIDMStore getStore(){
        return store;
    }

    public static final void setStore(final IIDMStore s){
        store = s;
    }
}
