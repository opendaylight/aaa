/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * Cassandra row.
 * <P>
 * Cassandra stores data in the following format:
 * <P>
 * Map&lt;RowKey, SortedMap&lt;ColumnKey, ColumnValue&gt;&gt;
 * <P>
 * This class is not thread-safe, but it doesn't have to because the DAO won't use it in a
 * multi-thread environment since instances of this class are not shared among threads.
 * 
 * @param <K> type of the row key
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public final class CassandraRow<K extends Serializable, C extends Serializable & Comparable<C>> {

    private K key;

    // This map does not need to be sorted as long as the implementation keeps the keeps order as they are inserted.
    // Cassandra will sort data and columns will be inserted in order. No need to spend time sorting in the client.
    // private SortedMap<ColumnName<C, ?>, Column<C, ?>> columns;
    private Map<ColumnName<C, ?>, Column<C, ?>> columns;

    // In cassandra columns must be explicitly deleted
    private Set<ColumnName<C, ?>> deletedColumns;

    /**
     * Creates a cassandra persistent object.
     * 
     * @param rowKey row key
     */
    public CassandraRow(K rowKey) {
        if (rowKey == null) {
            throw new NullPointerException("id cannot be null");
        }

        this.key = rowKey;
        // LinkedHashMap is used in case the number of columns is big.
        this.columns = new LinkedHashMap<ColumnName<C, ?>, Column<C, ?>>();
        this.deletedColumns = new HashSet<ColumnName<C, ?>>();
    }

    /**
     * Returns the row key
     * 
     * @return the row key
     */
    public K getKey() {
        return this.key;
    }

    /**
     * Gets the value of the given column.
     * 
     * @param name name of the column to get
     * @return the column if it exists, {@code null} otherwise
     */
    public Column<C, ?> getColumn(ColumnName<C, ?> name) {
        return this.columns.get(name);
    }

    /**
     * Sets a column. If the column already exists it is replaced.
     * 
     * @param column column to set
     */
    public void setColumn(Column<C, ?> column) {
        this.columns.put(column.getName(), column);
        this.deletedColumns.remove(column.getName());
    }

    /**
     * Deletes a column.
     * 
     * @param name name of the column to delete
     */
    public void delete(ColumnName<C, ?> name) {
        if (this.columns.remove(name) != null) {
            this.deletedColumns.add(name);
        }
    }

    /**
     * Gets the columns.
     * 
     * @return the columns
     */
    public Collection<Column<C, ?>> getColumns() {
        return this.columns.values();
    }

    /**
     * Gets the deleted columns.
     * 
     * @return the columns
     */
    public Set<ColumnName<C, ?>> getDeletedColumns() {
        return Collections.unmodifiableSet(this.deletedColumns);
    }
}
