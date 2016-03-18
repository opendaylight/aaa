/*
 * Copyright (c) 2016 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.config.aaa.authn.hsqldb.store.rev160318;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.osgi.framework.BundleContext;

public class AAAHSQLDBStoreModuleFactory extends org.opendaylight.yang.gen.v1.config.aaa.authn.hsqldb.store.rev160318.AbstractAAAHSQLDBStoreModuleFactory {
    @Override
    public AAAHSQLDBStoreModule instantiateModule(String instanceName, DependencyResolver dependencyResolver, AAAHSQLDBStoreModule oldModule, AutoCloseable oldInstance, BundleContext bundleContext) {
        AAAHSQLDBStoreModule module = super.instantiateModule(instanceName, dependencyResolver, oldModule, oldInstance, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }

    @Override
    public AAAHSQLDBStoreModule instantiateModule(String instanceName, DependencyResolver dependencyResolver, BundleContext bundleContext) {
        AAAHSQLDBStoreModule module = super.instantiateModule(instanceName, dependencyResolver, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }
}
