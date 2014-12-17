/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;

/**
 * Column family.
 * 
 * @param <K> type of the row key
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public class ColumnFamily<K extends Serializable, C extends Serializable & Comparable<C>> {

    private String name;
    private DataType<K> keyValidator;
    private DataType<?> defaultValidator;
    private DataType<C> comparator;
    private String comment;
    private Set<SecondaryIndex> secondaryIndexes;

    /**
     * Creates a column family definition that stores column values as bytes.
     * 
     * @param name name of the column family
     * @param keyValidator row key type
     * @param comparator column name type. Within a row, columns are always stored in sorted order
     *            by their column name. The comparator specifies the data type for the column name,
     *            as well as the sort order in which columns are stored within a row. Unlike
     *            validators, the comparator may not be changed after the column family is defined,
     *            so this is an important consideration when defining a column family in Cassandra.
     * @param comment column family comment or description
     * @param secondaryIndexes built-in secondary indexes (Automatically handled by Cassandra). Note
     *            these are different from {@link CustomSecondaryIndex}.
     */
    public ColumnFamily(String name, DataType<K> keyValidator, DataType<C> comparator,
            String comment, SecondaryIndex... secondaryIndexes) {
        this(name, keyValidator, BasicType.BYTE_ARRAY, comparator, comment, secondaryIndexes);
    }

    /**
     * Creates a column family definition.
     * 
     * @param name name of the column family
     * @param keyValidator row key type
     * @param defaultValidator column value type. Note: If columns will have numeric values do not
     *            use UTF8 as the default validator because numeric values will mostly not be
     *            encoded/decoded to characters and vice versa. Otherwise "String didn't validate"
     *            error will be thrown.
     * @param comparator column name type. Within a row, columns are always stored in sorted order
     *            by their column name. The comparator specifies the data type for the column name,
     *            as well as the sort order in which columns are stored within a row. Unlike
     *            validators, the comparator may not be changed after the column family is defined,
     *            so this is an important consideration when defining a column family in Cassandra.
     * @param comment column family comment or description
     * @param secondaryIndexes secondary indexes
     */
    public ColumnFamily(String name, DataType<K> keyValidator, DataType<?> defaultValidator, DataType<C> comparator,
            String comment, SecondaryIndex... secondaryIndexes) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name = name;
        this.keyValidator = keyValidator;
        this.defaultValidator = defaultValidator;
        this.comparator = comparator;
        this.comment = comment;

        if (secondaryIndexes != null) {
            this.secondaryIndexes = new HashSet<SecondaryIndex>(Arrays.asList(secondaryIndexes));
            this.secondaryIndexes = Collections.unmodifiableSet(this.secondaryIndexes);
        }
        else {
            this.secondaryIndexes = Collections.emptySet();
        }
    }

    /**
     * Gets the column family name.
     * 
     * @return the column family name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the key validator.
     *
     * @return the key validator
     */
    public DataType<K> getKeyValidator() {
        return this.keyValidator;
    }

    /**
     * Gets the default validator or column value type.
     *
     * @return the default validator
     */
    public DataType<?> getDefaultValidator() {
        return this.defaultValidator;
    }

    /**
     * Gets the comparator or column name type.
     *
     * @return the comparator
     */
    public DataType<C> getComparator() {
        return this.comparator;
    }

    /**
     * Gets the column family comment or description.
     *
     * @return the column family comment or description
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Gets the secondary indexes.
     *
     * @return the secondary indexes
     */
    public Set<SecondaryIndex> getSecondaryIndexes() {
        return this.secondaryIndexes;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("name", this.name),
                Property.valueOf("keyValidator", this.keyValidator),
                Property.valueOf("defaultValidator", this.defaultValidator),
                Property.valueOf("comparator", this.comparator),
                Property.valueOf("comment", this.comment)
        );
    }

    /**
     * Native secondary index (The support for this is built-in in Cassandra).
     */
    public static final class SecondaryIndex {
        // Secondary indexes are allowed for column with string names. The name of the index will be the name of the column with no spaces.
        private final ColumnName<String, ?> columnName;
        private final DataType<?> validator;
        private final String indexName;

        /**
         * Creates a secondary index.
         *
         * @param columnName name of the column to create an index for
         * @param validator type of the index (Type of the column value for which the index is
         *            created for)
         */
        public SecondaryIndex(ColumnName<String, ?> columnName, DataType<?> validator) {
            if (columnName == null) {
                throw new NullPointerException("columnName cannot be null");
            }

            if (validator == null) {
                throw new NullPointerException("validator cannot be null");
            }

            this.columnName = columnName;
            this.validator = validator;
            // index names cannot have blank chars. Removes all whitespaces and non visible characters such as tab, \n.
            this.indexName = columnName.getValue().replaceAll("\\s", "");
        }

        /**
         * Gets the column name.
         *
         * @return the column name
         */
        public ColumnName<String, ?> getColumnName() {
            return this.columnName;
        }

        /**
         * Gets the index validator: type of the index (Type of the column value for which the index
         * is created for).
         *
         * @return the column name
         */
        public DataType<?> getValidator() {
            return this.validator;
        }

        /**
         * Gets the index name.
         *
         * @return the index name
         */
        public String getIndexName() {
            return this.indexName;
        }

        @Override
        public int hashCode() {
            return this.columnName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            SecondaryIndex other = (SecondaryIndex)obj;

            if (!this.columnName.equals(other.columnName)) {
                return false;
            }

            return true;
        }
    }
}
