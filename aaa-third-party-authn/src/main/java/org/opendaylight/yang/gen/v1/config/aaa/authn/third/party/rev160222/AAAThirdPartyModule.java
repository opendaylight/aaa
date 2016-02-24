package org.opendaylight.yang.gen.v1.config.aaa.authn.third.party.rev160222;

import java.util.Hashtable;
import org.opendaylight.aaa.api.ThirdPartyAuthenticationService;
import org.opendaylight.aaa.thirdparty.authn.LDAPConfiguration;
import org.opendaylight.aaa.thirdparty.authn.LdapAndRadiusAuthenticationService;
import org.opendaylight.aaa.thirdparty.authn.RadiusConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAThirdPartyModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.third.party.rev160222.AbstractAAAThirdPartyModule {

    private static final Logger LOG = LoggerFactory.getLogger(AAAThirdPartyModule.class);
    public static final String LDAP_PID = "ldap";
    public static final String RADIUS_PID = "radius";
    private BundleContext bundleContext = null;


    public AAAThirdPartyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAThirdPartyModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.third.party.rev160222.AAAThirdPartyModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        registerLdapConfiguration();
        registerRadiusConfiguration();
        LOG.info("LDAP & Radius Service Loaded...");
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(ThirdPartyAuthenticationService.class.getName(), new LdapAndRadiusAuthenticationService(), null);
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                serviceRegistration.unregister();
            }
        };
    }

    private void registerLdapConfiguration(){
        //Register Ldap Configuration File
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, LDAP_PID);
        bundleContext.registerService(ManagedService.class.getName(), LDAPConfiguration.getInstance() , properties);
    }

    private void registerRadiusConfiguration(){
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, RADIUS_PID);
        bundleContext.registerService(ManagedService.class.getName(), RadiusConfiguration.getInstance() , properties);
    }

    public void setBundleContext(BundleContext b){
        this.bundleContext = b;
    }
}
