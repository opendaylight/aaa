/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.aaa.cassandra.store;

import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.cassandra.persistence.CassandraConfig;
import org.opendaylight.aaa.cassandra.persistence.CassandraStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class AAACassandraStoreModule extends org.opendaylight.controller.config.yang.config.aaa.cassandra.store.AbstractAAACassandraStoreModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AAACassandraStoreModule.class);
    private BundleContext bundleContext = null;
    public AAACassandraStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAACassandraStoreModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.aaa.cassandra.store.AAACassandraStoreModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        CassandraStore store = new CassandraStore();
        final ServiceRegistration<IIDMStore> iidmStoreServiceRegistration = bundleContext.registerService(IIDMStore.class, store, null);
        registerCassandraConfiguration();
        LOGGER.info("AAA Cassandra Store Initialized");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                iidmStoreServiceRegistration.unregister();
            }
        };
    }

    public void setBundleContext(BundleContext b){
        this.bundleContext = b;
    }

    private  void registerCassandraConfiguration(){
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, "aaa-cassandra");
        bundleContext.registerService(ManagedService.class.getName(), CassandraConfig.getInstance() , properties);
    }

}
