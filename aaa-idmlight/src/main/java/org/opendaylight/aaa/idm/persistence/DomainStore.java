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
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Domains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class DomainStore {
   private static Logger logger = LoggerFactory.getLogger(DomainStore.class);

   protected Connection  dbConnection = null;
   protected final static String SQL_ID             = "domainid";
   protected final static String SQL_NAME           = "name";
   protected final static String SQL_DESCR          = "description";
   protected final static String SQL_ENABLED        = "enabled";

   protected Connection getDBConnect() throws StoreException {
      dbConnection = IdmLightApplication.getConnection(dbConnection);
      return dbConnection;
   }

   protected void dbClean() throws StoreException, SQLException{
      Connection c = dbConnect();
      String sql = "delete from DOMAINS where true";
      c.createStatement().execute(sql);
      c.close();
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
         String[] tableTypes = {"TABLE"};
         ResultSet rs = dbm.getTables(null, null, "DOMAINS", tableTypes);//Caps required in get Tables
         if (rs.next()) {
            debug("DOMAINS Table already exists.");
         }
         else
         {
            logger.info("in dbConnect, domains Table does not exist, creating table");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE DOMAINS "  +
                         "(domainid   VARCHAR(128)      PRIMARY KEY," +
                         "name        VARCHAR(128)      UNIQUE NOT NULL, " +
                         "description VARCHAR(128)      , " +
                         "enabled     INTEGER           NOT NULL)" ;
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
   protected void finalize () throws Throwable  {
      dbClose();
      super.finalize();
   }

   protected Domain rsToDomain(ResultSet rs) throws SQLException {
      Domain domain = new Domain();
      try {
         domain.setDomainid(rs.getString(SQL_ID));
         domain.setName(rs.getString(SQL_NAME));
         domain.setDescription(rs.getString(SQL_DESCR));
         domain.setEnabled(rs.getInt(SQL_ENABLED)==1?true:false);
      }
      catch (SQLException sqle) {
         logger.error( "SQL Exception : " + sqle);
            throw sqle;
      }
      return domain;
   }

   public Domains getDomains() throws StoreException {
      Domains domains = new Domains();
      List<Domain> domainList = new ArrayList<Domain>();
      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "SELECT * FROM DOMAINS";
      try {
         stmt=conn.createStatement();
         ResultSet rs=stmt.executeQuery(query);
         while (rs.next()) {
            Domain domain = rsToDomain(rs);
            domainList.add(domain);
         }
         rs.close();
         stmt.close();
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
        }
      domains.setDomains(domainList);
      return domains;
   }

   public Domains getDomains(String domainName) throws StoreException {
      debug("getDomains for:" + domainName);
      Domains domains = new Domains();
      List<Domain> domainList = new ArrayList<Domain>();
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM DOMAINS WHERE name = ?");
         pstmt.setString(1, domainName);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            Domain domain = rsToDomain(rs);
            domainList.add(domain);
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
      domains.setDomains(domainList);
      return domains;
   }


   public Domain getDomain(String id) throws StoreException {
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM DOMAINS WHERE domainid = ? ");
         pstmt.setString(1, id);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            Domain domain = rsToDomain(rs);
            rs.close();
            pstmt.close();
            return domain;
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

   public Domain createDomain(Domain domain) throws StoreException {
       Preconditions.checkNotNull(domain);
       Preconditions.checkNotNull(domain.getName());
       Preconditions.checkNotNull(domain.getEnabled());
       Connection conn = dbConnect();
       try {
          String query = "insert into DOMAINS (domainid,name,description,enabled) values(?, ?, ?, ?)";
          PreparedStatement statement = conn.prepareStatement(query);
          statement.setString(1,domain.getName());
          statement.setString(2,domain.getName());
          statement.setString(3,domain.getDescription());
          statement.setInt(4,domain.getEnabled()?1:0);
          int affectedRows = statement.executeUpdate();
          if (affectedRows == 0) {
             throw new StoreException("Creating domain failed, no rows affected.");
          }
          domain.setDomainid(domain.getName());
          return domain;
       }
       catch (SQLException s) {
          throw new StoreException("SQL Exception : " + s);
       }
       finally {
           dbClose();
       }
   }

   public Domain putDomain(Domain domain) throws StoreException {
      Domain savedDomain = this.getDomain(domain.getDomainid());
      if (savedDomain==null) {
         return null;
      }

      if (domain.getDescription()!=null) {
         savedDomain.setDescription(domain.getDescription());
      }
      if (domain.getName()!=null) {
         savedDomain.setName(domain.getName());
      }
      if (domain.getEnabled()!=null) {
         savedDomain.setEnabled(domain.getEnabled());
      }

      Connection conn = dbConnect();
      try {
         String query = "UPDATE DOMAINS SET description = ?, enabled = ? WHERE domainid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, savedDomain.getDescription());
         statement.setInt(2, savedDomain.getEnabled()?1:0);
         statement.setString(3,savedDomain.getDomainid());
         statement.executeUpdate();
         statement.close();
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
          dbClose();
        }

      return savedDomain;
   }

   public Domain deleteDomain(Domain domain) throws StoreException {
      Domain deletedDomain = this.getDomain(domain.getDomainid());
      if (deletedDomain==null) {
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
