/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import java.io.Serializable;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.Identifiable;
import com.hp.util.common.converter.IdentityBidirectionalConverter;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily.SecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Column family to store objects where the key is a primitive or basic type understood by
 * Cassandra: String, Long Integer, etc.
 * <p>
 * Column names are considered as strings.
 * <ul>
 * <li>Row key (Key validation: String): object's id.</li>
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
 * @param <K> type of the row key
 * @param <T> type of the identifiable object
 * @author Fabiel Zuniga
 */
public class MainColumnFamilyPrimitiveKey<K extends Serializable, T extends Identifiable<? super T, K>> extends
        MainColumnFamily<K, K, T> {

    /**
     * Creates a main column family handler.
     * 
     * @param columnFamilyName name for the column family
     * @param keyDataType row key data type
     * @param comment column family comment or description
     * @param columnValueTypeProvider column value type provider
     * @param converter identifiable object - {@link CassandraRow} converter
     * @param secondaryIndexes built-in secondary indexes (Automatically handled by Cassandra). Note
     *            these are different from {@link CustomSecondaryIndex}.
     */
    public MainColumnFamilyPrimitiveKey(String columnFamilyName, DataType<K> keyDataType, String comment,
            ColumnValueTypeProvider<String> columnValueTypeProvider,
            BidirectionalConverter<T, CassandraRow<K, String>> converter, SecondaryIndex... secondaryIndexes) {
        super(columnFamilyName, keyDataType, comment, columnValueTypeProvider, IdentityBidirectionalConverter
                .<K> getInstance(), converter, secondaryIndexes);
    }
}
