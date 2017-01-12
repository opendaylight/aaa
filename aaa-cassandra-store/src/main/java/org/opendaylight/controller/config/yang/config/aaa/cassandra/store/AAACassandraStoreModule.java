/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.aaa.cassandra.store;

import java.util.Hashtable;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.cassandra.persistence.CassandraConfig;
import org.opendaylight.aaa.cassandra.persistence.CassandraStore;
import org.opendaylight.aaa.cassandra.persistence.TokenStoreImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class AAACassandraStoreModule extends org.opendaylight.controller.config.yang.config.aaa.cassandra.store.AbstractAAACassandraStoreModule implements AutoCloseable{
    private static final Logger LOG = LoggerFactory.getLogger(AAACassandraStoreModule.class);
    private BundleContext bundleContext = null;
    private ServiceRegistration<IIDMStore> iidmStoreServiceRegistration = null;
    private ServiceRegistration<TokenStore> tokenStoreServiceRegistration = null;

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
        registerCassandraConfiguration();
        final CassandraStore store = new CassandraStore();
        this.iidmStoreServiceRegistration = bundleContext.registerService(IIDMStore.class, store, null);
        try {
            final TokenStore tokenStore = new TokenStoreImpl(store);
            this.tokenStoreServiceRegistration = bundleContext.registerService(TokenStore.class, tokenStore, null);
        } catch (NoSuchMethodException e) {
            LOG.error("Failed to instantiate token store",e);
        }

        LOG.info("AAA Cassandra Store Initialized");
        return this;
    }

    @Override
    public void close() throws Exception {
        iidmStoreServiceRegistration.unregister();
        tokenStoreServiceRegistration.unregister();
    }

    public void setBundleContext(BundleContext b){
        this.bundleContext = b;
    }

    private  void registerCassandraConfiguration(){
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(Constants.SERVICE_PID, "aaacassandra");
        bundleContext.registerService(ManagedService.class.getName(), CassandraConfig.getInstance() , properties);
    }

}
