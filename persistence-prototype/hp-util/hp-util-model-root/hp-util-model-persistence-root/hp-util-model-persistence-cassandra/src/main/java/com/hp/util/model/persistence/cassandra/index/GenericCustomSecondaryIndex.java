/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnFactory;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.SameTypeColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Generic implementation of {@link CustomSecondaryIndex}
 * 
 * @param <K> type of the value (or composite values) of the indexed column (or columns) in the main
 *            column family. This value (or composite value) will become the row key in the
 *            secondary index column.
 * @param <C> type of the column name in the secondary index column family (row key in the main
 *            column family or composite value when sorting information is included)
 * @param <D> type of the denormalized data to set as the value in the indexed columns
 * @author Fabiel Zuniga
 */
public class GenericCustomSecondaryIndex<K extends Serializable, C extends Serializable & Comparable<C>, D> implements
        CustomSecondaryIndex<K, C, D> {

    private final ColumnFamily<K, C> columnFamily;
    private final DataType<D> denormalizedDataType;
    private final ColumnValueTypeProvider<C> columnValueTypeProvider;

    /**
     * Creates a {@link CustomSecondaryIndex}.
     * 
     * @param columnFamily column family this index will keep data into
     * @param denormalizedDataType denormalized data type; column value in the custom secondary
     */
    public GenericCustomSecondaryIndex(ColumnFamily<K, C> columnFamily, DataType<D> denormalizedDataType) {
        if (columnFamily == null) {
            throw new NullPointerException("columnFamilyDefinition cannot be null");
        }

        if (denormalizedDataType == null) {
            throw new NullPointerException("denormalizedDataType cannot be null");
        }

        this.columnFamily = columnFamily;
        this.denormalizedDataType = denormalizedDataType;
        this.columnValueTypeProvider = new SameTypeColumnValueTypeProvider<C, D>(denormalizedDataType);
    }

    @Override
    public Collection<ColumnFamily<?, ?>> getColumnFamilies() {
        Collection<ColumnFamily<?, ?>> definitions = new ArrayList<ColumnFamily<?, ?>>(1);
        definitions.add(this.columnFamily);
        return definitions;
    }

    @Override
    public <N> void insert(C indexEntry, D denormalizedData, K indexKey, CassandraContext<N> context)
            throws PersistenceException {
        ColumnName<C, D> indexedColumnName = ColumnName.valueOf(indexEntry);
        Column<C, D> indexedColumn = ColumnFactory.getInstance().create(indexedColumnName, denormalizedData,
                this.denormalizedDataType);
        context.getCassandraClient().insert(indexedColumn, indexKey, this.columnFamily, context);
    }

    @Override
    public <N> void delete(C indexEntry, K indexKey, CassandraContext<N> context) throws PersistenceException {
        ColumnName<C, Void> columnName = ColumnName.valueOf(indexEntry);
        context.getCassandraClient().delete(columnName, indexKey, this.columnFamily, context);
    }

    @Override
    public <N> void delete(K indexKey, CassandraContext<N> context) throws PersistenceException {
        context.getCassandraClient().delete(indexKey, this.columnFamily, context);
    }

    @Override
    public <N> void clear(CassandraContext<N> context) {
        try {
            context.getCassandraClient().truncateColumnFamily(this.columnFamily, context);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <N> long count(K indexKey, CassandraContext<N> context) throws PersistenceException {
        return context.getCassandraClient().countColumns(indexKey, this.columnFamily, context);
    }

    @Override
    public <N> List<Column<C, D>> read(K indexKey, CassandraContext<N> context) throws PersistenceException {
        CassandraRow<K, C> row = context.getCassandraClient().read(indexKey, this.columnFamily,
                this.columnValueTypeProvider,
                context);
        if (row != null) {
            return convertIndexedColumns(row.getColumns());
        }
        return Collections.emptyList();
    }

    @Override
    public <N> MarkPage<Column<C, D>> read(K indexKey, MarkPageRequest<C> pageRequest, CassandraContext<N> context)
            throws PersistenceException {
        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        ColumnName<C, ?> convertedMark = null;
        if (pageRequest.getMark() != null) {
            convertedMark = ColumnName.valueOf(pageRequest.getMark());
        }

        ColumnName<C, ?> end = null;

        MarkPageRequest<ColumnName<C, ?>> convertedMarkPageRequest = pageRequest
                .<ColumnName<C, ?>> convert(convertedMark);
        MarkPage<Column<C, ?>> page = context.getCassandraClient().read(indexKey, convertedMarkPageRequest, end,
                this.columnFamily, this.columnValueTypeProvider, context);

        @SuppressWarnings("unchecked")
        Column<C, D> markToReturn = (Column<C, D>) page.getRequest().getMark();
        List<Column<C, D>> convertedData = convertIndexedColumns(page.getData());
        return new MarkPage<Column<C, D>>(page.getRequest().convert(markToReturn), convertedData);
    }

    @Override
    public <N> List<Column<C, D>> read(List<C> indexEntries, K indexKey, CassandraContext<N> context)
            throws PersistenceException {
        if (indexEntries == null) {
            throw new NullPointerException("indexEntries cannot be null");
        }

        List<ColumnName<C, ?>> names = new ArrayList<ColumnName<C, ?>>(indexEntries.size());
        for (C entry : indexEntries) {
            names.add(ColumnName.valueOf(entry));
        }

        CassandraRow<K, C> row = context.getCassandraClient().readColumns(names, indexKey, this.columnFamily,
                this.columnValueTypeProvider, context);

        if (row != null) {
            return convertIndexedColumns(row.getColumns());
        }

        return Collections.emptyList();
    }

    /**
     * Returns the column value type provider for the denormalized data.
     * 
     * @return the column value type provider
     */
    public ColumnValueTypeProvider<C> getDenormalizedDataTypeProvider() {
        return this.columnValueTypeProvider;
    }

    @SuppressWarnings("unchecked")
    private static <C extends Serializable & Comparable<C>, D> List<Column<C, D>> convertIndexedColumns(
            Collection<Column<C, ?>> columns) {
        /*
         * All types for column values in a custom secondary index are of type same type Column<C,
         * D>, but since they are read using CassandraClient, the type of the collection for the
         * values is ? (Since Cassandra can store columns with different value).
         */
        List<Column<C, D>> indexedColumns = new LinkedList<Column<C, D>>();
        for (Column<C, ?> column : columns) {
            indexedColumns.add((Column<C, D>) column);
        }
        return indexedColumns;
    }
}
