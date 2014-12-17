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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.hp.util.common.Mutator;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnFactory;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.index.StringSecondaryIndex.StringIndexEntry;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Custom secondary index column family to keep strings in a wide row to provide "equals",
 * "starts with" and "ends with" reads.
 * <p>
 * <b>String Secondary Index Column Family:</b>
 * <ul>
 * <li>Row key (Key validation: String): A few rows with a well-known key.</li>
 * <li>Column value (default validation: Bytes): Denormalized data.</li>
 * <li>Column name (comparator: composite(String, row key in main column family): The string value
 * and the key in the main column family.</li>
 * 
 * <pre>
 * column_family_name {
 *     "strings": {
 *         &lt;string, id_1&gt;: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         &lt;string, id_n&gt;: &lt;data provided by the denormalizer&gt;,
 *     }
 *     "reversed_strings": {
 *         &lt;reversed_string, id_1&gt;: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         &lt;reversed_string, id_n&gt;: &lt;data provided by the denormalizer&gt;,
 *     }
 * }
 * </pre>
 * 
 * See {@link IndexEntryHandler} for a recipe to update secondary indexes
 * 
 * @param <I> type of the row key in the main column family
 * @param <C> type of the column name in the custom secondary index
 * @param <D> type of the denormalized data to set as the value in the indexed columns
 * @author Fabiel Zuniga
 */
public class StringSecondaryIndex<I extends Serializable & Comparable<I>, C extends Serializable & Comparable<C> & StringIndexEntry<I>, D>
        implements ColumnFamilyHandler {

    private final GenericCustomSecondaryIndex<String, C, D> delegate;

    private ColumnFamily<String, C> columnFamily;
    private final DataType<D> denormalizedDataType;

    private RangeLimit<I> rangeLimit;
    private Mutator<C, String> mutator;

    private final ColumnShardStrategy<String, String> shardStrategy;
    private final ColumnShardStrategy<String, String> shardStrategyReversed;

    /**
     * Creates a custom secondary index.
     * 
     * @param columnFamilyName name for the column family this index will keep data into
     * @param indexedColumnNameDataType type of the secondary index column name
     * @param rangeLimit range limit for the row key in the main column family
     * @param mutator mutator to replace the indexed string value
     * @param denormalizedDataType denormalized data type; column value in the custom secondary
     */
    public StringSecondaryIndex(String columnFamilyName, DataType<C> indexedColumnNameDataType,
            RangeLimit<I> rangeLimit, Mutator<C, String> mutator, DataType<D> denormalizedDataType) {
        if (columnFamilyName == null) {
            throw new NullPointerException("columnFamilyName cannot be null");
        }

        if (columnFamilyName.isEmpty()) {
            throw new IllegalArgumentException("columnFamilyName cannot be empty");
        }

        if (indexedColumnNameDataType == null) {
            throw new NullPointerException("indexedColumnNameDataType cannot be null");
        }

        if (rangeLimit == null) {
            throw new NullPointerException("rangeLimit cannot be null");
        }

        if (mutator == null) {
            throw new NullPointerException("mutator cannot be null");
        }

        if (denormalizedDataType == null) {
            throw new NullPointerException("denormalizedDataType cannot be null");
        }

        this.denormalizedDataType = denormalizedDataType;
        this.rangeLimit = rangeLimit;
        this.mutator = mutator;

        this.columnFamily = new ColumnFamily<String, C>(columnFamilyName, BasicType.STRING_UTF8, indexedColumnNameDataType,
                "Custom secondary index column family that uses strings as indexed values.");

        this.delegate = new GenericCustomSecondaryIndex<String, C, D>(this.columnFamily, denormalizedDataType);

        this.shardStrategy = new StringShardStrategy("strings");
        this.shardStrategyReversed = new StringShardStrategy("reversed_strings");
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
        if (indexEntry == null) {
            throw new NullPointerException("indexEntry cannot be null");
        }

        String indexedStringValue = indexEntry.getIndexedStringValue();
        String reversedStringValue = reverse(indexedStringValue);
        C reversedIndexEntry = this.mutator.mutate(indexEntry, reversedStringValue);

        String rowKey = this.shardStrategy.getShard(indexedStringValue);
        String reversedRowKey = this.shardStrategyReversed.getShard(reversedStringValue);

        this.delegate.insert(indexEntry, denormalizedData, rowKey, context);
        this.delegate.insert(reversedIndexEntry, denormalizedData, reversedRowKey, context);
    }

    /**
     * Updates the index after a row has been deleted from the main column family.
     * 
     * @param indexEntry key of the row removed from the main column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void delete(C indexEntry, CassandraContext<N> context) throws PersistenceException {
        if (indexEntry == null) {
            throw new NullPointerException("indexEntry cannot be null");
        }

        String indexedStringValue = indexEntry.getIndexedStringValue();
        String reversedStringValue = reverse(indexedStringValue);
        C reversedIndexEntry = this.mutator.mutate(indexEntry, reversedStringValue);

        String rowKey = this.shardStrategy.getShard(indexedStringValue);
        String reversedRowKey = this.shardStrategyReversed.getShard(reversedStringValue);

        this.delegate.delete(indexEntry, rowKey, context);
        this.delegate.delete(reversedIndexEntry, reversedRowKey, context);
    }

    /**
     * Updates the index after deleting all rows from the main column family.
     * 
     * @param context data store context
     */
    public <N> void clear(CassandraContext<N> context) {
        this.delegate.clear(context);
    }

    /**
     * Counts the index entries matching {@code indexedStringValue}.
     * 
     * @param indexedStringValue indexed string value to count
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return the number of rows in the main column family or the number of columns in the index
     *         column family
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> long count(String indexedStringValue, StringIndexEntryFactory<I, C> indexEntryFactory,
            CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValue == null) {
            throw new NullPointerException("indexedStringValue cannot be null");
        }

        if (indexEntryFactory == null) {
            throw new NullPointerException("indexEntryFactory cannot be null");
        }

        C start = indexEntryFactory.create(indexedStringValue, this.rangeLimit.getStart());
        C end = indexEntryFactory.create(indexedStringValue, this.rangeLimit.getEnd());
        String rowKey = this.shardStrategy.getShard(indexedStringValue);

        return context.getCassandraClient().countColumnRange(rowKey, ColumnName.valueOf(start),
                ColumnName.valueOf(end), false, Integer.MAX_VALUE, this.columnFamily, context);
    }

    /**
     * Reads the index entries matching {@code indexedStringValue}.
     * 
     * @param indexedStringValue indexed string value to read
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return the list of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> read(String indexedStringValue, StringIndexEntryFactory<I, C> indexEntryFactory,
            CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValue == null) {
            throw new NullPointerException("indexedStringValue cannot be null");
        }

        if (indexEntryFactory == null) {
            throw new NullPointerException("indexEntryFactory cannot be null");
        }

        C start = indexEntryFactory.create(indexedStringValue, this.rangeLimit.getStart());
        C end = indexEntryFactory.create(indexedStringValue, this.rangeLimit.getEnd());
        String rowKey = this.shardStrategy.getShard(indexedStringValue);

        CassandraRow<String, C> row = context.getCassandraClient().readColumnRange(rowKey, ColumnName.valueOf(start),
                ColumnName.valueOf(end), false, Integer.MAX_VALUE, this.columnFamily,
                this.delegate.getDenormalizedDataTypeProvider(), context);

        return convertIndexedColumns(row.getColumns());
    }

    /**
     * Reads a page of entries matching {@code indexedStringValue}.
     * <p>
     * A range query is executed internally, thus if the mark in {@link MarkPageRequest pageRequest}
     * isn't null but doesn't exist, the page will not necessarily be the first page. However, if
     * the mark does not exist, then the mark in the resultant {@link MarkPage} will be null.
     * 
     * @param indexedStringValue indexed string value to read
     * @param pageRequest page request
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return a page of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> MarkPage<Column<C, D>> read(String indexedStringValue, MarkPageRequest<C> pageRequest,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValue == null) {
            throw new NullPointerException("indexedStringValue cannot be null");
        }

        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        String rowKey = this.shardStrategy.getShard(indexedStringValue);

        ColumnName<C, ?> convertedMark = null;
        if (pageRequest.getMark() != null) {
            if (!pageRequest.getMark().getIndexedStringValue().contains(indexedStringValue)) {
                throw new IllegalArgumentException(
                        "page request's mark doesn't contain the given indexedStringValue. indexedStringValue="
                                + indexedStringValue + ", pageRequest=" + pageRequest);
            }

            convertedMark = ColumnName.valueOf(pageRequest.getMark());
        }
        else {
            convertedMark = ColumnName
                    .valueOf(indexEntryFactory.create(indexedStringValue, this.rangeLimit.getStart()));
        }

        ColumnName<C, ?> end = null;
        if (pageRequest.getNavigation() == Navigation.NEXT) {
            end = ColumnName.valueOf(indexEntryFactory.create(indexedStringValue, this.rangeLimit.getEnd()));
        }
        else {
            end = ColumnName.valueOf(indexEntryFactory.create(indexedStringValue, this.rangeLimit.getStart()));
        }

        MarkPageRequest<ColumnName<C, ?>> convertedMarkPageRequest = pageRequest
                .<ColumnName<C, ?>> convert(convertedMark);

        MarkPage<Column<C, ?>> page = context.getCassandraClient().read(rowKey, convertedMarkPageRequest, end,
                this.columnFamily, this.delegate.getDenormalizedDataTypeProvider(), context);

        @SuppressWarnings("unchecked")
        Column<C, D> markToReturn = (Column<C, D>) page.getRequest().getMark();
        List<Column<C, D>> convertedData = convertIndexedColumns(page.getData());
        return new MarkPage<Column<C, D>>(page.getRequest().convert(markToReturn), convertedData);
    }

    /**
     * Reads the index entries matching {@code indexedStringValue}.
     * <p>
     * An index is normally used to get rows (from the main column family) that match a specific
     * indexed value, not to load entries known to match the indexed value - like in this method.
     * This method has been defined to allow secondary indexes to be used by a
     * {@link SecondaryIndexIntegrator.SecondaryIndexReader}.
     * 
     * @param indexedStringValue indexed string value to read
     * @param indexEntries index entries to read
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context.
     * @return the list of indexed columns with the denormalized data. Any index entry from
     *         {@code indexEntries} that doesn't exist in the index given by {@code indexKey} is not
     *         included in the result.
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> read(String indexedStringValue, List<C> indexEntries,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        // Used to check whether an entry belongs to the result (Faster implementation of
        // contains(Object)).
        Set<C> indexEntriesSet = new LinkedHashSet<C>(indexEntries);

        List<Column<C, D>> result = new LinkedList<Column<C, D>>();

        MarkPageRequest<C> pageRequest = new MarkPageRequest<C>(10000);
        MarkPage<Column<C, D>> page = null;

        do {
            page = read(indexedStringValue, pageRequest, indexEntryFactory, context);

            for (Column<C, D> column : page.getData()) {
                if (indexEntriesSet.contains(column.getName().getValue())) {
                    result.add(column);
                }
            }

            MarkPageRequest<Column<C, D>> nextPageRequest = page.getNextPageRequest();
            pageRequest = null;
            if (nextPageRequest != null) {
                pageRequest = nextPageRequest.<C> convert(nextPageRequest.getMark() != null ? nextPageRequest.getMark()
                        .getName().getValue() : null);
            }

        }
        while (!page.isEmpty());

        return result;
    }

    /**
     * Counts the index entries matching {@code indexedStringValuePrefix}.
     * 
     * @param indexedStringValuePrefix prefix of the indexed string value to count
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return the number of rows in the main column family or the number of columns in the index
     *         column family
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> long countStartsWith(String indexedStringValuePrefix, StringIndexEntryFactory<I, C> indexEntryFactory,
            CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValuePrefix == null) {
            throw new NullPointerException("indexedStringValuePrefix cannot be null");
        }

        if (indexEntryFactory == null) {
            throw new NullPointerException("indexEntryFactory cannot be null");
        }

        C start = indexEntryFactory.create(indexedStringValuePrefix, this.rangeLimit.getStart());
        C end = indexEntryFactory.create(indexedStringValuePrefix + RangeLimit.STRING_RANGE_LIMIT.getEnd(),
                this.rangeLimit.getEnd());
        String rowKey = this.shardStrategy.getShard(indexedStringValuePrefix);

        return context.getCassandraClient().countColumnRange(rowKey, ColumnName.valueOf(start),
                ColumnName.valueOf(end), false, Integer.MAX_VALUE, this.columnFamily, context);
    }

    /**
     * Reads the index entries matching {@code indexedStringValuePrefix}.
     * 
     * @param indexedStringValuePrefix prefix of the indexed string value to read
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context.
     * @return the list of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> readStartsWith(String indexedStringValuePrefix,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValuePrefix == null) {
            throw new NullPointerException("indexedStringValuePrefix cannot be null");
        }

        if (indexEntryFactory == null) {
            throw new NullPointerException("indexEntryFactory cannot be null");
        }

        C start = indexEntryFactory.create(indexedStringValuePrefix, this.rangeLimit.getStart());
        C end = indexEntryFactory.create(indexedStringValuePrefix + RangeLimit.STRING_RANGE_LIMIT.getEnd(),
                this.rangeLimit.getEnd());
        String rowKey = this.shardStrategy.getShard(indexedStringValuePrefix);

        CassandraRow<String, C> row = context.getCassandraClient().readColumnRange(rowKey, ColumnName.valueOf(start),
                ColumnName.valueOf(end), false, Integer.MAX_VALUE, this.columnFamily,
                this.delegate.getDenormalizedDataTypeProvider(), context);

        return convertIndexedColumns(row.getColumns());
    }

    /**
     * Reads a page of entries matching {@code indexedStringValuePrefix}.
     * <p>
     * A range query is executed internally, thus if the mark in {@link MarkPageRequest pageRequest}
     * isn't null but doesn't exist, the page will not necessarily be the first page. However, if
     * the mark does not exist, then the mark in the resultant {@link MarkPage} will be null.
     * 
     * @param indexedStringValuePrefix prefix of the indexed string value to read
     * @param pageRequest page request
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return a page of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> MarkPage<Column<C, D>> readStartsWith(String indexedStringValuePrefix, MarkPageRequest<C> pageRequest,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValuePrefix == null) {
            throw new NullPointerException("indexedStringValuePrefix cannot be null");
        }

        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        String rowKey = this.shardStrategy.getShard(indexedStringValuePrefix);

        ColumnName<C, ?> convertedMark = null;
        if (pageRequest.getMark() != null) {
            if (!pageRequest.getMark().getIndexedStringValue().contains(indexedStringValuePrefix)) {
                throw new IllegalArgumentException(
                        "page request's mark doesn't contain the given indexedStringValuePrefix. indexedStringValuePrefix="
                                + indexedStringValuePrefix + ", pageRequest=" + pageRequest);
            }

            convertedMark = ColumnName.valueOf(pageRequest.getMark());
        }
        else {
            convertedMark = ColumnName.valueOf(indexEntryFactory.create(indexedStringValuePrefix,
                    this.rangeLimit.getStart()));
        }

        ColumnName<C, ?> end = null;
        if (pageRequest.getNavigation() == Navigation.NEXT) {
            end = ColumnName.valueOf(indexEntryFactory.create(
                    indexedStringValuePrefix + RangeLimit.STRING_RANGE_LIMIT.getEnd(), this.rangeLimit.getEnd()));
        }
        else {
            end = ColumnName.valueOf(indexEntryFactory.create(indexedStringValuePrefix, this.rangeLimit.getStart()));
        }

        MarkPageRequest<ColumnName<C, ?>> convertedMarkPageRequest = pageRequest
                .<ColumnName<C, ?>> convert(convertedMark);

        MarkPage<Column<C, ?>> page = context.getCassandraClient().read(rowKey, convertedMarkPageRequest, end,
                this.columnFamily, this.delegate.getDenormalizedDataTypeProvider(), context);

        @SuppressWarnings("unchecked")
        Column<C, D> markToReturn = (Column<C, D>) page.getRequest().getMark();
        List<Column<C, D>> convertedData = convertIndexedColumns(page.getData());
        return new MarkPage<Column<C, D>>(page.getRequest().convert(markToReturn), convertedData);
    }

    /**
     * Reads the index entries matching {@code indexedStringValuePrefix}.
     * <p>
     * An index is normally used to get rows (from the main column family) that match a specific
     * indexed value, not to load entries known to match the indexed value - like in this method.
     * This method has been defined to allow secondary indexes to be used by a
     * {@link SecondaryIndexIntegrator.SecondaryIndexReader}.
     * 
     * @param indexedStringValuePrefix prefix of the indexed string value to read
     * @param indexEntries index entries to read
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context.
     * @return the list of indexed columns with the denormalized data. Any index entry from
     *         {@code indexEntries} that doesn't exist in the index given by {@code indexKey} is not
     *         included in the result.
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> readStartsWith(String indexedStringValuePrefix, List<C> indexEntries,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        // Used to check whether an entry belongs to the result (Faster implementation of
        // contains(Object)).
        Set<C> indexEntriesSet = new LinkedHashSet<C>(indexEntries);

        List<Column<C, D>> result = new LinkedList<Column<C, D>>();

        MarkPageRequest<C> pageRequest = new MarkPageRequest<C>(10000);
        MarkPage<Column<C, D>> page = null;

        do {
            page = readStartsWith(indexedStringValuePrefix, pageRequest, indexEntryFactory, context);

            for (Column<C, D> column : page.getData()) {
                if (indexEntriesSet.contains(column.getName().getValue())) {
                    result.add(column);
                }
            }

            MarkPageRequest<Column<C, D>> nextPageRequest = page.getNextPageRequest();
            pageRequest = null;
            if (nextPageRequest != null) {
                pageRequest = nextPageRequest.<C> convert(nextPageRequest.getMark() != null ? nextPageRequest.getMark()
                        .getName().getValue() : null);
            }

        }
        while (!page.isEmpty());

        return result;
    }

    /**
     * Counts the index entries matching {@code indexedStringValueSuffix}.
     * 
     * @param indexedStringValueSuffix suffix of the indexed string value to count
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context.
     * @return the number of rows in the main column family or the number of columns in the index
     *         column family
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> long countEndsWith(String indexedStringValueSuffix, StringIndexEntryFactory<I, C> indexEntryFactory,
            CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValueSuffix == null) {
            throw new NullPointerException("indexedStringValueSuffix cannot be null");
        }

        if (indexEntryFactory == null) {
            throw new NullPointerException("indexEntryFactory cannot be null");
        }

        String reversedSuffix = reverse(indexedStringValueSuffix);
        C start = indexEntryFactory.create(reversedSuffix, this.rangeLimit.getStart());
        C end = indexEntryFactory.create(reversedSuffix + RangeLimit.STRING_RANGE_LIMIT.getEnd(),
                this.rangeLimit.getEnd());
        String rowKey = this.shardStrategyReversed.getShard(reversedSuffix);

        return context.getCassandraClient().countColumnRange(rowKey, ColumnName.valueOf(start),
                ColumnName.valueOf(end), false, Integer.MAX_VALUE, this.columnFamily, context);
    }

    /**
     * Reads the index entries matching {@code indexedStringValueSuffix}.
     * 
     * @param indexedStringValueSuffix suffix of the indexed string value to read
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return the list of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> readEndsWith(String indexedStringValueSuffix,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValueSuffix == null) {
            throw new NullPointerException("indexedStringValueSuffix cannot be null");
        }

        if (indexEntryFactory == null) {
            throw new NullPointerException("indexEntryFactory cannot be null");
        }

        String reversedSuffix = reverse(indexedStringValueSuffix);
        C start = indexEntryFactory.create(reversedSuffix, this.rangeLimit.getStart());
        C end = indexEntryFactory.create(reversedSuffix + RangeLimit.STRING_RANGE_LIMIT.getEnd(),
                this.rangeLimit.getEnd());
        String rowKey = this.shardStrategyReversed.getShard(reversedSuffix);

        CassandraRow<String, C> row = context.getCassandraClient().readColumnRange(rowKey, ColumnName.valueOf(start),
                ColumnName.valueOf(end), false, Integer.MAX_VALUE, this.columnFamily,
                this.delegate.getDenormalizedDataTypeProvider(), context);

        return restoreReversedStrings(row.getColumns());
    }

    /**
     * Reads a page of entries matching {@code indexedStringValueSuffix}.
     * <p>
     * A range query is executed internally, thus if the mark in {@link MarkPageRequest pageRequest}
     * isn't null but doesn't exist, the page will not necessarily be the first page. However, if
     * the mark does not exist, then the mark in the resultant {@link MarkPage} will be null.
     * 
     * @param indexedStringValueSuffix suffix of the indexed string value to read
     * @param pageRequest page request
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context
     * @return a page of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> MarkPage<Column<C, D>> readEndsWith(String indexedStringValueSuffix, MarkPageRequest<C> pageRequest,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        if (indexedStringValueSuffix == null) {
            throw new NullPointerException("indexedStringValueSuffix cannot be null");
        }

        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        String reversedSuffix = reverse(indexedStringValueSuffix);

        String rowKey = this.shardStrategyReversed.getShard(reversedSuffix);

        ColumnName<C, ?> convertedMark = null;
        if (pageRequest.getMark() != null) {
            if (!pageRequest.getMark().getIndexedStringValue().contains(indexedStringValueSuffix)) {
                throw new IllegalArgumentException(
                        "page request's mark doesn't contain the given indexedStringValueSuffix. indexedStringValueSuffix="
                                + indexedStringValueSuffix + ", pageRequest=" + pageRequest);
            }

            C reversedMark = this.mutator.mutate(pageRequest.getMark(), reverse(pageRequest.getMark()
                    .getIndexedStringValue()));
            convertedMark = ColumnName.valueOf(reversedMark);
        }
        else {
            convertedMark = ColumnName.valueOf(indexEntryFactory.create(reversedSuffix, this.rangeLimit.getStart()));
        }

        ColumnName<C, ?> end = null;
        if (pageRequest.getNavigation() == Navigation.NEXT) {
            end = ColumnName.valueOf(indexEntryFactory.create(reversedSuffix + RangeLimit.STRING_RANGE_LIMIT.getEnd(),
                    this.rangeLimit.getEnd()));
        }
        else {
            end = ColumnName.valueOf(indexEntryFactory.create(reversedSuffix, this.rangeLimit.getStart()));
        }

        MarkPageRequest<ColumnName<C, ?>> convertedMarkPageRequest = pageRequest
                .<ColumnName<C, ?>> convert(convertedMark);

        MarkPage<Column<C, ?>> page = context.getCassandraClient().read(rowKey, convertedMarkPageRequest, end,
                this.columnFamily, this.delegate.getDenormalizedDataTypeProvider(), context);

        Column<C, D> markToReturn = null;
        if (page.getRequest().getMark() != null) {
            Column<C, D> restoredMark = restoreReversedString(page.getRequest().getMark());
            markToReturn = restoredMark;
        }
        return new MarkPage<Column<C, D>>(page.getRequest().convert(markToReturn),
                restoreReversedStrings(page.getData()));
    }

    /**
     * Reads the index entries matching {@code indexedStringValueSuffix}.
     * <p>
     * An index is normally used to get rows (from the main column family) that match a specific
     * indexed value, not to load entries known to match the indexed value - like in this method.
     * This method has been defined to allow secondary indexes to be used by a
     * {@link SecondaryIndexIntegrator.SecondaryIndexReader}.
     * 
     * @param indexedStringValueSuffix suffix of the indexed string value to read
     * @param indexEntries index entries to read
     * @param indexEntryFactory factory to create an index entry. This factory should add additional
     *            grouping and sorting information that is part of the index column name.
     * @param context data store context.
     * @return the list of indexed columns with the denormalized data. Any index entry from
     *         {@code indexEntries} that doesn't exist in the index given by {@code indexKey} is not
     *         included in the result.
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> readEndsWith(String indexedStringValueSuffix, List<C> indexEntries,
            StringIndexEntryFactory<I, C> indexEntryFactory, CassandraContext<N> context) throws PersistenceException {
        // Used to check whether an entry belongs to the result (Faster implementation of
        // contains(Object)).
        Set<C> indexEntriesSet = new LinkedHashSet<C>(indexEntries);

        List<Column<C, D>> result = new LinkedList<Column<C, D>>();

        MarkPageRequest<C> pageRequest = new MarkPageRequest<C>(10000);
        MarkPage<Column<C, D>> page = null;

        do {
            page = readEndsWith(indexedStringValueSuffix, pageRequest, indexEntryFactory, context);

            for (Column<C, D> column : page.getData()) {
                if (indexEntriesSet.contains(column.getName().getValue())) {
                    result.add(column);
                }
            }

            MarkPageRequest<Column<C, D>> nextPageRequest = page.getNextPageRequest();
            pageRequest = null;
            if (nextPageRequest != null) {
                pageRequest = nextPageRequest.<C> convert(nextPageRequest.getMark() != null ? nextPageRequest.getMark()
                        .getName().getValue() : null);
            }

        }
        while (!page.isEmpty());

        return result;
    }

    private static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }

    @SuppressWarnings("unchecked")
    private static <I extends Serializable & Comparable<I>, C extends Serializable & Comparable<C> & StringIndexEntry<I>, D> List<Column<C, D>> convertIndexedColumns(
            Collection<Column<C, ?>> columns) {
        List<Column<C, D>> indexedColumns = new LinkedList<Column<C, D>>();
        for (Column<C, ?> column : columns) {
            indexedColumns.add((Column<C, D>) column);
        }
        return indexedColumns;
    }

    private List<Column<C, D>> restoreReversedStrings(Collection<Column<C, ?>> columns) {
        List<Column<C, D>> restoredColumns = new LinkedList<Column<C, D>>();
        for (Column<C, ?> column : columns) {
            restoredColumns.add(restoreReversedString(column));
        }
        return restoredColumns;
    }

    private Column<C, D> restoreReversedString(Column<C, ?> reversedColumn) {
        /*
         * When reversed strings are used, the indexed string value in the column name will be
         * reversed and sorting will also be based in the reversed string value. A command is used
         * to restore the indexed string value. Sorting is not changed so it is consistent with
         * paging.
         */
        /*
         * All types for column values in a custom secondary index are of type same type Column<C,
         * D>, but since they are read using CassandraClient, the type of the collection for the
         * values is ? (Since Cassandra can store columns with different value).
         */
        @SuppressWarnings("unchecked")
        Column<C, D> typedReversedColumn = (Column<C, D>) reversedColumn;
        C reversedValue = typedReversedColumn.getName().getValue();
        String restoredIndexStringValue = reverse(reversedValue.getIndexedStringValue());
        C restoredValue = this.mutator.mutate(reversedValue, restoredIndexStringValue);
        ColumnName<C, D> restoredColumnName = ColumnName.valueOf(restoredValue);
        Column<C, D> restoredColumn = ColumnFactory.getInstance().create(restoredColumnName,
                typedReversedColumn.getValue(),
                this.denormalizedDataType);
        return restoredColumn;
    }

    /**
     * Type to use as column name in a {@link StringSecondaryIndex}.
     * 
     * @param <K> type of the row key in the main column family
     */
    public static interface StringIndexEntry<K extends Serializable & Comparable<K>> {

        /**
         * Gets the row key in the main column family.
         * 
         * @return the row key
         */
        public K getRowKey();

        /**
         * Gets the indexed {@link String} value.
         * 
         * @return the {@link String} value
         */
        public String getIndexedStringValue();
    }

    /**
     * Index entry factory.
     * 
     * @param <K> type of the row key in the main column family
     * @param <C> type of the column name in the custom secondary index
     */
    public static interface StringIndexEntryFactory<K extends Serializable & Comparable<K>, C extends StringIndexEntry<K>> {

        /**
         * Creates a {@link StringIndexEntry}.
         * 
         * @param indexedStringValue the indexed {@link String} value
         * @param rowKey row key in the main column family
         * @return a {@link StringIndexEntry}
         */
        public C create(String indexedStringValue, K rowKey);
    }
}
