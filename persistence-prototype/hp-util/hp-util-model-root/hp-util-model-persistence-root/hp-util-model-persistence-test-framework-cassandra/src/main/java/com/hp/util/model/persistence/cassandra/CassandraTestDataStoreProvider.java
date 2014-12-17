/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import com.hp.util.common.type.Port;
import com.hp.util.common.type.net.Host;
import com.hp.util.common.type.net.IpAddress;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.DatabaseTest.DatabaseServerHandler;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.client.astyanax.AstyanaxCassandraDataStore;
import com.hp.util.model.persistence.cassandra.keyspace.ConnectionConfiguration;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;
import com.hp.util.model.persistence.db.server.embedded.cassandra.EmbeddedCassandraServer;
import com.hp.util.test.TestEnvironment;
import com.hp.util.test.TestEnvironment.OperativeSystem;

/**
 * Cassandra Test Data Store Provider.
 * 
 * @author Fabiel Zuniga
 */
public class CassandraTestDataStoreProvider {

    private static DatabaseServerHandler<CassandraContext<Astyanax>> cassandraServerHandler;

    static {
        // cassandraServerHandler = new CassandraServerHandlerImpl();
        // Note: EmbeddedCassandra does not properly works on windows, run unit tests in Linux.
        cassandraServerHandler = new EmbeddedCassandraServerHandlerImpl();
    }

    private CassandraTestDataStoreProvider() {

    }

    /**
     * Verifies whether integration test using the configured database server is supported.
     *
     * @return {@code true} if integration test is supported, {@code false} otherwise
     */
    public static boolean isIntegrationTestSupported() {
        return cassandraServerHandler.isIntegrationTestSupported();
    }

    /**
     * Gets the data store service.
     *
     * @return the data store service
     */
    public static DataStore<CassandraContext<Astyanax>> getDataStore() {
        return cassandraServerHandler.getDataStore();
    }

    /**
     * Starts Cassandra.
     *
     * @throws Exception if error occur
     */
    public static void startCassandra() throws Exception {
        cassandraServerHandler.start();
    }

    /**
     * Clears Cassandra.
     *
     * @throws Exception if error occur
     */
    public static void clearCassandra() throws Exception {
        cassandraServerHandler.clear();
    }

    /**
     * Stops Cassandra.
     *
     * @throws Exception if error occur
     */
    public static void stopCassandra() throws Exception {
        cassandraServerHandler.stop();
    }

    /**
     * Cassandra server handler that uses a real Cassandra instance.
     */
    @SuppressWarnings("unused")
    private static class CassandraServerHandlerImpl implements DatabaseServerHandler<CassandraContext<Astyanax>> {

        private DataStore<CassandraContext<Astyanax>> dataStore;

        CassandraServerHandlerImpl() {
            Keyspace keyspace = new Keyspace("integration_test_keyspace", "Test Cluster");
            ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(null,
                    AstyanaxCassandraDataStore.DEFAULT_CASSANDRA_PORT, new Host(IpAddress.valueOf("127.0.0.1"),
                            AstyanaxCassandraDataStore.DEFAULT_CASSANDRA_PORT));
            System.out.println(CassandraTestDataStoreProvider.class.getName()
                    + " creating Cassandra data store service using " + keyspace + " " + connectionConfiguration);
            this.dataStore = new AstyanaxCassandraDataStore(keyspace, connectionConfiguration);
        }

        @Override
        public boolean isIntegrationTestSupported() {
            return true;
        }

        @Override
        public void start() throws Exception {

        }

        @Override
        public void clear() throws Exception {

        }

        @Override
        public void stop() throws Exception {

        }

        @Override
        public DataStore<CassandraContext<Astyanax>> getDataStore() {
            return this.dataStore;
        }
    }

    /**
     * Cassandra server handler that uses Embedded Cassandra Server.
     */
    private static class EmbeddedCassandraServerHandlerImpl implements
            DatabaseServerHandler<CassandraContext<Astyanax>> {

        /*
        private static final Path DATABASE_FOLDER = FileUtil.getPath(FileUtil.getTempDirectory(),
                "unit-test-data-embedded-cassandra");
        */

        private EmbeddedCassandraServer server;
        private DataStore<CassandraContext<Astyanax>> dataStore;
        private Keyspace keyspace;
        private ConnectionConfiguration connectionConfiguration;
        private final boolean isIntegrationTestSupported;

        EmbeddedCassandraServerHandlerImpl() {
            // -------------------------------------------------------------------------------------
            /*
             * TODO: Unfortunately EmbeddedCassandraServerHelper expects the yaml file to be part of
             * the resources. Thus the cluster and port cannot be received as parameter. See
             * EmbeddedCassandraServer notes.
             */
            /*
            FileUtil.deleteRecursively(DATABASE_FOLDER);
            Files.createDirectories(DATABASE_FOLDER);
            Port port = Port.valueOf(9272);
            this.server = new EmbeddedCassandraServer(port, "Test Cluster", DATABASE_FOLDER);
            */
            // -------------------------------------------------------------------------------------
            
            this.server = new EmbeddedCassandraServer();
            Port port = this.server.getPort();
            this.keyspace = new Keyspace("integration_test_keyspace", "Test Cluster");
            this.connectionConfiguration = new ConnectionConfiguration(null, port, new Host(
                    IpAddress.valueOf("127.0.0.1"), port));

            // EmbeddedCassandraServer does not work on Windows.
            // It just has been confirmed that works on Linux.
            OperativeSystem os = TestEnvironment.getOperativeSystem();
            this.isIntegrationTestSupported = os == OperativeSystem.LINUX;
            if (!this.isIntegrationTestSupported) {
                System.err.println("-----------------------------------------------------------------------------------");
                System.err.println(getClass().getName() + ": Integration Test NOT SUPPORTED for operative system: " + os);
                System.err.println("-----------------------------------------------------------------------------------");
            }
        }

        @Override
        public boolean isIntegrationTestSupported() {
            return this.isIntegrationTestSupported;
        }

        @Override
        public void start() throws Exception {
            this.server.start();
            System.out.println(CassandraTestDataStoreProvider.class.getName()
                    + " creating Cassandra data store service using " + this.keyspace + " "
                    + this.connectionConfiguration);
            this.dataStore = new AstyanaxCassandraDataStore(this.keyspace, this.connectionConfiguration);
        }

        @Override
        public void clear() throws Exception {
            this.server.clearData();
        }

        @Override
        public void stop() throws Exception {
            this.server.stop();
            // FileUtil.deleteRecursively(DATABASE_FOLDER);
        }

        @Override
        public DataStore<CassandraContext<Astyanax>> getDataStore() {
            return this.dataStore;
        }
    }
}
