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
import java.util.List;
import java.util.UUID;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.ByteArrayColumn;
import com.hp.util.model.persistence.cassandra.column.ByteColumn;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnCommandVisitor;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.CustomColumn;
import com.hp.util.model.persistence.cassandra.column.DateColumn;
import com.hp.util.model.persistence.cassandra.column.DoubleColumn;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.FloatColumn;
import com.hp.util.model.persistence.cassandra.column.IntegerColumn;
import com.hp.util.model.persistence.cassandra.column.LongColumn;
import com.hp.util.model.persistence.cassandra.column.SameTypeColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.column.VoidColumn;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Column family to keep indexes values to facilitate updating custom secondary indexes.
 * <p>
 * Reading is easy with secondary indexes using regular column range operations. However updates are
 * harder because the old index value needs to be removed before inserting the new one (Read before
 * write).
 * <p>
 * This class offers a method to update the manual secondary indexes column families.
 * <p>
 * For example, in a users by location indexing the following column families would be needed:
 * <ul>
 * <li>Users: To keep users information.</li>
 * <li>Users by location: To index users by location. Row key would be the location and columns
 * would represent the users.</li>
 * <li>Users index entries: To keep previous values of users locations used to update
 * "Users by Location" column family (and other indexes) after users updates. This same column
 * family can be used to update multiple column families representing indexes.
 * {@code IndexEntryHandler} is an utility class to maintain this table.</li>
 * </ul>
 * <p>
 * <b>IndexEntriesColumnFamily:</b>
 * <ul>
 * <li>Row key (Key validation: K): The same row key from the main column family.</li>
 * <li>Column value (default validation: Bytes): Index value.</li>
 * <li>Column name (comparator: <String, Date>): Composite column name with the attribute name or
 * indexed column name ("Location" for the example above) and the update timestamp.</li>
 * 
 * <pre>
 * column_family_name {
 *     "row_key_in_main_column_family": {
 *         &lt;attr 1, timestamp 1&gt;: old value for attr 1,
 *         &lt;attr 1, timestamp 2&gt;: old value for attr 1,
 *         &lt;attr 2, timestamp 3&gt;: old value for attr 2,
 *         ...
 *         &lt;attr n, timestamp m&gt;: old value for attr n,
 *     }
 * }
 * </pre>
 * 
 * <p>
 * We read previous index values from {@code IndexEntryHandler} rather than the main column family
 * (or source data column family) to deal with concurrency. The timestamp is used to avoid locking
 * for concurrent updates; old values are not override so they are deleted from the index column
 * families.
 * <p>
 * Note: A unique id is actually used rather than a timestamp to guarantee uniqueness and avoid
 * overriding values.
 * <p>
 * <b>Steps to update a row in the main column family:</b>
 * <ol>
 * <li>Using range query read all columns related to the attribute to update from
 * {@code IndexEntryHandler} (All values of "Location" in the example above, or all values of
 * "attr 1" in the column family structure example above).</li>
 * <li>Delete all the read entries (attribute's old values) from all index column families (For
 * example: Users by location column family).</li>
 * <li>Delete all the read entries (attribute's old values) from {@code IndexEntryHandler}.</li>
 * <li>Insert into {@code IndexEntryHandler} the new attribute's value (For example: The new
 * location value).</li>
 * <li>Insert into all index column families the new attribute's value (For example: Users by
 * location column family).</li>
 * <li>Update the row in the main column family with the new attribute's value (For example: users
 * column family).</li>
 * </ol>
 * <p>
 * <b>Some points to notice</b>
 * <ul>
 * <li>This method works for multi-threading environments without the need of implementing any
 * locking (nor local nor distributed) as long as update operations remain idempotent (inserts
 * normally are in Cassandra). Indexes will be eventually consistent.</li>
 * <li>If something goes wrong the same operation can be applied as many times as needed until it
 * completes since it is idempotent.</li>
 * <li>It is possible to get a false positive because updates are being made in flight. You could
 * get a result that is not present while the index column families are being updated. If this is a
 * problem use filter on read rather than locking.</li>
 * </ul>
 * 
 * @param <K> type of the row key in the main column family
 * @author Fabiel Zuniga
 */
public class IndexEntryHandler<K extends Serializable> implements ColumnFamilyHandler {

    private final ColumnFamily<K, IndexEntry> columnFamily;
    private final CompositeType<IndexEntry> indexEntryType;
    private final ColumnCommandVisitor<String, Column<IndexEntry, ?>, IndexEntry> indexEntryColumnConverter;

    /**
     * Creates a column family to keep previous values of indexes.
     * 
     * @param columnFamilyName name for the column family
     * @param rowKeyDataType type of the row key in the main column family
     */
    public IndexEntryHandler(String columnFamilyName, DataType<K> rowKeyDataType) {
        if (columnFamilyName == null) {
            throw new NullPointerException("columnFamilyName cannot be null");
        }

        if (columnFamilyName.isEmpty()) {
            throw new IllegalArgumentException("columnFamilyName cannot be empty");
        }

        if (rowKeyDataType == null) {
            throw new NullPointerException("rowKeyDataType cannot be null");
        }

        this.indexEntryType = new CompositeType<IndexEntry>(new IndexEntrySerializer(), BasicType.STRING_UTF8,
                BasicType.STRING_UTF8);
        this.columnFamily = new ColumnFamily<K, IndexEntry>(columnFamilyName, rowKeyDataType, this.indexEntryType,
                "Column family to keep previous values of indexes");

        this.indexEntryColumnConverter = new IndexEntryColumnConverter();
    }

    @Override
    public Collection<ColumnFamily<?, ?>> getColumnFamilies() {
        Collection<ColumnFamily<?, ?>> definitions = new ArrayList<ColumnFamily<?, ?>>(1);
        definitions.add(this.columnFamily);
        return definitions;
    }

    /**
     * Adds an indexed value.
     * 
     * @param rowKey row key
     * @param indexedColumn indexed column
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void addIndexedValue(K rowKey, Column<String, ?> indexedColumn, CassandraContext<N> context)
            throws PersistenceException {
        if (indexedColumn == null) {
            throw new NullPointerException("indexedColumn cannot be null");
        }

        /*
         * This id is used to make updates on the same attribute unique. In normal conditions the
         * column family will have a single indexed (old) value. Thus, this UUID is considered
         * enough to assume uniqueness. Index uniqueness is given by: Row key (from main column
         * family) + Attribute name + discriminationValue.
         */
        String discriminationValue = UUID.randomUUID().toString();
        IndexEntry indexEntry = new IndexEntry(indexedColumn.getName().getValue(), discriminationValue);

        // Converts the column to use IndexEntry as the name to make it unique
        Column<IndexEntry, ?> indexEntryColumn = indexedColumn.accept(this.indexEntryColumnConverter, indexEntry);

        context.getCassandraClient().insert(indexEntryColumn, rowKey, this.columnFamily, context);
    }

    /**
     * Gets the indexed values (old values).
     * 
     * @param rowKey row key
     * @param indexedColumnName name of the indexed column to retrieve index values (old values) for
     * @param columnValueType column value type
     * @param context data store context
     * @return current indexed values
     * @throws PersistenceException if persistence errors occur while executing the operation
     * @throws ClassCastException id the type of the indexed column is not the same than the one the
     *             given decoder produces
     */
    public <N, D> Collection<Column<IndexEntry, D>> getIndexedValues(K rowKey, ColumnName<String, D> indexedColumnName,
            DataType<D> columnValueType, CassandraContext<N> context) throws PersistenceException,
            ClassCastException {

        // All the values for the same attribute are of the same type
        ColumnValueTypeProvider<IndexEntry> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<IndexEntry, D>(
                columnValueType);

        ColumnName<IndexEntry, ?> start = ColumnName.valueOf(new IndexEntry(indexedColumnName.getValue(),
                RangeLimit.STRING_RANGE_LIMIT.getStart()));
        ColumnName<IndexEntry, ?> end = ColumnName.valueOf(new IndexEntry(indexedColumnName.getValue(),
                RangeLimit.STRING_RANGE_LIMIT.getEnd()));

        CassandraRow<K, IndexEntry> row = context.getCassandraClient().readColumnRange(rowKey, start, end, false,
                Integer.MAX_VALUE, this.columnFamily, columnValueTypeProvider, context);

        Collection<Column<IndexEntry, ?>> indexedValuesColumn = row.getColumns();
        List<Column<IndexEntry, D>> indexedValues = new ArrayList<Column<IndexEntry, D>>(indexedValuesColumn.size());
        for (Column<IndexEntry, ?> column : indexedValuesColumn) {
            @SuppressWarnings("unchecked")
            Column<IndexEntry, D> indexedValue = (Column<IndexEntry, D>) column;
            indexedValues.add(indexedValue);
        }

        return indexedValues;
    }

    /**
     * Deletes the given indexed values (old values).
     * 
     * @param rowKey row key
     * @param indexedValues indexed values to delete
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N, D> void deleteIndexedValues(K rowKey, Collection<Column<IndexEntry, D>> indexedValues,
            CassandraContext<N> context) throws PersistenceException {
        if (rowKey == null) {
            throw new NullPointerException("rowKey cannot be null");
        }

        if (indexedValues == null) {
            throw new NullPointerException("indexedValues cannot be null");
        }

        Collection<ColumnName<IndexEntry, ?>> columnsNames = new ArrayList<ColumnName<IndexEntry, ?>>(
                indexedValues.size());
        for (Column<IndexEntry, ?> column : indexedValues) {
            columnsNames.add(column.getName());
        }

        context.getCassandraClient().delete(columnsNames, rowKey, this.columnFamily, context);
    }

    /**
     * Composite column name to keep the indexed attribute (or column name in the main column
     * family) and a discrimination value to make the indexed value unique to avoid overriding
     * previous values.
     */
    public static class IndexEntry implements Serializable, Comparable<IndexEntry> {
        private static final long serialVersionUID = 1L;

        private String indexedColumnName;
        private String discriminationValue;

        IndexEntry(String indexedColumnName, String discriminationValue) {
            if (indexedColumnName == null) {
                throw new NullPointerException("indexedColumnName cannot be null");
            }
            if (discriminationValue == null) {
                throw new NullPointerException("discriminationValue cannot be null");
            }

            this.indexedColumnName = indexedColumnName;
            this.discriminationValue = discriminationValue;
        }

        /**
         * Gets the name of the indexed attribute (or column name in the main column family).
         * 
         * @return the indexed column name
         */
        public String getIndexedColumnName() {
            return this.indexedColumnName;
        }

        private String getDiscriminationValue() {
            return this.discriminationValue;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.indexedColumnName.hashCode();
            result = prime * result + this.discriminationValue.hashCode();
            return result;
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

            IndexEntry other = (IndexEntry) obj;

            if (!this.indexedColumnName.equals(other.indexedColumnName)) {
                return false;
            }

            if (!this.discriminationValue.equals(other.discriminationValue)) {
                return false;
            }

            return true;
        }

        @Override
        public int compareTo(IndexEntry other) {
            int comparison = this.indexedColumnName.compareTo(other.indexedColumnName);

            if (comparison == 0) {
                comparison = this.discriminationValue.compareTo(other.discriminationValue);
            }

            return comparison;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this,
                    Property.valueOf("indexedColumnName", this.indexedColumnName),
                    Property.valueOf("discriminationValue", this.discriminationValue)
            );
        }
    }

    private static class IndexEntrySerializer implements CompositeTypeSerializer<IndexEntry> {

        @Override
        public List<Component<IndexEntry, ?>> serialize(IndexEntry compositeValue) {
            List<Component<IndexEntry, ?>> components = new ArrayList<Component<IndexEntry, ?>>();
            components.add(new Component<IndexEntry, String>(BasicType.STRING_UTF8, compositeValue
                    .getIndexedColumnName()));
            components.add(new Component<IndexEntry, String>(BasicType.STRING_UTF8, compositeValue
                    .getDiscriminationValue()));
            return components;
        }

        @Override
        public IndexEntry deserialize(List<Component<IndexEntry, ?>> components) {
            String indexedColumnName = (String) components.get(0).getValue();
            String discriminationValue = (String) components.get(1).getValue();
            return new IndexEntry(indexedColumnName, discriminationValue);
        }
    }
    
    private static class IndexEntryColumnConverter implements
            ColumnCommandVisitor<String, Column<IndexEntry, ?>, IndexEntry> {

        @Override
        public Column<IndexEntry, ?> visit(VoidColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Void> columnName = ColumnName.valueOf(input);
            return new VoidColumn<IndexEntry>(columnName);
        }

        @Override
        public Column<IndexEntry, ?> visit(BooleanColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Boolean> columnName = ColumnName.valueOf(input);
            return new BooleanColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(ByteColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Byte> columnName = ColumnName.valueOf(input);
            return new ByteColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(ByteArrayColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, byte[]> columnName = ColumnName.valueOf(input);
            return new ByteArrayColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(DateColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Date> columnName = ColumnName.valueOf(input);
            return new DateColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(DoubleColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Double> columnName = ColumnName.valueOf(input);
            return new DoubleColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(FloatColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Float> columnName = ColumnName.valueOf(input);
            return new FloatColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(IntegerColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Integer> columnName = ColumnName.valueOf(input);
            return new IntegerColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(LongColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, Long> columnName = ColumnName.valueOf(input);
            return new LongColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        public Column<IndexEntry, ?> visit(StringColumn<String> column, IndexEntry input) {
            ColumnName<IndexEntry, String> columnName = ColumnName.valueOf(input);
            return new StringColumn<IndexEntry>(columnName, column.getValue());
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Column<IndexEntry, ?> visit(EnumColumn<String, ? extends Enum<?>> column, IndexEntry input) {
            ColumnName columnName = ColumnName.valueOf(input);
            return new EnumColumn(columnName, column.getValue());
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Column<IndexEntry, ?> visit(CustomColumn<String, ?> column, IndexEntry input) {
            ColumnName columnName = ColumnName.valueOf(input);
            return new CustomColumn(columnName, column.getValue(), column.getDataType());
        }
    }
}
