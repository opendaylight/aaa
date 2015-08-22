/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
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

public class GrantStore {
   private static Logger logger = LoggerFactory.getLogger(GrantStore.class);
   protected Connection  dbConnection = null;
   protected final static String SQL_ID             = "grantid";
   protected final static String SQL_TENANTID       = "domainid";
   protected final static String SQL_USERID         = "userid";
   protected final static String SQL_ROLEID         = "roleid";

   protected Connection getDBConnect() throws StoreException {
      dbConnection = IdmLightApplication.getConnection(dbConnection);
      return dbConnection;
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
                         "(grantid    VARCHAR(128) PRIMARY KEY," +
                         "domainid    VARCHAR(128)         NOT NULL, " +
                         "userid      VARCHAR(128)         NOT NULL, " +
                         "roleid      VARCHAR(128)         NOT NULL)" ;
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
protected void finalize () throws Throwable {
      dbClose();
      super.finalize();
   }

   protected Grant rsToGrant(ResultSet rs) throws SQLException {
      Grant grant = new Grant();
      try {
         grant.setGrantid(rs.getString(SQL_ID));
         grant.setDomainid(rs.getString(SQL_TENANTID));
         grant.setUserid(rs.getString(SQL_USERID));
         grant.setRoleid(rs.getString(SQL_ROLEID));
      }
      catch (SQLException sqle) {
         logger.error( "SQL Exception : " + sqle);
            throw sqle;
      }
      return grant;
   }

   public Grants getGrants(String did, String uid) throws StoreException {
      Grants grants = new Grants();
      List<Grant> grantList = new ArrayList<Grant>();
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM grants WHERE domainid = ? AND userid = ?");
         pstmt.setString(1, did);
         pstmt.setString(2, uid);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            Grant grant = rsToGrant(rs);
            grantList.add(grant);
         }
         rs.close();
         pstmt.close();
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
       }
      grants.setGrants(grantList);
      return grants;
   }

   public Grants getGrants(int uid) throws StoreException {
      Grants grants = new Grants();
      List<Grant> grantList = new ArrayList<Grant>();
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM GRANTS WHERE userid = ? ");
         pstmt.setInt(1, uid);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            Grant grant = rsToGrant(rs);
            grantList.add(grant);
         }
         rs.close();
         pstmt.close();
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
      }
      grants.setGrants(grantList);
      return grants;
   }


   public Grant  getGrant(String id) throws StoreException {
      Connection conn = dbConnect();
      try {
          PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM GRANTS WHERE grantid = ? ");
          pstmt.setString(1, id);
          debug("query string: " + pstmt.toString());
          ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            pstmt.close();
            return grant;
         }
         else {
            rs.close();
            pstmt.close();
            return null;
         }
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
        }
   }

   public Grant getGrant(String did,String uid,String rid) throws StoreException {
      Connection conn = dbConnect();
      try {
          PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM GRANTS WHERE domainid = ? AND userid = ? AND roleid = ? ");
          pstmt.setString(1, did);
          pstmt.setString(2, uid);
          pstmt.setString(3, rid);
          debug("query string: " + pstmt.toString());
          ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            pstmt.close();
            return grant;
         }
         else {
            rs.close();
            pstmt.close();
            return null;
         }
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
        }
   }


   public Grant createGrant(Grant grant) throws StoreException {
       Connection conn = dbConnect();
       try {
          String query = "insert into grants  (grantid,domainid,userid,roleid) values(?,?,?,?)";
          PreparedStatement statement = conn.prepareStatement(query);
          statement.setString(1,grant.getUserid()+"@"+grant.getDomainid()+"@"+grant.getRoleid());
          statement.setString(2,grant.getDomainid());
          statement.setString(3,grant.getUserid());
          statement.setString(4,grant.getRoleid());
          int affectedRows = statement.executeUpdate();
          if (affectedRows == 0) {
             throw new StoreException("Creating grant failed, no rows affected.");
          }
          return grant;
       }
       catch (SQLException s) {
          throw new StoreException("SQL Exception : " + s);
       }
       finally {
          dbClose();
         }
   }

   public Grant deleteGrant(Grant grant) throws StoreException {
      Grant savedGrant = this.getGrant(grant.getGrantid());
      if (savedGrant==null) {
         return null;
      }

      Connection conn = dbConnect();
      try {
          String query = "DELETE FROM GRANTS WHERE grantid = ?";
          PreparedStatement statement = conn.prepareStatement(query);
          statement.setString(1, savedGrant.getGrantid());
         int deleteCount = statement.executeUpdate(query);
         debug("deleted " + deleteCount + " records");
         statement.close();
         return savedGrant;
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
          dbClose();
        }
   }

   private static final void debug(String msg) {
       if (logger.isDebugEnabled()) {
           logger.debug(msg);
       }
   }
}

