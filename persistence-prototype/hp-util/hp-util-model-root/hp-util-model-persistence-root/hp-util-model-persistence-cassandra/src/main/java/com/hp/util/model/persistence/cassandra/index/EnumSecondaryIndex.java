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
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Custom secondary index column family that uses enumerations as indexed values.
 * <p>
 * <b>Enum Secondary Index Column Family:</b>
 * <ul>
 * <li>Row key (Key validation: String): One row for each enum constant.</li>
 * <li>Column value (default validation: Bytes): Denormalized data.</li>
 * <li>Column name (comparator: row key in the main column family or composite value when sorting
 * information is included): The row key in the main column family. For each row there will be a an
 * entry in the columns to keep the row key.</li>
 * 
 * <pre>
 * column_family_name {
 *     "enum_constant_i": {
 *         id_1: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         id_n: &lt;data provided by the denormalizer&gt;,
 *     }
 *     "enum_constant_j": {
 *         id_1: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         id_n: &lt;data provided by the denormalizer&gt;,
 *     }
 * }
 * </pre>
 * 
 * See {@link IndexEntryHandler} for a recipe to update secondary indexes.
 * 
 * @param <E> type of the enum constant (indexed column value)
 * @param <C> type of the row key in the main column family. Such row key will be the column name in
 *            the secondary index column.
 * @param <D> type of the denormalized data to set as the value in the indexed columns
 * @author Fabiel Zuniga
 */
public class EnumSecondaryIndex<E extends Enum<E>, C extends Serializable & Comparable<C>, D> implements
        CustomSecondaryIndex<E, C, D> {

    private final CustomSecondaryIndex<String, C, D> delegate;
    private Class<E> enumClass;

    /**
     * Creates a custom secondary index.
     * 
     * @param enumClass enumeration class
     * @param columnFamilyName name for the column family this index will keep data into
     * @param indexedColumnNameDataType type of the secondary index column name
     * @param denormalizedDataType denormalized data type; column value in the custom secondary
     */
    public EnumSecondaryIndex(Class<E> enumClass, String columnFamilyName, DataType<C> indexedColumnNameDataType,
            DataType<D> denormalizedDataType) {
        if (enumClass == null) {
            throw new NullPointerException("enumClass cannot be null");
        }

        if (columnFamilyName == null) {
            throw new NullPointerException("columnFamilyName cannot be null");
        }

        if (columnFamilyName.isEmpty()) {
            throw new IllegalArgumentException("columnFamilyName cannot be empty");
        }

        if (indexedColumnNameDataType == null) {
            throw new NullPointerException("indexedColumnNameDataType cannot be null");
        }

        this.enumClass = enumClass;
        ColumnFamily<String, C> columnFamilyDefinition = new ColumnFamily<String, C>(
                columnFamilyName, BasicType.STRING_UTF8, indexedColumnNameDataType,
                "Custom secondary index column family that uses enumerations as indexed values");

        this.delegate = new GenericCustomSecondaryIndex<String, C, D>(columnFamilyDefinition, denormalizedDataType);
    }

    @Override
    public Collection<ColumnFamily<?, ?>> getColumnFamilies() {
        return this.delegate.getColumnFamilies();
    }

    @Override
    public <N> void insert(C indexEntry, D denormalizedData, E indexKey, CassandraContext<N> context)
            throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }
        this.delegate.insert(indexEntry, denormalizedData, indexKey.name(), context);
    }

    @Override
    public <N> void delete(C indexEntry, E indexKey, CassandraContext<N> context) throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }
        this.delegate.delete(indexEntry, indexKey.name(), context);
    }

    @Override
    public <N> void delete(E indexKey, CassandraContext<N> context) throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }

        this.delegate.delete(indexKey.name(), context);
    }

    @Override
    public <N> void clear(CassandraContext<N> context) throws PersistenceException {
        for (E constant : this.enumClass.getEnumConstants()) {
            delete(constant, context);
        }
    }

    @Override
    public <N> long count(E indexKey, CassandraContext<N> context) throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }
        return this.delegate.count(indexKey.name(), context);
    }

    @Override
    public <N> List<Column<C, D>> read(E indexKey, CassandraContext<N> context) throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }
        return this.delegate.read(indexKey.name(), context);
    }

    @Override
    public <N> MarkPage<Column<C, D>> read(E indexKey, MarkPageRequest<C> pageRequest, CassandraContext<N> context)
            throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }
        return this.delegate.read(indexKey.name(), pageRequest, context);
    }

    @Override
    public <N> List<Column<C, D>> read(List<C> indexEntries, E indexKey, CassandraContext<N> context)
            throws PersistenceException {
        if (indexKey == null) {
            throw new NullPointerException("indexKey cannot be null");
        }
        return this.delegate.read(indexEntries, indexKey.name(), context);
    }
}
