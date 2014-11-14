package org.opendaylight.aaa.idm.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.idm.config.IdmLightConfig;
import org.slf4j.Logger;
import org.sqlite.JDBC;

public class OStoreConnectionPool {
    public static Connection getDBConnect() throws StoreException, SQLException {    	
          JDBC jdbc = new JDBC();
          if(IdmLightApplication.config==null){        	 
        	  IdmLightApplication.config = new IdmLightConfig();
        	  IdmLightApplication.config.load();
          }
          Connection conn = DriverManager.getConnection (IdmLightApplication.config.dbPath);
          return conn;
    }

    public static void closeConnection(Connection conn,Logger logger){
        if (conn != null)
        {
           try {conn.close ();}catch (Exception e) {
              logger.error("Cannot close Database Connection " + e);
            }
         }
    }
}
