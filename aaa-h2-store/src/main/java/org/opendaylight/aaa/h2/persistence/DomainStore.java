/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class DomainStore {

    private static Logger logger = LoggerFactory.getLogger(DomainStore.class);

    /**
     * the name of the backing table in the H2 database
     */
    public static final String DOMAINS_TABLE = "DOMAINS";

    /**
     * the sql to create the domains table
     */
    public static final String CREATE_DOMAINS_TABLE_SQL =
            "CREATE TABLE DOMAINS " +
            "(domainid   VARCHAR(128)      PRIMARY KEY," +
            "name        VARCHAR(128)      UNIQUE NOT NULL, " +
            "description VARCHAR(128)      , " +
            "enabled     INTEGER           NOT NULL)";

    protected final static String SQL_ID = "domainid";
    protected final static String SQL_NAME = "name";
    protected final static String SQL_DESCR = "description";
    protected final static String SQL_ENABLED = "enabled";

    protected Connection dbConnection = null;

    protected DomainStore() {
        reinitDb();
    }

    protected void dbClean() throws StoreException, SQLException {
        String sql = "delete from DOMAINS where true";
        dbConnection.createStatement().execute(sql);
    }

    protected void reinitDb() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (Exception e) {
                logger.error("Cannot close Database Connection " + e);
            }
        }
        try {
            dbConnection = H2Store.getConnection(dbConnection);
        } catch (StoreException e) {
            logger.error("Failed to connect to the database", e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        reinitDb();
        super.finalize();
    }

    protected Domain rsToDomain(ResultSet rs) throws SQLException {
        Domain domain = new Domain();
        try {
            domain.setDomainid(rs.getString(SQL_ID));
            domain.setName(rs.getString(SQL_NAME));
            domain.setDescription(rs.getString(SQL_DESCR));
            domain.setEnabled(rs.getInt(SQL_ENABLED) == 1 ? true : false);
        } catch (SQLException sqle) {
            logger.error("SQL Exception : " + sqle);
            throw sqle;
        }
        return domain;
    }

    protected Domains getDomains() throws StoreException {
        Domains domains = new Domains();
        List<Domain> domainList = new ArrayList<Domain>();
        Statement stmt = null;
        String query = "SELECT * FROM DOMAINS";
        try {
            stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Domain domain = rsToDomain(rs);
                domainList.add(domain);
            }
            rs.close();
            stmt.close();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            reinitDb();
        }
        domains.setDomains(domainList);
        return domains;
    }

    protected Domains getDomains(String domainName) throws StoreException {
        debug("getDomains for:" + domainName);
        Domains domains = new Domains();
        List<Domain> domainList = new ArrayList<Domain>();
        try {
            PreparedStatement pstmt = dbConnection.prepareStatement("SELECT * FROM DOMAINS WHERE name = ?");
            pstmt.setString(1, domainName);
            debug("query string: " + pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Domain domain = rsToDomain(rs);
                domainList.add(domain);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            reinitDb();
        }
        domains.setDomains(domainList);
        return domains;
    }

    protected Domain getDomain(String id) throws StoreException {
        try {
            PreparedStatement pstmt = dbConnection.prepareStatement("SELECT * FROM DOMAINS WHERE domainid = ? ");
            pstmt.setString(1, id);
            debug("query string: " + pstmt.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Domain domain = rsToDomain(rs);
                rs.close();
                pstmt.close();
                return domain;
            } else {
                rs.close();
                pstmt.close();
                return null;
            }
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            reinitDb();
        }
    }

    protected Domain createDomain(Domain domain) throws StoreException {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getName());
        Preconditions.checkNotNull(domain.isEnabled());
        try {
            String query = "insert into DOMAINS (domainid,name,description,enabled) values(?, ?, ?, ?)";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            statement.setString(1, domain.getName());
            statement.setString(2, domain.getName());
            statement.setString(3, domain.getDescription());
            statement.setInt(4, domain.isEnabled() ? 1 : 0);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new StoreException("Creating domain failed, no rows affected.");
            }
            domain.setDomainid(domain.getName());
            return domain;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            reinitDb();
        }
    }

    protected Domain putDomain(Domain domain) throws StoreException {
        Domain savedDomain = this.getDomain(domain.getDomainid());
        if (savedDomain == null) {
            return null;
        }

        if (domain.getDescription() != null) {
            savedDomain.setDescription(domain.getDescription());
        }
        if (domain.getName() != null) {
            savedDomain.setName(domain.getName());
        }
        if (domain.isEnabled() != null) {
            savedDomain.setEnabled(domain.isEnabled());
        }

        try {
            String query = "UPDATE DOMAINS SET description = ?, enabled = ? WHERE domainid = ?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            statement.setString(1, savedDomain.getDescription());
            statement.setInt(2, savedDomain.isEnabled() ? 1 : 0);
            statement.setString(3, savedDomain.getDomainid());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            reinitDb();
        }

        return savedDomain;
    }

    protected Domain deleteDomain(String domainid) throws StoreException {
        Domain deletedDomain = this.getDomain(domainid);
        if (deletedDomain == null) {
            return null;
        }

        try {
            String query = "DELETE FROM DOMAINS WHERE domainid = ?";
            PreparedStatement statement = dbConnection.prepareStatement(query);
            statement.setString(1, deletedDomain.getDomainid());
            int deleteCount = statement.executeUpdate();
            debug("deleted " + deleteCount + " records");
            statement.close();
            return deletedDomain;
        } catch (SQLException s) {
            throw new StoreException("SQL Exception : " + s);
        } finally {
            reinitDb();
        }
    }

    private static final void debug(String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }
}
