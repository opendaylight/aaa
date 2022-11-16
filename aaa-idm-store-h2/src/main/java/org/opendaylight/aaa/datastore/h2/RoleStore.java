/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store for roles.
 *
 * @author peter.mellquist@hp.com
 *
 */
public class RoleStore extends AbstractStore<Role> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleStore.class);

    public static final String SQL_ID = "roleid";
    protected static final String SQL_DOMAIN_ID = "domainid";
    public static final String SQL_NAME = "name";
    public static final String SQL_DESCR = "description";
    private static final String TABLE_NAME = "ROLES";

    public RoleStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE_NAME);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE ROLES " + "(roleid     VARCHAR(128)   PRIMARY KEY,"
                + "name        VARCHAR(128)   NOT NULL, " + "domainid    VARCHAR(128)   NOT NULL, "
                + "description VARCHAR(128)      NOT NULL)";
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

    protected Role getRole(final String id) throws StoreException {
        try (Connection conn = dbConnect();
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM ROLES WHERE roleid = ? ")) {
            pstmt.setString(1, id);
            LOG.debug("query string: {}", pstmt);
            return firstFromStatement(pstmt);
        } catch (SQLException s) {
            throw new StoreException("SQL Exception: " + s);
        }
    }

    protected Role createRole(final Role role) throws StoreException {
        requireNonNull(role);
        requireNonNull(role.getName());
        requireNonNull(role.getDomainid());
        String query = "insert into roles (roleid,domainid,name,description) values(?,?,?,?)";
        try (Connection conn = dbConnect(); PreparedStatement statement = conn.prepareStatement(query)) {
            role.setRoleid(IDMStoreUtil.createRoleid(role.getName(), role.getDomainid()));
            statement.setString(1, role.getRoleid());
            statement.setString(2, role.getDomainid());
            statement.setString(3, role.getName());
            statement.setString(4, role.getDescription());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
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

        String query = "UPDATE roles SET description = ? WHERE roleid = ?";
        try (Connection conn = dbConnect(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, savedRole.getDescription());
            statement.setString(2, savedRole.getRoleid());
            statement.executeUpdate();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }

        return savedRole;
    }

    protected Role deleteRole(final String roleid) throws StoreException {
        Role savedRole = getRole(roleid);
        if (savedRole == null) {
            return null;
        }

        String query = "DELETE FROM ROLES WHERE roleid = ?";
        try (Connection conn = dbConnect(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, roleid);
            int deleteCount = statement.executeUpdate();
            LOG.debug("deleted {} records", deleteCount);
            return savedRole;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }
}
