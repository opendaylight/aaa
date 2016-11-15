/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for providing configuration properties for the IDMLight/H2
 * data store implementation.
 *
 * @author peter.mellquist@hp.com
 *
 */
public class IdmLightConfig {

    private static final Logger LOG = LoggerFactory.getLogger(IdmLightConfig.class);

    /**
     * The default timeout for db connections in seconds.
     */
    private static final int DEFAULT_DB_TIMEOUT = 3;

    /**
     * The default password for the database
     */
    private static final String DEFAULT_PASSWORD = "bar";

    /**
     * The default username for the database
     */
    private static final String DEFAULT_USERNAME = "foo";

    /**
     * The default driver for the databse is H2;  a pure-java implementation
     * of JDBC.
     */
    private static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";

    /**
     * The default connection string includes the intention to use h2 as
     * the JDBC driver.
     */
    private static final String DEFAULT_CONNECTION_STRING_PREFIX = "jdbc:h2:";

    /**
     * The default directory for the h2 database file.
     * Either absolute or relative to KARAF_HOME.
     */
    private static final String DEFAULT_DIRECTORY = "./";

    /**
     * The default filename for the database file.
     */
    private static final String DEFAULT_IDMLIGHT_DB_FILENAME = "idmlight.db";

    /**
     * The database filename
     */
    private String dbName;

    /**
     * The database directory.
     */
    private String dbDirectory;

    /**
     * The database driver (i.e., H2)
     */
    private final String dbDriver;

    /**
     * The database password.  This is not the same as AAA credentials!
     */
    private String dbUser;

    /**
     * The database username.  This is not the same as AAA credentials!
     */
    private String dbPwd;

    /**
     * Timeout for database connections in seconds
     */
    private int dbValidTimeOut;

    /**
     * Creates an valid database configuration using default values.
     */
    public IdmLightConfig() {
        dbDirectory = DEFAULT_DIRECTORY;
        dbName = DEFAULT_IDMLIGHT_DB_FILENAME;
        dbDriver = DEFAULT_JDBC_DRIVER;
        dbUser = DEFAULT_USERNAME;
        dbPwd = DEFAULT_PASSWORD;
        dbValidTimeOut = DEFAULT_DB_TIMEOUT;
    }

    /**
     * Outputs some debugging information surrounding idmlight config
     */
    public void log() {
        LOG.info("DB Path                 : {}", getDbPath());
        LOG.info("DB Driver               : {}", dbDriver);
        LOG.info("DB Valid Time Out       : {}", dbValidTimeOut);
    }

    public String getDbName() {
        return this.dbName;
    }

    /**
     * Get the database connection string.
     */
    public String getDbPath() {
        return DEFAULT_CONNECTION_STRING_PREFIX + dbDirectory + dbName;
    }

    public String getDbDriver() {
        return this.dbDriver;
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public String getDbPwd() {
        return this.dbPwd;
    }

    public int getDbValidTimeOut() {
        return this.dbValidTimeOut;
    }

    public void setDbDirectory(String dbDirectory) {
        this.dbDirectory = dbDirectory;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    public void setDbValidTimeOut(int dbValidTimeOut) {
        this.dbValidTimeOut = dbValidTimeOut;
    }

}
