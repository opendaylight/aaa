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

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdmLightConfig implements ManagedService{
   private static Logger logger = LoggerFactory.getLogger(IdmLightConfig.class);

   private final static String DB_PATH_KEY = "db.file.path";
   private final static String DB_USER_KEY = "db.user";
   private final static String DB_PSWD_KEY = "db.pswd";
   
   private IdmLightConfig(){
   } 
   
   private static IdmLightConfig IDMLIGHTINSTANCE = new IdmLightConfig();
   
   public static IdmLightConfig getInstance(){
	   return IDMLIGHTINSTANCE;
   }
   
   public static final Dictionary<String, String> defaults = new Hashtable<>();
   static {
       defaults.put(DB_PATH_KEY, "~/h2");
       defaults.put(DB_USER_KEY, "foo");
       defaults.put(DB_PSWD_KEY, "bar");
   }
   
   public static String dbName;
   public String dbPath;
   public String dbDriver;
   public String dbUser;
   public String dbPwd;
   public int dbValidTimeOut;
   
   public boolean load() {
      dbName = "idmlight.db";
      dbDriver = "org.h2.Driver";
      dbValidTimeOut = 3;

      return true;
   }

   public void log() {
      logger.info("DB Path                 : " + dbPath);
      logger.info("DB Driver               : " + dbDriver);
      logger.info("DB Valid Time Out       : " + dbValidTimeOut);
   }

   @Override
   public void updated(Dictionary<String, ?> properties)
		   throws ConfigurationException {
	  if (properties == null) {
		   return;
	  }
			
	  String pathProp = (String) properties.get(DB_PATH_KEY);
	  String userProp = (String) properties.get(DB_USER_KEY);
	  String pwdProp = (String) properties.get(DB_PSWD_KEY);

	  if(null == dbPath || dbPath.isEmpty()){
		  throw new ConfigurationException(DB_PATH_KEY, "DB Path Field empty/null");
	  }else if(null == dbUser || dbUser.isEmpty()){
		  throw new ConfigurationException(DB_USER_KEY, "DB User Field empty/null");
	  }else if(null == dbPwd || dbPwd.isEmpty()){
		  throw new ConfigurationException(DB_PSWD_KEY, "DB Pwd Field empty/null");
	  }
	  
	  dbPath = pathProp;
	  dbUser = userProp;
	  dbPwd = pwdProp;
   }

}
