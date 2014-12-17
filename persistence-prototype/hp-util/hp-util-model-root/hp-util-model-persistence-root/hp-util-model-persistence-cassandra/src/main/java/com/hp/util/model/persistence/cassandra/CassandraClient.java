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
import java.util.List;

import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.cql.CqlPredicate;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;
import com.hp.util.model.persistence.cassandra.keyspace.KeyspaceConfiguration;

/**
 * Cassandra client facade.
 * <p>
 * Provides a simplified interface to the Cassandra client library.
 * 
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public interface CassandraClient<N> {

    /**
     * Verifies whether a keyspace already exists.
     * 
     * @param keyspace keyspace to verify
     * @param context data store context
     * @return {@code true} if the keyspace exists, {@code false} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public boolean exists(Keyspace keyspace, CassandraContext<N> context) throws PersistenceException;

    /**
     * Creates a keyspace if it does not exist.
     * 
     * @param keyspace keyspace to create
     * @param configuration keyspace configuration
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void createKeyspace(Keyspace keyspace, KeyspaceConfiguration configuration, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Removes a keyspace if it exists.
     * 
     * @param keyspace keyspace to remove
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void dropKeyspace(Keyspace keyspace, CassandraContext<N> context) throws PersistenceException;

    /**
     * Verifies whether a column family already exists.
     * 
     * @param columnFamily column family to verify
     * @param keyspace keyspace to verify
     * @param context data store context
     * @return {@code true} if the column family exists, {@code false} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public boolean exists(ColumnFamily<?, ?> columnFamily, Keyspace keyspace, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Creates a column family if it does not exists.
     * 
     * @param columnFamilyDefinition column family definition
     * @param keyspace keyspace to create the column family into
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void createColumnFamily(ColumnFamily<?, ?> columnFamilyDefinition, Keyspace keyspace,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Removes a column family.
     * 
     * @param columnFamily column family to remove
     * @param keyspace keyspace to remove the column family from
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void dropColumnFamily(ColumnFamily<?, ?> columnFamily, Keyspace keyspace, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Removes all rows from a column family.
     * <p>
     * Note: This operation is expensive.
     * 
     * @param columnFamily column family to remove all rows from
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void truncateColumnFamily(ColumnFamily<?, ?> columnFamily, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Inserts (Creates or updates) a single column. e
     * 
     * @param column column to insert
     * @param rowKey row key
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void insert(Column<C, ?> column, K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Inserts (Creates or updates) a row.
     * 
     * @param row rows to insert
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void insert(CassandraRow<K, C> row,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Inserts (Creates or updates) a collection of rows.
     * 
     * @param rows rows to insert
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void insert(
            Collection<CassandraRow<K, C>> rows, ColumnFamily<K, C> columnFamily, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Deletes a single column.
     * 
     * @param columnName name of the column to delete
     * @param rowKey row key
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(ColumnName<C, ?> columnName,
            K rowKey, ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Deletes a collection of columns.
     * 
     * @param columnsNames names of the columns to delete
     * @param rowKey row key
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(
            Collection<ColumnName<C, ?>> columnsNames, K rowKey, ColumnFamily<K, C> columnFamily,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Deletes a row.
     * 
     * @param rowKey key of the row to delete
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Deletes a collection of rows.
     * 
     * @param keys keys of the rows to delete
     * @param columnFamily column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(Collection<K> keys,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Verifies if a row key exists.
     * 
     * @param rowKey row key
     * @param columnFamily column family
     * @param context data store context
     * @return {@code true} if the row key exists, {@code false} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> boolean exist(K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads a single column.
     * 
     * @param columnName name of the column to read
     * @param rowKey row key
     * @param columnFamily column family
     * @param columnValueType type of the column value
     * @param context data store context
     * @return the column if it is found, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>, D> Column<C, D> read(
            ColumnName<C, D> columnName, K rowKey, ColumnFamily<K, C> columnFamily, DataType<D> columnValueType,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads an entire row.
     * 
     * @param rowKey row key
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return the row if it is found, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> read(K rowKey,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads a slice of rows.
     * 
     * @param rowKeys keys of the rows to read
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return the collection of found rows
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> List<CassandraRow<K, C>> read(
            List<K> rowKeys, ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads a column slice (Not necessarily consecutive columns).
     * 
     * @param columnNames columns to read
     * @param rowKey row key
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return a row containing just the columns from {@code columnNames} that exists in the row
     *         with key {@code rowKey}
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> readColumns(
            Collection<ColumnName<C, ?>> columnNames, K rowKey, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Reads all rows in a column family.
     * 
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return the collection of found rows
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> Collection<CassandraRow<K, C>> read(
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads rows that satisfy the given predicate.
     * <p>
     * <strong>Node:</strong> Having a query language like CQL available doesn't mean the same set
     * of operations supported by SQL are also supported with CQL. The operations available with CQL
     * are the same provided by the native Cassandra Client (Astyanax for example). Therefore, you
     * can accomplish the same using the native Cassandra Client, but in most cases in a type-safe
     * manner.
     * 
     * @param predicate predicate to apply
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return the rows that satisfy the given predicate
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    @Deprecated
    public <K extends Serializable, C extends Serializable & Comparable<C>> List<CassandraRow<K, C>> read(
            CqlPredicate predicate, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Count the number of columns.
     * 
     * @param rowKey row key
     * @param columnFamily column family
     * @param context data store context
     * @return the column count
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> long countColumns(K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads a column range (consecutive columns).
     * 
     * @param rowKey row key
     * @param startColumn First column in the range. {@code null} to guarantee the first column in
     *            the row is included.
     * @param endColumn Last column in the range. {@code null} to guarantee the last column in the
     *            row is included.
     * @param reverse {@code True} if the order should be reversed. Note that for reversed,
     *            {@code startColumn} should be greater than {@code endColumn}
     * @param maxSize Maximum number of columns to return (similar to SQL LIMIT)
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return A row with the selected columns
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> readColumnRange(
            K rowKey, ColumnName<C, ?> startColumn, ColumnName<C, ?> endColumn, boolean reverse, int maxSize,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<N> context) throws PersistenceException;

    /* *
     * Reads a column range (consecutive columns).
     * 
     * @param rowKey row key
     * @param range range
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return A row with the selected columns
     * @throws PersistenceException if persistence errors occur while executing the operation
     * /
    @Deprecated
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> readColumnRange(
            K rowKey, com.netflix.astyanax.model.ByteBufferRange.ByteBufferRange range,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<N> context) throws PersistenceException;
     */

    /**
     * Counts columns in a range (consecutive columns).
     * 
     * @param rowKey row key
     * @param startColumn First column in the range. {@code null} to guarantee the first column in
     *            the row is included.
     * @param endColumn Last column in the range. {@code null} to guarantee the last column in the
     *            row is included.
     * @param reverse {@code True} if the order should be reversed. Note that for reversed,
     *            {@code startColumn} should be greater than {@code endColumn}
     * @param maxSize Maximum number of columns to count (similar to SQL LIMIT)
     * @param columnFamily column family
     * @param context data store context
     * @return the number of columns in the range if it is less than {@code maxSize},
     *         {@code maxSize} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */

    public <K extends Serializable, C extends Serializable & Comparable<C>> long countColumnRange(K rowKey,
            ColumnName<C, ?> startColumn, ColumnName<C, ?> endColumn, boolean reverse, int maxSize,
            ColumnFamily<K, C> columnFamily, CassandraContext<N> context) throws PersistenceException;

    /* *
     * Counts columns in a range (consecutive columns).
     * 
     * @param rowKey row key
     * @param range range
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return the number of columns in the range
     * @throws PersistenceException if persistence errors occur while executing the operation
     * /
    @Deprecated
    public <K extends Serializable, C extends Serializable & Comparable<C>> long countColumnRange(K rowKey,
            com.netflix.astyanax.model.ByteBufferRangeByteBufferRange range, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, CassandraContext<N> context)
            throws PersistenceException;
     */

    /**
     * Reads a page of columns.
     * <p>
     * A range query is executed internally, thus if the mark in {@link MarkPageRequest pageRequest}
     * isn't null but doesn't exist, the page will not necessarily be the first page. However, if
     * the mark does not exist, then the mark in the resultant {@link MarkPage} will be null.
     * 
     * @param rowKey row key
     * @param pageRequest page request
     * @param end last column to consider. {@code null} to consider up to the last column in the row
     *            if navigating to the next page or fist column if navigating to the previous page.
     *            Note that this end must be greater than the page request's mark if navigating to
     *            the next page and less than the mark if navigating to the previous page.
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param context data store context
     * @return a page of columns
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>> MarkPage<Column<C, ?>> read(K rowKey,
            MarkPageRequest<ColumnName<C, ?>> pageRequest, ColumnName<C, ?> end, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Performs a search based on an indexed column (secondary index).
     * 
     * @param columnName column name to apply the expression on. Note that the column must be
     *            indexed.
     * @param condition condition to apply. Note: UNEQUAL mode not supported yet.
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param indexType secondary index type
     * @param context data store context
     * @return the rows matching the condition
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>, D> List<CassandraRow<K, C>> searchWithIndex(
            ColumnName<C, D> columnName, EqualityCondition<D> condition, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, DataType<D> indexType, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Performs a search based on an indexed column (secondary index).
     * 
     * @param columnName column name to apply the expression on. Note that the column must be
     *            indexed
     * @param condition condition to apply. Note: NOT_IN is not supported.
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param indexType secondary index type
     * @param context data store context
     * @return the rows matching the condition
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>, D> List<CassandraRow<K, C>> searchWithIndex(
            ColumnName<C, D> columnName, SetCondition<D> condition, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, DataType<D> indexType, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Performs a search based on an indexed column (secondary index). This method supports both
     * modes IN and NOT_IN.
     * 
     * @param columnName column name to apply the expression on. Note that the column must be
     *            indexed.
     * @param condition condition to apply
     * @param enumClass enum class
     * @param columnFamily column family
     * @param columnValueTypeProvider column value type provider
     * @param indexType secondary index type
     * @param context data store context
     * @return the rows matching the condition
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <K extends Serializable, C extends Serializable & Comparable<C>, D extends Enum<D>> List<CassandraRow<K, C>> searchWithIndex(
            ColumnName<C, D> columnName, SetCondition<D> condition, Class<D> enumClass,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider, DataType<D> indexType,
            CassandraContext<N> context) throws PersistenceException;

    /**
     * Prepares a context for batch operations.
     * 
     * @param context data store context
     * @return a batch
     */
    public Batch<N> prepareBatch(CassandraContext<N> context);

    /* 
     * Performs a search based on an indexed column (secondary index).
     *
     * @param columnName column name to apply the expression on. Note that the column must be indexed
     * @param condition condition to apply
     * @param columnFamily column family
     * @param decoderProvider column decoder provider
     * @param expressionStrategy index value expression strategy
     * @param context data store context
     * @return the rows matching the condition
     * /
    public <K extends Serializable, C extends Serializable & Comparable<C>, D extends Comparable<D>> List<CassandraRow<K, C>> searchWithIndex(
        ColumnName<C, D> columnName, ComparabilityCondition<D> condition, ColumnFamily<K, C> columnFamily,
        Provider<ColumnDecoder<C, ?>, ColumnName<C, ?>> decoderProvider,
        IndexValueExpressionStrategy<K, C, D> expressionStrategy, CassandraContext<N> context);
    */
}
