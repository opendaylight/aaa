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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdmLightConfig {
   private static Logger logger = LoggerFactory.getLogger(IdmLightConfig.class);

   public static String dbName;
   public String dbPath;
   public String dbDriver;
   public String dbUser;
   public String dbPwd;
   public int dbValidTimeOut;

   public boolean load() {
      dbName = "idmlight.db";
      //TODO make configurable
      dbPath = "jdbc:h2:./" + dbName;
      dbDriver = "org.h2.Driver";
      dbUser = "foo";
      dbPwd = "bar";
      dbValidTimeOut = 3;

      return true;
   }

   public void log() {
      logger.info("DB Path                 : " + dbPath);
      logger.info("DB Driver               : " + dbDriver);
      logger.info("DB Valid Time Out       : " + dbValidTimeOut);
   }

}
