/*
 * Copyright (c) 2014, 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.h2.config.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */
public class RoleStore extends AbstractStore<Role> {
    private static final Logger LOG = LoggerFactory.getLogger(RoleStore.class);

    protected final static String SQL_ID = "roleid";
    protected final static String SQL_DOMAIN_ID = "domainid";
    protected final static String SQL_NAME = "name";
    protected final static String SQL_DESCR = "description";
    private static final String TABLE_NAME = "ROLES";

    protected RoleStore(ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE_NAME);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE ROLES "
                + "(roleid     VARCHAR(128)   PRIMARY KEY,"
                + "name        VARCHAR(128)   NOT NULL, "
                + "domainid    VARCHAR(128)   NOT NULL, "
                + "description VARCHAR(128)      NOT NULL)";
    }

    @Override
    protected Role fromResultSet(ResultSet rs) throws SQLException {
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

    protected Roles getRoles() throws StoreException {
        Roles roles = new Roles();
        roles.setRoles(listAll());
        return roles;
    }

    protected Role getRole(String id) throws StoreException {
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn
                     .prepareStatement("SELECT * FROM ROLES WHERE roleid = ? ")) {
            pstmt.setString(1, id);
            LOG.debug("query string: {}", pstmt.toString());
            return firstFromStatement(pstmt);
        } catch (SQLException s) {
            throw new StoreException("SQL Exception: " + s);
        }
    }

    protected Role createRole(Role role) throws StoreException {
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getName());
        Preconditions.checkNotNull(role.getDomainid());
        String query = "insert into roles (roleid,domainid,name,description) values(?,?,?,?)";
        try (Connection conn = dbConnect();
             PreparedStatement statement = conn.prepareStatement(query)) {
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

    protected Role putRole(Role role) throws StoreException {

        Role savedRole = this.getRole(role.getRoleid());
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
        try (Connection conn = dbConnect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, savedRole.getDescription());
            statement.setString(2, savedRole.getRoleid());
            statement.executeUpdate();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }

        return savedRole;
    }

    protected Role deleteRole(String roleid) throws StoreException {
        roleid = StringEscapeUtils.escapeHtml4(roleid);
        Role savedRole = this.getRole(roleid);
        if (savedRole == null) {
            return null;
        }

        String query = String.format("DELETE FROM ROLES WHERE roleid = '%s'", roleid);
        try (Connection conn = dbConnect();
             Statement statement = conn.createStatement()) {
            int deleteCount = statement.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedRole;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        }
    }
}
