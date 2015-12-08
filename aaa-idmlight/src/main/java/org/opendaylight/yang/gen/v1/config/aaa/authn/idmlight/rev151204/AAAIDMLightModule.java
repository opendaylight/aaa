package org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204;

import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.aaa.idm.StoreBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAIDMLightModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AbstractAAAIDMLightModule {

    public static final Logger LOGGER = LoggerFactory.getLogger(AAAIDMLightModule.class);
    private BundleContext bundleContext = null;
    private static final int WAITING_TIME = 5;
    private static final int NUMBER_OF_RETRYS = 60;
    private static volatile IIDMStore store = null;

    public AAAIDMLightModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAIDMLightModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final IdmLightProxy proxy = new IdmLightProxy();
        final ServiceRegistration<?> idmService = bundleContext.registerService(IdMService.class.getName(), proxy, null);
        final ServiceRegistration<?> clientAuthService = bundleContext.registerService(CredentialAuth.class.getName(), proxy, null);

        final StoreServiceLocator locator = new StoreServiceLocator();
        locator.setDaemon(true);
        locator.start();

        LOGGER.info("AAA IDM Light Module Initialized");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                idmService.unregister();
                clientAuthService.unregister();
            }
        };
    }

    public void setBundleContext(BundleContext b){
        this.bundleContext = b;
    }

    private class StoreServiceLocator extends Thread {
        private int retryCount = 0;

        public StoreServiceLocator() {
            setDaemon(true);
        }

        public void run(){
            while(store==null) {
                retryCount++;
                LOGGER.info("Trying to wire the IIDMStore service, Attempt # {}",retryCount);
                final ServiceReference<IIDMStore> serviceReference = bundleContext.getServiceReference(IIDMStore.class);
                if (serviceReference != null) {
                    store = bundleContext.getService(serviceReference);
                    LOGGER.info("Store service was found!");
                    try {
                        StoreBuilder.init();
                    } catch (IDMStoreException e) {
                        LOGGER.error("Failed to initialize data in store",e);
                    }
                    break;
                }
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error("Failed to sleep",e);
                    break;
                }
                if(retryCount>=NUMBER_OF_RETRYS){
                    LOGGER.error("Failed to wire the store service.");
                    break;
                }
            }

        }
    }

    public static final IIDMStore getStore(){
        return store;
    }

    public static final void setStore(IIDMStore s){
        store = s;
    }
}
