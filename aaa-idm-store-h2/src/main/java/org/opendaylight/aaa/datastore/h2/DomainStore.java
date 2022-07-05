/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 * Copyright (c) 2022 PANTHEON.tech, s.r.o.
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
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain store.
 */
final class DomainStore extends AbstractStore<Domain> {
    private static final Logger LOG = LoggerFactory.getLogger(DomainStore.class);

    /**
     * Name of our SQL table. This constant lives here rather than in {@link SQLTable} for brevity.
     */
    // FIXME: AAA-221: this is a system table
    static final @NonNull String TABLE = "DOMAINS";

    static {
        SQLTable.DOMAIN.verifyTable(TABLE);
    }

    /**
     * Column storing {@link Domain#getDomainid()}, which is a flat namespace.
     */
    // FIXME: rename to "id"
    @VisibleForTesting
    static final String COL_ID = "domainid";
    /**
     * Column storing {@link Domain#getName()}, which is a short name.
     */
    @VisibleForTesting
    static final String COL_NAME = "name";
    /**
     * Column storing {@link Domain#getDescription()}, which is a detailed description.
     */
    @VisibleForTesting
    static final String COL_DESC = "description";
    /**
     * Column storing {@link Domain#isEnabled()}, which is ... not used anywhere.
     */
    // FIXME: remove or audit for potential callers of isEnabled()
    @VisibleForTesting
    static final String COL_ENABLED = "enabled";

    DomainStore(final ConnectionProvider dbConnectionFactory) {
        super(dbConnectionFactory, TABLE);
    }

    @Override
    void createTable(final Statement stmt) throws SQLException {
        stmt.executeUpdate("CREATE TABLE " + TABLE + " (\n"
            // FIXME: on delete cascade? RoleStore.COL_DOMAIN_ID seems to reference this
            + COL_ID      + " VARCHAR(128) PRIMARY KEY,\n"
            + COL_NAME    + " VARCHAR(128) UNIQUE NOT NULL,\n"
            + COL_DESC    + " VARCHAR(128),\n"
            // FIXME: change boolean
            + COL_ENABLED + " INTEGER      NOT NULL)");
    }

    @Override
    void cleanTable(final Statement stmt) throws SQLException {
        stmt.execute("DELETE FROM " + TABLE);
    }

    @Override
    protected Domain fromResultSet(final ResultSet rs) throws SQLException {
        Domain domain = new Domain();
        domain.setDomainid(rs.getString(COL_ID));
        domain.setName(rs.getString(COL_NAME));
        domain.setDescription(rs.getString(COL_DESC));
        domain.setEnabled(rs.getInt(COL_ENABLED) == 1);
        return domain;
    }

    Domains getDomains() throws StoreException {
        Domains domains = new Domains();
        domains.setDomains(listAll());
        return domains;
    }

    // FIXME: seems to be unused
    Domains getDomains(final String domainName) throws StoreException {
        final Domains domains;
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + COL_NAME + " = ?")) {
            stmt.setString(1, domainName);

            domains = new Domains();
            LOG.debug("getDomains() request: {}", stmt);
            domains.setDomains(listFromStatement(stmt));
        } catch (SQLException e) {
            LOG.error("Error listing domains matching {}", domainName, e);
            throw new StoreException("Error listing domains", e);
        }
        return domains;
    }

    Domain getDomain(final String id) throws StoreException {
        try (var conn = dbConnect();
             var stmt = conn.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + COL_ID + " = ?")) {
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
             var stmt = conn.prepareStatement("INSERT INTO " + TABLE + " ("
                 + COL_ID + ", "
                 + COL_NAME + ", "
                 + COL_DESC + ", "
                 + COL_ENABLED + ") values(?, ?, ?, ?)")) {
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
        final var savedDomain = getDomain(domain.getDomainid());
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
             var stmt = conn.prepareStatement("UPDATE " + TABLE
                 + " SET "
                 + COL_NAME + " = ?, "
                 + COL_DESC + " = ?, "
                 + COL_ENABLED + " = ? "
                 + "WHERE " + COL_ID + " = ?")) {
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

    @SuppressFBWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE", justification = "Weird original code")
    Domain deleteDomain(final String domainid) throws StoreException {
        // FIXME: remove this once we have a more modern H2
        final String escaped = StringEscapeUtils.escapeHtml4(domainid);
        final var deletedDomain = getDomain(escaped);
        if (deletedDomain == null) {
            return null;
        }

        // FIXME: prepare statement instead
        final String query = String.format("DELETE FROM " + TABLE + " WHERE " + COL_ID + " = '%s'", escaped);
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
