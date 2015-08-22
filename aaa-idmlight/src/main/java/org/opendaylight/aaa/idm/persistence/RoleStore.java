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
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleStore {
   private static Logger logger = LoggerFactory.getLogger(RoleStore.class);
   protected Connection  dbConnection = null;
   protected final static String SQL_ID             = "roleid";
   protected final static String SQL_DOMAIN_ID      = "domainid";
   protected final static String SQL_NAME           = "name";
   protected final static String SQL_DESCR          = "description";
   public final static int       MAX_FIELD_LEN      = 128;

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
         String[] tableTypes = {"TABLE"};
         ResultSet rs = dbm.getTables(null, null, "ROLES", tableTypes);
         if (rs.next()) {
            debug("roles Table already exists");
         }
         else
         {
            logger.info("roles Table does not exist, creating table");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE ROLES " +
                         "(roleid     VARCHAR(128)   PRIMARY KEY," +
                         "name        VARCHAR(128)   NOT NULL, " +
                         "domainid    VARCHAR(128)   NOT NULL, " +
                         "description VARCHAR(128)      NOT NULL)";
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

   protected  Role rsToRole(ResultSet rs) throws SQLException {
      Role role = new Role();
      try {
         role.setRoleid(rs.getString(SQL_ID));
         role.setDomainID(rs.getString(SQL_DOMAIN_ID));
         role.setName(rs.getString(SQL_NAME));
         role.setDescription(rs.getString(SQL_DESCR));
      }
      catch (SQLException sqle) {
         logger.error( "SQL Exception : " + sqle);
            throw sqle;
      }
      return role;
   }

   public Roles getRoles() throws StoreException {
      Roles roles = new Roles();
      List<Role> roleList = new ArrayList<Role>();
      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "SELECT * FROM roles";
      try {
         stmt=conn.createStatement();
         ResultSet rs=stmt.executeQuery(query);
         while (rs.next()) {
            Role role = rsToRole(rs);
            roleList.add(role);
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
      roles.setRoles(roleList);
      return roles;
   }

   public Role getRole(String id) throws StoreException {
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM ROLES WHERE roleid = ? ");
         pstmt.setString(1, id);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            Role role = rsToRole(rs);
            rs.close();
            pstmt.close();
            return role;
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

   public Role createRole(Role role) throws StoreException {
       Connection conn = dbConnect();
       try {
          String query = "insert into roles (roleid,domainid,name,description) values(?,?,?,?)";
          PreparedStatement statement = conn.prepareStatement(query);
          statement.setString(1, role.getName()+"@"+role.getDomainID());
          statement.setString(2, role.getDomainID());
          statement.setString(3,role.getName());
          statement.setString(4,role.getDescription());
          int affectedRows = statement.executeUpdate();
          if (affectedRows == 0) {
             throw new StoreException("Creating role failed, no rows affected.");
          }
          return role;
       }
       catch (SQLException s) {
          throw new StoreException("SQL Exception : " + s);
       }
       finally {
          dbClose();
        }
   }

   public Role putRole(Role role) throws StoreException {

      Role savedRole = this.getRole(role.getRoleid());
      if (savedRole==null) {
         return null;
      }

      if (role.getDescription()!=null) {
         savedRole.setDescription(role.getDescription());
      }
      if (role.getName()!=null) {
         savedRole.setName(role.getName());
      }

      Connection conn = dbConnect();
      try {
         String query = "UPDATE roles SET description = ? WHERE roleid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, savedRole.getDescription());
         statement.setString(2,savedRole.getRoleid());
         statement.executeUpdate();
         statement.close();
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
       }

      return savedRole;
   }

   public Role deleteRole(Role role) throws StoreException {
      Role savedRole = this.getRole(role.getRoleid());
      if (savedRole==null) {
         return null;
      }

      Connection conn = dbConnect();
      try {
         String query = "DELETE FROM DOMAINS WHERE domainid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, savedRole.getRoleid());
         int deleteCount = statement.executeUpdate(query);
         debug("deleted " + deleteCount + " records");
         statement.close();
         return savedRole;
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

