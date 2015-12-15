/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

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

import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.h2.persistence.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrantStore {
   private static Logger logger = LoggerFactory.getLogger(GrantStore.class);
   protected Connection dbConnection = null;
   protected final static String SQL_ID = "grantid";
   protected final static String SQL_TENANTID = "domainid";
   protected final static String SQL_USERID = "userid";
   protected final static String SQL_ROLEID = "roleid";

   protected GrantStore() {
   }

   protected Connection getDBConnect() throws StoreException {
      dbConnection = H2Store.getConnection(dbConnection);
      return dbConnection;
   }

   protected void dbClean() throws StoreException, SQLException {
      Connection c = dbConnect();
      String sql = "delete from GRANTS where true";
      c.createStatement().execute(sql);
      c.close();
   }

   protected Connection dbConnect() throws StoreException {

      final Connection conn = getDBConnect();
      Statement stmt = null;
      try {
         final DatabaseMetaData dbm = conn.getMetaData();
         final String[] tableTypes = { "TABLE" };

         // Adds a synchronization barrier to ensure only one thread is creating the
         // table.  The synchronization covers the check for table existence, as well
         // as table creation, forcing atomicity in the application layer.
         synchronized (this) {
            final ResultSet rs = dbm.getTables(null, null, "GRANTS",
                  tableTypes);
            if (rs.next()) {
               logger.info("The GRANTS table already exists;  GrantStore will utilize the existing table");
            } else {
               logger.info("The GRANTS table does not exist; creating the table");
               stmt = conn.createStatement();
               final String sql = "CREATE TABLE GRANTS "
                     + "(grantid    VARCHAR(128) PRIMARY KEY,"
                     + "domainid    VARCHAR(128)         NOT NULL, "
                     + "userid      VARCHAR(128)         NOT NULL, "
                     + "roleid      VARCHAR(128)         NOT NULL)";
               stmt.executeUpdate(sql);
            }
         }
      } catch (SQLException e) {
         logger.error("SQLException", e);
         throw new StoreException(e);
      } finally {
         if (stmt != null) {
            try {
               stmt.close();
            } catch (SQLException e) {
               logger.error("Unable to close the open statement", e);
               throw new StoreException(e);
            }
         }
      }
      return conn;
   }

   protected void dbClose() {
      if (dbConnection != null) {
         try {
            dbConnection.close();
         } catch (Exception e) {
            logger.error("Cannot close Database Connection " + e);
         }
      }
   }

   @Override
   protected void finalize() throws Throwable {
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
      } catch (SQLException sqle) {
         logger.error("SQL Exception : " + sqle);
         throw sqle;
      }
      return grant;
   }

   protected Grants getGrants(String did, String uid) throws StoreException {
      Grants grants = new Grants();
      List<Grant> grantList = new ArrayList<Grant>();
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn
               .prepareStatement("SELECT * FROM grants WHERE domainid = ? AND userid = ?");
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
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
      grants.setGrants(grantList);
      return grants;
   }

   protected Grants getGrants(String userid) throws StoreException {
      Grants grants = new Grants();
      List<Grant> grantList = new ArrayList<Grant>();
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn
               .prepareStatement("SELECT * FROM GRANTS WHERE userid = ? ");
         pstmt.setString(1, userid);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            Grant grant = rsToGrant(rs);
            grantList.add(grant);
         }
         rs.close();
         pstmt.close();
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
      grants.setGrants(grantList);
      return grants;
   }

   protected Grant getGrant(String id) throws StoreException {
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn
               .prepareStatement("SELECT * FROM GRANTS WHERE grantid = ? ");
         pstmt.setString(1, id);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            Grant grant = rsToGrant(rs);
            rs.close();
            pstmt.close();
            return grant;
         } else {
            rs.close();
            pstmt.close();
            return null;
         }
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
   }

   protected Grant getGrant(String did, String uid, String rid)
         throws StoreException {
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn
               .prepareStatement("SELECT * FROM GRANTS WHERE domainid = ? AND userid = ? AND roleid = ? ");
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
         } else {
            rs.close();
            pstmt.close();
            return null;
         }
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
   }

   protected Grant createGrant(Grant grant) throws StoreException {
      Connection conn = dbConnect();
      try {
         String query = "insert into grants  (grantid,domainid,userid,roleid) values(?,?,?,?)";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(
               1,
               IDMStoreUtil.createGrantid(grant.getUserid(),
                     grant.getDomainid(), grant.getRoleid()));
         statement.setString(2, grant.getDomainid());
         statement.setString(3, grant.getUserid());
         statement.setString(4, grant.getRoleid());
         int affectedRows = statement.executeUpdate();
         if (affectedRows == 0) {
            throw new StoreException(
                  "Creating grant failed, no rows affected.");
         }
         grant.setGrantid(IDMStoreUtil.createGrantid(grant.getUserid(),
               grant.getDomainid(), grant.getRoleid()));
         return grant;
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
   }

   protected Grant deleteGrant(String grantid) throws StoreException {
      Grant savedGrant = this.getGrant(grantid);
      if (savedGrant == null) {
         return null;
      }

      Connection conn = dbConnect();
      try {
         String query = "DELETE FROM GRANTS WHERE grantid = '" + grantid
               + "'";
         Statement st = conn.createStatement();// PreparedStatement statement
                                       // =
                                       // conn.prepareStatement(query);
         // statement.setString(1, savedGrant.getGrantid());
         int deleteCount = st.executeUpdate(query);
         debug("deleted " + deleteCount + " records");
         st.close();
         return savedGrant;
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
   }

   private static final void debug(String msg) {
      if (logger.isDebugEnabled()) {
         logger.debug(msg);
      }
   }
}
