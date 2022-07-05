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
final class DomainStore extends AbstractStore<Domain> {
    private static final Logger LOG = LoggerFactory.getLogger(DomainStore.class);

    private static final String TABLE_NAME = "DOMAINS";
    @VisibleForTesting
    static final String SQL_ID = "domainid";
    @VisibleForTesting
    static final String SQL_NAME = "name";
    @VisibleForTesting
    static final String SQL_DESCR = "description";
    @VisibleForTesting
    static final String SQL_ENABLED = "enabled";

    DomainStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE_NAME);
    }

    @Override
    protected String getTableCreationStatement() {
        return "CREATE TABLE " + TABLE_NAME + "(\n"
            + SQL_ID      + " VARCHAR(128) PRIMARY KEY,\n"
            + SQL_NAME    + " VARCHAR(128) UNIQUE NOT NULL,\n"
            + SQL_DESCR   + " VARCHAR(128),\n"
            // FIXME: boolean?
            + SQL_ENABLED + " INTEGER      NOT NULL\n"
            + ")";
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

    Domains getDomains() throws StoreException {
        Domains domains = new Domains();
        domains.setDomains(listAll());
        return domains;
    }

    // FIXME: seems to be unused
    Domains getDomains(final String domainName) throws StoreException {
        LOG.debug("getDomains for: {}", domainName);
        Domains domains = new Domains();
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE " + SQL_NAME + " = ?")) {
            stmt.setString(1, domainName);
            LOG.debug("query string: {}", stmt);
            domains.setDomains(listFromStatement(stmt));
        } catch (SQLException e) {
            LOG.error("Error listing domains matching {}", domainName, e);
            throw new StoreException("Error listing domains", e);
        }
        return domains;
    }

    Domain getDomain(final String id) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE " + SQL_ID + " = ?")) {
            stmt.setString(1, id);

            LOG.debug("getDomain() request: {}", stmt);
            return firstFromStatement(stmt);
        } catch (SQLException e) {
            LOG.error("Error retrieving domain {}", id, e);
            throw new StoreException("Error loading domain", e);
        }
    }

    Domain createDomain(final Domain domain) throws StoreException {
        requireNonNull(domain);
        requireNonNull(domain.getName());
        requireNonNull(domain.isEnabled());
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("INSERT INTO " + TABLE_NAME + " ("
                 + SQL_ID + ", "
                 + SQL_NAME + ", "
                 + SQL_DESCR + ", "
                 + SQL_ENABLED + ") values(?, ?, ?, ?)")) {
            stmt.setString(1, domain.getName());
            stmt.setString(2, domain.getName());
            stmt.setString(3, domain.getDescription());
            stmt.setInt(4, domain.isEnabled() ? 1 : 0);

            LOG.debug("createDomain() request: {}", stmt);
            if (stmt.executeUpdate() == 0) {
                throw new StoreException("Creating domain failed, no rows affected.");
            }
            domain.setDomainid(domain.getName());
            return domain;
        } catch (SQLException e) {
            LOG.error("Error creating domain {}", domain.getName(), e);
            throw new StoreException("Error creating domain", e);
        }
    }

    Domain putDomain(final Domain domain) throws StoreException {
        final Domain savedDomain = getDomain(domain.getDomainid());
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

        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("UPDATE " + TABLE_NAME
                 + " SET "
                 + SQL_NAME + " = ?, "
                 + SQL_DESCR + " = ?, "
                 + SQL_ENABLED + " = ? "
                 + "WHERE " + SQL_ID + " = ?")) {
            stmt.setString(1, savedDomain.getName());
            stmt.setString(2, savedDomain.getDescription());
            stmt.setInt(3, savedDomain.isEnabled() ? 1 : 0);
            stmt.setString(4, savedDomain.getDomainid());

            LOG.debug("putDomain() request: {}", stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Error updating domain {}", domain.getDomainid(), e);
            throw new StoreException("Error updating domain", e);
        }

        return savedDomain;
    }

    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    Domain deleteDomain(final String domainid) throws StoreException {
        // FIXME: remove this once we have a more modern H2
        final String escaped = StringEscapeUtils.escapeHtml4(domainid);
        Domain deletedDomain = getDomain(escaped);
        if (deletedDomain == null) {
            return null;
        }

        // FIXME: prepare statement instead
        final String query = String.format("DELETE FROM " + TABLE_NAME + " WHERE " + SQL_ID + " = '%s'", escaped);
        try (var conn = dbConnect();
             var stmt = conn.createStatement()) {

            LOG.debug("deleteDomain() request: {}", query);
            int deleteCount = stmt.executeUpdate(query);
            LOG.debug("deleted {} records", deleteCount);
            return deletedDomain;
        } catch (SQLException e) {
            LOG.error("Error deleting domain {}", domainid, e);
            throw new StoreException("Error deleting domain", e);
        }
    }
}
