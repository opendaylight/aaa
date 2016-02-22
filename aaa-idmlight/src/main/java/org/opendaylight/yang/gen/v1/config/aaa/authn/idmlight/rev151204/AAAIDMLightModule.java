package org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204;

import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.ThirdPartyAuthenticationService;
import org.opendaylight.aaa.idm.IdmLightProxy;
import org.opendaylight.aaa.idm.StoreBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAIDMLightModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AbstractAAAIDMLightModule {

    private static final Logger LOG = LoggerFactory.getLogger(AAAIDMLightModule.class);
    private BundleContext bundleContext = null;
    private static volatile IIDMStore store = null;
    private static volatile ThirdPartyAuthenticationService thirdPartyAuthenticationService = null;

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

        final ServiceTracker<IIDMStore, IIDMStore> storeServiceTracker = new ServiceTracker<>(bundleContext, IIDMStore.class,
                new ServiceTrackerCustomizer<IIDMStore, IIDMStore>() {
                    @Override
                    public IIDMStore addingService(ServiceReference<IIDMStore> reference) {
                        store = reference.getBundle().getBundleContext().getService(reference);
                        LOG.info("IIDMStore service {} was found", store.getClass());
                        try {
                            StoreBuilder.init(store);
                        } catch (IDMStoreException e) {
                            LOG.error("Failed to initialize data in store", e);
                        }

                        return store;
                    }

                    @Override
                    public void modifiedService(ServiceReference<IIDMStore> reference, IIDMStore service) {
                    }

                    @Override
                    public void removedService(ServiceReference<IIDMStore> reference, IIDMStore service) {
                    }
                });

        storeServiceTracker.open();

        final ServiceTracker<ThirdPartyAuthenticationService, ThirdPartyAuthenticationService> thirdPartyAuthenticationTracker = new ServiceTracker<>(bundleContext, ThirdPartyAuthenticationService.class,
                new ServiceTrackerCustomizer<ThirdPartyAuthenticationService, ThirdPartyAuthenticationService>() {
                    @Override
                    public ThirdPartyAuthenticationService addingService(ServiceReference<ThirdPartyAuthenticationService> reference) {
                        thirdPartyAuthenticationService = reference.getBundle().getBundleContext().getService(reference);
                        LOG.info("Third Party Authentication service {} was found", store.getClass());
                        return thirdPartyAuthenticationService;
                    }

                    @Override
                    public void modifiedService(ServiceReference<ThirdPartyAuthenticationService> reference, ThirdPartyAuthenticationService service) {
                    }

                    @Override
                    public void removedService(ServiceReference<ThirdPartyAuthenticationService> reference, ThirdPartyAuthenticationService service) {
                    }
                });

        thirdPartyAuthenticationTracker.open();

        LOG.info("AAA IDM Light Module Initialized");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                idmService.unregister();
                clientAuthService.unregister();
                storeServiceTracker.close();
            }
        };
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

    public static final ThirdPartyAuthenticationService getThirdPartyAuthenticationService(){
        return thirdPartyAuthenticationService;
    }
}
