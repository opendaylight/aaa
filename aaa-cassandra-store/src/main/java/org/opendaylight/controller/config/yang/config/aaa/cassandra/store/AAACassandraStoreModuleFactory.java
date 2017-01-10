/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.aaa.cassandra.store;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.osgi.framework.BundleContext;

@Deprecated
public class AAACassandraStoreModuleFactory extends org.opendaylight.controller.config.yang.config.aaa.cassandra.store.AbstractAAACassandraStoreModuleFactory {
    @Override
    public AAACassandraStoreModule instantiateModule(String instanceName, DependencyResolver dependencyResolver, AAACassandraStoreModule oldModule, AutoCloseable oldInstance, BundleContext bundleContext) {
        AAACassandraStoreModule module =  super.instantiateModule(instanceName, dependencyResolver, oldModule, oldInstance, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }

    @Override
    public AAACassandraStoreModule instantiateModule(String instanceName, DependencyResolver dependencyResolver, BundleContext bundleContext) {
        AAACassandraStoreModule module = super.instantiateModule(instanceName, dependencyResolver, bundleContext);
        module.setBundleContext(bundleContext);
        return module;
    }
}
