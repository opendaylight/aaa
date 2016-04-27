/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426;

import org.opendaylight.aaa.api.AAAEncryptionService;
import org.opendaylight.aaa.encrypt.AAAEncryptServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptServiceModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426.AbstractAAAEncryptServiceModule {

    private BundleContext bundleContext = null;

    public AAAEncryptServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAEncryptServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426.AAAEncryptServiceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final AAAEncryptServiceImpl impl = new AAAEncryptServiceImpl("password",new byte[]{ 0, 5, 0, 0, 7, 81, 0, 3, 0, 0, 0, 0, 0, 43, 0, 1 });
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(AAAEncryptionService.class.getName(), impl, null);

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                serviceRegistration.unregister();
            }
        };
    }

    public void setBundleContext(BundleContext bundleContext){
        this.bundleContext = bundleContext;
    }
}
