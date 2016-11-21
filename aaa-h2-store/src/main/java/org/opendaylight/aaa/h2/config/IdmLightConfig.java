/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.config;

import java.io.File;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for providing configuration properties for the IDMLight/H2
 * data store implementation.
 *
 * @author peter.mellquist@hp.com - Initial contribution
 * @author Michael Vorburger.ch - Made it configurable, as Immutable with Builder
 */
@Immutable
@Value.Style(stagedBuilder = true, strictBuilder = true,
    builder = "new", typeImmutable = "*Impl", visibility = ImplementationVisibility.PRIVATE)
public abstract class IdmLightConfig {

    private static final Logger LOG = LoggerFactory.getLogger(IdmLightConfig.class);

    /**
     * The filename for the H2 database file.
     */
    @Default public String getDbName() {
        return "idmlight.db";
    }

    /**
     * The database directory for the h2 database file.
     * Either absolute or relative to KARAF_HOME.
     */
    @Default public String getDbDirectory() {
        return ".";
    }

    /**
     * The database JDBC driver, default is H2;  a pure-java implementation.
     */
    @Default public String getDbDriver() {
        return "org.h2.Driver";
    }

    /**
     * The database username.  This is not the same as AAA credentials!
     */
    @Default public String getDbUser() {
        return "foo";
    }

    /**
     * The database password.  This is not the same as AAA credentials!
     */
    @Default public String getDbPwd() {
        return "bar";
    }

    /**
     * Timeout for database connections in seconds.
     */
    @Default public int getDbValidTimeOut() {
        return 3;
    }

    /**
     * The JDBC default connection string.
     */
    @Default public String getDbConnectionStringPrefix() {
        return "jdbc:h2:";
    }

    /**
     * The JDBC database connection string.
     */
    @Default public String getDbConnectionString() {
        return getDbConnectionStringPrefix() + getDbDirectory() + File.separatorChar + getDbName();
    }

    public void log() {
        LOG.info("DB Path                 : {}", getDbConnectionString());
        LOG.info("DB Driver               : {}", getDbDriver());
        LOG.info("DB Valid Time Out       : {}", getDbValidTimeOut());
    }

    /**
     * @deprecated use {@link #getDbConnectionString()}
     */
    @Deprecated public String getDbPath() {
        return getDbConnectionString();
    }
}
