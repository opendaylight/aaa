/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import com.hp.demo.plugable.persistence.model.persistence.cassandra.query.CassandraQueryFactory;
import com.hp.util.common.type.Port;
import com.hp.util.common.type.net.Host;
import com.hp.util.common.type.net.IpAddress;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.client.astyanax.AstyanaxCassandraDataStore;
import com.hp.util.model.persistence.cassandra.keyspace.ConnectionConfiguration;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;
import com.hp.util.model.persistence.db.server.embedded.cassandra.EmbeddedCassandraServer;

/**
 * @author Fabiel Zuniga
 */
class CassandraPersistenceService extends AbstractPersistenceService<CassandraContext<Astyanax>> {

    /**
     * Creates a JPA Persistence Service
     * 
     * @throws PersistenceException if errors occur while creating the persistence service
     */
    public CassandraPersistenceService() throws PersistenceException {
        super(DataStoreProvider.createDataStore(), new CassandraQueryFactory<Astyanax>());
        getDataStore().execute(getQueryFactory().configuration().createSchema());
    }

    private static class DataStoreProvider {

        private static EmbeddedCassandraServer databaseServer;

        public static DataStore<CassandraContext<Astyanax>> createDataStore() {
            databaseServer = new EmbeddedCassandraServer();
            Port port = databaseServer.getPort();
            Keyspace keyspace = new Keyspace("plugable_persistence_demo", "Plugable Persistence Demo Cluster");
            ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(null, port, new Host(
                    IpAddress.valueOf("127.0.0.1"), port));
            databaseServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    databaseServer.stop();
                }
            });

            return new AstyanaxCassandraDataStore(keyspace, connectionConfiguration);
        }
    }
}
