/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.datastore.h2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
 *
 */
public class GrantStore extends AbstractStore<Grant> {
    private static final Logger LOG = LoggerFactory.getLogger(GrantStore.class);

    public static final String SQL_ID = "grantid";
    public static final String SQL_TENANTID = "domainid";
    public static final String SQL_USERID = "userid";
    public static final String SQL_ROLEID = "roleid";
    private static final String TABLE_NAME = "GRANTS";

    public GrantStore(ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE_NAME);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE GRANTS "
            + "(grantid    VARCHAR(128) PRIMARY KEY, "
            + "domainid    VARCHAR(128) NOT NULL, "
            + "userid      VARCHAR(128) NOT NULL, "
            + "roleid      VARCHAR(128) NOT NULL)";
    }

    @Override
    protected Grant fromResultSet(ResultSet rs) throws SQLException {
        Grant grant = new Grant();
        try {
            grant.setGrantid(rs.getString(SQL_ID));
            grant.setDomainid(rs.getString(SQL_TENANTID));
            grant.setUserid(rs.getString(SQL_USERID));
            grant.setRoleid(rs.getString(SQL_ROLEID));
        } catch (SQLException sqle) {
            LOG.error("SQL Exception: ", sqle);
            throw sqle;
        }
        return grant;
    }

    public Grants getGrants(String did, String uid) throws StoreException {
        Grants grants = new Grants();
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn
                     .prepareStatement("SELECT * FROM grants WHERE domainid = ? AND userid = ?")) {
            pstmt.setString(1, did);
            pstmt.setString(2, uid);
            LOG.debug("query string: {}", pstmt);
            grants.setGrants(listFromStatement(pstmt));
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
        return grants;
    }

    protected Grants getGrants(String userid) throws StoreException {
        Grants grants = new Grants();
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM GRANTS WHERE userid = ? ")) {
            pstmt.setString(1, userid);
            LOG.debug("query string: {}", pstmt);
            grants.setGrants(listFromStatement(pstmt));
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
        return grants;
    }

    protected Grant getGrant(String id) throws StoreException {
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM GRANTS WHERE grantid = ? ")) {
            pstmt.setString(1, id);
            LOG.debug("query string: {}", pstmt);
            return firstFromStatement(pstmt);
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }

    protected Grant getGrant(String did, String uid, String rid) throws StoreException {
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn
                     .prepareStatement("SELECT * FROM GRANTS WHERE domainid = ? AND userid = ? AND roleid = ? ")) {
            pstmt.setString(1, did);
            pstmt.setString(2, uid);
            pstmt.setString(3, rid);
            LOG.debug("query string: {}", pstmt);
            return firstFromStatement(pstmt);
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }

    protected Grant createGrant(Grant grant) throws StoreException {
        String query = "insert into grants  (grantid,domainid,userid,roleid) values(?,?,?,?)";
        try (Connection conn = dbConnect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(
                    1,
                    IDMStoreUtil.createGrantid(grant.getUserid(), grant.getDomainid(),
                            grant.getRoleid()));
            statement.setString(2, grant.getDomainid());
            statement.setString(3, grant.getUserid());
            statement.setString(4, grant.getRoleid());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new StoreException("Creating grant failed, no rows affected.");
            }
            grant.setGrantid(IDMStoreUtil.createGrantid(grant.getUserid(), grant.getDomainid(),
                    grant.getRoleid()));
            return grant;
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    protected Grant deleteGrant(String grantid) throws StoreException {
        grantid = StringEscapeUtils.escapeHtml4(grantid);
        Grant savedGrant = this.getGrant(grantid);
        if (savedGrant == null) {
            return null;
        }

        String query = String.format("DELETE FROM GRANTS WHERE grantid = '%s'", grantid);
        try (Connection conn = dbConnect();
             Statement statement = conn.createStatement()) {
            int deleteCount = statement.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return savedGrant;
        } catch (SQLException e) {
            throw new StoreException("SQL Exception", e);
        }
    }
}
