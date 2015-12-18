/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */
public class UserStore {
    private static final Logger LOG = LoggerFactory.getLogger(UserStore.class);
    protected Connection dbConnection = null;
    protected final static String SQL_ID = "userid";
    protected final static String SQL_DOMAIN_ID = "domainid";
    protected final static String SQL_NAME = "name";
    protected final static String SQL_EMAIL = "email";
    protected final static String SQL_PASSWORD = "password";
    protected final static String SQL_DESCR = "description";
    protected final static String SQL_ENABLED = "enabled";
    protected final static String SQL_SALT = "salt";
    public final static int MAX_FIELD_LEN = 128;

    protected UserStore() {
    }

    protected Connection getDBConnect() throws StoreException {
        dbConnection = H2Store.getConnection(dbConnection);
        return dbConnection;
    }

    protected void dbClean() throws StoreException, SQLException {
        Connection c = dbConnect();
        String sql = "delete from users where true";
        c.createStatement().execute(sql);
        c.close();
    }

    protected Connection dbConnect() throws StoreException {
        Connection conn;
        try {
            conn = getDBConnect();
        } catch (StoreException se) {
            throw se;
        }
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            String[] tableTypes = { "TABLE" };
            ResultSet rs = dbm.getTables(null, null, "USERS", tableTypes);
            if (rs.next()) {
                LOG.debug("users Table already exists");
            } else {
                LOG.info("users Table does not exist, creating table");
                Statement stmt = null;
                stmt = conn.createStatement();
                String sql = "CREATE TABLE users " + "(userid    VARCHAR(128) PRIMARY KEY,"
                        + "name       VARCHAR(128)      NOT NULL, "
                        + "domainid   VARCHAR(128)      NOT NULL, "
                        + "email      VARCHAR(128)      NOT NULL, "
                        + "password   VARCHAR(128)      NOT NULL, "
                        + "description VARCHAR(128)     NOT NULL, "
                        + "salt        VARCHAR(15)      NOT NULL, "
                        + "enabled     INTEGER          NOT NULL)";
                stmt.executeUpdate(sql);
                stmt.close();
            }
        } catch (SQLException sqe) {
            throw new StoreException("Cannot connect to database server " + sqe);
        }
        return conn;
    }

    protected void dbClose() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (Exception e) {
                LOG.error("Cannot close Database Connection", e);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        dbClose();
        super.finalize();
    }

    protected User rsToUser(ResultSet rs) throws SQLException {
        User user = new User();
        try {
            user.setUserid(rs.getString(SQL_ID));
            user.setDomainid(rs.getString(SQL_DOMAIN_ID));
            user.setName(rs.getString(SQL_NAME));
            user.setEmail(rs.getString(SQL_EMAIL));
            user.setPassword(rs.getString(SQL_PASSWORD));
            user.setDescription(rs.getString(SQL_DESCR));
            user.setEnabled(rs.getInt(SQL_ENABLED) == 1 ? true : false);
            user.setSalt(rs.getString(SQL_SALT));
        } catch (SQLException sqle) {
            LOG.error("SQL Exception: ", sqle);
            throw sqle;
        }
        return user;
    }

    protected Users getUsers() throws StoreException {
        Users users = new Users();
        List<User> userList = new ArrayList<User>();
        Connection conn = dbConnect();
        Statement stmt = null;
        String query = "SELECT * FROM users";
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                User user = rsToUser(rs);
                userList.add(user);
            }
            rs.close();
            stmt.close();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception");
        } finally {
            dbClose();
        }
        users.setUsers(userList);
        return users;
    }

    protected Users getUsers(String username, String domain) throws StoreException {
        LOG.debug("getUsers for: {} in domain {}", username, domain);

        Users users = new Users();
        List<User> userList = new ArrayList<User>();
        Connection conn = dbConnect();
        try {
            PreparedStatement pstmt = conn
                    .prepareStatement("SELECT * FROM USERS WHERE userid = ? ");
            pstmt.setString(1, IDMStoreUtil.createUserid(username, domain));
            LOG.debug("query string: {}", pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = rsToUser(rs);
                userList.add(user);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            dbClose();
        }
        users.setUsers(userList);
        return users;
    }

    protected User getUser(String id) throws StoreException {
        Connection conn = dbConnect();
        try {
            PreparedStatement pstmt = conn
                    .prepareStatement("SELECT * FROM USERS WHERE userid = ? ");
            pstmt.setString(1, id);
            LOG.debug("query string: {}", pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = rsToUser(rs);
                rs.close();
                pstmt.close();
                return user;
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

    protected User createUser(User user) throws StoreException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getName());
        Preconditions.checkNotNull(user.getDomainid());

        Connection conn = dbConnect();
        try {
            user.setSalt(SHA256Calculator.generateSALT());
            String query = "insert into users (userid,domainid,name,email,password,description,enabled,salt) values(?,?,?,?,?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(query);
            user.setUserid(IDMStoreUtil.createUserid(user.getName(), user.getDomainid()));
            statement.setString(1, user.getUserid());
            statement.setString(2, user.getDomainid());
            statement.setString(3, user.getName());
            statement.setString(4, user.getEmail());
            statement.setString(5, SHA256Calculator.getSHA256(user.getPassword(), user.getSalt()));
            statement.setString(6, user.getDescription());
            statement.setInt(7, user.isEnabled() ? 1 : 0);
            statement.setString(8, user.getSalt());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new StoreException("Creating user failed, no rows affected.");
            }
            return user;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            dbClose();
        }
    }

    protected User putUser(User user) throws StoreException {

        User savedUser = this.getUser(user.getUserid());
        if (savedUser == null) {
            return null;
        }

        if (user.getDescription() != null) {
            savedUser.setDescription(user.getDescription());
        }
        if (user.getName() != null) {
            savedUser.setName(user.getName());
        }
        if (user.isEnabled() != null) {
            savedUser.setEnabled(user.isEnabled());
        }
        if (user.getEmail() != null) {
            savedUser.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            savedUser.setPassword(SHA256Calculator.getSHA256(user.getPassword(), user.getSalt()));
        }

        Connection conn = dbConnect();
        try {
            String query = "UPDATE users SET email = ?, password = ?, description = ?, enabled = ? WHERE userid = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, savedUser.getEmail());
            statement.setString(2, savedUser.getPassword());
            statement.setString(3, savedUser.getDescription());
            statement.setInt(4, savedUser.isEnabled() ? 1 : 0);
            statement.setString(5, savedUser.getUserid());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            dbClose();
        }

        return savedUser;
    }

    protected User deleteUser(String userid) throws StoreException {
        User savedUser = this.getUser(userid);
        if (savedUser == null) {
            return null;
        }

        Connection conn = dbConnect();
        try {
            String query = "DELETE FROM DOMAINS WHERE domainid = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, savedUser.getUserid());
            int deleteCount = statement.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            statement.close();
            return savedUser;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            dbClose();
        }
    }
}
