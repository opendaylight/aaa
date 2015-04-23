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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.aaa.idm.IdmLightApplication;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Grants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.h2.Driver;

public class GrantStore {
   private static Logger logger = LoggerFactory.getLogger(GrantStore.class);
   protected Connection  dbConnection = null;
   protected final static String SQL_ID             = "grantid";
   protected final static String SQL_DESCR          = "description";
   protected final static String SQL_TENANTID       = "domainid";
   protected final static String SQL_USERID         = "userid";
   protected final static String SQL_ROLEID         = "roleid";

   protected Connection getDBConnect() throws StoreException {
      if ( dbConnection==null ) {
         try {
	    //Class.forName (IdmLightApplication.config.dbDriver).newInstance ();
              Driver jdbc = new org.h2.Driver();
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
                      Driver jdbc = new org.h2.Driver();
		  dbConnection = DriverManager.getConnection (IdmLightApplication.config.dbPath);
		  return dbConnection;
               }
               catch (Exception e) {
                  throw new StoreException("Cannot connect to database server "+ e);
               }
            }
            else {
               return dbConnection;
            }
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
         ResultSet rs = dbm.getTables(null, null, "GRANTS", null);
         if (rs.next()) {
            debug("grants Table already exists");
         }
         else
         {
            logger.info("grants Table does not exist, creating table");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE GRANTS " +
                         "(grantid    INTEGER PRIMARY KEY AUTO_INCREMENT," +
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

   @Override
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
      String query = "SELECT * FROM grants WHERE domainid= ? AND userid= ?";
      try {
         PreparedStatement pstmt = conn.prepareStatement(query);
         pstmt.setLong(1, did);
         pstmt.setLong(2, uid);
         ResultSet rs=pstmt.executeQuery();
         while (rs.next()) {
            Grant grant = rsToGrant(rs);
            grantList.add(grant);
         }
         rs.close();
         pstmt.close();
         dbClose();
      }
      catch (SQLException s) {
         dbClose();
         throw new StoreException("SQL Exception : " + s);
      }
      grants.setGrants(grantList);
      return grants;
   }

   public Grants getGrants(long uid) throws StoreException {
      Grants grants = new Grants();
      List<Grant> grantList = new ArrayList<Grant>();
      Connection conn = dbConnect();
      String query = "SELECT * FROM grants WHERE userid= ?";
      try {
         PreparedStatement pstmt = conn.prepareStatement(query);
         pstmt.setLong(1, uid);
         ResultSet rs=pstmt.executeQuery();
         while (rs.next()) {
            Grant grant = rsToGrant(rs);
            grantList.add(grant);
         }
         rs.close();
         pstmt.close();
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

      String query = "SELECT * FROM grants WHERE grantid= ?";
      try {
         PreparedStatement pstmt = conn.prepareStatement(query);
         pstmt.setLong(1, id);
         ResultSet rs=pstmt.executeQuery();
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            pstmt.close();
            dbClose();
            return grant;
         }
         else {
            rs.close();
            pstmt.close();
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
      String query = "SELECT * FROM grants WHERE domainid= ? AND userid= ? AND roleid= ?";
      try {
         PreparedStatement pstmt = conn.prepareStatement(query);
         pstmt.setLong(1,did);
         pstmt.setLong(2,uid);
         pstmt.setLong(3,rid);
         ResultSet rs=pstmt.executeQuery();
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            pstmt.close();
            dbClose();
            return grant;
         }
         else {
            rs.close();
            pstmt.close();
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
          if (affectedRows == 0) {
             throw new StoreException("Creating grant failed, no rows affected.");
          }
          ResultSet generatedKeys = statement.getGeneratedKeys();
          if (generatedKeys.next()) {
             key = generatedKeys.getInt(1);
          }
          else {
             throw new StoreException("Creating grant failed, no generated key obtained.");
          }
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
      if (savedGrant==null) {
         return null;
      }

      Connection conn = dbConnect();
      String query = "DELETE FROM grants WHERE grantid= ?";
      try {
         PreparedStatement pstmt = conn.prepareStatement(query);
         pstmt.setInt(1, grant.getGrantid());
         int deleteCount = pstmt.executeUpdate();
         debug("deleted " + deleteCount + " records");
         pstmt.close();
         dbClose();
         return savedGrant;
      }
      catch (SQLException s) {
         dbClose();
         throw new StoreException("SQL Exception : " + s);
      }
   }

   private static final void debug(String msg) {
       if (logger.isDebugEnabled()) {
           logger.debug(msg);
       }
   }
}

