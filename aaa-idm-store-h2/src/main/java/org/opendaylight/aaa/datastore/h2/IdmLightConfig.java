/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.datastore.h2;

import java.io.File;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for providing configuration properties for the IDMLight/H2 data
 * store implementation.
 *
 * @author peter.mellquist@hp.com - Initial contribution
 * @author Michael Vorburger.ch - Made it configurable, as Immutable with
 *         Builder
 */
@Immutable
@Value.Style(strictBuilder = true, builder = "new",
    typeImmutable = "*Impl", visibility = ImplementationVisibility.PRIVATE)
public abstract class IdmLightConfig {

    private static final Logger LOG = LoggerFactory.getLogger(IdmLightConfig.class);

    /**
     * The filename for the H2 database file.
     *
     * @return data base name
     */
    @Default
    public String getDbName() {
        return "idmlight.db";
    }

    /**
     * The database directory for the h2 database file. Either absolute or
     * relative to KARAF_HOME.
     *
     * @return data base dir
     */
    @Default
    public String getDbDirectory() {
        return "./data";
    }

    /**
     * The database JDBC driver, default is H2; a pure-java implementation.
     *
     * @return data base driver
     */
    @Default
    public String getDbDriver() {
        return "org.h2.Driver";
    }

    /**
     * The database username. This is not the same as AAA credentials!
     *
     * @return data base user
     */
    public abstract String getDbUser();

    /**
     * The database password. This is not the same as AAA credentials!
     *
     * @return data base password
     */
    public abstract String getDbPwd();

    /**
     * Timeout for database connections in seconds.
     *
     * @return data base valid time out
     */
    @Default
    public int getDbValidTimeOut() {
        return 3;
    }

    /**
     * The JDBC default connection string.
     *
     * @return data base connection prefix
     */
    @Default
    public String getDbConnectionStringPrefix() {
        return "jdbc:h2:";
    }

    /**
     * The JDBC database connection string.
     *
     * @return data base connection
     */
    @Default
    public String getDbConnectionString() {
        return getDbConnectionStringPrefix() + getDbDirectory() + File.separatorChar + getDbName();
    }

    public void log() {
        LOG.info("DB Path                 : {}", getDbConnectionString());
        LOG.info("DB Driver               : {}", getDbDriver());
        LOG.info("DB Valid Time Out       : {}", getDbValidTimeOut());
    }
}
