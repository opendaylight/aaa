/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class for database tests.
 * 
 * @param <C> type of the query's execution context
 * @author Fabiel Zuniga
 */
public abstract class DatabaseTest<C> {

    private DatabaseServerHandler<C> databaseServerHandler;

    /**
     * Creates a new test.
     * 
     * @param databaseServerHandler database server handler
     */
    protected DatabaseTest(DatabaseServerHandler<C> databaseServerHandler) {
        this.databaseServerHandler = databaseServerHandler;
    }

    /**
     * Method executed before running the class tests.
     *
     * @throws Exception if errors occur
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        // TODO: DatabaseServerHandler.start() cannot be called here because this method is static
    }

    /**
     * Method executed after running the class tests.
     *
     * @throws Exception if errors occur
     */
    @AfterClass
    public static void afterClass() throws Exception {
        // TODO: DatabaseServerHandler.stop() cannot be called here because this method is static
    }

    /**
     * Method executed before running each test.
     * 
     * @throws Exception if any errors occur during execution
     */
    @Before
    public void beforeTest() throws Exception {
        if (this.databaseServerHandler.isIntegrationTestSupported()) {
            this.databaseServerHandler.start();
        }
    }

    /**
     * Method executed after running each test.
     * 
     * @throws Exception if any errors occur during execution
     */
    @Before
    public void afterTest() throws Exception {
        if (this.databaseServerHandler.isIntegrationTestSupported()) {
            this.databaseServerHandler.clear();
            this.databaseServerHandler.stop();
        }
    }

    /**
     * Executes a query.
     * 
     * @param query query to execute
     * @return the query result
     * @throws Exception if errors occur executing the query
     */
    public <R> R execute(Query<R, C> query) throws Exception {
        if (!this.databaseServerHandler.isIntegrationTestSupported()) {
            throw new RuntimeException("Integration test is not supported");
        }
        return this.databaseServerHandler.getDataStore().execute(query);
    }

    /**
     * Database server handler.
     * 
     * @param <C> type of the data store execution context
     */
    public static interface DatabaseServerHandler<C> {

        /**
         * Verifies whether integration test using this server is supported.
         * 
         * @return {@code true} if integration test is supported, {@code false} otherwise
         */
        public boolean isIntegrationTestSupported();

        /**
         * Starts the server. This method will be called before the test class.
         * 
         * @throws Exception if errors occur while executing the operation
         */
        public void start() throws Exception;

        /**
         * Executes any cleaning in the database server. This method will be called before each test
         * method.
         * 
         * @throws Exception if errors occur while executing the operation
         */
        public void clear() throws Exception;

        /**
         * Stops the server. This method will be called after the test class.
         * 
         * @throws Exception if errors occur while executing the operation
         */
        public void stop() throws Exception;

        /**
         * Gets the data store.
         * 
         * @return the data store
         */
        public DataStore<C> getDataStore();
    }
}
