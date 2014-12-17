/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Custom secondary index column family to keep all rows from another column family (or main column
 * family) as columns in a wide row so they are counted and paged.
 * <p>
 * <b>All Rows Secondary Index Column Family:</b>
 * <ul>
 * <li>Row key (Key validation: String): Single row with a well-known key.</li>
 * <li>Column value (default validation: Bytes): Denormalized data.</li>
 * <li>Column name (comparator: row key in the main column family or composite value when sorting
 * information is included): The row key in the main column family. For each row there will be a an
 * entry in the columns to keep the row key.</li>
 * 
 * <pre>
 * column_family_name {
 *     "rows": {
 *         id_1: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         id_n: &lt;data provided by the denormalizer&gt;,
 *     }
 * }
 * </pre>
 * 
 * See {@link IndexEntryHandler} for a recipe to update secondary indexes.
 * 
 * @param <C> type of the row key in the main column family. Such row key will be the column name in
 *            the secondary index column.
 * @param <D> type of the denormalized data to set as the value in the indexed columns
 * @author Fabiel Zuniga
 */
public class AllRowsSecondaryIndex<C extends Serializable & Comparable<C>, D> implements ColumnFamilyHandler {
    // TODO: Use sharding (balancing columns in multiple rows) if there could be more than 2 billion
    // rows in the main column.
    private static final String ROW_KEY = "rows";

    private final CustomSecondaryIndex<String, C, D> delegate;

    /**
     * Creates a custom secondary index.
     * 
     * @param columnFamilyName name for the column family this index will keep data into
     * @param indexedColumnNameDataType type of the secondary index column name
     * @param denormalizedDataType denormalized data type; column value in the custom secondary
     */
    public AllRowsSecondaryIndex(String columnFamilyName, DataType<C> indexedColumnNameDataType,
            DataType<D> denormalizedDataType) {
        if (columnFamilyName == null) {
            throw new NullPointerException("columnFamilyName cannot be null");
        }

        if (columnFamilyName.isEmpty()) {
            throw new IllegalArgumentException("columnFamilyName cannot be empty");
        }

        if (indexedColumnNameDataType == null) {
            throw new NullPointerException("indexedColumnNameDataType cannot be null");
        }

        ColumnFamily<String, C> columnFamilyDefinition = new ColumnFamily<String, C>(
                columnFamilyName, BasicType.STRING_UTF8, indexedColumnNameDataType,
                "Secondary index column family to keep all rows in a main column family as columns for counting and pagination");

        this.delegate = new GenericCustomSecondaryIndex<String, C, D>(columnFamilyDefinition, denormalizedDataType);
    }

    @Override
    public Collection<ColumnFamily<?, ?>> getColumnFamilies() {
        return this.delegate.getColumnFamilies();
    }

    /**
     * Updates the index after a row has been inserted into the main column family.
     * 
     * @param indexEntry key of the row inserted in the main column family
     * @param denormalizedData denormalized data to include as part of the indexed columns.
     *            {@code null} if no denormalization is used.
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void insert(C indexEntry, D denormalizedData, CassandraContext<N> context) throws PersistenceException {
        this.delegate.insert(indexEntry, denormalizedData, ROW_KEY, context);
    }

    /**
     * Updates the index after a row has been deleted from the main column family.
     * 
     * @param indexEntry key of the row removed from the main column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void delete(C indexEntry, CassandraContext<N> context) throws PersistenceException {
        this.delegate.delete(indexEntry, ROW_KEY, context);
    }

    /**
     * Updates the index after deleting all rows from the main column family.
     * 
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void clear(CassandraContext<N> context) throws PersistenceException {
        this.delegate.delete(ROW_KEY, context);
    }

    /**
     * Counts all the rows in the main column family or the number of columns in the index column
     * family.
     * 
     * @param context data store context
     * @return the number of rows in the main column family or the number of columns in the index
     *         column family
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> long count(CassandraContext<N> context) throws PersistenceException {
        return this.delegate.count(ROW_KEY, context);
    }

    /**
     * Reads the index entries.
     * <p>
     * Note that it is simpler to read the entire main column family. This method just guarantees
     * sorting by row key but then the rows will need to be loaded from the main column family.
     * 
     * @param context data store context
     * @return the list of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> read(CassandraContext<N> context) throws PersistenceException {
        return this.delegate.read(ROW_KEY, context);
    }

    /**
     * Reads a page of index entries.
     * 
     * @param pageRequest page request
     * @param context data store context
     * @return a page of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> MarkPage<Column<C, D>> read(MarkPageRequest<C> pageRequest, CassandraContext<N> context)
            throws PersistenceException {
        return this.delegate.read(ROW_KEY, pageRequest, context);
    }

    /**
     * Reads the index entries.
     * <p>
     * An index is normally used to get rows (from the main column family) that match a specific
     * indexed value, not to load entries known to match the indexed value - like in this method.
     * This method has been defined to allow secondary indexes to be used by a
     * {@link SecondaryIndexIntegrator.SecondaryIndexReader}.
     * 
     * @param indexEntries index entries to read
     * @param context data store context
     * @return the list of indexed columns with the denormalized data. Any index entry from
     *         {@code indexEntries} that doesn't exist is not included in the result.
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> read(List<C> indexEntries, CassandraContext<N> context) throws PersistenceException {
        return this.delegate.read(indexEntries, ROW_KEY, context);
    }
}
