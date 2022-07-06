/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.text.StringEscapeUtils;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for users.
 *
 * @author peter.mellquist@hp.com
 */
final class UserStore extends AbstractStore<User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserStore.class);

    static final String TABLE = "AAA_USERS";

    static {
        SQLTable.USER.verifyTable(TABLE);
    }

    /**
     * Column storing {@link User#getUserid()}, which is a flat namespace.
     */
    // FIXME: rename to "id"
    @VisibleForTesting
    static final String COL_ID = "userid";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_DOMAIN_ID = "domainid";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_NAME = "name";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_EMAIL = "email";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_PASSWORD = "password";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_DESC = "description";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_ENABLED = "enabled";
    // FIXME: javadoc
    private static final String COL_SALT = "salt";

    private final PasswordHashService passwordService;

    UserStore(final ConnectionProvider dbConnectionFactory, final PasswordHashService passwordService) {
        super(dbConnectionFactory, TABLE);
        this.passwordService = requireNonNull(passwordService);
    }

    @Override
    void createTable(final Statement stmt) throws SQLException {
        stmt.executeUpdate("CREATE TABLE " + TABLE + " ("
            + COL_ID        + " VARCHAR(128) PRIMARY KEY, "
            + COL_NAME      + " VARCHAR(128) NOT NULL, "
            // FIXME: foreign key to DomainStore.COL_ID?
            + COL_DOMAIN_ID + " VARCHAR(128) NOT NULL, "
            + COL_EMAIL     + " VARCHAR(128) NOT NULL, "
            + COL_DESC      + " VARCHAR(128) NOT NULL, "
            // FIXME: is 'salt' even used? Some comparators are not storing hashes, either
            + COL_PASSWORD  + " VARCHAR(128) NOT NULL, "
            + COL_SALT      + " VARCHAR(128) NOT NULL, "
            + COL_ENABLED   + " BOOLEAN      NOT NULL)");
    }

    @Override
    void cleanTable(final Statement stmt) throws SQLException {
        stmt.execute("DELETE FROM " + TABLE);
    }

    @Override
    protected User fromResultSet(final ResultSet rs) throws SQLException {
        User user = new User();
        try {
            user.setUserid(rs.getString(COL_ID));
            user.setDomainid(rs.getString(COL_DOMAIN_ID));
            user.setName(rs.getString(COL_NAME));
            user.setEmail(rs.getString(COL_EMAIL));
            user.setPassword(rs.getString(COL_PASSWORD));
            user.setDescription(rs.getString(COL_DESC));
            user.setEnabled(rs.getBoolean(COL_ENABLED));
            user.setSalt(rs.getString(COL_SALT));
        } catch (SQLException e) {
            LOG.error("SQL Exception: ", e);
            throw e;
        }
        return user;
    }

    Users getUsers() throws StoreException {
        Users users = new Users();
        users.setUsers(listAll());
        return users;
    }

    Users getUsers(final String username, final String domain) throws StoreException {
        final Users users;
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " USERS WHERE " + COL_ID + " = ?")) {
            stmt.setString(1, IDMStoreUtil.createUserid(username, domain));
            LOG.debug("getUsers() request: {}", stmt);

            users = new Users();
            users.setUsers(listFromStatement(stmt));
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
        return users;
    }

    User getUser(final String id) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + COL_ID + " = ? ")) {
            stmt.setString(1, id);
            LOG.debug("getUser() request: {}", stmt);

            return firstFromStatement(stmt);
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }

    User createUser(final User user) throws StoreException {
        requireNonNull(user);
        requireNonNull(user.getName());
        requireNonNull(user.getDomainid());

        final var passwordHash = passwordService.getPasswordHash(user.getPassword());
        user.setSalt(passwordHash.getSalt());

        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("INSERT INTO " + TABLE + " ("
                 + COL_ID + ", " + COL_DOMAIN_ID + ", " + COL_NAME + ", " + COL_EMAIL + ", " + COL_PASSWORD + ", "
                 + COL_DESC + ", " + COL_ENABLED + ", " + COL_SALT + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            user.setUserid(IDMStoreUtil.createUserid(user.getName(), user.getDomainid()));
            stmt.setString(1, user.getUserid());
            stmt.setString(2, user.getDomainid());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, passwordHash.getHashedPassword());
            stmt.setString(6, user.getDescription());
            stmt.setBoolean(7, user.isEnabled());
            stmt.setString(8, user.getSalt());
            LOG.debug("createUser() request: {}", stmt);

            if (stmt.executeUpdate() == 0) {
                throw new StoreException("Creating user failed, no rows affected.");
            }
            return user;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }

    User putUser(final User user) throws StoreException {
        final var savedUser = getUser(user.getUserid());
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
            // If a new salt is provided, use it. Otherwise, derive salt from existing.
            String salt = user.getSalt();
            if (salt == null) {
                salt = savedUser.getSalt();
            }
            final var passwordHash = passwordService.getPasswordHash(user.getPassword(), salt);
            savedUser.setPassword(passwordHash.getHashedPassword());
        }

        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("UPDATE " + TABLE + " SET " + COL_EMAIL + " = ?, "
                 + COL_PASSWORD + " = ?," + COL_DESC + " = ?, " + COL_ENABLED + "= ? WHERE " + COL_ID + " = ?")) {
            stmt.setString(1, savedUser.getEmail());
            stmt.setString(2, savedUser.getPassword());
            stmt.setString(3, savedUser.getDescription());
            stmt.setBoolean(4, savedUser.isEnabled());
            stmt.setString(5, savedUser.getUserid());
            LOG.debug("putUser() request: {}", stmt);

            stmt.executeUpdate();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }

        return savedUser;
    }

    @SuppressFBWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", justification = "Weird original code")
    User deleteUser(final String userid) throws StoreException {
        // FIXME: remove this once we have a more modern H2
        final var escaped = StringEscapeUtils.escapeHtml4(userid);
        final var savedUser = getUser(escaped);
        if (savedUser == null) {
            return null;
        }

        try (var conn = dbConnect();
             var stmt = conn.createStatement()) {
            // FIXME: prepare statement instead
            final var query = String.format("DELETE FROM " + TABLE + " WHERE " + COL_ID + " = '%s'", escaped);
            LOG.debug("deleteUser() request: {}", query);

            int deleteCount = stmt.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedUser;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }
}
