/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.KeyspaceConfiguration;
import com.hp.util.model.persistence.cassandra.keyspace.Strategy;

/**
 * Utility methods to test Cassandra code.
 * 
 * @author Fabiel Zuniga
 */
public class CassandraTestUtil {

    private CassandraTestUtil() {

    }

    /**
     * Verifies whether integration test using the configured database server is supported.
     * 
     * @return {@code true} if integration test is supported, {@code false} otherwise
     */
    public static boolean isIntegrationTestSupported() {
        return CassandraTestDataStoreProvider.isIntegrationTestSupported();
    }

    /**
     * Executes a query using the data store for testing.
     * 
     * @param query query to execute
     * @return the query result
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <R> R execute(Query<R, CassandraContext<Astyanax>> query) throws PersistenceException {
        if (!isIntegrationTestSupported()) {
            throw new RuntimeException("Integration test is not supported");
        }
        return CassandraTestDataStoreProvider.getDataStore().execute(query);
    }

    /**
     * Method to call before the test class starts.
     * 
     * @throws Exception if errors occur
     */
    public static void beforeTestClass() throws Exception {
        if (isIntegrationTestSupported()) {
            CassandraTestDataStoreProvider.startCassandra();
            createTestKeyspace();
        }
    }

    /**
     * Method to call after the test class starts.
     * 
     * @throws Exception if errors occur
     */
    public static void afterTestClass() throws Exception {
        if (isIntegrationTestSupported()) {
            dropTestKeyspace();
            CassandraTestDataStoreProvider.clearCassandra();
            CassandraTestDataStoreProvider.stopCassandra();
        }
    }

    /**
     * Method to call before each test method.
     * 
     * @throws Exception if errors occur
     */
    public static void beforeTest() throws Exception {
    }

    /**
     * Method to call after each test method.
     * 
     * @throws Exception if errors occur
     */
    public static void afterTest() throws Exception {
    }

    /**
     * Creates the keyspace used for testing.
     * 
     * @throws Exception if errors occur
     */
    private static void createTestKeyspace() throws Exception {
        final KeyspaceConfiguration configuration = new KeyspaceConfiguration(Strategy.SIMPLE, 1);

        execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                context.getCassandraClient().createKeyspace(context.getKeyspace(), configuration, context);
                return null;
            }
        });
    }

    /**
     * Removes the keyspace used for testing.
     * 
     * @throws Exception if errors occur.
     */
    private static void dropTestKeyspace() throws Exception {
        execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                context.getCassandraClient().dropKeyspace(context.getKeyspace(), context);
                return null;
            }
        });
    }

    /**
     * Creates the column families needed by the {@code columnFamilyHandler}
     * 
     * @param columnFamilyHandler column family handler to create column families for
     * @throws Exception if errors occur.
     */
    public static void createColumnFamilies(final ColumnFamilyHandler columnFamilyHandler) throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> definition : columnFamilyHandler.getColumnFamilies()) {
                    context.getCassandraClient().createColumnFamily(definition, context.getKeyspace(), context);
                }
                return null;
            }
        });
    }

    /**
     * Drops the column families created by the {@code columnFamilyHandler}
     * 
     * @param columnFamilyHandler column family handler to drop column families for
     * @throws Exception if errors occur.
     */
    public static void dropColumnFamilies(final ColumnFamilyHandler columnFamilyHandler) throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> columnfamily : columnFamilyHandler.getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(columnfamily, context.getKeyspace(), context);
                }
                return null;
            }
        });
    }

    /**
     * Clears the column families used by the {@code columnFamilyHandler}
     * 
     * @param columnFamilyHandler column family handler to clear column families for
     * @throws Exception if errors occur.
     */
    public static void clearColumnFamilies(final ColumnFamilyHandler columnFamilyHandler) throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> columnfamily : columnFamilyHandler.getColumnFamilies()) {
                    context.getCassandraClient().truncateColumnFamily(columnfamily, context);
                }
                return null;
            }
        });

    }
}
