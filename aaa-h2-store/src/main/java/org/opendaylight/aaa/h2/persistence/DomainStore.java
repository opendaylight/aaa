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

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class DomainStore {
   private static Logger logger = LoggerFactory.getLogger(DomainStore.class);

   protected Connection dbConnection = null;
   protected final static String SQL_ID = "domainid";
   protected final static String SQL_NAME = "name";
   protected final static String SQL_DESCR = "description";
   protected final static String SQL_ENABLED = "enabled";

   protected DomainStore() {
   }

   protected Connection getDBConnect() throws StoreException {
      dbConnection = H2Store.getConnection(dbConnection);
      return dbConnection;
   }

   protected void dbClean() throws StoreException, SQLException {
      Connection c = dbConnect();
      String sql = "delete from DOMAINS where true";
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
            // capitalization is required for DOMAINS
            final ResultSet rs = dbm.getTables(null, null, "DOMAINS",
                  tableTypes);
            if (rs.next()) {
               logger.info("The DOMAINS table already exists;  DomainStore will utilize the existing table");
            } else {
               logger.info("The DOMAINS table does not exist; creating the table");
               stmt = conn.createStatement();
               final String sql = "CREATE TABLE DOMAINS "
                     + "(domainid   VARCHAR(128)      PRIMARY KEY,"
                     + "name        VARCHAR(128)      UNIQUE NOT NULL, "
                     + "description VARCHAR(128)      , "
                     + "enabled     INTEGER           NOT NULL)";
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

   protected Domain rsToDomain(ResultSet rs) throws SQLException {
      Domain domain = new Domain();
      try {
         domain.setDomainid(rs.getString(SQL_ID));
         domain.setName(rs.getString(SQL_NAME));
         domain.setDescription(rs.getString(SQL_DESCR));
         domain.setEnabled(rs.getInt(SQL_ENABLED) == 1 ? true : false);
      } catch (SQLException sqle) {
         logger.error("SQL Exception : " + sqle);
         throw sqle;
      }
      return domain;
   }

   protected Domains getDomains() throws StoreException {
      Domains domains = new Domains();
      List<Domain> domainList = new ArrayList<Domain>();
      Connection conn = dbConnect();
      Statement stmt = null;
      String query = "SELECT * FROM DOMAINS";
      try {
         stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query);
         while (rs.next()) {
            Domain domain = rsToDomain(rs);
            domainList.add(domain);
         }
         rs.close();
         stmt.close();
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
      domains.setDomains(domainList);
      return domains;
   }

   protected Domains getDomains(String domainName) throws StoreException {
      debug("getDomains for:" + domainName);
      Domains domains = new Domains();
      List<Domain> domainList = new ArrayList<Domain>();
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn
               .prepareStatement("SELECT * FROM DOMAINS WHERE name = ?");
         pstmt.setString(1, domainName);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            Domain domain = rsToDomain(rs);
            domainList.add(domain);
         }
         rs.close();
         pstmt.close();
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
      domains.setDomains(domainList);
      return domains;
   }

   protected Domain getDomain(String id) throws StoreException {
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn
               .prepareStatement("SELECT * FROM DOMAINS WHERE domainid = ? ");
         pstmt.setString(1, id);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            Domain domain = rsToDomain(rs);
            rs.close();
            pstmt.close();
            return domain;
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

   protected Domain createDomain(Domain domain) throws StoreException {
      Preconditions.checkNotNull(domain);
      Preconditions.checkNotNull(domain.getName());
      Preconditions.checkNotNull(domain.isEnabled());
      Connection conn = dbConnect();
      try {
         String query = "insert into DOMAINS (domainid,name,description,enabled) values(?, ?, ?, ?)";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, domain.getName());
         statement.setString(2, domain.getName());
         statement.setString(3, domain.getDescription());
         statement.setInt(4, domain.isEnabled() ? 1 : 0);
         int affectedRows = statement.executeUpdate();
         if (affectedRows == 0) {
            throw new StoreException(
                  "Creating domain failed, no rows affected.");
         }
         domain.setDomainid(domain.getName());
         return domain;
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }
   }

   protected Domain putDomain(Domain domain) throws StoreException {
      Domain savedDomain = this.getDomain(domain.getDomainid());
      if (savedDomain == null) {
         return null;
      }

      if (domain.getDescription() != null) {
         savedDomain.setDescription(domain.getDescription());
      }
      if (domain.getName() != null) {
         savedDomain.setName(domain.getName());
      }
      if (domain.isEnabled() != null) {
         savedDomain.setEnabled(domain.isEnabled());
      }

      Connection conn = dbConnect();
      try {
         String query = "UPDATE DOMAINS SET description = ?, enabled = ? WHERE domainid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, savedDomain.getDescription());
         statement.setInt(2, savedDomain.isEnabled() ? 1 : 0);
         statement.setString(3, savedDomain.getDomainid());
         statement.executeUpdate();
         statement.close();
      } catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      } finally {
         dbClose();
      }

      return savedDomain;
   }

   protected Domain deleteDomain(String domainid) throws StoreException {
      Domain deletedDomain = this.getDomain(domainid);
      if (deletedDomain == null) {
         return null;
      }
      Connection conn = dbConnect();
      try {
         String query = "DELETE FROM DOMAINS WHERE domainid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, deletedDomain.getDomainid());
         int deleteCount = statement.executeUpdate();
         debug("deleted " + deleteCount + " records");
         statement.close();
         return deletedDomain;
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
