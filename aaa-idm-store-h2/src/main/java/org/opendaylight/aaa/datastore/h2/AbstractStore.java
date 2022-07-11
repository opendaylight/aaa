/*
 * Copyright © 2016 Red Hat, Inc. and others.
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for H2 stores.
 */
abstract class AbstractStore<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStore.class);

    /**
     * The name of the table used to represent this store.
     */
    private final @NonNull String tableName;
    /**
     * Database connection factory.
     */
    private final @NonNull ConnectionProvider dbConnectionFactory;
    /**
     * Table types we're interested in (when checking tables' existence).
     */
    @VisibleForTesting
    static final String[] TABLE_TYPES = new String[] { "TABLE" };

    /**
     * Creates an instance.
     *
     * @param dbConnectionFactory factory to obtain JDBC Connections from
     * @param tableName The name of the table being managed.
     */
    AbstractStore(final ConnectionProvider dbConnectionFactory, final String tableName) {
        this.dbConnectionFactory = requireNonNull(dbConnectionFactory);
        this.tableName = requireNonNull(tableName);
    }

    /**
     * Returns a database connection. It is the caller's responsibility to close it. If the managed table does not
     * exist, it will be created (using {@link #getTableCreationStatement()}).
     *
     * @return A database connection.
     * @throws StoreException if an error occurs.
     */
    final Connection dbConnect() throws StoreException {
        final var conn = dbConnectionFactory.getConnection();
        // Ensure table check/creation is atomic
        synchronized (this) {
            try {
                final var dbm = conn.getMetaData();
                try (var rs = dbm.getTables(null, null, tableName, TABLE_TYPES)) {
                    if (!rs.next()) {
                        LOG.info("Table {} does not exist, creating it", tableName);
                        try (var stmt = conn.createStatement()) {
                            createTable(stmt);
                        }
                    } else {
                        LOG.debug("Table {} already exists", tableName);
                    }
                }
            } catch (SQLException e) {
                LOG.error("Error connecting to the H2 database", e);
                throw new StoreException("Cannot connect to database server", e);
            }
        }
        return conn;
    }

    /**
     * Create a managed table for on a particular connection..
     *
     * @param stmt A pre-allocated SQL statement
     * @throws SQLException If table creation fails
     */
    abstract void createTable(Statement stmt) throws SQLException;

    /**
     * Empties the store.
     *
     * @throws StoreException if a connection error occurs.
     */
    @VisibleForTesting
    @SuppressFBWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
        justification = "table name cannot be a parameter in a prepared statement")
    final void dbClean() throws StoreException {
        try (var c = dbConnect();
             var statement = c.createStatement()) {
            // FIXME: can we somehow make this a constant?
            statement.execute("DELETE FROM " + tableName);
        } catch (SQLException e) {
            LOG.error("Error clearing table {}", tableName, e);
            throw new StoreException("Error clearing table " + tableName, e);
        }
    }

    abstract void cleanTable(Statement stmt) throws SQLException;

    /**
     * Lists all the stored items.
     *
     * @return The stored item.
     * @throws StoreException if an error occurs.
     */
    @SuppressFBWarnings(value = "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE",
        justification = "table name cannot be a parameter in a prepared statement")
    final List<T> listAll() throws StoreException {
        List<T> result = new ArrayList<>();
        try (var conn = dbConnect();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            while (rs.next()) {
                result.add(fromResultSet(rs));
            }
        } catch (SQLException e) {
            LOG.error("Error listing all items from {}", tableName, e);
            throw new StoreException(e);
        }
        return result;
    }

    /**
     * Lists the stored items returned by the given statement.
     *
     * @param ps The statement (which must be ready for execution). It is the caller's responsibility to close this.
     * @return The stored items.
     * @throws StoreException if an error occurs.
     */
    final List<T> listFromStatement(final PreparedStatement ps) throws StoreException {
        final var result = new ArrayList<T>();
        try (var rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(fromResultSet(rs));
            }
        } catch (SQLException e) {
            LOG.error("Error listing matching items from {}", tableName, e);
            throw new StoreException(e);
        }
        return result;
    }

    /**
     * Extracts the first item returned by the given statement, if any.
     *
     * @param ps The statement (which must be ready for execution). It is the caller's responsibility to close this.
     * @return The first item, or {@code null} if none.
     * @throws StoreException if an error occurs.
     */
    final @Nullable T firstFromStatement(final PreparedStatement ps) throws StoreException {
        try (var rs = ps.executeQuery()) {
            return rs.next() ? fromResultSet(rs) : null;
        } catch (SQLException e) {
            LOG.error("Error listing first matching item from {}", tableName, e);
            throw new StoreException(e);
        }
    }

    /**
     * Converts a single row in a result set to an instance of the managed type.
     *
     * @param rs The result set (which is ready for extraction; {@link ResultSet#next()} must <b>not</b> be called).
     * @return The corresponding instance.
     * @throws SQLException if an error occurs.
     */
    abstract T fromResultSet(ResultSet rs) throws SQLException;
}
