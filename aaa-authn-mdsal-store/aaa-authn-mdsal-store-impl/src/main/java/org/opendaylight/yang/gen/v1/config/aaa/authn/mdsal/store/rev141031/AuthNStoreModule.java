package org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031;

import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.authn.mdsal.store.AuthNStore;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.osgi.framework.BundleContext;

public class AuthNStoreModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.AbstractAuthNStoreModule {
    private BundleContext bundleContext;

    public AuthNStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AuthNStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.AuthNStoreModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        final AuthNStore authNStore = new AuthNStore();

        DataBroker dataBrokerService = getDataBrokerDependency();
        authNStore.setBroker(dataBrokerService);
        authNStore.setTimeToLive(getTimeToLive());

        //Register the MD-SAL Token store with OSGI
        bundleContext.registerService(TokenStore.class.getName(), authNStore, null);

        final class AutoCloseableStore implements AutoCloseable {

            @Override
            public void close() throws Exception {
                authNStore.close();
            }
        }


        return new AutoCloseableStore();

//        return authNStore;

//        throw new java.lang.UnsupportedOperationException();
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