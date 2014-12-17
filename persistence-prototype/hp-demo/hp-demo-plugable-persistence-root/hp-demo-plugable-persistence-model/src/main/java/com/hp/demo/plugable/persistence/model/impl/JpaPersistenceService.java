/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import java.nio.file.Path;

import com.hp.demo.plugable.persistence.model.persistence.jpa.query.JpaQueryFactory;
import com.hp.util.common.io.FileUtil;
import com.hp.util.common.log.LoggerProvider;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.hsqldb.HsqldbServer;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.JpaDataStore;

/**
 * @author Fabiel Zuniga
 */
class JpaPersistenceService extends AbstractPersistenceService<JpaContext> {

    /**
     * Creates a JPA Persistence Service
     * 
     * @param loggerProvider logger provider
     */
    public JpaPersistenceService(LoggerProvider<Class<?>> loggerProvider) {
        super(DataStoreProvider.createDataStore(loggerProvider), new JpaQueryFactory());
    }

    private static class DataStoreProvider {
        private static final String PERSISTENCE_UNIT = "plugable-persistence-demo";
        private static final String DATABASE_NAME = "plugable-persistence-demo-db";
        private static final Path DATABASE_TMP_DATA = FileUtil.getPath(FileUtil.getTempDirectory(),
                "plugable-persistence-demo-tmp-data");

        private static HsqldbServer databaseServer;

        public static DataStore<JpaContext> createDataStore(LoggerProvider<Class<?>> loggerProvider) {
            databaseServer = new HsqldbServer(DATABASE_NAME, DATABASE_TMP_DATA);
            databaseServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    databaseServer.stop();
                }
            });

            return new JpaDataStore(PERSISTENCE_UNIT, loggerProvider);
        }
    }
}
