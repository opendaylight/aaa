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
import org.apache.commons.text.StringEscapeUtils;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain store.
 *
 * @author peter.mellquist@hp.com
 *
 */
public class DomainStore extends AbstractStore<Domain> {
    private static final Logger LOG = LoggerFactory.getLogger(DomainStore.class);

    public static final String SQL_ID = "domainid";
    public static final String SQL_NAME = "name";
    public static final String SQL_DESCR = "description";
    public static final String SQL_ENABLED = "enabled";
    private static final String TABLE_NAME = "DOMAINS";

    public DomainStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE_NAME);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE DOMAINS "
                + "(domainid   VARCHAR(128)      PRIMARY KEY,"
                + "name        VARCHAR(128)      UNIQUE NOT NULL, "
                + "description VARCHAR(128)      , "
                + "enabled     INTEGER           NOT NULL)";
    }

    @Override
    protected Domain fromResultSet(final ResultSet rs) throws SQLException {
        Domain domain = new Domain();
        domain.setDomainid(rs.getString(SQL_ID));
        domain.setName(rs.getString(SQL_NAME));
        domain.setDescription(rs.getString(SQL_DESCR));
        domain.setEnabled(rs.getInt(SQL_ENABLED) == 1);
        return domain;
    }

    public Domains getDomains() throws StoreException {
        Domains domains = new Domains();
        domains.setDomains(listAll());
        return domains;
    }

    protected Domains getDomains(final String domainName) throws StoreException {
        LOG.debug("getDomains for: {}", domainName);
        Domains domains = new Domains();
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM DOMAINS WHERE name = ?")) {
            pstmt.setString(1, domainName);
            LOG.debug("query string: {}", pstmt);
            domains.setDomains(listFromStatement(pstmt));
        } catch (SQLException e) {
            LOG.error("Error listing domains matching {}", domainName, e);
            throw new StoreException("Error listing domains", e);
        }
        return domains;
    }

    protected Domain getDomain(final String id) throws StoreException {
        try (Connection conn = dbConnect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM DOMAINS WHERE domainid = ? ")) {
            pstmt.setString(1, id);
            LOG.debug("query string: {}", pstmt);
            return firstFromStatement(pstmt);
        } catch (SQLException e) {
            LOG.error("Error retrieving domain {}", id, e);
            throw new StoreException("Error loading domain", e);
        }
    }

    public Domain createDomain(final Domain domain) throws StoreException {
        requireNonNull(domain);
        requireNonNull(domain.getName());
        requireNonNull(domain.isEnabled());
        String query = "insert into DOMAINS (domainid,name,description,enabled) values(?, ?, ?, ?)";
        try (Connection conn = dbConnect();
             PreparedStatement statement = conn.prepareStatement(query)) {
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
        } catch (SQLException e) {
            LOG.error("Error creating domain {}", domain.getName(), e);
            throw new StoreException("Error creating domain", e);
        }
    }

    protected Domain putDomain(final Domain domain) throws StoreException {
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

        String query = "UPDATE domains SET description = ?, enabled = ?, name = ? WHERE domainid = ?";
        try (Connection conn = dbConnect();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, savedDomain.getDescription());
            statement.setInt(2, savedDomain.isEnabled() ? 1 : 0);
            statement.setString(3, savedDomain.getName());
            statement.setString(4, savedDomain.getDomainid());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Error updating domain {}", domain.getDomainid(), e);
            throw new StoreException("Error updating domain", e);
        }

        return savedDomain;
    }

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    protected Domain deleteDomain(String domainid) throws StoreException {
        domainid = StringEscapeUtils.escapeHtml4(domainid);
        Domain deletedDomain = this.getDomain(domainid);
        if (deletedDomain == null) {
            return null;
        }
        String query = String.format("DELETE FROM DOMAINS WHERE domainid = '%s'", domainid);
        try (Connection conn = dbConnect();
             Statement statement = conn.createStatement()) {
            int deleteCount = statement.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return deletedDomain;
        } catch (SQLException e) {
            LOG.error("Error deleting domain {}", domainid, e);
            throw new StoreException("Error deleting domain", e);
        }
    }
}
