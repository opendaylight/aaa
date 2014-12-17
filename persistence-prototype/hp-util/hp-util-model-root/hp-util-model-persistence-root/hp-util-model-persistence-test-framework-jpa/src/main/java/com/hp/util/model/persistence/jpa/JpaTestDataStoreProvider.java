/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa;

import java.io.File;
import java.nio.file.Path;

import com.hp.util.common.io.FileUtil;
import com.hp.util.common.log.voidcase.VoidLoggerProvider;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.hsqldb.HsqldbServer;

/**
 * Manages the database used for unit test.
 * 
 * @author Fabiel Zuniga
 */
public class JpaTestDataStoreProvider {

    private static final String PERSISTENCE_UNIT_NAME = "unit-test-persistence-unit";
    private static final Path HSQLDB_DATABASE_FOLDER = FileUtil.getPath(FileUtil.getTempDirectory(),
            "unit-test-data-hsqldb");
    private static final String TEST_DATABASE_NAME = "UnitTestDb";

    private static DataStore<JpaContext> dataStore;
    private static HsqldbServer databaseServer;

    static {
        /*
         * Setup HSQLDB server to allow remote client queries to the
         * embedded database.  Only do this once for all tests as there is an
         * issue with starting it up multiple times.
         */
        if (databaseServer == null) {
            File dir = new File(HSQLDB_DATABASE_FOLDER.toString());
            if (dir.exists()) {
                for (File file : dir.listFiles()) {
                    file.delete();
                }
            }

            databaseServer = new HsqldbServer(TEST_DATABASE_NAME, HSQLDB_DATABASE_FOLDER);
            databaseServer.start();
            dataStore = new JpaDataStore(PERSISTENCE_UNIT_NAME, VoidLoggerProvider.<Class<?>> getInstance());
        }
    }

    private JpaTestDataStoreProvider() {

    }

    /**
     * Gets the persistence unit name.
     * 
     * @return the persistence unit name
     */
    public static String getPersistenceUnitName() {
        return PERSISTENCE_UNIT_NAME;
    }

    /**
     * Gets the data store service.
     * 
     * @return the data store
     */
    public static DataStore<JpaContext> getDataStore() {
        return dataStore;
    }
}
