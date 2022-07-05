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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.text.StringEscapeUtils;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for roles.
 *
 * @author peter.mellquist@hp.com
 */
public final class RoleStore extends AbstractStore<Role> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleStore.class);

    private static final String SQL_ID = "roleid";
    private static final String SQL_DOMAIN_ID = "domainid";
    private static final String SQL_NAME = "name";
    private static final String SQL_DESCR = "description";
    private static final String TABLE_NAME = "ROLES";

    public RoleStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE_NAME);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE " + TABLE_NAME + " (\n"
            + SQL_ID        + " VARCHAR(128)   PRIMARY KEY,\n"
            + SQL_NAME      + " VARCHAR(128)   NOT NULL,\n"
            + SQL_DOMAIN_ID + " VARCHAR(128)   NOT NULL,\n"
            + SQL_DESCR     + " VARCHAR(128)   NOT NULL\n"
            + ")";
    }

    @Override
    protected Role fromResultSet(final ResultSet rs) throws SQLException {
        Role role = new Role();
        try {
            role.setRoleid(rs.getString(SQL_ID));
            role.setDomainid(rs.getString(SQL_DOMAIN_ID));
            role.setName(rs.getString(SQL_NAME));
            role.setDescription(rs.getString(SQL_DESCR));
        } catch (SQLException sqle) {
            LOG.error("SQL Exception: ", sqle);
            throw sqle;
        }
        return role;
    }

    public Roles getRoles() throws StoreException {
        Roles roles = new Roles();
        roles.setRoles(listAll());
        return roles;
    }

    private Role getRole(final String id) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE " + SQL_ID + " = ?")) {
            stmt.setString(1, id);

            LOG.debug("getRole() request: {}", stmt);
            return firstFromStatement(stmt);
        } catch (SQLException s) {
            throw new StoreException("SQL Exception: " + s);
        }
    }

    protected Role createRole(final Role role) throws StoreException {
        requireNonNull(role);
        requireNonNull(role.getName());
        requireNonNull(role.getDomainid());

        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("INSERT INTO " + TABLE_NAME + " ("
                + SQL_ID + ", "
                + SQL_DOMAIN_ID + ", "
                + SQL_NAME + ", "
                + SQL_DESCR + ") values(?, ?, ?, ?)")) {
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

    protected Role putRole(final Role role) throws StoreException {
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
                 "UPDATE " + TABLE_NAME + " SET " + SQL_DESCR + " = ? WHERE " + SQL_ID + " = ?")) {
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
    protected Role deleteRole(final String roleid) throws StoreException {
        // FIXME: remove this once we have a more modern H2
        final String escaped = StringEscapeUtils.escapeHtml4(roleid);
        Role savedRole = getRole(roleid);
        if (savedRole == null) {
            return null;
        }

        final String query = String.format("DELETE FROM " + TABLE_NAME + " WHERE " + SQL_ID + " = '%s'", escaped);
        try (Connection conn = dbConnect(); Statement statement = conn.createStatement()) {
            int deleteCount = statement.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedRole;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }
}
