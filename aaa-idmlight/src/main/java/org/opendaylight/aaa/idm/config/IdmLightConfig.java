/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.config;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */

import java.sql.Connection;
import java.sql.DriverManager;

import org.opendaylight.aaa.idm.persistence.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdmLightConfig {
   private static Logger logger = LoggerFactory.getLogger(IdmLightConfig.class);

   private String dbName;
   private String dbPath;
   private String dbDriver;
   private String dbUser;
   private String dbPwd;
   private int dbValidTimeOut;

   private static final IdmLightConfig INSTANCE = new IdmLightConfig();

   private IdmLightConfig() {
      dbName = "idmlight.db";
      // TODO make configurable
      dbPath = "jdbc:h2:./" + dbName;
      dbDriver = "org.h2.Driver";
      dbUser = "foo";
      dbPwd = "bar";
      dbValidTimeOut = 3;
   }

   public static IdmLightConfig getInstance() {
      return INSTANCE;
   }

   public void log() {
      logger.info("DB Path                 : " + dbPath);
      logger.info("DB Driver               : " + dbDriver);
      logger.info("DB Valid Time Out       : " + dbValidTimeOut);
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
