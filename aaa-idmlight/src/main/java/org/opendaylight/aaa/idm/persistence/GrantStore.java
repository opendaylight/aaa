/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.persistence;

/**
 *
 * @author peter.mellquist@hp.com 
 *
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.idm.model.Grants;
import org.opendaylight.aaa.idm.model.Grant;
import org.sqlite.JDBC;

public class GrantStore {
   private static Logger logger = LoggerFactory.getLogger(GrantStore.class);
   protected Connection  dbConnection = null;
   private static Calendar calendar = Calendar.getInstance();
   
   protected final static String SQL_ID             = "grantid";
   protected final static String SQL_DESCR          = "description";
   protected final static String SQL_TENANTID       = "domainid";
   protected final static String SQL_USERID         = "userid";
   protected final static String SQL_ROLEID         = "roleid";

   protected Connection getDBConnect() throws StoreException {
      if ( dbConnection==null ) {
         try {           
	    //Class.forName (IdmLightApplication.config.dbDriver).newInstance ();
            JDBC jdbc = new JDBC();
	    dbConnection = DriverManager.getConnection (IdmLightApplication.config.dbPath); 
            return dbConnection;
         }
         catch (Exception e) {
            throw new StoreException("Cannot connect to database server "+ e);
         }       
      }
      else {
         try {
            if ( dbConnection.isClosed()) {
               try {          
		  //Class.forName (IdmLightApplication.config.dbDriver).newInstance ();
                  JDBC jdbc = new JDBC(); 
		  dbConnection = DriverManager.getConnection (IdmLightApplication.config.dbPath);
		  return dbConnection;
               }
               catch (Exception e) {
                  throw new StoreException("Cannot connect to database server "+ e);
               }      
            }
            else
               return dbConnection;
         }
	 catch (SQLException sqe) {
            throw new StoreException("Cannot connect to database server "+ sqe);
         }
      }
   }

   protected Connection dbConnect() throws StoreException {
      Connection conn;
      try {
         conn = getDBConnect();
      }
      catch (StoreException se) {
         throw se;
      }
      try {
         DatabaseMetaData dbm = conn.getMetaData();
         ResultSet rs = dbm.getTables(null, null, "grants", null);
         if (rs.next()) {
            logger.info("grants Table already exists");
         }
         else
         {
            logger.info("grants Table does not exist, creating table");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE grants " +
                         "(grantid    INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "description VARCHAR(128)     NOT NULL, " +
                         "domainid     INTEGER         NOT NULL, " +
                         "userid     INTEGER           NOT NULL, " +
                         "roleid     INTEGER           NOT NULL)" ;
           stmt.executeUpdate(sql);
           stmt.close();
         }
      }
      catch (SQLException sqe) {
         throw new StoreException("Cannot connect to database server "+ sqe);
      }
      return conn;
   }



      
   protected void dbClose() {
      if (dbConnection != null)
      {
         try {
            dbConnection.close ();
          }
          catch (Exception e) { 
            logger.error("Cannot close Database Connection " + e);
          }
       }
   }
	
   protected void finalize ()  {
      dbClose();
   }

   protected Grant rsToGrant(ResultSet rs) throws SQLException {
      Grant grant = new Grant();
      try {
         grant.setGrantid(rs.getInt(SQL_ID));
         grant.setDescription(rs.getString(SQL_DESCR));
         grant.setDomainid(rs.getInt(SQL_TENANTID));
         grant.setUserid(rs.getInt(SQL_USERID));
         grant.setRoleid(rs.getInt(SQL_ROLEID));
      }
      catch (SQLException sqle) {
         logger.error( "SQL Exception : " + sqle);
            throw sqle;
      }
      return grant;
   }
   
   public Grants getGrants(long did, long uid) throws StoreException {
      Grants grants = new Grants();
      List<Grant> grantList = new ArrayList<Grant>();
      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "SELECT * FROM grants WHERE domainid=" + did + " AND userid="+uid;
      try {
         stmt=conn.createStatement();
         ResultSet rs=stmt.executeQuery(query);
         while (rs.next()) {
            Grant grant = rsToGrant(rs);
            grantList.add(grant);
         }
         rs.close();
         stmt.close();
         dbClose();
      }
      catch (SQLException s) {
         dbClose();
         throw new StoreException("SQL Exception : " + s);
      }
      grants.setGrants(grantList);
      return grants;
   }

   public Grant  getGrant(long id) throws StoreException {
      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "SELECT * FROM grants WHERE grantid=" + id;
      try {
         stmt=conn.createStatement();
         ResultSet rs=stmt.executeQuery(query);
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            stmt.close();
            dbClose();
            return grant;
         }
         else {
            rs.close();
            stmt.close();
            dbClose();
            return null; 
         } 
      }
      catch (SQLException s) {
         dbClose();
         throw new StoreException("SQL Exception : " + s);
      }
   }

   public Grant getGrant(long did,long uid,long rid) throws StoreException {
      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "SELECT * FROM grants WHERE domainid=" + did + " AND userid=" + uid + " AND roleid="+rid;
      try {
         stmt=conn.createStatement();
         ResultSet rs=stmt.executeQuery(query);
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            stmt.close();
            dbClose();
            return grant;
         }
         else {
            rs.close();
            stmt.close();
            dbClose();
            return null;
         }
      }
      catch (SQLException s) {
         dbClose();
         throw new StoreException("SQL Exception : " + s);
      }
   }


   public Grant createGrant(Grant grant) throws StoreException {
       int key=0;
       Connection conn = dbConnect();
       try {
          String query = "insert into grants  (description,domainid,userid,roleid) values(?,?,?,?)";
          PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
          statement.setString(1,grant.getDescription());
          statement.setInt(2,grant.getDomainid());
          statement.setInt(3,grant.getUserid());
          statement.setInt(4,grant.getRoleid());
          int affectedRows = statement.executeUpdate();
          if (affectedRows == 0) 
             throw new StoreException("Creating grant failed, no rows affected."); 
          ResultSet generatedKeys = statement.getGeneratedKeys();
          if (generatedKeys.next()) 
             key = generatedKeys.getInt(1);
          else 
             throw new StoreException("Creating grant failed, no generated key obtained.");
          grant.setGrantid(key);
          dbClose();
          return grant;
       }
       catch (SQLException s) {
          dbClose();
          throw new StoreException("SQL Exception : " + s);
       }
   }

   public Grant deleteGrant(Grant grant) throws StoreException {
      Grant savedGrant = this.getGrant(grant.getGrantid());
      if (savedGrant==null)
         return null;

      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "DELETE FROM grants WHERE grantid=" + grant.getGrantid();
      try {
         stmt=conn.createStatement();
         int deleteCount = stmt.executeUpdate(query);
         logger.info("deleted " + deleteCount + " records");
         stmt.close();
         dbClose();
         return savedGrant;
      }
      catch (SQLException s) {
         dbClose();
         throw new StoreException("SQL Exception : " + s);
      }
   }
   
}

