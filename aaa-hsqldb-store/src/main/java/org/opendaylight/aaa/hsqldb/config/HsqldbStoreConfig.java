/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.hsqldb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for providing configuration properties for the IDMLight/HSQLDB
 * data store implementation.
 *
 * @author peter.mellquist@hp.com
 *
 */
public class HsqldbStoreConfig {

    private static final Logger LOG = LoggerFactory.getLogger(HsqldbStoreConfig.class);

    /**
     * The default password for the database
     */
    private static final String DEFAULT_PASSWORD = "";

    /**
     * The default username for the database
     */
    private static final String DEFAULT_USERNAME = "sa";

    /**
     * The default connection string includes the intention to use hsqldb as
     * the JDBC driver, and the path for the file is located relative to
     * KARAF_HOME.
     */
    private static final String DEFAULT_CONNECTION_STRING = "jdbc:hsqldb:./aaa/";

    /**
     * The database filename
     */
    private String dbName = "aaa-hsqldb";

    /**
     * the database connection string
     */
    private String dbPath;

    /**
     * The database password.  This is not the same as AAA credentials!
     */
    private String dbUser;

    /**
     * The database username.  This is not the same as AAA credentials!
     */
    private String dbPwd;

    /**
     * Creates an valid database configuration using default values.
     */
    public HsqldbStoreConfig() {
        // TODO make this configurable
        dbPath = DEFAULT_CONNECTION_STRING + dbName;
        dbUser = DEFAULT_USERNAME;
        dbPwd = DEFAULT_PASSWORD;
    }

    /**
     * Outputs some debugging information surrounding idmlight config
     */
    public void log() {
        LOG.info("DB Path                 : {}", dbPath);
        LOG.info("DB Driver               : {}","hsqldb");
    }

    public String getDbPath() {
        return this.dbPath;
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public String getDbPwd() {
        return this.dbPwd;
    }
}
