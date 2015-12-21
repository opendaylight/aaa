/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.config.yang.config.aaa_authz.srv;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.authz.srv.AuthzBrokerImpl;
import org.opendaylight.aaa.authz.srv.AuthzServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthzSrvModule extends
        org.opendaylight.controller.config.yang.config.aaa_authz.srv.AbstractAuthzSrvModule {
    private static final Logger LOG = LoggerFactory.getLogger(AuthzSrvModule.class);
    private static boolean simple_config_switch;
    private BundleContext bundleContext;

    public AuthzSrvModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AuthzSrvModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            org.opendaylight.controller.config.yang.config.aaa_authz.srv.AuthzSrvModule oldModule,
            java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // checkNotNull(getDomBroker(), domBrokerJmxAttribute);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        // Get new AuthZ Broker
        final AuthzBrokerImpl authzBrokerImpl = new AuthzBrokerImpl();

        // Provide real broker to the new Authz broker
        authzBrokerImpl.setBroker(getDomBrokerDependency());

        // Get AuthN service reference and register it with the authzBroker
        ServiceReference<AuthenticationService> authServiceReference = bundleContext
                .getServiceReference(AuthenticationService.class);
        AuthenticationService as = bundleContext.getService(authServiceReference);
        authzBrokerImpl.setAuthenticationService(as);

        // Set the policies list to authz serviceimpl
        AuthzServiceImpl.setPolicies(getPolicies());

        // Register AuthZ broker with the real Broker as a provider; triggers
        // "onSessionInitiated" in AuthzBrokerImpl
        getDomBrokerDependency().registerProvider(authzBrokerImpl);
        // TODO ActionType is of type string, not ENUM due to improper
        // serialization of ENUMs by config/netconf subsystem. This needs to be
        // fixed as soon as config/netconf fixes the problem.
        getAction();

        LOG.info("AuthZ Service Initialized from Config subsystem");
        return authzBrokerImpl;

    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
