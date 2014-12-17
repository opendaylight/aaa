/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.util.common.Memory;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.memory.LocalMemory;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.Batch;
import com.hp.util.model.persistence.cassandra.CassandraClient;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.ByteArrayColumn;
import com.hp.util.model.persistence.cassandra.column.ByteColumn;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider.ColumnValueTypeHandler;
import com.hp.util.model.persistence.cassandra.column.ColumnVisitor;
import com.hp.util.model.persistence.cassandra.column.CustomColumn;
import com.hp.util.model.persistence.cassandra.column.CustomColumn.CustomColumnVisitor;
import com.hp.util.model.persistence.cassandra.column.DateColumn;
import com.hp.util.model.persistence.cassandra.column.DoubleColumn;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.FloatColumn;
import com.hp.util.model.persistence.cassandra.column.IntegerColumn;
import com.hp.util.model.persistence.cassandra.column.LongColumn;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.column.VoidColumn;
import com.hp.util.model.persistence.cassandra.cql.CqlPredicate;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily.SecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;
import com.hp.util.model.persistence.cassandra.keyspace.KeyspaceConfiguration;
import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.Serializer;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.exceptions.NotFoundException;
import com.netflix.astyanax.ddl.ColumnDefinition;
import com.netflix.astyanax.ddl.KeyspaceDefinition;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.query.ColumnCountQuery;
import com.netflix.astyanax.query.ColumnFamilyQuery;
import com.netflix.astyanax.query.ColumnQuery;
import com.netflix.astyanax.query.CqlQuery;
import com.netflix.astyanax.query.IndexColumnExpression;
import com.netflix.astyanax.query.IndexOperationExpression;
import com.netflix.astyanax.query.IndexQuery;
import com.netflix.astyanax.query.IndexValueExpression;
import com.netflix.astyanax.query.RowQuery;

/**
 * Cassandra client Facade.
 * <p>
 * Provides a simplified interface to Astyanax library.
 * 
 * @author Fabiel Zuniga
 */
class AstyanaxCassandraClient implements CassandraClient<Astyanax> {

    // LinkedList should be used if the number of elements might be big.

    private final DataTypeClassProvider dataTypeClassProvider;
    private final DataTypeSerializerProvider dataTypeSerializerProvider;
    private final DataTypeColumnDecoderProvider dataTypeColumnDecoderProvider;
    private final DataTypeIndexQueryProvider dataTypeIndexQueryProvider;

    public AstyanaxCassandraClient() {
        this.dataTypeClassProvider = new DataTypeClassProvider();
        this.dataTypeSerializerProvider = new DataTypeSerializerProvider();
        this.dataTypeColumnDecoderProvider = new DataTypeColumnDecoderProvider(this.dataTypeSerializerProvider);
        this.dataTypeIndexQueryProvider = new DataTypeIndexQueryProvider(this.dataTypeSerializerProvider);
    }

    @Override
    public boolean exists(Keyspace keyspace, CassandraContext<Astyanax> context) throws PersistenceException {
        if (keyspace == null) {
            throw new NullPointerException("keyspace cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        try {
            return context.getNativeClient().getCluster().describeKeyspace(keyspace.getName()) != null;
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void createKeyspace(Keyspace keyspace, KeyspaceConfiguration configuration,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (keyspace == null) {
            throw new NullPointerException("keyspace cannot be null");
        }

        if (configuration == null) {
            throw new NullPointerException("configuration cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        if (!exists(keyspace, context)) {
            KeyspaceDefinition keyspaceDefinition = context.getNativeClient().getCluster().makeKeyspaceDefinition();
            keyspaceDefinition.setName(keyspace.getName());
            keyspaceDefinition.setStrategyClass(configuration.getStrategy().getStrategyClass());

            Map<String, String> strategyOptions = new HashMap<String, String>();
            strategyOptions.put("replication_factor", String.valueOf(configuration.getReplicationFactor()));

            keyspaceDefinition.setStrategyOptions(strategyOptions);
            try {
                context.getNativeClient().getCluster().addKeyspace(keyspaceDefinition);
            }
            catch (ConnectionException e) {
                throw new PersistenceException(e);
            }
        }
    }

    @Override
    public void dropKeyspace(Keyspace keyspace, CassandraContext<Astyanax> context) throws PersistenceException {
        if (exists(keyspace, context)) {
            try {
                context.getNativeClient().getCluster().dropKeyspace(keyspace.getName());
            }
            catch (ConnectionException e) {
                throw new PersistenceException(e);
            }
        }
    }

    @Override
    public boolean exists(ColumnFamily<?, ?> columnFamily, Keyspace keyspace, CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (keyspace == null) {
            throw new NullPointerException("keyspace cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        KeyspaceDefinition keyspaceDefintion;
        try {
            keyspaceDefintion = context.getNativeClient().getCluster().describeKeyspace(keyspace.getName());
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        if (keyspaceDefintion == null) {
            return false;
        }

        return keyspaceDefintion.getColumnFamily(columnFamily.getName()) != null;
    }

    @Override
    public void createColumnFamily(ColumnFamily<?, ?> columnFamily, Keyspace keyspace,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (columnFamily == null) {
            throw new NullPointerException("columnFamilyDefinition cannot be null");
        }

        if (!exists(columnFamily, keyspace, context)) {
            com.netflix.astyanax.ddl.ColumnFamilyDefinition astyanaxDefinition = context.getNativeClient().getCluster()
                    .makeColumnFamilyDefinition();
            astyanaxDefinition.setName(columnFamily.getName());
            astyanaxDefinition.setKeyspace(keyspace.getName());

            if (columnFamily.getKeyValidator() != null) {
                String keyValidatorClass = this.dataTypeClassProvider.getDataTypeClass(columnFamily.getKeyValidator());
                astyanaxDefinition.setKeyValidationClass(keyValidatorClass);
            }

            if (columnFamily.getDefaultValidator() != null) {
                String defaultValidatorClass = this.dataTypeClassProvider.getDataTypeClass(columnFamily
                        .getDefaultValidator());
                astyanaxDefinition.setDefaultValidationClass(defaultValidatorClass);
            }

            if (columnFamily.getComparator() != null) {
                String comparatorClass = this.dataTypeClassProvider.getDataTypeClass(columnFamily.getComparator());
                astyanaxDefinition.setComparatorType(comparatorClass);
            }

            astyanaxDefinition.setComment(columnFamily.getComment());

            for (SecondaryIndex secondaryIndex : columnFamily.getSecondaryIndexes()) {
                ColumnDefinition columnDefinition = context.getNativeClient().getCluster().makeColumnDefinition();
                /*
                 * At the moment of writing Cassandra only supported KEYS index types which is
                 * essentially a hash lookup.
                 */
                columnDefinition.setName(secondaryIndex.getColumnName().getValue());
                columnDefinition.setKeysIndex(secondaryIndex.getIndexName());
                String secondaryIndexValidationClass = this.dataTypeClassProvider.getDataTypeClass(secondaryIndex
                        .getValidator());
                columnDefinition.setValidationClass(secondaryIndexValidationClass);

                astyanaxDefinition.addColumnDefinition(columnDefinition);
            }

            try {
                context.getNativeClient().getCluster().addColumnFamily(astyanaxDefinition);
            }
            catch (ConnectionException e) {
                throw new PersistenceException(e);
            }
        }
    }

    @Override
    public void dropColumnFamily(ColumnFamily<?, ?> columnFamily, Keyspace keyspace, CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (exists(columnFamily, keyspace, context)) {
            try {
                context.getNativeClient().getCluster().dropColumnFamily(keyspace.getName(), columnFamily.getName());
            }
            catch (ConnectionException e) {
                throw new PersistenceException(e);
            }
        }
    }

    @Override
    public void truncateColumnFamily(ColumnFamily<?, ?> columnFamily, CassandraContext<Astyanax> context)
            throws PersistenceException {

        // Note: This operation is expensive.

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        try {
            context.getNativeClient().getKeyspace().truncateColumnFamily(toAstyanax(columnFamily));
        }
        catch (Exception e) {
            throw new PersistenceException();
        }
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void insert(Column<C, ?> column, K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (column == null) {
            throw new NullPointerException("column cannot be null");
        }

        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        /*
         * The following commented implementation worked fine. It was changed to be able to record this write
         * operation using com.hp.util.model.persistence.impl.cassandra.Batch (Since a MutationBatch is needed
         * to record the operation).

        final ColumnMutation mutation = context.getKeyspace().prepareColumnMutation(columnFamily, rowKey,
            column.getName().getValue());
        mutation.setConsistencyLevel(context.getWriteConsistencyLevel().toAstyanaxLevel());

        Execution<Void> execution = null;

        if (column.getValue() == null) {
            execution = mutation.putEmptyColumn(null);
        }
        else {
            ColumnCommand<C, Execution<Void>> columnCommand = new ColumnCommand<C, Execution<Void>>() {

                @Override
                protected Execution<Void> getResult(ValuelessColumn<C> c) {
                    // This line is actually not executed because the value is checked for null before
                    return mutation.putEmptyColumn(null);
                }

                @Override
                protected Execution<Void> getResult(BooleanColumn<C> c) {
                    return mutation.putValue(c.getValue().booleanValue(), null);
                }

                @Override
                protected Execution<Void> getResult(ByteColumn<C> c) {
                    return mutation.putValue(c.getValue().byteValue(), null);
                }

                @Override
                protected Execution<Void> getResult(ByteArrayColumn<C> c) {
                    return mutation.putValue(c.getValue(), null);
                }

                @Override
                protected Execution<Void> getResult(DateColumn<C> c) {
                    return mutation.putValue(c.getValue(), null);
                }

                @Override
                protected Execution<Void> getResult(DoubleColumn<C> c) {
                    return mutation.putValue(c.getValue().doubleValue(), null);
                }

                @Override
                protected Execution<Void> getResult(FloatColumn<C> c) {
                    // TODO: Astyanax issue if using c.getValue().floatValue()
                    return mutation.putValue(c.getValue().doubleValue(), null);
                }

                @Override
                protected Execution<Void> getResult(IntegerColumn<C> c) {
                    return mutation.putValue(c.getValue().intValue(), null);
                }

                @Override
                protected Execution<Void> getResult(LongColumn<C> c) {
                    return mutation.putValue(c.getValue().longValue(), null);
                }

                @Override
                protected Execution<Void> getResult(StringColumn<C> c) {
                    return mutation.putValue(c.getValue(), null);
                }

                @Override
                protected Execution<Void> getResult(EnumColumn<C, ? extends Enum<?>> c) {
                    return mutation.putValue(EnumColumnDecoder.encode(c.getValue()), null);
                }

                @Override
                protected Execution<Void> getResult(CustomColumn<C, ?> c) {
                    return c.addToMutation(mutation);
                }
            };

            execution = columnCommand.execute(column);
        }

        try {
            execution.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
        */

        MutationBatch mutation = context.getNativeClient().getKeyspace().prepareMutationBatch();
        mutation.setConsistencyLevel(context.getWriteConsistencyLevel().toAstyanaxLevel());
        final ColumnListMutation<C> columnListMutation = mutation.withRow(toAstyanax(columnFamily), rowKey);
        ColumnVisitor<C, Void> columnUpdater = new ColumnInsertionVisitor<C>(columnListMutation,
                this.dataTypeSerializerProvider);
        column.accept(columnUpdater, null);

        try {
            mutation.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void insert(CassandraRow<K, C> row,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (row == null) {
            throw new NullPointerException("row cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        List<CassandraRow<K, C>> rows = new ArrayList<CassandraRow<K, C>>(1);
        rows.add(row);

        insert(rows, columnFamily, context);
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void insert(
            Collection<CassandraRow<K, C>> rows, ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (rows == null) {
            throw new NullPointerException("rows cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        MutationBatch mutation = context.getNativeClient().getKeyspace().prepareMutationBatch();
        mutation.setConsistencyLevel(context.getWriteConsistencyLevel().toAstyanaxLevel());

        for (CassandraRow<K, C> row : rows) {
            final ColumnListMutation<C> columnListMutation = mutation.withRow(toAstyanax(columnFamily), row.getKey());
            ColumnVisitor<C, Void> columnUpdater = new ColumnInsertionVisitor<C>(columnListMutation,
                    this.dataTypeSerializerProvider);

            for (Column<C, ?> column : row.getColumns()) {
                column.accept(columnUpdater, null);
            }

            for (ColumnName<C, ?> columnName : row.getDeletedColumns()) {
                columnListMutation.deleteColumn(columnName.getValue());
            }
        }

        try {
            mutation.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(ColumnName<C, ?> columnName,
            K rowKey, ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        Collection<ColumnName<C, ?>> columnsNames = new ArrayList<ColumnName<C, ?>>(1);
        columnsNames.add(columnName);
        delete(columnsNames, rowKey, columnFamily, context);
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(
            Collection<ColumnName<C, ?>> columnsNames, K rowKey, ColumnFamily<K, C> columnFamily,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (columnsNames == null) {
            throw new NullPointerException("columnsNames cannot be null");
        }

        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        MutationBatch mutation = context.getNativeClient().getKeyspace().prepareMutationBatch();
        mutation.setConsistencyLevel(context.getWriteConsistencyLevel().toAstyanaxLevel());
        ColumnListMutation<C> columnListMutation = mutation.withRow(toAstyanax(columnFamily), rowKey);
        for (ColumnName<C, ?> columnName : columnsNames) {
            columnListMutation.deleteColumn(columnName.getValue());
        }

        try {
            mutation.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        List<K> rowKeys = new ArrayList<K>(1);
        rowKeys.add(rowKey);
        delete(rowKeys, columnFamily, context);
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> void delete(Collection<K> keys,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (keys == null) {
            throw new NullPointerException("keys cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        MutationBatch mutation = context.getNativeClient().getKeyspace().prepareMutationBatch();
        mutation.setConsistencyLevel(context.getWriteConsistencyLevel().toAstyanaxLevel());
        for (K id : keys) {
            mutation.withRow(toAstyanax(columnFamily), id).delete();
        }
        try {
            mutation.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> boolean exist(K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        OperationResult<ColumnList<C>> result;
        try {
            result = columnFamilyQuery.getKey(rowKey).execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        return !result.getResult().isEmpty();
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>, D> Column<C, D> read(
            ColumnName<C, D> columnName, K rowKey, ColumnFamily<K, C> columnFamily, DataType<D> columnValueType,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueType == null) {
            throw new NullPointerException("columnValueType cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        ColumnQuery<C> columnQuery = columnFamilyQuery.getKey(rowKey).getColumn(columnName.getValue());
        OperationResult<com.netflix.astyanax.model.Column<C>> result = null;
        try {
            result = columnQuery.execute();
        }
        catch (ConnectionException e) {
            // Astyanax throws an exception if the row key or column is not found
            if (e.getCause() == null || e.getCause() instanceof NotFoundException) {
                throw new PersistenceException(e);
            }
        }

        Column<C, D> column = null;
        if (result != null && result.getResult() != null) {
            ColumnDecoder<C, D> columnDecoder = this.dataTypeColumnDecoderProvider.getColumnDecoder(columnValueType);
            column = columnDecoder.decode(result.getResult());
        }

        return column;
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> read(K rowKey,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        List<K> rowKeys = new ArrayList<K>(1);
        rowKeys.add(rowKey);

        Collection<CassandraRow<K, C>> rows = read(rowKeys, columnFamily, columnValueTypeProvider, context);
        if (rows.isEmpty()) {
            return null;
        }

        return rows.iterator().next();
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> List<CassandraRow<K, C>> read(
            List<K> rowKeys, ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (rowKeys == null) {
            throw new NullPointerException("rowKeys cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        OperationResult<Rows<K, C>> result;
        try {
            result = columnFamilyQuery.getKeySlice(rowKeys).execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        List<CassandraRow<K, C>> rows = toCassandraRows(result.getResult(), columnValueTypeProvider);

        if (rows.size() > 1) {
            // ColumnFamilyQuery.getKeySlice(Collection<K>) expects a collection and not a list thus
            // read
            // rows are not necessarily returned in the order they are requested.
            final Map<K, Integer> indexes = new HashMap<K, Integer>(rowKeys.size());
            for (int i = rowKeys.size() - 1; i >= 0; i--) {
                indexes.put(rowKeys.get(i), Integer.valueOf(i));
            }
            Collections.sort(rows, new Comparator<CassandraRow<K, C>>() {
                @Override
                public int compare(CassandraRow<K, C> o1, CassandraRow<K, C> o2) {
                    return indexes.get(o1.getKey()).compareTo(indexes.get(o2.getKey()));
                }
            });
        }

        return rows;
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> readColumns(
            Collection<ColumnName<C, ?>> columnNames, K rowKey, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (columnNames == null) {
            throw new NullPointerException("columnNames cannot be null");
        }

        if (columnNames.size() <= 0) {
            throw new IllegalArgumentException("columnNames cannot be empty");
        }

        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        Collection<C> rawColumnNames = new ArrayList<C>(columnNames.size());
        for (ColumnName<C, ?> columnName : columnNames) {
            rawColumnNames.add(columnName.getValue());
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        OperationResult<ColumnList<C>> result;
        try {
            result = columnFamilyQuery.getKey(rowKey).withColumnSlice(rawColumnNames).execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        CassandraRow<K, C> row = null;
        // If we query a row key that does not exists, Astyanax returns a row with no columns.
        if (result != null && result.getResult().size() > 0) {
            row = new CassandraRow<K, C>(rowKey);
            setColumns(row, result.getResult(), columnValueTypeProvider);
        }

        return row;
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> Collection<CassandraRow<K, C>> read(
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        OperationResult<Rows<K, C>> result;
        try {
            result = columnFamilyQuery.getAllRows().execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        return toCassandraRows(result.getResult(), columnValueTypeProvider);
    }

    @Override
    @Deprecated
    public <K extends Serializable, C extends Serializable & Comparable<C>> List<CassandraRow<K, C>> read(
            CqlPredicate predicate, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        StringBuilder strCqlQuery = new StringBuilder(64);
        strCqlQuery.append("Select * from ");
        strCqlQuery.append(columnFamily.getName());
        strCqlQuery.append(" Using Consistency ");
        strCqlQuery.append(context.getReadConsistencyLevel().name());
        if (predicate != null) {
            strCqlQuery.append(" Where ");
            strCqlQuery.append(predicate.getPredicate());
        }
        strCqlQuery.append(';');

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        CqlQuery<K, C> cqlQuery = columnFamilyQuery.withCql(strCqlQuery.toString());
        OperationResult<CqlResult<K, C>> result;
        try {
            result = cqlQuery.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        return toCassandraRows(result.getResult().getRows(), columnValueTypeProvider);
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> long countColumns(K rowKey,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        ColumnCountQuery columnQuery = columnFamilyQuery.getKey(rowKey).getCount();
        OperationResult<Integer> result;
        try {
            result = columnQuery.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        if (result.getResult() != null) {
            return result.getResult().longValue();
        }

        return 0;
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> readColumnRange(
            K rowKey, ColumnName<C, ?> startColumn, ColumnName<C, ?> endColumn, boolean reverse, int maxSize,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        if (startColumn != null && endColumn != null) {
            if (!reverse && startColumn.getValue().compareTo(endColumn.getValue()) >= 0) {
                throw new IllegalArgumentException(
                        "startColumn must come before endColumn for range reads: startColumn=" + startColumn
                                + ", endColumn=" + endColumn);
            }

            if (reverse && startColumn.getValue().compareTo(endColumn.getValue()) <= 0) {
                throw new IllegalArgumentException(
                        "startColumn must come after endColumn for reversed range reads: startColumn=" + startColumn
                                + ", endColumn=" + endColumn);
            }
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        RowQuery<K, C> rowQuery = columnFamilyQuery.getKey(rowKey);
        OperationResult<com.netflix.astyanax.model.ColumnList<C>> result;
        try {
            result = rowQuery.withColumnRange(startColumn != null ? startColumn.getValue() : null,
                    endColumn != null ? endColumn.getValue() : null, reverse, maxSize).execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        CassandraRow<K, C> row = new CassandraRow<K, C>(rowKey);
        setColumns(row, result.getResult(), columnValueTypeProvider);
        return row;
    }

    /*
    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> CassandraRow<K, C> readColumnRange(
            K rowKey, ByteBufferRange range, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (range == null) {
            throw new NullPointerException("range cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        RowQuery<K, C> rowQuery = columnFamilyQuery.getKey(rowKey);
        OperationResult<com.netflix.astyanax.model.ColumnList<C>> result;
        try {
            result = rowQuery.withColumnRange(range).execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        CassandraRow<K, C> row = new CassandraRow<K, C>(rowKey);
        setColumns(row, result.getResult(), columnValueTypeProvider);
        return row;
    }
    */

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> long countColumnRange(K rowKey,
            ColumnName<C, ?> startColumn, ColumnName<C, ?> endColumn, boolean reverse, int maxSize,
            ColumnFamily<K, C> columnFamily, CassandraContext<Astyanax> context) throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        RowQuery<K, C> rowQuery = columnFamilyQuery.getKey(rowKey);
        OperationResult<Integer> result;
        try {
            result = rowQuery
                    .withColumnRange(startColumn != null ? startColumn.getValue() : null,
                            endColumn != null ? endColumn.getValue() : null, reverse, maxSize).getCount().execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        if (result.getResult() != null) {
            return result.getResult().longValue();
        }

        return 0;
    }

    /*
    @Override
    @Deprecated
    public <K extends Serializable, C extends Serializable & Comparable<C>> long countColumnRange(K rowKey,
            ByteBufferRange range, ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider,
            CassandraContext<Astyanax> context) throws PersistenceException {

        // TODO: Count with range not working

        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (range == null) {
            throw new NullPointerException("range cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        RowQuery<K, C> rowQuery = columnFamilyQuery.getKey(rowKey);
        OperationResult<Integer> result;
        try {
            result = rowQuery.withColumnRange(range).getCount().execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        if (result.getResult() != null) {
            return result.getResult().longValue();
        }

        return 0;
    }
    */

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>> MarkPage<Column<C, ?>> read(final K rowKey,
            MarkPageRequest<ColumnName<C, ?>> pageRequest, ColumnName<C, ?> end, final ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, final CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        int size = pageRequest.getSize();

        ColumnName<C, ?> start = null;
        if (pageRequest.getMark() != null) {
            start = pageRequest.getMark();
            size++;
        }

        if (start != null && start.equals(end)) {
            List<Column<C, ?>> pageData = Collections.emptyList();
            final Memory<Column<C, ?>> markColumnMemory = new LocalMemory<Column<C, ?>>();
            columnValueTypeProvider.getColumnValueType(start, new ColumnValueTypeHandler<C, Void>() {
                @Override
                public <D> void handle(ColumnName<C, D> columnName, DataType<D> dataType, Void input)
                        throws PersistenceException {
                    Column<C, D> markColumn = read(columnName, rowKey, columnFamily, dataType, context);
                    markColumnMemory.write(markColumn);
                }
            }, null);
            MarkPageRequest<Column<C, ?>> convertedPageRequest = pageRequest.<Column<C, ?>> convert(markColumnMemory
                    .read());
            return new MarkPage<Column<C, ?>>(convertedPageRequest, pageData);
        }

        CassandraRow<K, C> row = null;
        if (pageRequest.getNavigation() == Navigation.NEXT) {
            row = readColumnRange(rowKey, start, end, false, size, columnFamily, columnValueTypeProvider, context);
        }
        else {
            row = readColumnRange(rowKey, start, end, true, size, columnFamily, columnValueTypeProvider, context);
        }

        boolean extraElement = false;
        Column<C, ?> markColumn = null;
        if (start != null) {
            markColumn = row.getColumn(start);
            row.delete(start);

            /*
             * If the mark does not exist the page will have one extra element because size was
             * incremented because the mark is part of the range.
             */
            if (markColumn == null) {
                extraElement = true;
            }
        }

        List<Column<C, ?>> columns = new LinkedList<Column<C, ?>>(row.getColumns());
        if (pageRequest.getNavigation() == Navigation.PREVIOUS) {
            // Columns are returned inverted by the range query
            Collections.reverse(columns);
        }

        if (extraElement && columns.size() > 0) {
            columns.remove(columns.size() - 1);
        }

        MarkPageRequest<Column<C, ?>> convertedPageRequest = pageRequest.<Column<C, ?>> convert(markColumn);

        return new MarkPage<Column<C, ?>>(convertedPageRequest, columns);
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>, D> List<CassandraRow<K, C>> searchWithIndex(
            ColumnName<C, D> columnName, EqualityCondition<D> condition, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, DataType<D> indexType,
            CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (indexType == null) {
            throw new NullPointerException("indexType cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        if (condition.getValue() == null) {
            throw new IllegalArgumentException("condition's value cannot be null");
        }

        if (condition.getMode() == EqualityCondition.Mode.UNEQUAL) {
            throw new IllegalArgumentException("Unequal mode not supported");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                .prepareQuery(toAstyanax(columnFamily));
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        IndexQuery<K, C> indexQuery = columnFamilyQuery.searchWithIndex();
        IndexColumnExpression<K, C> indexColumnExpression = indexQuery.addExpression();
        IndexOperationExpression<K, C> indexOperationExpression = indexColumnExpression.whereColumn(columnName
                .getValue());
        IndexValueExpression<K, C> indexValueExpression = indexOperationExpression.equals();
        IndexValueExpressionStrategy<K, C, D> expressionStrategy = this.dataTypeIndexQueryProvider
                .getStrategy(indexType);
        indexQuery = expressionStrategy.getIndexQuery(indexValueExpression, condition.getValue());
        OperationResult<Rows<K, C>> result;
        try {
            result = indexQuery.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        return toCassandraRows(result.getResult(), columnValueTypeProvider);
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>, D> List<CassandraRow<K, C>> searchWithIndex(
            ColumnName<C, D> columnName, SetCondition<D> condition, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, DataType<D> indexType,
            CassandraContext<Astyanax> context)
            throws PersistenceException {
        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("columnValueTypeProvider cannot be null");
        }

        if (indexType == null) {
            throw new NullPointerException("indexType cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        if (condition.getValues() == null || condition.getValues().isEmpty()) {
            throw new IllegalArgumentException("condition's values cannot be null nor empty");
        }

        if (condition.getMode() == SetCondition.Mode.NOT_IN) {
            throw new IllegalArgumentException("Not-in mode not supported");
        }

        List<CassandraRow<K, C>> rows = new LinkedList<CassandraRow<K, C>>();
        for (D value : condition.getValues()) {
            ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace()
                    .prepareQuery(toAstyanax(columnFamily));
            columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
            IndexQuery<K, C> indexQuery = columnFamilyQuery.searchWithIndex();
            IndexColumnExpression<K, C> indexColumnExpression = indexQuery.addExpression();
            IndexOperationExpression<K, C> indexOperationExpression = indexColumnExpression.whereColumn(columnName
                    .getValue());
            IndexValueExpression<K, C> indexValueExpression = indexOperationExpression.equals();
            IndexValueExpressionStrategy<K, C, D> expressionStrategy = this.dataTypeIndexQueryProvider
                    .getStrategy(indexType);
            indexQuery = expressionStrategy.getIndexQuery(indexValueExpression, value);
            OperationResult<Rows<K, C>> result;
            try {
                result = indexQuery.execute();
            }
            catch (ConnectionException e) {
                throw new PersistenceException(e);
            }
            rows.addAll(toCassandraRows(result.getResult(), columnValueTypeProvider));
        }

        return rows;
    }

    @Override
    public <K extends Serializable, C extends Serializable & Comparable<C>, D extends Enum<D>> List<CassandraRow<K, C>> searchWithIndex(
            ColumnName<C, D> columnName, SetCondition<D> condition, Class<D> enumClass,
            ColumnFamily<K, C> columnFamily, ColumnValueTypeProvider<C> columnValueTypeProvider, DataType<D> indexType,
            CassandraContext<Astyanax> context) throws PersistenceException {
        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (enumClass == null) {
            throw new NullPointerException("enumClass cannot be null");
        }

        if (condition.getValues() == null || condition.getValues().isEmpty()) {
            throw new IllegalArgumentException("condition's values cannot be null nor empty");
        }

        SetCondition<D> translatedCondition = condition;

        if (condition.getMode() == SetCondition.Mode.NOT_IN) {
            Set<D> values = new HashSet<D>(Arrays.asList(enumClass.getEnumConstants()));
            values.removeAll(condition.getValues());
            translatedCondition = SetCondition.in(values);
        }

        return searchWithIndex(columnName, translatedCondition, columnFamily, columnValueTypeProvider, indexType,
                context);
    }

    @Override
    public Batch<Astyanax> prepareBatch(CassandraContext<Astyanax> context) {
        return new BatchImpl(context);
    }

    /* *
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
    public static <K extends Serializable, C extends Serializable & Comparable<C>, D extends Comparable<D>> List<CassandraRow<K, C>> searchWithIndex(
        ColumnName<C, D> columnName, ComparabilityCondition<D> condition, ColumnFamily<K, C> columnFamily,
        Provider<ColumnDecoder<C, ?>, ColumnName<C, ?>> decoderProvider,
        IndexValueExpressionStrategy<K, C, D> expressionStrategy, CassandraContext<Astyanax> context) {

        // TODO: This method is not working
        // Cassandra secondary index search requires at least one equality operation to run.
        // InvalidRequestException(why:No indexed columns present in index clause with operator EQ)

        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily cannot be null");
        }

        if (decoderProvider == null) {
            throw new NullPointerException("decoderProvider cannot be null");
        }

        if (expressionStrategy == null) {
            throw new NullPointerException("expressionStrategy cannot be null");
        }

        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        if (condition.getValue() == null) {
            throw new IllegalArgumentException("condition's value cannot be null");
        }

        ColumnFamilyQuery<K, C> columnFamilyQuery = context.getNativeClient().getKeyspace().prepareQuery(columnFamily);
        columnFamilyQuery.setConsistencyLevel(context.getReadConsistencyLevel().toAstyanaxLevel());
        IndexQuery<K, C> indexQuery = columnFamilyQuery.searchWithIndex();
        IndexColumnExpression<K, C> indexColumnExpression = indexQuery.addExpression();
        IndexOperationExpression<K, C> indexOperationExpression = indexColumnExpression.whereColumn(columnName.getValue());
        IndexValueExpression<K, C> indexValueExpression = null;
        switch (condition.getMode()) {
            case EQUAL:
                indexValueExpression = indexOperationExpression.equals();
                break;
            case GREATER_THAN:
                indexValueExpression = indexOperationExpression.greaterThan();
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                indexValueExpression = indexOperationExpression.greaterThanEquals();
                break;
            case LESS_THAN:
                indexValueExpression = indexOperationExpression.lessThan();
                break;
            case LESS_THAN_OR_EQUAL_TO:
                indexValueExpression = indexOperationExpression.lessThanEquals();
                break;

        }
        indexQuery = expressionStrategy.getIndexQuery(indexValueExpression, condition.getValue());
        OperationResult<Rows<K, C>> result;
        try {
            result = indexQuery.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }

        return toCassandraRows(result.getResult(), decoderProvider);
    }
    */

    private <K extends Serializable, C extends Serializable & Comparable<C>> com.netflix.astyanax.model.ColumnFamily<K, C> toAstyanax(
            ColumnFamily<K, C> columnFamily) {
        Serializer<K> keyValidatorSerializer = this.dataTypeSerializerProvider.getSerializer(columnFamily
                .getKeyValidator());
        Serializer<C> comparatorSerializer = this.dataTypeSerializerProvider
                .getSerializer(columnFamily.getComparator());
        return new com.netflix.astyanax.model.ColumnFamily<K, C>(columnFamily.getName(), keyValidatorSerializer,
                comparatorSerializer);
    }

    private <K extends Serializable, C extends Serializable & Comparable<C>> List<CassandraRow<K, C>> toCassandraRows(
            Rows<K, C> rows, ColumnValueTypeProvider<C> columnValueTypeProvider) throws PersistenceException {
        // rows.size() throws an exception when all rows are read (IllegalStateException at
        // ThriftAllRowsImpl.size()).
        List<CassandraRow<K, C>> cassandraRows = new LinkedList<CassandraRow<K, C>>();

        for (Row<K, C> astyanaxRow : rows) {
            // If we query a row key that does not exists, Astyanax returns a row with no columns.
            if (!astyanaxRow.getColumns().isEmpty()) {
                CassandraRow<K, C> row = new CassandraRow<K, C>(astyanaxRow.getKey());
                setColumns(row, astyanaxRow.getColumns(), columnValueTypeProvider);
                cassandraRows.add(row);
            }
        }

        return cassandraRows;
    }

    private <K extends Serializable, C extends Serializable & Comparable<C>> void setColumns(
            final CassandraRow<K, C> row,
            ColumnList<C> columns, ColumnValueTypeProvider<C> columnValueTypeProvider) throws PersistenceException {

        ColumnValueTypeHandler<C, com.netflix.astyanax.model.Column<C>> handler = new ColumnValueTypeHandler<C, com.netflix.astyanax.model.Column<C>>() {
            @Override
            public <D> void handle(ColumnName<C, D> columnName, DataType<D> dataType,
                    com.netflix.astyanax.model.Column<C> input) throws PersistenceException {
                ColumnDecoder<C, D> decoder = AstyanaxCassandraClient.this.dataTypeColumnDecoderProvider
                        .getColumnDecoder(dataType);
                if (decoder == null) {
                    // TODO: Verify whether an exception is really the best way. Newer versions of
                    // code
                    // could not consider old columns.
                    // Maybe a log would be sufficient.
                    throw new PersistenceException("ColumnDecoder null for column " + columnName);
                }
                row.setColumn(decoder.decode(input));
            }
        };

        for (com.netflix.astyanax.model.Column<C> astyanaxColumn : columns) {
            columnValueTypeProvider.getColumnValueType(ColumnName.valueOf(astyanaxColumn.getName()), handler,
                    astyanaxColumn);
        }
    }

    private static class ColumnInsertionVisitor<C extends Serializable & Comparable<C>> implements
            ColumnVisitor<C, Void> {

        private final ColumnListMutation<C> mutation;
        private final DataTypeSerializerProvider dataTypeSerializerProvider;

        protected ColumnInsertionVisitor(ColumnListMutation<C> columnListMutation,
                DataTypeSerializerProvider dataTypeSerializerProvider) {
            this.mutation = columnListMutation;
            this.dataTypeSerializerProvider = dataTypeSerializerProvider;
        }

        private boolean handleEmptyColumn(Column<C, ?> column) {
            if (column.getValue() == null) {
                this.mutation.putEmptyColumn(column.getName().getValue());
                return true;
            }
            return false;
        }

        @Override
        public void visit(VoidColumn<C> column, Void input) {
            handleEmptyColumn(column);
        }

        @Override
        public void visit(BooleanColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue().booleanValue());
            }
        }

        @Override
        public void visit(ByteColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue().byteValue());
            }
        }

        @Override
        public void visit(ByteArrayColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue());
            }
        }

        @Override
        public void visit(DateColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue().toDate());
            }
        }

        @Override
        public void visit(DoubleColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue().doubleValue());
            }
        }

        @Override
        public void visit(FloatColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                // TODO: Astyanax issue if using column.getValue().floatValue()
                this.mutation.putColumn(column.getName().getValue(), column.getValue().doubleValue());
            }
        }

        @Override
        public void visit(IntegerColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue().intValue());
            }
        }

        @Override
        public void visit(LongColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue().longValue());
            }
        }

        @Override
        public void visit(StringColumn<C> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(), column.getValue());
            }
        }

        @Override
        public void visit(EnumColumn<C, ? extends Enum<?>> column, Void input) {
            if (!handleEmptyColumn(column)) {
                this.mutation.putColumn(column.getName().getValue(),
                        DataTypeSerializerProvider.EnumSerializer.encode(column.getValue()));
            }
        }

        @Override
        public void visit(CustomColumn<C, ?> column, Void input) {
            if (!handleEmptyColumn(column)) {
                CustomColumnVisitor<C> customColumnVisitor = new CustomColumnVisitor<C>() {
                    @Override
                    public <D> void visit(CustomColumn<C, D> customColumn) {
                        CompositeType<D> dataType = customColumn.getDataType();
                        Serializer<D> serializer = ColumnInsertionVisitor.this.dataTypeSerializerProvider
                                .getSerializer(dataType);
                        ColumnInsertionVisitor.this.mutation.putColumn(customColumn.getName().getValue(),
                                customColumn.getValue(), serializer, null);
                    }
                };
                column.accept(customColumnVisitor);
            }
        }
    }
}
