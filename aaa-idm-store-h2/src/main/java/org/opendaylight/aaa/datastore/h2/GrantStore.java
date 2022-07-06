/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.datastore.h2;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.text.StringEscapeUtils;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Grant store.
 *
 * @author peter.mellquist@hp.com
 */
final class GrantStore extends AbstractStore<Grant> {
    private static final Logger LOG = LoggerFactory.getLogger(GrantStore.class);

    static final String TABLE = "AAA_GRANTS";

    static {
        SQLTable.GRANT.verifyTable(TABLE);
    }

    // FIXME: javadoc
    static final String COL_ID = "grantid";

    // FIXME: javadoc
    // FIXME: 'tenant' vs 'domain' ?
    @VisibleForTesting
    static final String COL_TENANTID = "domainid";

    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_USERID = "userid";
    // FIXME: javadoc
    @VisibleForTesting
    static final String COL_ROLEID = "roleid";

    GrantStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE);
    }

    @Override
    void createTable(final Statement stmt) throws SQLException {
        stmt.executeUpdate("CREATE TABLE " + TABLE + " ("
            + COL_ID       + " VARCHAR(128) PRIMARY KEY, "
            // FIXME: foreign key to DomainStore.COL_ID?
            + COL_TENANTID + " VARCHAR(128) NOT NULL, "
            // FIXME: foreign key to UserStore.COL_ID?
            + COL_USERID   + " VARCHAR(128) NOT NULL, "
            // FIXME: foreign key to RoleStore.COL_ID?
            + COL_ROLEID   + " VARCHAR(128) NOT NULL)");
    }

    @Override
    void cleanTable(final Statement stmt) throws SQLException {
        stmt.execute("DELETE FROM " + TABLE);
    }

    @Override
    protected Grant fromResultSet(final ResultSet rs) throws SQLException {
        Grant grant = new Grant();
        try {
            grant.setGrantid(rs.getString(COL_ID));
            grant.setDomainid(rs.getString(COL_TENANTID));
            grant.setUserid(rs.getString(COL_USERID));
            grant.setRoleid(rs.getString(COL_ROLEID));
        } catch (SQLException sqle) {
            LOG.error("SQL Exception: ", sqle);
            throw sqle;
        }
        return grant;
    }

    Grants getGrants(final String domainId, final String userId) throws StoreException {
        final Grants grants;
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE
                 + " WHERE " + COL_TENANTID + " = ? AND " + COL_USERID + " = ?")) {
            stmt.setString(1, domainId);
            stmt.setString(2, userId);
            LOG.debug("getGrants() request: {}", stmt);

            grants = new Grants();
            grants.setGrants(listFromStatement(stmt));
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
        return grants;
    }

    Grants getGrants(final String userid) throws StoreException {
        final Grants grants;
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + COL_USERID + " = ?")) {
            stmt.setString(1, userid);
            LOG.debug("getGrants() request: {}", stmt);

            grants = new Grants();
            grants.setGrants(listFromStatement(stmt));
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
        return grants;
    }

    Grant getGrant(final String id) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + COL_ID + " = ?")) {
            stmt.setString(1, id);
            LOG.debug("getGrant() request: {}", stmt);

            return firstFromStatement(stmt);
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }

    // FIXME: seems to be unused
    Grant getGrant(final String did, final String uid, final String rid) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE
                 + " WHERE " + COL_TENANTID + " = ? AND " + COL_USERID + " = ? AND " + COL_ROLEID + " = ?")) {
            stmt.setString(1, did);
            stmt.setString(2, uid);
            stmt.setString(3, rid);
            LOG.debug("getGrant() request: {}", stmt);

            return firstFromStatement(stmt);
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }

    Grant createGrant(final Grant grant) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("INSERT INTO " + TABLE + " ("
                 + COL_ID + ", " + COL_TENANTID + ", " + COL_USERID + ", " + COL_ROLEID + ") VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, IDMStoreUtil.createGrantid(grant.getUserid(), grant.getDomainid(), grant.getRoleid()));
            stmt.setString(2, grant.getDomainid());
            stmt.setString(3, grant.getUserid());
            stmt.setString(4, grant.getRoleid());
            LOG.debug("createGrant() request: {}", stmt);

            if (stmt.executeUpdate() == 0) {
                throw new StoreException("Creating grant failed, no rows affected.");
            }
            grant.setGrantid(IDMStoreUtil.createGrantid(grant.getUserid(), grant.getDomainid(), grant.getRoleid()));
            return grant;
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }

    @SuppressFBWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", justification = "Weird original code")
    Grant deleteGrant(final String grantid) throws StoreException {
        final String escaped = StringEscapeUtils.escapeHtml4(grantid);
        final var savedGrant = getGrant(escaped);
        if (savedGrant == null) {
            return null;
        }

        try (var conn = dbConnect();
             var stmt = conn.createStatement()) {
            // FIXME: prepare statement instead
            final String query = String.format("DELETE FROM " + TABLE +  " WHERE " + COL_ID + " = '%s'", escaped);
            LOG.debug("deleteGrant() request: {}", query);

            int deleteCount = stmt.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedGrant;
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }
}
