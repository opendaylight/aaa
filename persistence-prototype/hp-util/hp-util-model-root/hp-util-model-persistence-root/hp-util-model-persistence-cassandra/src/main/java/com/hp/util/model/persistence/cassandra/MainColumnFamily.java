/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.Converter;
import com.hp.util.common.Identifiable;
import com.hp.util.common.converter.InverseBidirectionalConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily.SecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Column family to store objects where the primary key is a mapped to a different type (MacAddress,
 * IpAddress, etc) that must be converted to a type that Cassandra understands (String, Long,
 * Integer, etc).
 * <p>
 * Column names are considered as strings.
 * <ul>
 * <li>Row key (Key validation: String): Identifiable object's id.</li>
 * <li>Column value (default validation: Bytes): attributes' values.</li>
 * <li>Column name (comparator: String): Name of the object's attributes.
 * <ul></li>
 * 
 * <pre>
 * cf_regular_dao {
 *     id_1: {
 *         attr_1: attr_1_value,
 *         ...
 *         attr_n: attr_n_value,
 *     }
 *     ...
 *     id_n: {
 *         attr_1: attr_1_value,
 *         ...
 *         attr_n: attr_n_value,
 *     }
 * }
 * </pre>
 * 
 * @param <I> type of the id (value type). This type should be immutable. It is critical this type
 *            implements equals() and hashCode() correctly.
 * @param <K> type of the row key the id is mapped to
 * @param <T> type of the identifiable object
 * @author Fabiel Zuniga
 */
public class MainColumnFamily<I extends Serializable, K extends Serializable, T extends Identifiable<? super T, I>>
        implements ColumnFamilyHandler {

    private final ColumnFamily<K, String> columnFamily;
    private final ColumnValueTypeProvider<String> columnValueTypeProvider;
    private final BidirectionalConverter<T, CassandraRow<I, String>> converter;
    private final BidirectionalConverter<I, K> keyConverter;
    private final BidirectionalConverter<K, I> inverseKeyConverter;

    /**
     * Creates a main column family handler.
     * 
     * @param columnFamilyName name for the column family
     * @param keyDataType row key data type
     * @param comment column family comment or description
     * @param columnValueTypeProvider column value type provider
     * @param keyConverter key converter
     * @param converter identifiable object - {@link CassandraRow} converter
     * @param secondaryIndexes built-in secondary indexes (Automatically handled by Cassandra). Note
     *            these are different from {@link CustomSecondaryIndex}.
     */
    public MainColumnFamily(String columnFamilyName, DataType<K> keyDataType, String comment,
            ColumnValueTypeProvider<String> columnValueTypeProvider,
            BidirectionalConverter<I, K> keyConverter, BidirectionalConverter<T, CassandraRow<I, String>> converter,
            SecondaryIndex... secondaryIndexes) {
        if (columnFamilyName == null) {
            throw new NullPointerException("columnFamilyName cannot be null");
        }

        if (columnFamilyName.isEmpty()) {
            throw new IllegalArgumentException("columnFamilyName cannot be empty");
        }

        if (keyDataType == null) {
            throw new NullPointerException("keyDataType cannot be null");
        }

        if (columnValueTypeProvider == null) {
            throw new NullPointerException("decoderProvider cannot be null");
        }

        if (keyConverter == null) {
            throw new NullPointerException("keyConverter cannot be null");
        }

        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }

        this.columnFamily = new ColumnFamily<K, String>(columnFamilyName, keyDataType, BasicType.STRING_UTF8, comment,
                secondaryIndexes);
        this.columnValueTypeProvider = columnValueTypeProvider;
        this.keyConverter = keyConverter;
        this.inverseKeyConverter = InverseBidirectionalConverter.inverse(keyConverter);
        this.converter = converter;
    }

    /**
     * Inserts an object.
     * 
     * @param identifiable object to insert
     * @param context data store context
     * @return inserted row
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> CassandraRow<I, String> insert(T identifiable, CassandraContext<N> context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }
        CassandraRow<I, String> row = this.converter.convert(identifiable);
        CassandraRow<K, String> mappedRow = map(row, this.keyConverter);
        context.getCassandraClient().insert(mappedRow, this.columnFamily, context);
        return row;
    }

    /**
     * Deletes an object.
     * 
     * @param id id of the object to delete
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void delete(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        K mappedKey = this.keyConverter.convert(id.getValue());
        context.getCassandraClient().delete(Arrays.asList(mappedKey), this.columnFamily, context);
    }

    /**
     * Reads a row.
     * 
     * @param id row key to read
     * @param context data store context
     * @return a {@link CassandraRow} if there is one with the given key, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> CassandraRow<I, String> read(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        K mappedKey = this.keyConverter.convert(id.getValue());
        CassandraRow<K, String> mappedRow = context.getCassandraClient().read(mappedKey, this.columnFamily,
                this.columnValueTypeProvider, context);
        CassandraRow<I, String> row = map(mappedRow, this.inverseKeyConverter);
        return row;
    }

    /**
     * Verifies if a row exists.
     * 
     * @param id row key to verify
     * @param context data store context
     * @return {@code true} if the row exists, {@code false} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> boolean exist(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        K key = this.keyConverter.convert(id.getValue());
        return context.getCassandraClient().exist(key, this.columnFamily, context);
    }

    /**
     * Reads a collection of rows.
     * 
     * @param rowKeys keys of the rows to read
     * @param context data store context
     * @return a {@link CassandraRow} if there is one with the given key, {@code null} otherwise
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<CassandraRow<I, String>> read(List<I> rowKeys, CassandraContext<N> context)
            throws PersistenceException {
        if (rowKeys == null) {
            throw new NullPointerException("rowKeys cannot be null");
        }

        if (rowKeys.isEmpty()) {
            return Collections.emptyList();
        }

        /*
         * Performance tuning to avoid mapping in case an Identity converter is used for the key.
         * See MainColumnFamily.
         */
        boolean identityKeyConverter = false;
        I keySample = rowKeys.get(0);
        K mappedKeySample = this.keyConverter.convert(keySample);
        if (keySample == mappedKeySample) {
            identityKeyConverter = true;
        }

        List<CassandraRow<I, String>> rows = null;

        if (identityKeyConverter) {
            @SuppressWarnings("unchecked")
            List<K> mappedRowKeys = (List<K>) rowKeys;

            List<CassandraRow<K, String>> mappedRows = context.getCassandraClient().read(mappedRowKeys,
                    this.columnFamily, this.columnValueTypeProvider, context);

            @SuppressWarnings("rawtypes")
            List rawList = mappedRows;
            @SuppressWarnings("unchecked")
            List<CassandraRow<I, String>> uncheckedList = rawList;
            rows = uncheckedList;
        }
        else {
            List<K> mappedRowKeys = new ArrayList<K>(rowKeys.size());
            for (I key : rowKeys) {
                mappedRowKeys.add(this.keyConverter.convert(key));
            }

            List<CassandraRow<K, String>> mappedRows = context.getCassandraClient().read(mappedRowKeys,
                    this.columnFamily, this.columnValueTypeProvider, context);

            rows = new ArrayList<CassandraRow<I, String>>(mappedRows.size());
            for (CassandraRow<K, String> mappedRow : mappedRows) {
                rows.add(map(mappedRow, this.inverseKeyConverter));
            }
        }

        return rows;
    }

    /**
     * Converts a row to the corresponding identifiable object.
     * 
     * @param row Cassandra row
     * @return corresponding identifiable object
     */
    public T convert(CassandraRow<I, String> row) {
        if (row == null) {
            throw new NullPointerException("row cannot be null");
        }

        return this.converter.restore(row);
    }

    @Override
    public Collection<ColumnFamily<?, ?>> getColumnFamilies() {
        Collection<ColumnFamily<?, ?>> definitions = new ArrayList<ColumnFamily<?, ?>>(1);
        definitions.add(this.columnFamily);
        return definitions;
    }

    /**
     * Gets the column family identifiable objects are stored at.
     * 
     * @return the column family
     */
    public ColumnFamily<K, String> getColumnFamily() {
        return this.columnFamily;
    }

    /**
     * Returns the column value type provider.
     * 
     * @return the decoder provider
     */
    public ColumnValueTypeProvider<String> getColumnValueTypeProvider() {
        return this.columnValueTypeProvider;
    }

    /**
     * Gets the identifiable object - {@link CassandraRow} converter.
     * 
     * @return the converter
     */
    public BidirectionalConverter<T, CassandraRow<I, String>> getConverter() {
        return this.converter;
    }

    @SuppressWarnings("unchecked")
    private static <S extends Serializable, T extends Serializable> CassandraRow<T, String> map(
            CassandraRow<S, String> row, Converter<S, T> keyConverter) {
        if (row == null) {
            return null;
        }

        T mappedKey = keyConverter.convert(row.getKey());
        if (mappedKey == row.getKey()) {
            /*
             * Performance tuning to avoid mapping in case an Identity converter is used for the
             * key. See MainColumnFamily.
             */
            return (CassandraRow<T, String>) row;
        }

        CassandraRow<T, String> mappedRow = new CassandraRow<T, String>(mappedKey);
        for (Column<String, ?> column : row.getColumns()) {
            mappedRow.setColumn(column);
        }
        return mappedRow;
    }
}
