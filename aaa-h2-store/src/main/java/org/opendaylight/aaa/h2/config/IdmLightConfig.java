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
 *
 * @author peter.mellquist@hp.com
 *
 */
public class IdmLightConfig {
    private static final Logger LOG = LoggerFactory.getLogger(IdmLightConfig.class);

    private String dbName;
    private String dbPath;
    private String dbDriver;
    private String dbUser;
    private String dbPwd;
    private int dbValidTimeOut;

    public IdmLightConfig() {
        dbName = "idmlight.db";
        // TODO make configurable
        dbPath = "jdbc:h2:./" + dbName;
        dbDriver = "org.h2.Driver";
        dbUser = "foo";
        dbPwd = "bar";
        dbValidTimeOut = 3;
    }

    public void log() {
        LOG.info("DB Path                 : {}", dbPath);
        LOG.info("DB Driver               : {}", dbDriver);
        LOG.info("DB Valid Time Out       : {}", dbValidTimeOut);
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getDbPath() {
        return this.dbPath;
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
}
