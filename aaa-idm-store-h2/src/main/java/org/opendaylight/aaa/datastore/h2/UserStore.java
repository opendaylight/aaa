/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import org.apache.commons.text.StringEscapeUtils;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for users.
 *
 * @author peter.mellquist@hp.com
 *
 */
public class UserStore extends AbstractStore<User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserStore.class);

    public static final String SQL_ID = "userid";
    public static final String SQL_DOMAIN_ID = "domainid";
    public static final String SQL_NAME = "name";
    public static final String SQL_EMAIL = "email";
    public static final String SQL_PASSWORD = "password";
    public static final String SQL_DESCR = "description";
    public static final String SQL_ENABLED = "enabled";
    public static final String SQL_SALT = "salt";
    private static final String TABLE_NAME = "USERS";

    private final PasswordHashService passwordService;

    public UserStore(final ConnectionProvider dbConnectionFactory, final PasswordHashService passwordService) {
        super(dbConnectionFactory, TABLE_NAME);
        this.passwordService = Objects.requireNonNull(passwordService);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE users "
            + "(userid     VARCHAR(128) PRIMARY KEY, "
            + "name        VARCHAR(128)      NOT NULL, "
            + "domainid    VARCHAR(128)      NOT NULL, "
            + "email       VARCHAR(128)      NOT NULL, "
            + "password    VARCHAR(128)      NOT NULL, "
            + "description VARCHAR(128)      NOT NULL, "
            + "salt        VARCHAR(128)      NOT NULL, "
            + "enabled     INTEGER           NOT NULL)";
    }

    @Override
    protected User fromResultSet(final ResultSet rs) throws SQLException {
        User user = new User();
        try {
            user.setUserid(rs.getString(SQL_ID));
            user.setDomainid(rs.getString(SQL_DOMAIN_ID));
            user.setName(rs.getString(SQL_NAME));
            user.setEmail(rs.getString(SQL_EMAIL));
            user.setPassword(rs.getString(SQL_PASSWORD));
            user.setDescription(rs.getString(SQL_DESCR));
            user.setEnabled(rs.getInt(SQL_ENABLED) == 1);
            user.setSalt(rs.getString(SQL_SALT));
        } catch (SQLException sqle) {
            LOG.error("SQL Exception: ", sqle);
            throw sqle;
        }
        return user;
    }

    public Users getUsers() throws StoreException {
        Users users = new Users();
        users.setUsers(listAll());
        return users;
    }

    protected Users getUsers(final String username, final String domain) throws StoreException {
        LOG.debug("getUsers for: {} in domain {}", username, domain);

        Users users = new Users();
        try (Connection conn = dbConnect();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM USERS WHERE userid = ? ")) {
            pstmt.setString(1, IDMStoreUtil.createUserid(username, domain));
            LOG.debug("query string: {}", pstmt);
            users.setUsers(listFromStatement(pstmt));
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
        return users;
    }

    public User getUser(final String id) throws StoreException {
        try (Connection conn = dbConnect();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM USERS WHERE userid = ? ")) {
            pstmt.setString(1, id);
            LOG.debug("query string: {}", pstmt);
            return firstFromStatement(pstmt);
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }

    protected User createUser(final User user) throws StoreException {
        requireNonNull(user);
        requireNonNull(user.getName());
        requireNonNull(user.getDomainid());

        final PasswordHash passwordHash = passwordService.getPasswordHash(user.getPassword());
        user.setSalt(passwordHash.getSalt());
        String query =
                "insert into users"
                + " (userid,domainid,name,email,password,description,enabled,salt) values(?,?,?,?,?,?,?,?)";
        try (Connection conn = dbConnect(); PreparedStatement statement = conn.prepareStatement(query)) {
            user.setUserid(IDMStoreUtil.createUserid(user.getName(), user.getDomainid()));
            statement.setString(1, user.getUserid());
            statement.setString(2, user.getDomainid());
            statement.setString(3, user.getName());
            statement.setString(4, user.getEmail());
            statement.setString(5, passwordHash.getHashedPassword());
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
        }
    }

    public User putUser(final User user) throws StoreException {

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
            // If a new salt is provided, use it. Otherwise, derive salt from
            // existing.
            String salt = user.getSalt();
            if (salt == null) {
                salt = savedUser.getSalt();
            }
            final PasswordHash passwordHash = passwordService.getPasswordHash(user.getPassword(), salt);
            savedUser.setPassword(passwordHash.getHashedPassword());
        }

        String query = "UPDATE users SET email = ?, password = ?, description = ?, enabled = ? WHERE userid = ?";
        try (Connection conn = dbConnect(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, savedUser.getEmail());
            statement.setString(2, savedUser.getPassword());
            statement.setString(3, savedUser.getDescription());
            statement.setInt(4, savedUser.isEnabled() ? 1 : 0);
            statement.setString(5, savedUser.getUserid());
            statement.executeUpdate();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }

        return savedUser;
    }

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    protected User deleteUser(String userid) throws StoreException {
        userid = StringEscapeUtils.escapeHtml4(userid);
        User savedUser = this.getUser(userid);
        if (savedUser == null) {
            return null;
        }

        String query = String.format("DELETE FROM USERS WHERE userid = '%s'", userid);
        try (Connection conn = dbConnect(); Statement statement = conn.createStatement()) {
            int deleteCount = statement.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedUser;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }
}
