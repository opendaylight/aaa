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
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserStore {
   private static Logger logger = LoggerFactory.getLogger(UserStore.class);
   protected Connection  dbConnection = null;
   protected final static String SQL_ID             = "userid";
   protected final static String SQL_DOMAIN_ID      = "domainid";
   protected final static String SQL_NAME           = "name";
   protected final static String SQL_EMAIL          = "email";
   protected final static String SQL_PASSWORD       = "password";
   protected final static String SQL_DESCR          = "description";
   protected final static String SQL_ENABLED        = "enabled";
   protected final static String SQL_SALT           = "salt";
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
         ResultSet rs = dbm.getTables(null, null, "USERS", tableTypes);
         if (rs.next()) {
            debug("users Table already exists");
         }
         else
         {
            logger.info("users Table does not exist, creating table");
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "CREATE TABLE users " +
                         "(userid    VARCHAR(128) PRIMARY KEY," +
                         "name       VARCHAR(128)      NOT NULL, " +
                         "domainid   VARCHAR(128)      NOT NULL, " +
                         "email      VARCHAR(128)      NOT NULL, " +
                         "password   VARCHAR(128)      NOT NULL, " +
                         "description VARCHAR(128)     NOT NULL, " +
                         "salt        VARCHAR(15)      NOT NULL, " +
                         "enabled     INTEGER          NOT NULL)" ;
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

   protected User rsToUser(ResultSet rs) throws SQLException {
      User user = new User();
      try {
         user.setUserid(rs.getString(SQL_ID));
         user.setDomainID(rs.getString(SQL_DOMAIN_ID));
         user.setName(rs.getString(SQL_NAME));
         user.setEmail(rs.getString(SQL_EMAIL));
         user.setPassword(rs.getString(SQL_PASSWORD));
         user.setDescription(rs.getString(SQL_DESCR));
         user.setEnabled(rs.getInt(SQL_ENABLED)==1?true:false);
         user.setSalt(rs.getString(SQL_SALT));
      }
      catch (SQLException sqle) {
         logger.error( "SQL Exception : " + sqle);
            throw sqle;
      }
      return user;
   }

   public Users getUsers() throws StoreException {
      Users users = new Users();
      List<User> userList = new ArrayList<User>();
      Connection conn = dbConnect();
      Statement stmt=null;
      String query = "SELECT * FROM users";
      try {
         stmt=conn.createStatement();
         ResultSet rs=stmt.executeQuery(query);
         while (rs.next()) {
            User user = rsToUser(rs);
            userList.add(user);
         }
         rs.close();
         stmt.close();
      }
      catch (SQLException s) {
    	  throw new StoreException("SQL Exception");
   	  }
      finally {
         dbClose();
      }
      users.setUsers(userList);
      return users;
   }

   public Users getUsers(String username,String domain) throws StoreException {
      debug("getUsers for:" + username + " in domain "+domain);
      Users users = new Users();
      List<User> userList = new ArrayList<User>();
      Connection conn = dbConnect();
      try {
          PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM USERS WHERE userid = ? ");
          pstmt.setString(1, username+"@"+domain);
          debug("query string: " + pstmt.toString());
          ResultSet rs = pstmt.executeQuery();
          while (rs.next()) {
              User user = rsToUser(rs);
              userList.add(user);
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
      users.setUsers(userList);
      return users;
   }


   public User getUser(String id) throws StoreException {
      Connection conn = dbConnect();
      try {
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM USERS WHERE userid = ? ");
         pstmt.setString(1, id);
         debug("query string: " + pstmt.toString());
         ResultSet rs = pstmt.executeQuery();
         if (rs.next()) {
            User user = rsToUser(rs);
            rs.close();
            pstmt.close();
            return user;
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

   public User createUser(User user) throws StoreException {
       Connection conn = dbConnect();
       try {
          user.setSalt(SHA256Calculator.generateSALT());
          String query = "insert into users (userid,domainid,name,email,password,description,enabled,salt) values(?,?,?,?,?,?,?,?)";
          PreparedStatement statement = conn.prepareStatement(query);
          user.setUserid(user.getName()+"@"+user.getDomainID());          
          statement.setString(1,user.getUserid());
          statement.setString(2,user.getDomainID());
          statement.setString(3,user.getName());
          statement.setString(4,user.getEmail());
          statement.setString(5,SHA256Calculator.getSHA256(user.getPassword(),user.getSalt()));
          statement.setString(6,user.getDescription());
          statement.setInt(7,user.getEnabled()?1:0);
          statement.setString(8, user.getSalt());
          int affectedRows = statement.executeUpdate();
          if (affectedRows == 0) {
             throw new StoreException("Creating user failed, no rows affected.");
          }
          return user;
       }
       catch (SQLException s) {
          throw new StoreException("SQL Exception : " + s);
       }
       finally {
          dbClose();
        }
   }

   public User putUser(User user) throws StoreException {

      User savedUser = this.getUser(user.getUserid());
      if (savedUser==null) {
         return null;
      }

      if (user.getDescription()!=null) {
         savedUser.setDescription(user.getDescription());
      }
      if (user.getName()!=null) {
         savedUser.setName(user.getName());
      }
      if (user.getEnabled()!=null) {
         savedUser.setEnabled(user.getEnabled());
      }
      if (user.getEmail()!=null) {
         savedUser.setEmail(user.getEmail());
      }
      if (user.getPassword()!=null) {
         savedUser.setPassword(SHA256Calculator.getSHA256(user.getPassword(),user.getSalt()));
      }

      Connection conn = dbConnect();
      try {
         String query = "UPDATE users SET email = ?, password = ?, description = ?, enabled = ? WHERE userid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, savedUser.getEmail());
         statement.setString(2, savedUser.getPassword());
         statement.setString(3, savedUser.getDescription());
         statement.setInt(4, savedUser.getEnabled()?1:0);
         statement.setString(5,savedUser.getUserid());
         statement.executeUpdate();
         statement.close();
      }
      catch (SQLException s) {
         throw new StoreException("SQL Exception : " + s);
      }
      finally {
         dbClose();
       }

      return savedUser;
   }

   public User deleteUser(User user) throws StoreException {
      User savedUser = this.getUser(user.getUserid());
      if (savedUser==null) {
         return null;
      }

      Connection conn = dbConnect();
      try {
         String query = "DELETE FROM DOMAINS WHERE domainid = ?";
         PreparedStatement statement = conn.prepareStatement(query);
         statement.setString(1, savedUser.getUserid());
         int deleteCount = statement.executeUpdate(query);
         debug("deleted " + deleteCount + " records");
         statement.close();
         return savedUser;
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

