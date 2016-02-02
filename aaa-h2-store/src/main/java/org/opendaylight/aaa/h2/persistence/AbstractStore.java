/*
 * Copyright © 2016 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.h2.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for H2 stores.
 */
abstract class AbstractStore<T> {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStore.class);

    /**
     * The name of the table used to represent this store.
     */
    private final String tableName;

    /**
     * Database connection, only used for tests.
     */
    Connection dbConnection = null;

    /**
     * Creates an instance.
     *
     * @param tableName The name of the table being managed.
     */
    protected AbstractStore(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Returns a database connection. It is the caller's responsibility to close it. If the managed table does not
     * exist, it will be created (using {@link #getTableCreationStatement()}).
     *
     * @return A database connection.
     *
     * @throws StoreException if an error occurs.
     */
    protected Connection dbConnect() throws StoreException {
        Connection conn = H2Store.getConnection(dbConnection);
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            String[] tableTypes = { "TABLE" };
            try (ResultSet rs = dbm.getTables(null, null, tableName, tableTypes)) {
                if (rs.next()) {
                    LOG.debug("Table {} already exists", tableName);
                } else {
                    LOG.info("Table {} does not exist, creating it", tableName);
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(getTableCreationStatement());
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("Error connecting to the H2 database", e);
            throw new StoreException("Cannot connect to database server", e);
        }
        return conn;
    }

    /**
     * Empties the store.
     *
     * @throws StoreException if a connection error occurs.
     */
    protected void dbClean() throws StoreException {
        try (Connection c = dbConnect()) {
            // The table name can't be a parameter in a prepared statement
            String sql = "delete from " + tableName + " where true";
            c.createStatement().execute(sql);
        } catch (SQLException e) {
            LOG.error("Error clearing table {}", tableName, e);
            throw new StoreException("Error clearing table " + tableName, e);
        }
    }

    /**
     * Returns the SQL code required to create the managed table.
     *
     * @return The SQL table creation statement.
     */
    protected abstract String getTableCreationStatement();

    /**
     * Lists all the stored items.
     *
     * @return The stored item.
     *
     * @throws StoreException if an error occurs.
     */
    protected List<T> listAll() throws StoreException {
        List<T> result = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;
        try (Connection conn = dbConnect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
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
     * @param ps The statement (which must be ready for execution). It is the caller's reponsibility to close this.
     *
     * @return The stored items.
     *
     * @throws StoreException if an error occurs.
     */
    protected List<T> listFromStatement(PreparedStatement ps) throws StoreException {
        List<T> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
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
     * @param ps The statement (which must be ready for execution). It is the caller's reponsibility to close this.
     *
     * @return The first item, or {@code null} if none.
     *
     * @throws StoreException if an error occurs.
     */
    protected T firstFromStatement(PreparedStatement ps) throws StoreException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return fromResultSet(rs);
            } else {
                return null;
            }
        } catch (SQLException e) {
            LOG.error("Error listing first matching item from {}", tableName, e);
            throw new StoreException(e);
        }
    }

    /**
     * Converts a single row in a result set to an instance of the managed type.
     *
     * @param rs The result set (which is ready for extraction; {@link ResultSet#next()} must <b>not</b> be called).
     *
     * @return The corresponding instance.
     *
     * @throws SQLException if an error occurs.
     */
    protected abstract T fromResultSet(ResultSet rs) throws SQLException;
}
