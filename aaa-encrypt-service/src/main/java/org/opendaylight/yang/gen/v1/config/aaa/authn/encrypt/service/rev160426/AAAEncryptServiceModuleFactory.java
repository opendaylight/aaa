/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.osgi.framework.BundleContext;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptServiceModuleFactory extends org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426.AbstractAAAEncryptServiceModuleFactory {
    @Override
    public AAAEncryptServiceModule instantiateModule(String instanceName, DependencyResolver dependencyResolver, AAAEncryptServiceModule oldModule, AutoCloseable oldInstance, BundleContext bundleContext) {
        AAAEncryptServiceModule module = super.instantiateModule(instanceName, dependencyResolver, oldModule, oldInstance, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }

    @Override
    public AAAEncryptServiceModule instantiateModule(String instanceName, DependencyResolver dependencyResolver, BundleContext bundleContext) {
        AAAEncryptServiceModule module = super.instantiateModule(instanceName, dependencyResolver, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }
}
