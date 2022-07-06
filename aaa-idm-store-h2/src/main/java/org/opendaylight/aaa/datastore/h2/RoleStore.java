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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AbstractStore} of {@link Role}s.
 *
 * @author peter.mellquist@hp.com
 */
final class RoleStore extends AbstractStore<Role> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleStore.class);

    /**
     * Name of our SQL table. This constant lives here rather than in {@link SQLTable} for brevity.
     */
    static final @NonNull String TABLE = "AAA_ROLES";

    static {
        SQLTable.ROLE.verifyTable(TABLE);
    }

    /**
     * Column storing {@link Role#getRoleid()}, which is a flat namespace.
     */
    // FIXME: rename to "id"
    @VisibleForTesting
    static final String COL_ID = "roleid";
    /**
     * Column storing {@link Role#getName()}, which is a short name.
     */
    @VisibleForTesting
    static final String COL_NAME = "name";
    // FIXME: document this column
    // FIXME: rename to "domain_id"
    // FIXME: cross-reference DomainStore?
    private static final String COL_DOMAIN_ID = "domainid";
    /**
     * Column storing {@link Role#getDescription()}, which is a detailed description.
     * FIXME: this should be optional, justlike {@link DomainStore#COL_DESC}
     */
    @VisibleForTesting
    static final String COL_DESC = "description";

    RoleStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE);
    }

    @Override
    void createTable(final Statement stmt) throws SQLException {
        stmt.executeUpdate("CREATE TABLE " + TABLE + " ("
            + COL_ID        + " VARCHAR(128) PRIMARY KEY, "
            + COL_NAME      + " VARCHAR(128) NOT NULL, "
            // FIXME: FOREIGN_KEY to DomainStore?
            + COL_DOMAIN_ID + " VARCHAR(128) NOT NULL, "
            + COL_DESC      + " VARCHAR(128) NOT NULL)");
    }

    @Override
    void cleanTable(final Statement stmt) throws SQLException {
        stmt.execute("DELETE FROM " + TABLE);
    }

    @Override
    protected Role fromResultSet(final ResultSet rs) throws SQLException {
        Role role = new Role();
        try {
            role.setRoleid(rs.getString(COL_ID));
            role.setDomainid(rs.getString(COL_DOMAIN_ID));
            role.setName(rs.getString(COL_NAME));
            role.setDescription(rs.getString(COL_DESC));
        } catch (SQLException sqle) {
            LOG.error("SQL Exception: ", sqle);
            throw sqle;
        }
        return role;
    }

    Roles getRoles() throws StoreException {
        Roles roles = new Roles();
        roles.setRoles(listAll());
        return roles;
    }

    Role getRole(final String id) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + COL_ID + " = ?")) {
            stmt.setString(1, id);

            LOG.debug("getRole() request: {}", stmt);
            return firstFromStatement(stmt);
        } catch (SQLException s) {
            throw new StoreException("SQL Exception: " + s);
        }
    }

    Role createRole(final Role role) throws StoreException {
        requireNonNull(role);
        requireNonNull(role.getName());
        requireNonNull(role.getDomainid());

        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("INSERT INTO " + TABLE + " ("
                + COL_ID + ", " + COL_DOMAIN_ID + ", " + COL_NAME + ", " + COL_DESC + ") VALUES (?, ?, ?, ?)")) {
            role.setRoleid(IDMStoreUtil.createRoleid(role.getName(), role.getDomainid()));
            stmt.setString(1, role.getRoleid());
            stmt.setString(2, role.getDomainid());
            stmt.setString(3, role.getName());
            stmt.setString(4, role.getDescription());

            LOG.debug("createRole() request: {}", stmt);
            if (stmt.executeUpdate() == 0) {
                throw new StoreException("Creating role failed, no rows affected.");
            }
            return role;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }

    Role putRole(final Role role) throws StoreException {
        Role savedRole = getRole(role.getRoleid());
        if (savedRole == null) {
            return null;
        }

        if (role.getDescription() != null) {
            savedRole.setDescription(role.getDescription());
        }
        if (role.getName() != null) {
            savedRole.setName(role.getName());
        }

        try (var conn = dbConnect();
             var stmt = conn.prepareStatement(
                 "UPDATE " + TABLE + " SET " + COL_DESC + " = ? WHERE " + COL_ID + " = ?")) {
            stmt.setString(1, savedRole.getDescription());
            stmt.setString(2, savedRole.getRoleid());

            LOG.debug("putRole() request: {}", stmt);
            stmt.executeUpdate();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }

        return savedRole;
    }

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    Role deleteRole(final String roleid) throws StoreException {
        // FIXME: remove this once we have a more modern H2
        final String escaped = StringEscapeUtils.escapeHtml4(roleid);
        Role savedRole = getRole(escaped);
        if (savedRole == null) {
            return null;
        }

        try (var conn = dbConnect();
             var stmt = conn.createStatement()) {
            // FIXME: prepare statement instead
            final String query = String.format("DELETE FROM " + TABLE + " WHERE " + COL_ID + " = '%s'", escaped);
            LOG.debug("deleteRole() request: {}", query);

            int deleteCount = stmt.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedRole;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }
}
