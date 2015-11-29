package org.opendaylight.controller.config.yang.config.aaa.cassandra.store;

import org.opendaylight.aaa.cassandra.persistence.CassandraStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAACassandraStoreModule extends org.opendaylight.controller.config.yang.config.aaa.cassandra.store.AbstractAAACassandraStoreModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(AAACassandraStoreModule.class);
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
        LOGGER.info("AAA Cassandra Store Initialized");
        CassandraStore store = new CassandraStore();
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
            }
        };
    }
}
