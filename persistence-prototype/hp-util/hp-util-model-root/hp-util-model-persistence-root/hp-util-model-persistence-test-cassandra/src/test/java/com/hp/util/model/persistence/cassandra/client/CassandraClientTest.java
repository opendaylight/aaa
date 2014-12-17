/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraClient;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.CassandraTestUtil;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.ByteArrayColumn;
import com.hp.util.model.persistence.cassandra.column.ByteColumn;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.CustomColumn;
import com.hp.util.model.persistence.cassandra.column.DateColumn;
import com.hp.util.model.persistence.cassandra.column.DoubleColumn;
import com.hp.util.model.persistence.cassandra.column.DynamicColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.FloatColumn;
import com.hp.util.model.persistence.cassandra.column.IntegerColumn;
import com.hp.util.model.persistence.cassandra.column.LongColumn;
import com.hp.util.model.persistence.cassandra.column.SameTypeColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.column.VoidColumn;
import com.hp.util.model.persistence.cassandra.cql.CqlPredicate;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily.SecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;
import com.hp.util.model.persistence.cassandra.keyspace.KeyspaceConfiguration;
import com.hp.util.model.persistence.cassandra.keyspace.Strategy;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc" })
public abstract class CassandraClientTest {

    // TODO: In Windows there are problems dropping tables that use composite
    // column names. This doesn't happen in Linux.

    private static final ColumnFamily<String, String> TEST_COLUMN_FAMILY = new ColumnFamily<String, String>(
            "cf_test_astyanax_facade", BasicType.STRING_UTF8, BasicType.STRING_UTF8,
            "Column family to test the AstyanaxFacade");

    private static final CompositeType<CompositeValue> COMPOSITE_TYPE = new CompositeType<CompositeValue>(
            new CompositeValueSerializer(), BasicType.STRING_UTF8, BasicType.INTEGER);

    @BeforeClass
    public static void beforeClass() throws Exception {
        CassandraTestUtil.beforeTestClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        CassandraTestUtil.afterTestClass();
    }

    /**
     * Gets the cassandra client to test.
     * 
     * @return the cassandra client to test
     */
    protected abstract CassandraClient<Astyanax> getCassandraClient();

    @Before
    public void beforeTest() throws Exception {
        Assume.assumeTrue(CassandraTestUtil.isIntegrationTestSupported());

        CassandraTestUtil.beforeTest();

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                getCassandraClient().createColumnFamily(TEST_COLUMN_FAMILY, context.getKeyspace(), context);
                return null;
            }
        });
    }

    @After
    public void afterTest() throws Exception {
        if (!CassandraTestUtil.isIntegrationTestSupported()) {
            return;
        }

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                getCassandraClient().dropColumnFamily(TEST_COLUMN_FAMILY, context.getKeyspace(), context);
                return null;
            }
        });

        CassandraTestUtil.afterTest();
    }

    @Test
    public void testCreateDropExistKeyspace() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                Keyspace keyspace = new Keyspace("keyspace_to_test_create_drop_and_exist", context.getKeyspace()
                        .getClusterName());
                KeyspaceConfiguration configuration = new KeyspaceConfiguration(Strategy.SIMPLE, 1);

                Assert.assertFalse(getCassandraClient().exists(keyspace, context));
                getCassandraClient().createKeyspace(keyspace, configuration, context);

                Assert.assertTrue(getCassandraClient().exists(keyspace, context));

                getCassandraClient().dropKeyspace(keyspace, context);

                Assert.assertFalse(getCassandraClient().exists(keyspace, context));

                return null;
            }
        });
    }

    @Test
    public void testCreateDropExistColumnFamily() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                ColumnFamily<String, String> columnfamily = new ColumnFamily<String, String>(
                        "cf_to_test_create_drop_and_exist", BasicType.STRING_UTF8, BasicType.STRING_UTF8,
                        BasicType.STRING_UTF8, "Column family to test create drop and exist");

                Assert.assertFalse(getCassandraClient().exists(columnfamily, context.getKeyspace(), context));

                getCassandraClient().createColumnFamily(columnfamily, context.getKeyspace(), context);

                Assert.assertTrue(getCassandraClient().exists(context.getKeyspace(), context));

                getCassandraClient().dropColumnFamily(columnfamily, context.getKeyspace(), context);

                Assert.assertFalse(getCassandraClient().exists(columnfamily, context.getKeyspace(), context));

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteVoidColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Void> columnName = ColumnName.valueOf("column name");
                Column<String, Void> column = new VoidColumn<String>(columnName);
                DataType<Void> columnValueType = BasicType.VOID;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Void> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNotNull(readColumn);
                Assert.assertNull(readColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertNull(readColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteStringColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, String> columnName = ColumnName.valueOf("column name");
                ColumnName<String, String> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, String> column = new StringColumn<String>(columnName, "Hello World");
                Column<String, String> nullValueColumn = new StringColumn<String>(nullValueColumnName);
                DataType<String> columnValueType = BasicType.STRING_UTF8;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, String> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, String> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteBooleanColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Boolean> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Boolean> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Boolean> column = new BooleanColumn<String>(columnName, Boolean.TRUE);
                Column<String, Boolean> nullValueColumn = new BooleanColumn<String>(nullValueColumnName);
                DataType<Boolean> columnValueType = BasicType.BOOLEAN;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Boolean> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Boolean> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteByteColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Byte> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Byte> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Byte> column = new ByteColumn<String>(columnName, Byte.valueOf((byte) 1));
                Column<String, Byte> nullValueColumn = new ByteColumn<String>(nullValueColumnName);
                DataType<Byte> columnValueType = BasicType.BYTE;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Byte> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Byte> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteByteArrayColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, byte[]> columnName = ColumnName.valueOf("column name");
                ColumnName<String, byte[]> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, byte[]> column = new ByteArrayColumn<String>(columnName,
                        new byte[] { (byte) 1, (byte) 2 });
                Column<String, byte[]> nullValueColumn = new ByteArrayColumn<String>(nullValueColumnName);
                DataType<byte[]> columnValueType = BasicType.BYTE_ARRAY;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, byte[]> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, byte[]> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);

                byte[] readValue = readColumn.getValue();
                Assert.assertEquals(column.getValue().length, readValue.length);
                for (int i = 0; i < readValue.length; i++) {
                    Assert.assertEquals(column.getValue()[i], readValue[i]);
                }

                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteDateColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Date> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Date> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Date> column = new DateColumn<String>(columnName, Date.currentTime());
                Column<String, Date> nullValueColumn = new DateColumn<String>(nullValueColumnName);
                DataType<Date> columnValueType = BasicType.DATE;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Date> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Date> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteDateColumnWithDateName() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<Date, Date> columnName = ColumnName.valueOf(Date.valueOf(1));
                ColumnName<Date, Date> nullValueColumnName = ColumnName.valueOf(Date.valueOf(2));
                Column<Date, Date> column = new DateColumn<Date>(columnName, Date.valueOf(3));
                Column<Date, Date> nullValueColumn = new DateColumn<Date>(nullValueColumnName);
                DataType<Date> columnValueType = BasicType.DATE;

                ColumnFamily<String, Date> columnFamily = new ColumnFamily<String, Date>(
                        "testInsertReadDeleteDateColumnWithDateName", BasicType.STRING_UTF8, BasicType.DATE,
                        "Column family to test InsertReadDeleteDateColumnWithDateName");

                // Insert
                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(column, rowKey, columnFamily, context);
                getCassandraClient().insert(nullValueColumn, rowKey, columnFamily, context);

                // Read
                Column<Date, Date> readColumn = getCassandraClient().read(columnName, rowKey, columnFamily,
                        columnValueType, context);
                Column<Date, Date> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        columnFamily, columnValueType, context);
                Assert.assertEquals(column.getName(), readColumn.getName());
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertEquals(nullValueColumn.getName(), readNullValueColumn.getName());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, columnFamily, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, columnFamily, context);
                readColumn = getCassandraClient().read(columnName, rowKey, columnFamily, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, columnFamily,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteDoubleColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Double> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Double> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Double> column = new DoubleColumn<String>(columnName, Double.valueOf(1d));
                Column<String, Double> nullValueColumn = new DoubleColumn<String>(nullValueColumnName);
                DataType<Double> columnValueType = BasicType.DOUBLE;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Double> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Double> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteFloatColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Float> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Float> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Float> column = new FloatColumn<String>(columnName, Float.valueOf(1f));
                Column<String, Float> nullValueColumn = new FloatColumn<String>(nullValueColumnName);
                DataType<Float> columnValueType = BasicType.FLOAT;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Float> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Float> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteIntegerColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Integer> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Integer> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Integer> column = new IntegerColumn<String>(columnName, Integer.valueOf(1));
                Column<String, Integer> nullValueColumn = new IntegerColumn<String>(nullValueColumnName);
                DataType<Integer> columnValueType = BasicType.INTEGER;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Integer> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Integer> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteLongColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, Long> columnName = ColumnName.valueOf("column name");
                ColumnName<String, Long> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, Long> column = new LongColumn<String>(columnName, Long.valueOf(1));
                Column<String, Long> nullValueColumn = new LongColumn<String>(nullValueColumnName);
                DataType<Long> columnValueType = BasicType.LONG;

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, Long> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, Long> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteEnumColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, EnumMock> columnName = ColumnName.valueOf("column name");
                ColumnName<String, EnumMock> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, EnumMock> column = new EnumColumn<String, EnumMock>(columnName, EnumMock.ELEMENT_1);
                Column<String, EnumMock> nullValueColumn = new EnumColumn<String, EnumMock>(nullValueColumnName);
                DataType<EnumMock> columnValueType = EnumType.valueOf(EnumMock.class);

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, EnumMock> readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, EnumMock> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        TEST_COLUMN_FAMILY, columnValueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient()
                        .read(columnName, rowKey, TEST_COLUMN_FAMILY, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteEnumColumnWithEnumName() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<EnumMock, EnumMock> columnName = ColumnName.valueOf(EnumMock.ELEMENT_1);
                ColumnName<EnumMock, EnumMock> nullValueColumnName = ColumnName.valueOf(EnumMock.ELEMENT_2);
                Column<EnumMock, EnumMock> column = new EnumColumn<EnumMock, EnumMock>(columnName, EnumMock.ELEMENT_3);
                Column<EnumMock, EnumMock> nullValueColumn = new EnumColumn<EnumMock, EnumMock>(nullValueColumnName);
                DataType<EnumMock> columnValueType = EnumType.valueOf(EnumMock.class);

                ColumnFamily<String, EnumMock> columnFamily = new ColumnFamily<String, EnumMock>(
                        "testInsertReadDeleteEnumColumnWithEnumName", BasicType.STRING_UTF8, columnValueType,
                        "Column family to test InsertReadDeleteEnumColumnWithEnumName");

                // Insert
                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(column, rowKey, columnFamily, context);
                getCassandraClient().insert(nullValueColumn, rowKey, columnFamily, context);

                // Read
                Column<EnumMock, EnumMock> readColumn = getCassandraClient().read(columnName, rowKey, columnFamily,
                        columnValueType, context);
                Column<EnumMock, EnumMock> readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey,
                        columnFamily, columnValueType, context);
                Assert.assertEquals(column.getName(), readColumn.getName());
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertEquals(nullValueColumn.getName(), readNullValueColumn.getName());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, columnFamily, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, columnFamily, context);
                readColumn = getCassandraClient().read(columnName, rowKey, columnFamily, columnValueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, columnFamily,
                        columnValueType, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteCompositeColumnValue() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, CompositeValue> columnName = ColumnName.valueOf("column name");
                ColumnName<String, CompositeValue> nullValueColumnName = ColumnName.valueOf("null value column name");
                Column<String, CompositeValue> column = new CustomColumn<String, CompositeValue>(columnName,
                        new CompositeValue("string attr", 1), COMPOSITE_TYPE);
                Column<String, CompositeValue> nullValueColumn = new CustomColumn<String, CompositeValue>(
                        nullValueColumnName);

                // Insert
                getCassandraClient().insert(column, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(nullValueColumn, rowKey, TEST_COLUMN_FAMILY, context);

                // Read
                Column<String, CompositeValue> readColumn = getCassandraClient().read(columnName, rowKey,
                        TEST_COLUMN_FAMILY, COMPOSITE_TYPE, context);
                Column<String, CompositeValue> readNullValueColumn = getCassandraClient().read(nullValueColumnName,
                        rowKey, TEST_COLUMN_FAMILY, COMPOSITE_TYPE, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY, context);
                readColumn = getCassandraClient().read(columnName, rowKey, TEST_COLUMN_FAMILY, COMPOSITE_TYPE, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, TEST_COLUMN_FAMILY,
                        COMPOSITE_TYPE, context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteCompositeColumnName() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnFamily<String, CompositeValue> columnFamily = new ColumnFamily<String, CompositeValue>(
                        "cf_test_read_column_composite_column_name", BasicType.STRING_UTF8, COMPOSITE_TYPE,
                        "Test read column with composite column name");

                String rowKey = "row key";

                ColumnName<CompositeValue, String> columnName = ColumnName
                        .valueOf(new CompositeValue("string attr", 1));
                ColumnName<CompositeValue, String> nullValueColumnName = ColumnName.valueOf(new CompositeValue(
                        "null value column name", 1));

                Column<CompositeValue, String> column = new StringColumn<CompositeValue>(columnName,
                        "some value for the column");
                Column<CompositeValue, String> nullValueColumn = new StringColumn<CompositeValue>(nullValueColumnName);

                DataType<String> valueType = BasicType.STRING_UTF8;

                // Insert
                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(column, rowKey, columnFamily, context);
                getCassandraClient().insert(nullValueColumn, rowKey, columnFamily, context);

                // Read
                Column<CompositeValue, String> readColumn = getCassandraClient().read(columnName, rowKey, columnFamily,
                        valueType, context);
                Column<CompositeValue, String> readNullValueColumn = getCassandraClient().read(nullValueColumnName,
                        rowKey, columnFamily, valueType, context);
                Assert.assertEquals(column.getValue(), readColumn.getValue());
                Assert.assertNull(readNullValueColumn.getValue());

                // Delete
                getCassandraClient().delete(columnName, rowKey, columnFamily, context);
                getCassandraClient().delete(nullValueColumnName, rowKey, columnFamily, context);
                readColumn = getCassandraClient().read(columnName, rowKey, columnFamily, valueType, context);
                readNullValueColumn = getCassandraClient().read(nullValueColumnName, rowKey, columnFamily, valueType,
                        context);
                Assert.assertNull(readColumn);
                Assert.assertNull(readNullValueColumn);

                // Delete created table
                getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteRow() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                final DataType<EnumMock> enumColumnValueType = EnumType.valueOf(EnumMock.class);

                final ColumnName<String, Void> voidColumnName = ColumnName.valueOf("valueless value column name");
                final ColumnName<String, String> nullValueColumnName = ColumnName.valueOf("null value column name");
                final ColumnName<String, String> stringColumnName = ColumnName.valueOf("string column name");
                final ColumnName<String, Boolean> booleanColumnName = ColumnName.valueOf("boolean column name");
                final ColumnName<String, Byte> byteColumnName = ColumnName.valueOf("byte column name");
                final ColumnName<String, byte[]> byteArrayColumnName = ColumnName.valueOf("byte array column name");
                final ColumnName<String, Date> dateColumnName = ColumnName.valueOf("date column name");
                final ColumnName<String, Double> doubleColumnName = ColumnName.valueOf("double column name");
                final ColumnName<String, Float> floatColumnName = ColumnName.valueOf("float column name");
                final ColumnName<String, Integer> integerColumnName = ColumnName.valueOf("integer column name");
                final ColumnName<String, Long> longColumnName = ColumnName.valueOf("long column name");
                final ColumnName<String, EnumMock> enumColumnName = ColumnName.valueOf("enum column name");
                final ColumnName<String, CompositeValue> compositeColumnName = ColumnName
                        .valueOf("composite column name");

                Column<String, Void> voidColumn = new VoidColumn<String>(voidColumnName);
                Column<String, String> nullValueColumn = new StringColumn<String>(nullValueColumnName);
                Column<String, String> stringColumn = new StringColumn<String>(stringColumnName, "Hello World");
                Column<String, Boolean> booleanColumn = new BooleanColumn<String>(booleanColumnName, Boolean.TRUE);
                Column<String, Byte> byteColumn = new ByteColumn<String>(byteColumnName, Byte.valueOf((byte) 1));
                Column<String, byte[]> byteArrayColumn = new ByteArrayColumn<String>(byteArrayColumnName, new byte[] {
                        (byte) 1, (byte) 2 });
                Column<String, Date> dateColumn = new DateColumn<String>(dateColumnName, Date.currentTime());
                Column<String, Double> doubleColumn = new DoubleColumn<String>(doubleColumnName, Double.valueOf(1.0));
                Column<String, Float> floatColumn = new FloatColumn<String>(floatColumnName, Float.valueOf(1f));
                Column<String, Integer> integerColumn = new IntegerColumn<String>(integerColumnName, Integer.valueOf(1));
                Column<String, Long> longColumn = new LongColumn<String>(longColumnName, Long.valueOf(1));
                Column<String, EnumMock> enumColumn = new EnumColumn<String, EnumMock>(enumColumnName,
                        EnumMock.ELEMENT_1);

                Column<String, CompositeValue> compositeColumn = new CustomColumn<String, CompositeValue>(
                        compositeColumnName, new CompositeValue("string attr", 1), COMPOSITE_TYPE);

                DynamicColumnValueTypeProvider<String> columnValueTypeProvider = new DynamicColumnValueTypeProvider<String>();
                columnValueTypeProvider.registerColumnValueType(voidColumnName, BasicType.VOID);
                columnValueTypeProvider.registerColumnValueType(nullValueColumnName, BasicType.STRING_UTF8);
                columnValueTypeProvider.registerColumnValueType(stringColumnName, BasicType.STRING_UTF8);
                columnValueTypeProvider.registerColumnValueType(booleanColumnName, BasicType.BOOLEAN);
                columnValueTypeProvider.registerColumnValueType(byteColumnName, BasicType.BYTE);
                columnValueTypeProvider.registerColumnValueType(byteArrayColumnName, BasicType.BYTE_ARRAY);
                columnValueTypeProvider.registerColumnValueType(dateColumnName, BasicType.DATE);
                columnValueTypeProvider.registerColumnValueType(doubleColumnName, BasicType.DOUBLE);
                columnValueTypeProvider.registerColumnValueType(floatColumnName, BasicType.FLOAT);
                columnValueTypeProvider.registerColumnValueType(integerColumnName, BasicType.INTEGER);
                columnValueTypeProvider.registerColumnValueType(longColumnName, BasicType.LONG);
                columnValueTypeProvider.registerColumnValueType(enumColumnName, enumColumnValueType);
                columnValueTypeProvider.registerColumnValueType(compositeColumnName, COMPOSITE_TYPE);

                // Insert
                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(voidColumn);
                row.setColumn(nullValueColumn);
                row.setColumn(stringColumn);
                row.setColumn(booleanColumn);
                row.setColumn(byteColumn);
                row.setColumn(byteArrayColumn);
                row.setColumn(dateColumn);
                row.setColumn(doubleColumn);
                row.setColumn(floatColumn);
                row.setColumn(integerColumn);
                row.setColumn(longColumn);
                row.setColumn(enumColumn);
                row.setColumn(compositeColumn);
                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());

                Assert.assertNotNull(readRow.getColumn(voidColumnName));
                Assert.assertNull(readRow.getColumn(voidColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(nullValueColumnName));
                Assert.assertNull(readRow.getColumn(nullValueColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(stringColumnName));
                Assert.assertEquals(stringColumn.getValue(), readRow.getColumn(stringColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(booleanColumnName));
                Assert.assertEquals(booleanColumn.getValue(), readRow.getColumn(booleanColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(byteColumnName));
                Assert.assertEquals(byteColumn.getValue(), readRow.getColumn(byteColumnName).getValue());

                byte[] expectedValue = byteArrayColumn.getValue();
                byte[] readValue = (byte[]) readRow.getColumn(byteArrayColumnName).getValue();
                Assert.assertEquals(expectedValue.length, readValue.length);
                for (int i = 0; i < expectedValue.length; i++) {
                    Assert.assertEquals(expectedValue[i], readValue[i]);
                }

                Assert.assertNotNull(readRow.getColumn(dateColumnName));
                Assert.assertEquals(dateColumn.getValue(), readRow.getColumn(dateColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(doubleColumnName));
                Assert.assertEquals(doubleColumn.getValue(), readRow.getColumn(doubleColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(floatColumnName));
                Assert.assertEquals(floatColumn.getValue(), readRow.getColumn(floatColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(integerColumnName));
                Assert.assertEquals(integerColumn.getValue(), readRow.getColumn(integerColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(longColumnName));
                Assert.assertEquals(longColumn.getValue(), readRow.getColumn(longColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(enumColumnName));
                Assert.assertEquals(enumColumn.getValue(), readRow.getColumn(enumColumnName).getValue());

                Assert.assertNotNull(readRow.getColumn(compositeColumnName));
                Assert.assertEquals(compositeColumn.getValue(), readRow.getColumn(compositeColumnName).getValue());

                // Delete
                getCassandraClient().delete(rowKey, TEST_COLUMN_FAMILY, context);
                readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteRowCompositeKey() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnFamily<CompositeValue, String> indexedColumnFamily = new ColumnFamily<CompositeValue, String>(
                        "cf_test_read_row_composite_key", COMPOSITE_TYPE, BasicType.STRING_UTF8,
                        "Test read row with composite key");

                CompositeValue rowKey = new CompositeValue("string attr", 1);
                ColumnName<String, String> columnName = ColumnName.valueOf("column name");
                Column<String, String> column = new StringColumn<String>(columnName, "column value");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                // Insert
                CassandraRow<CompositeValue, String> row = new CassandraRow<CompositeValue, String>(rowKey);
                row.setColumn(column);
                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row, indexedColumnFamily, context);

                // Read
                CassandraRow<CompositeValue, String> readRow = getCassandraClient().read(rowKey, indexedColumnFamily,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());

                Assert.assertNotNull(readRow.getColumn(columnName));
                Assert.assertEquals(column.getValue(), readRow.getColumn(columnName).getValue());

                // Delete
                getCassandraClient().delete(rowKey, indexedColumnFamily, context);
                readRow = getCassandraClient().read(rowKey, indexedColumnFamily, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                // Delete column family
                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testInsertReadDeleteRowCompositeColumnName() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnFamily<String, CompositeValue> columnFamily = new ColumnFamily<String, CompositeValue>(
                        "cf_test_read_row_composite_column_name", BasicType.STRING_UTF8, COMPOSITE_TYPE,
                        "Test read row with composite column name");

                String rowKey = "row key";

                ColumnName<CompositeValue, String> columnName = ColumnName
                        .valueOf(new CompositeValue("string attr", 1));
                ColumnName<CompositeValue, String> nullValueColumnName = ColumnName.valueOf(new CompositeValue(
                        "null value column name", 1));

                Column<CompositeValue, String> column = new StringColumn<CompositeValue>(columnName,
                        "some value for the column");
                Column<CompositeValue, String> nullValueColumn = new StringColumn<CompositeValue>(nullValueColumnName);

                ColumnValueTypeProvider<CompositeValue> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<CompositeValue, String>(
                        BasicType.STRING_UTF8);

                // Insert
                CassandraRow<String, CompositeValue> row = new CassandraRow<String, CompositeValue>(rowKey);
                row.setColumn(column);
                row.setColumn(nullValueColumn);
                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row, columnFamily, context);

                // Read
                CassandraRow<String, CompositeValue> readRow = getCassandraClient().read(rowKey, columnFamily,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());

                Assert.assertNotNull(readRow.getColumn(columnName));
                Assert.assertEquals(column.getValue(), readRow.getColumn(columnName).getValue());

                Assert.assertNotNull(readRow.getColumn(nullValueColumnName));
                Assert.assertNull(readRow.getColumn(nullValueColumnName).getValue());

                // Delete
                getCassandraClient().delete(rowKey, columnFamily, context);
                readRow = getCassandraClient().read(rowKey, columnFamily, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                // Delete column family
                getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testDeleteColumns() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");
                Column<String, String> column1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World 2");
                DataType<String> columnValueType = BasicType.STRING_UTF8;

                getCassandraClient().insert(column1, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(column2, rowKey, TEST_COLUMN_FAMILY, context);

                Column<String, ?> readColumn1 = getCassandraClient().read(columnName1, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);
                Column<String, ?> readColumn2 = getCassandraClient().read(columnName2, rowKey, TEST_COLUMN_FAMILY,
                        columnValueType, context);

                Assert.assertNotNull(readColumn1);
                Assert.assertNotNull(readColumn2);

                Collection<ColumnName<String, ?>> columnsToDelete = new ArrayList<ColumnName<String, ?>>(2);
                columnsToDelete.add(columnName1);
                columnsToDelete.add(columnName2);

                getCassandraClient().delete(columnsToDelete, rowKey, TEST_COLUMN_FAMILY, context);

                readColumn1 = getCassandraClient().read(columnName1, rowKey, TEST_COLUMN_FAMILY, columnValueType,
                        context);
                readColumn2 = getCassandraClient().read(columnName1, rowKey, TEST_COLUMN_FAMILY, columnValueType,
                        context);

                Assert.assertNull(readColumn1);
                Assert.assertNull(readColumn2);

                return null;
            }
        });
    }

    @Test
    public void testTruncateColumnFamily() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnName<String, String> columnName = ColumnName.valueOf("column name");
                String rowKey1 = "row key 1";
                Column<String, String> column1 = new StringColumn<String>(columnName, "Hello World 1");
                String rowKey2 = "row key 2";
                Column<String, String> column2 = new StringColumn<String>(columnName, "Hello World 2");
                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);

                row1.setColumn(column1);
                row2.setColumn(column2);

                getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);

                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey1, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);
                Assert.assertNotNull(readRow);

                readRow = getCassandraClient().read(rowKey2, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNotNull(readRow);

                getCassandraClient().truncateColumnFamily(TEST_COLUMN_FAMILY, context);

                readRow = getCassandraClient().read(rowKey1, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                readRow = getCassandraClient().read(rowKey2, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                return null;
            }
        });
    }

    @Test
    public void testDeleteRows() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnName<String, String> columnName = ColumnName.valueOf("column name");
                String rowKey1 = "row key 1";
                Column<String, String> column1 = new StringColumn<String>(columnName, "Hello World 1");
                String rowKey2 = "row key 2";
                Column<String, String> column2 = new StringColumn<String>(columnName, "Hello World 2");
                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);

                row1.setColumn(column1);
                row2.setColumn(column2);

                getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);

                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey1, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);
                Assert.assertNotNull(readRow);

                readRow = getCassandraClient().read(rowKey2, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNotNull(readRow);

                getCassandraClient().delete(Arrays.asList(rowKey1, rowKey2), TEST_COLUMN_FAMILY, context);

                readRow = getCassandraClient().read(rowKey1, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                readRow = getCassandraClient().read(rowKey2, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);
                Assert.assertNull(readRow);

                return null;
            }
        });
    }

    @Test
    public void testCountColumns() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";
                ColumnName<String, String> ColumnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");
                Column<String, String> column1 = new StringColumn<String>(ColumnName1);
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World");
                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(column1);
                row.setColumn(column2);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                Long columnCount = Long.valueOf(getCassandraClient().countColumns(rowKey, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(2), columnCount);

                columnCount = Long.valueOf(getCassandraClient().countColumns("Non existent key", TEST_COLUMN_FAMILY,
                        context));
                Assert.assertEquals(Long.valueOf(0), columnCount);

                return null;
            }
        });
    }

    @Test
    public void testReadAllRows() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, String> column1Row1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column1Row2 = new StringColumn<String>(columnName1, "Hello World 2");

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);

                row1.setColumn(column1Row1);
                row1.setColumn(column2Row1);

                row2.setColumn(column1Row2);
                row2.setColumn(column2Row2);

                getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);

                Collection<CassandraRow<String, String>> queryResult = getCassandraClient().read(TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());
                for (CassandraRow<String, String> row : queryResult) {
                    Assert.assertTrue(row.getKey().equals(rowKey1) || row.getKey().equals(rowKey2));
                    Assert.assertEquals(2, row.getColumns().size());
                    Assert.assertNotNull(row.getColumn(columnName1));
                    Assert.assertNotNull(row.getColumn(columnName2));
                    Assert.assertNotNull(row.getColumn(columnName1).getValue());
                    Assert.assertNotNull(row.getColumn(columnName2).getValue());
                }

                return null;
            }
        });
    }

    @Test
    public void testExist() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, String> column1Row1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column1Row2 = new StringColumn<String>(columnName1, "Hello World 2");

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);

                row1.setColumn(column1Row1);
                row1.setColumn(column2Row1);

                row2.setColumn(column1Row2);
                row2.setColumn(column2Row2);

                getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);

                Assert.assertTrue(getCassandraClient().exist(rowKey1, TEST_COLUMN_FAMILY, context));
                Assert.assertTrue(getCassandraClient().exist(rowKey2, TEST_COLUMN_FAMILY, context));
                Assert.assertFalse(getCassandraClient().exist("unexistent key", TEST_COLUMN_FAMILY, context));

                return null;
            }
        });
    }

    @Test
    public void testReadColumns() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");
                ColumnName<String, String> columnName3 = ColumnName.valueOf("column name 3");

                Column<String, String> column1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World 2");
                Column<String, String> column3 = new StringColumn<String>(columnName3, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(column1);
                row.setColumn(column2);
                row.setColumn(column3);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                Collection<ColumnName<String, ?>> columnNames = new ArrayList<ColumnName<String, ?>>();
                columnNames.add(columnName1);
                columnNames.add(columnName3);

                CassandraRow<String, String> readRow = getCassandraClient().readColumns(columnNames, rowKey,
                        TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName3));
                Assert.assertEquals(column3.getValue(), readRow.getColumn(columnName3).getValue());

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRange() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey, columnNameB,
                        columnNameD, false, Integer.MAX_VALUE, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(3, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameD));
                Assert.assertEquals(columnD.getValue(), readRow.getColumn(columnNameD).getValue());

                List<Column<String, ?>> order = new ArrayList<Column<String, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameB, order.get(0).getName());
                Assert.assertEquals(columnNameC, order.get(1).getName());
                Assert.assertEquals(columnNameD, order.get(2).getName());

                // Count

                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, columnNameB, columnNameD,
                        false, Integer.MAX_VALUE, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(3), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeLimited() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey, columnNameB,
                        columnNameD, false, 2, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                List<Column<String, ?>> order = new ArrayList<Column<String, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameB, order.get(0).getName());
                Assert.assertEquals(columnNameC, order.get(1).getName());

                // Count

                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, columnNameB, columnNameD,
                        false, 2, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(2), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeReversed() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey, columnNameC,
                        columnNameA, true, Integer.MAX_VALUE, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(3, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameA));
                Assert.assertEquals(columnA.getValue(), readRow.getColumn(columnNameA).getValue());

                List<Column<String, ?>> order = new ArrayList<Column<String, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameC, order.get(0).getName());
                Assert.assertEquals(columnNameB, order.get(1).getName());
                Assert.assertEquals(columnNameA, order.get(2).getName());

                // Count

                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, columnNameC, columnNameA, true,
                        Integer.MAX_VALUE, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(3), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeReversedLimited() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey, columnNameC,
                        columnNameA, true, 2, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                List<Column<String, ?>> order = new ArrayList<Column<String, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameC, order.get(0).getName());
                Assert.assertEquals(columnNameB, order.get(1).getName());

                // Count

                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, columnNameC, columnNameA, true,
                        2, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(2), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeEmpty() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey,
                        ColumnName.valueOf("F"), ColumnName.valueOf("G"), false, Integer.MAX_VALUE, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(0, readRow.getColumns().size());

                // Count
                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, ColumnName.valueOf("F"),
                        ColumnName.valueOf("G"), false, Integer.MAX_VALUE, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(0), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeUnbounded() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey, columnNameB, null,
                        false, Integer.MAX_VALUE, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(3, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameD));
                Assert.assertEquals(columnD.getValue(), readRow.getColumn(columnNameD).getValue());

                List<Column<String, ?>> order = new ArrayList<Column<String, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameB, order.get(0).getName());
                Assert.assertEquals(columnNameC, order.get(1).getName());
                Assert.assertEquals(columnNameD, order.get(2).getName());

                // Count
                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, columnNameB, null, false,
                        Integer.MAX_VALUE, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(3), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeCompositeColumnName() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnFamily<String, CompositeValue> columnFamily = new ColumnFamily<String, CompositeValue>(
                        "cf_test_read_column_range_composite_column_name", BasicType.STRING_UTF8, COMPOSITE_TYPE,
                        "Test read column range with composite column name");

                String rowKey = "row key";

                ColumnName<CompositeValue, String> columnNameA = ColumnName.valueOf(new CompositeValue("A", 1));
                ColumnName<CompositeValue, String> columnNameB = ColumnName.valueOf(new CompositeValue("B", 1));
                ColumnName<CompositeValue, String> columnNameC = ColumnName.valueOf(new CompositeValue("C", 2));
                ColumnName<CompositeValue, String> columnNameD = ColumnName.valueOf(new CompositeValue("D", 2));

                Column<CompositeValue, String> columnA = new StringColumn<CompositeValue>(columnNameA, "Hello World 1");
                Column<CompositeValue, String> columnB = new StringColumn<CompositeValue>(columnNameB, "Hello World 2");
                Column<CompositeValue, String> columnC = new StringColumn<CompositeValue>(columnNameC, "Hello World 3");
                Column<CompositeValue, String> columnD = new StringColumn<CompositeValue>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<CompositeValue> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<CompositeValue, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, CompositeValue> row = new CassandraRow<String, CompositeValue>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row, columnFamily, context);

                // Read
                CassandraRow<String, CompositeValue> readRow = getCassandraClient().readColumnRange(rowKey,
                        columnNameB, columnNameD, false, Integer.MAX_VALUE, columnFamily, columnValueTypeProvider,
                        context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(3, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameD));
                Assert.assertEquals(columnD.getValue(), readRow.getColumn(columnNameD).getValue());

                List<Column<CompositeValue, ?>> order = new ArrayList<Column<CompositeValue, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameB, order.get(0).getName());
                Assert.assertEquals(columnNameC, order.get(1).getName());
                Assert.assertEquals(columnNameD, order.get(2).getName());

                // Count
                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, columnNameB, columnNameD,
                        false, Integer.MAX_VALUE, columnFamily, context));
                Assert.assertEquals(Long.valueOf(3), count);

                // Delete column family
                getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    /*
    @Test
    @Ignore
    public void testReadCountColumnRangeCompositeColumnNameWithRange() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                // AnnotatedCompositeSerializer instead of Serializer so we have buildRange() method
                // available.
                AnnotatedCompositeSerializer<CompositeValue> compositeValueSerializer = new AnnotatedCompositeSerializer<CompositeValue>(
                        CompositeValue.class);

                ColumnFamily<String, CompositeValue> columnFamily = new ColumnFamily<String, CompositeValue>(
                        "cf_test_read_column_range_comp_col_name_range", BasicType.STRING_UTF8, COMPOSITE_TYPE,
                        "Test read column range with composite column name");

                String rowKey = "row key";

                ColumnName<CompositeValue, String> columnNameA = ColumnName.valueOf(new CompositeValue("A", 1));
                ColumnName<CompositeValue, String> columnNameB = ColumnName.valueOf(new CompositeValue("B", 1));
                ColumnName<CompositeValue, String> columnNameC = ColumnName.valueOf(new CompositeValue("B", 3));
                ColumnName<CompositeValue, String> columnNameD = ColumnName.valueOf(new CompositeValue("B", 2));

                Column<CompositeValue, String> columnA = new StringColumn<CompositeValue>(columnNameA, "Hello World 1");
                Column<CompositeValue, String> columnB = new StringColumn<CompositeValue>(columnNameB, "Hello World 2");
                Column<CompositeValue, String> columnC = new StringColumn<CompositeValue>(columnNameC, "Hello World 3");
                Column<CompositeValue, String> columnD = new StringColumn<CompositeValue>(columnNameD, "Hello World 3");

                ColumnValueTypeProvider<CompositeValue> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<CompositeValue, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, CompositeValue> row = new CassandraRow<String, CompositeValue>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);

                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row, columnFamily, context);

                // Read

                // Note: If the composite object contains more attributes this is possible:
                // compositeValueSerializer.buildRange().withPrefix("ordinal_0_attr").withPrefix("ordinal_1_attr").greaterThanEquals(...).lessThanEquals(...);
                // For example assume we keep country:state:city:person
                // compositeValueSerializer.buildRange().withPrefix("United States").withPrefix("CA").withPrefix("Roseville").greaterThanEquals(" ").lessThanEquals("~");
                // Note: Range queries must include all attributes: prefixes or all attributes but
                // the last one and a "start" and "end" expression for the last ordinal attribute,
                // otherwise the query will fail.
                // In the previous example, prefixes are used for country, state and city. For the
                // last attribute (person) " " is the start point and "~" as the end point.
                ByteBufferRange range = compositeValueSerializer.buildRange().withPrefix("B")
                        .greaterThanEquals(Integer.valueOf(1)).lessThan(Integer.valueOf(3));

                CassandraRow<String, CompositeValue> readRow = getCassandraClient().readColumnRange(rowKey, range,
                        columnFamily, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameB));
                Assert.assertEquals(columnB.getValue(), readRow.getColumn(columnNameB).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameD));
                Assert.assertEquals(columnD.getValue(), readRow.getColumn(columnNameD).getValue());

                List<Column<CompositeValue, ?>> order = new ArrayList<Column<CompositeValue, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameB, order.get(0).getName());
                Assert.assertEquals(columnNameD, order.get(1).getName());

                // Count

                // ------------------------------------------------------------------------------------------------------
                // TODO: Count with range is not working; if the Assert line is uncommented the
                // table is not deleted and that is causing problems in Windows if integration test
                // is performed with a real Cassandra instance. The machine would need to be
                // restarted.
                /*
                 * Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, range,
                 * columnFamilyDef.getColumnFamily(), decoderProvider, context));
                 * Assert.assertEquals(Long.valueOf(2), count);
                 * /
                // ------------------------------------------------------------------------------------------------------

                // Delete column family
                getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }
    */

    @Test
    public void testReadCountColumnRangeNonExistentStartColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");
                ColumnName<String, String> columnNameF = ColumnName.valueOf("F");

                ColumnName<String, String> start = ColumnName.valueOf("B");
                ColumnName<String, String> end = ColumnName.valueOf("E");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 2");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 3");
                Column<String, String> columnF = new StringColumn<String>(columnNameF, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnC);
                row.setColumn(columnD);
                row.setColumn(columnF);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                // Read
                CassandraRow<String, String> readRow = getCassandraClient().readColumnRange(rowKey, start, end, false,
                        Integer.MAX_VALUE, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameD));
                Assert.assertEquals(columnD.getValue(), readRow.getColumn(columnNameD).getValue());

                List<Column<String, ?>> order = new ArrayList<Column<String, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameC, order.get(0).getName());
                Assert.assertEquals(columnNameD, order.get(1).getName());

                // Count

                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, start, end, false,
                        Integer.MAX_VALUE, TEST_COLUMN_FAMILY, context));
                Assert.assertEquals(Long.valueOf(2), count);

                return null;
            }
        });
    }

    @Test
    public void testReadCountColumnRangeCompositeColumnNameNonExistentStartColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                ColumnFamily<String, CompositeValue> columnFamily = new ColumnFamily<String, CompositeValue>(
                        "cf_read_column_range_composite_column_name_2", BasicType.STRING_UTF8, COMPOSITE_TYPE,
                        "Test read column range with composite column name");

                String rowKey = "row key";

                ColumnName<CompositeValue, String> columnNameA = ColumnName.valueOf(new CompositeValue("A", 1));
                ColumnName<CompositeValue, String> columnNameC = ColumnName.valueOf(new CompositeValue("C", 1));
                ColumnName<CompositeValue, String> columnNameD = ColumnName.valueOf(new CompositeValue("D", 2));
                ColumnName<CompositeValue, String> columnNameF = ColumnName.valueOf(new CompositeValue("F", 2));

                ColumnName<CompositeValue, String> start = ColumnName.valueOf(new CompositeValue("B", 10));
                ColumnName<CompositeValue, String> end = ColumnName.valueOf(new CompositeValue("E", 10));

                Column<CompositeValue, String> columnA = new StringColumn<CompositeValue>(columnNameA, "Hello World 1");
                Column<CompositeValue, String> columnC = new StringColumn<CompositeValue>(columnNameC, "Hello World 2");
                Column<CompositeValue, String> columnD = new StringColumn<CompositeValue>(columnNameD, "Hello World 3");
                Column<CompositeValue, String> columnF = new StringColumn<CompositeValue>(columnNameF, "Hello World 3");

                ColumnValueTypeProvider<CompositeValue> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<CompositeValue, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, CompositeValue> row = new CassandraRow<String, CompositeValue>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnC);
                row.setColumn(columnD);
                row.setColumn(columnF);

                getCassandraClient().createColumnFamily(columnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row, columnFamily, context);

                // Read
                CassandraRow<String, CompositeValue> readRow = getCassandraClient().readColumnRange(rowKey, start, end,
                        false, Integer.MAX_VALUE, columnFamily, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnNameC));
                Assert.assertEquals(columnC.getValue(), readRow.getColumn(columnNameC).getValue());

                Assert.assertNotNull(readRow.getColumn(columnNameD));
                Assert.assertEquals(columnD.getValue(), readRow.getColumn(columnNameD).getValue());

                List<Column<CompositeValue, ?>> order = new ArrayList<Column<CompositeValue, ?>>(readRow.getColumns());
                Assert.assertEquals(columnNameC, order.get(0).getName());
                Assert.assertEquals(columnNameD, order.get(1).getName());

                // Count

                Long count = Long.valueOf(getCassandraClient().countColumnRange(rowKey, start, end, false,
                        Integer.MAX_VALUE, columnFamily, context));
                Assert.assertEquals(Long.valueOf(2), count);

                // Delete column family
                getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testReadColumnPage() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnNameA = ColumnName.valueOf("A");
                ColumnName<String, String> columnNameB = ColumnName.valueOf("B");
                ColumnName<String, String> columnNameC = ColumnName.valueOf("C");
                ColumnName<String, String> columnNameD = ColumnName.valueOf("D");
                ColumnName<String, String> columnNameF = ColumnName.valueOf("F");

                Column<String, String> columnA = new StringColumn<String>(columnNameA, "Hello World 1");
                Column<String, String> columnB = new StringColumn<String>(columnNameB, "Hello World 2");
                Column<String, String> columnC = new StringColumn<String>(columnNameC, "Hello World 3");
                Column<String, String> columnD = new StringColumn<String>(columnNameD, "Hello World 4");
                Column<String, String> columnF = new StringColumn<String>(columnNameF, "Hello World 5");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(columnA);
                row.setColumn(columnB);
                row.setColumn(columnC);
                row.setColumn(columnD);
                row.setColumn(columnF);

                List<Column<String, ?>> columns = new ArrayList<Column<String, ?>>(row.getColumns());

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);
                testPagedRead(rowKey, columns, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                return null;
            }
        });
    }

    protected <K extends Serializable, C extends Serializable & Comparable<C>> void testPagedRead(K rowKey,
            List<Column<C, ?>> rowContent, ColumnFamily<K, C> columnFamily,
            ColumnValueTypeProvider<C> columnValueTypeProvider, CassandraContext<Astyanax> context)
            throws PersistenceException {
        int totalColumns = rowContent.size();

        // Next Page

        for (int size = 1; size <= totalColumns; size++) {
            int totalPages = totalColumns / size;

            // handle extra non-full page at the end
            if (totalPages * size < totalColumns) {
                totalPages = totalPages + 1;
            }

            // Search result will contain the aggregated records from all pages to compare at the
            // end
            List<Column<C, ?>> aggregatedResult = new ArrayList<Column<C, ?>>(totalColumns);

            MarkPage<Column<C, ?>> page;
            MarkPageRequest<ColumnName<C, ?>> pageRequest = new MarkPageRequest<ColumnName<C, ?>>(size);

            ColumnName<C, ?> end = null;
            do {
                page = getCassandraClient().read(rowKey, pageRequest, end, columnFamily, columnValueTypeProvider,
                        context);
                aggregatedResult.addAll(page.getData());
                MarkPageRequest<Column<C, ?>> nextPageRequest = page.getNextPageRequest();
                pageRequest = null;
                if (nextPageRequest != null) {
                    pageRequest = nextPageRequest
                            .<ColumnName<C, ?>> convert(nextPageRequest.getMark() != null ? nextPageRequest.getMark()
                                    .getName() : null);
                }
            }
            while (!page.getData().isEmpty());

            Assert.assertEquals(rowContent, aggregatedResult);
        }

        // Previous Page

        for (int size = 1; size <= totalColumns; size++) {
            int totalPages = totalColumns / size;

            // handle extra non-full page at the end
            if (totalPages * size < totalColumns) {
                totalPages = totalPages + 1;
            }

            // Search result will contain the aggregated records from all pages to compare at the
            // end
            List<Column<C, ?>> aggregatedResult = new ArrayList<Column<C, ?>>(totalColumns);

            MarkPage<Column<C, ?>> page;
            Column<C, ?> columnMark = rowContent.get(totalColumns - 1);
            MarkPageRequest<ColumnName<C, ?>> pageRequest = new MarkPageRequest<ColumnName<C, ?>>(columnMark.getName(),
                    Navigation.PREVIOUS, size);

            // The mark is not included in the page
            aggregatedResult.add(columnMark);

            ColumnName<C, ?> end = null;
            do {
                page = getCassandraClient().read(rowKey, pageRequest, end, columnFamily, columnValueTypeProvider,
                        context);
                aggregatedResult.addAll(page.getData());
                MarkPageRequest<Column<C, ?>> previousPageRequest = page.getPreviousPageRequest();
                pageRequest = null;
                if (previousPageRequest != null) {
                    pageRequest = previousPageRequest
                            .<ColumnName<C, ?>> convert(previousPageRequest.getMark() != null ? previousPageRequest
                                    .getMark().getName() : null);
                }
            }
            while (!page.getData().isEmpty());

            Assert.assertEquals(rowContent.size(), aggregatedResult.size());
            Assert.assertTrue(rowContent.containsAll(aggregatedResult));
            Assert.assertTrue(aggregatedResult.containsAll(rowContent));
        }
    }

    @Test
    public void testColumnIncrementalAddition() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, String> column1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World 2");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                getCassandraClient().insert(column1, rowKey, TEST_COLUMN_FAMILY, context);

                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(1, readRow.getColumns().size());
                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                //

                getCassandraClient().insert(column2, rowKey, TEST_COLUMN_FAMILY, context);

                readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                return null;
            }
        });
    }

    @Test
    public void testColumnIncrementalDeletion() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, String> column1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World 2");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                getCassandraClient().insert(column1, rowKey, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(column2, rowKey, TEST_COLUMN_FAMILY, context);

                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                //

                getCassandraClient().delete(columnName1, rowKey, TEST_COLUMN_FAMILY, context);

                readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(1, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                return null;
            }
        });
    }

    @Test
    public void testRowIncrementalAddition() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");
                ColumnName<String, String> columnName3 = ColumnName.valueOf("column name 3");

                Column<String, String> column1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World 2");
                Column<String, String> column3 = new StringColumn<String>(columnName3, "Hello World 3");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(column1);
                row.setColumn(column2);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                //

                CassandraRow<String, String> overrideRow = new CassandraRow<String, String>(rowKey);
                overrideRow.setColumn(column3);

                getCassandraClient().insert(overrideRow, TEST_COLUMN_FAMILY, context);

                readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(3, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName3));
                Assert.assertEquals(column3.getValue(), readRow.getColumn(columnName3).getValue());

                return null;
            }
        });
    }

    @Test
    public void testRowColumnDeletion() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                String rowKey = "row key";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, String> column1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column2 = new StringColumn<String>(columnName2, "Hello World 2");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row = new CassandraRow<String, String>(rowKey);
                row.setColumn(column1);
                row.setColumn(column2);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                CassandraRow<String, String> readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY,
                        columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(2, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName1));
                Assert.assertEquals(column1.getValue(), readRow.getColumn(columnName1).getValue());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                //

                row.delete(columnName1);

                getCassandraClient().insert(row, TEST_COLUMN_FAMILY, context);

                readRow = getCassandraClient().read(rowKey, TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(readRow);
                Assert.assertNotNull(readRow.getKey());
                Assert.assertEquals(rowKey, readRow.getKey());
                Assert.assertEquals(1, readRow.getColumns().size());

                Assert.assertNotNull(readRow.getColumn(columnName2));
                Assert.assertEquals(column2.getValue(), readRow.getColumn(columnName2).getValue());

                return null;
            }
        });
    }

    @Test
    // TODO: ReadCqlPredicate not working
    @Ignore
    public void testReadCqlPredicate() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";

                ColumnName<String, String> columnName1 = ColumnName.valueOf("column name 1");
                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, String> column1Row1 = new StringColumn<String>(columnName1, "Hello World 1");
                Column<String, String> column1Row2 = new StringColumn<String>(columnName1, "Hello World 2");

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameTypeColumnValueTypeProvider<String, String>(
                        BasicType.STRING_UTF8);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                final CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);

                row1.setColumn(column1Row1);
                row1.setColumn(column2Row1);

                row2.setColumn(column1Row2);
                row2.setColumn(column2Row2);

                getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
                getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);

                CqlPredicate predicate = new CqlPredicate() {
                    @Override
                    public String getPredicate() {
                        return "KEY='" + rowKey1 + '\'';
                    }
                };

                @SuppressWarnings("deprecation")
                List<CassandraRow<String, String>> queryResult = getCassandraClient().read(predicate,
                        TEST_COLUMN_FAMILY, columnValueTypeProvider, context);

                Assert.assertNotNull(queryResult);
                // Assert.assertEquals(1, queryResult.size());
                // Assert.assertEquals(rowKey1, queryResult.get(0).getId());

                for (CassandraRow<String, String> row : queryResult) {
                    System.out.println("row: " + row.getKey());
                    for (Column<?, ?> column : row.getColumns()) {
                        System.out.println(column);
                    }
                }

                Assert.assertEquals(2, queryResult.get(0).getColumns().size());

                Assert.assertNotNull(queryResult.get(0).getColumn(columnName1));
                Assert.assertEquals(column1Row1.getValue(), queryResult.get(0).getColumn(columnName1).getValue());

                Assert.assertNotNull(queryResult.get(0).getColumn(columnName2));
                Assert.assertEquals(column2Row1.getValue(), queryResult.get(0).getColumn(columnName2).getValue());

                return null;
            }
        });
    }

    @Test
    public void testSearchWithIndexEqualityConditionBasicType() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final DataType<Integer> indexedColumnValueType = BasicType.INTEGER;
                final ColumnName<String, Integer> indexedColumnName = ColumnName.valueOf("indexed column name");
                SecondaryIndex secondaryIndex = new SecondaryIndex(indexedColumnName, indexedColumnValueType);

                ColumnFamily<String, String> indexedColumnFamily = new ColumnFamily<String, String>("cf_test_indexed",
                        BasicType.STRING_UTF8, BasicType.STRING_UTF8, "Test indexed column family", secondaryIndex);

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";
                String rowKey3 = "row key 3";

                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, Integer> indexedColumnRow1 = new IntegerColumn<String>(indexedColumnName, Integer
                        .valueOf(1));
                Column<String, Integer> indexedColumnRow2 = new IntegerColumn<String>(indexedColumnName, Integer
                        .valueOf(2));
                Column<String, Integer> indexedColumnRow3 = new IntegerColumn<String>(indexedColumnName, Integer
                        .valueOf(1));

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");
                Column<String, String> column2Row3 = new StringColumn<String>(columnName2, "Value 3");

                EqualityCondition<Integer> condition = EqualityCondition.equalTo(Integer.valueOf(1));

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameDataTypeButOneColumnValueTypeProvider<String, String, Integer>(
                        BasicType.STRING_UTF8, indexedColumnName, indexedColumnValueType);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);
                CassandraRow<String, String> row3 = new CassandraRow<String, String>(rowKey3);

                row1.setColumn(indexedColumnRow1);
                row1.setColumn(column2Row1);

                row2.setColumn(indexedColumnRow2);
                row2.setColumn(column2Row2);

                row3.setColumn(indexedColumnRow3);
                row3.setColumn(column2Row3);

                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row1, indexedColumnFamily, context);
                getCassandraClient().insert(row2, indexedColumnFamily, context);
                getCassandraClient().insert(row3, indexedColumnFamily, context);

                List<CassandraRow<String, String>> queryResult = getCassandraClient().searchWithIndex(
                        indexedColumnName, condition, indexedColumnFamily, columnValueTypeProvider,
                        indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());

                CassandraRow<String, String> readRowA = queryResult.get(0);
                CassandraRow<String, String> readRowB = queryResult.get(1);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));

                Assert.assertNotNull(readRowA.getColumn(indexedColumnName));
                Assert.assertNotNull(readRowB.getColumn(indexedColumnName));

                Assert.assertNotNull(readRowA.getColumn(columnName2));
                Assert.assertNotNull(readRowB.getColumn(columnName2));

                // *********************************************************************************************
                // Updates row2 to be part of the filter too

                row2.setColumn(new IntegerColumn<String>(indexedColumnName, Integer.valueOf(1)));
                getCassandraClient().insert(row2, indexedColumnFamily, context);

                queryResult = getCassandraClient().searchWithIndex(indexedColumnName, condition, indexedColumnFamily,
                        columnValueTypeProvider, indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(3, queryResult.size());

                readRowA = queryResult.get(0);
                readRowB = queryResult.get(1);
                CassandraRow<String, String> readRowC = queryResult.get(2);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey2)
                        || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey2)
                        || readRowB.getKey().equals(rowKey3));
                Assert.assertTrue(readRowC.getKey().equals(rowKey1) || readRowC.getKey().equals(rowKey2)
                        || readRowC.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));
                Assert.assertFalse(readRowA.getKey().equals(readRowC.getKey()));
                Assert.assertFalse(readRowB.getKey().equals(readRowC.getKey()));

                // *********************************************************************************************

                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testSearchWithIndexEqualityConditionEnum() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final DataType<EnumMock> indexedColumnValueType = EnumType.valueOf(EnumMock.class);
                final ColumnName<String, EnumMock> indexedColumnName = ColumnName.valueOf("indexed column name");
                SecondaryIndex secondaryIndex = new SecondaryIndex(indexedColumnName, indexedColumnValueType);

                ColumnFamily<String, String> indexedColumnFamily = new ColumnFamily<String, String>("cf_test_indexed",
                        BasicType.STRING_UTF8, BasicType.STRING_UTF8, "Test indexed column family", secondaryIndex);

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";
                String rowKey3 = "row key 3";

                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, EnumMock> indexedColumnRow1 = new EnumColumn<String, EnumMock>(indexedColumnName,
                        EnumMock.ELEMENT_1);
                Column<String, EnumMock> indexedColumnRow2 = new EnumColumn<String, EnumMock>(indexedColumnName,
                        EnumMock.ELEMENT_2);
                Column<String, EnumMock> indexedColumnRow3 = new EnumColumn<String, EnumMock>(indexedColumnName,
                        EnumMock.ELEMENT_1);

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");
                Column<String, String> column2Row3 = new StringColumn<String>(columnName2, "Value 3");

                EqualityCondition<EnumMock> condition = EqualityCondition.equalTo(EnumMock.ELEMENT_1);

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameDataTypeButOneColumnValueTypeProvider<String, String, EnumMock>(
                        BasicType.STRING_UTF8, indexedColumnName, indexedColumnValueType);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);
                CassandraRow<String, String> row3 = new CassandraRow<String, String>(rowKey3);

                row1.setColumn(indexedColumnRow1);
                row1.setColumn(column2Row1);

                row2.setColumn(indexedColumnRow2);
                row2.setColumn(column2Row2);

                row3.setColumn(indexedColumnRow3);
                row3.setColumn(column2Row3);

                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row1, indexedColumnFamily, context);
                getCassandraClient().insert(row2, indexedColumnFamily, context);
                getCassandraClient().insert(row3, indexedColumnFamily, context);

                List<CassandraRow<String, String>> queryResult = getCassandraClient().searchWithIndex(
                        indexedColumnName, condition, indexedColumnFamily, columnValueTypeProvider,
                        indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());

                CassandraRow<String, String> readRowA = queryResult.get(0);
                CassandraRow<String, String> readRowB = queryResult.get(1);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));

                Assert.assertNotNull(readRowA.getColumn(indexedColumnName));
                Assert.assertNotNull(readRowB.getColumn(indexedColumnName));

                Assert.assertNotNull(readRowA.getColumn(columnName2));
                Assert.assertNotNull(readRowB.getColumn(columnName2));

                // *********************************************************************************************
                // Updates row2 to be part of the filter too

                row2.setColumn(new EnumColumn<String, EnumMock>(indexedColumnName, EnumMock.ELEMENT_1));
                getCassandraClient().insert(row2, indexedColumnFamily, context);

                queryResult = getCassandraClient().searchWithIndex(indexedColumnName, condition, indexedColumnFamily,
                        columnValueTypeProvider, indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(3, queryResult.size());

                readRowA = queryResult.get(0);
                readRowB = queryResult.get(1);
                CassandraRow<String, String> readRowC = queryResult.get(2);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey2)
                        || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey2)
                        || readRowB.getKey().equals(rowKey3));
                Assert.assertTrue(readRowC.getKey().equals(rowKey1) || readRowC.getKey().equals(rowKey2)
                        || readRowC.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));
                Assert.assertFalse(readRowA.getKey().equals(readRowC.getKey()));
                Assert.assertFalse(readRowB.getKey().equals(readRowC.getKey()));

                // *********************************************************************************************

                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testSearchWithIndexEqualityConditionCompositeColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final ColumnName<String, CompositeValue> indexedColumnName = ColumnName.valueOf("indexed column name");
                SecondaryIndex secondaryIndex = new SecondaryIndex(indexedColumnName, COMPOSITE_TYPE);

                ColumnFamily<String, String> indexedColumnFamily = new ColumnFamily<String, String>("cf_test_indexed",
                        BasicType.STRING_UTF8, BasicType.STRING_UTF8, "Test indexed column family", secondaryIndex);

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";
                String rowKey3 = "row key 3";

                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, CompositeValue> indexedColumnRow1 = new CustomColumn<String, CompositeValue>(
                        indexedColumnName, new CompositeValue("value 1", 1), COMPOSITE_TYPE);
                Column<String, CompositeValue> indexedColumnRow2 = new CustomColumn<String, CompositeValue>(
                        indexedColumnName, new CompositeValue("value 2", 2), COMPOSITE_TYPE);
                Column<String, CompositeValue> indexedColumnRow3 = new CustomColumn<String, CompositeValue>(
                        indexedColumnName, new CompositeValue("value 1", 1), COMPOSITE_TYPE);

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");
                Column<String, String> column2Row3 = new StringColumn<String>(columnName2, "Value 3");

                EqualityCondition<CompositeValue> condition = EqualityCondition
                        .equalTo(new CompositeValue("value 1", 1));

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameDataTypeButOneColumnValueTypeProvider<String, String, CompositeValue>(
                        BasicType.STRING_UTF8, indexedColumnName, COMPOSITE_TYPE);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);
                CassandraRow<String, String> row3 = new CassandraRow<String, String>(rowKey3);

                row1.setColumn(indexedColumnRow1);
                row1.setColumn(column2Row1);

                row2.setColumn(indexedColumnRow2);
                row2.setColumn(column2Row2);

                row3.setColumn(indexedColumnRow3);
                row3.setColumn(column2Row3);

                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row1, indexedColumnFamily, context);
                getCassandraClient().insert(row2, indexedColumnFamily, context);
                getCassandraClient().insert(row3, indexedColumnFamily, context);

                List<CassandraRow<String, String>> queryResult = getCassandraClient().searchWithIndex(
                        indexedColumnName, condition, indexedColumnFamily, columnValueTypeProvider, COMPOSITE_TYPE,
                        context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());

                CassandraRow<String, String> readRowA = queryResult.get(0);
                CassandraRow<String, String> readRowB = queryResult.get(1);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));

                Assert.assertNotNull(readRowA.getColumn(indexedColumnName));
                Assert.assertNotNull(readRowB.getColumn(indexedColumnName));

                Assert.assertNotNull(readRowA.getColumn(columnName2));
                Assert.assertNotNull(readRowB.getColumn(columnName2));

                // *********************************************************************************************
                // Updates row2 to be part of the filter too

                row2.setColumn(new CustomColumn<String, CompositeValue>(indexedColumnName, new CompositeValue(
                        "value 1", 1), COMPOSITE_TYPE));
                getCassandraClient().insert(row2, indexedColumnFamily, context);

                queryResult = getCassandraClient().searchWithIndex(indexedColumnName, condition, indexedColumnFamily,
                        columnValueTypeProvider, COMPOSITE_TYPE, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(3, queryResult.size());

                readRowA = queryResult.get(0);
                readRowB = queryResult.get(1);
                CassandraRow<String, String> readRowC = queryResult.get(2);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey2)
                        || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey2)
                        || readRowB.getKey().equals(rowKey3));
                Assert.assertTrue(readRowC.getKey().equals(rowKey1) || readRowC.getKey().equals(rowKey2)
                        || readRowC.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));
                Assert.assertFalse(readRowA.getKey().equals(readRowC.getKey()));
                Assert.assertFalse(readRowB.getKey().equals(readRowC.getKey()));

                // *********************************************************************************************

                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testSearchWithIndexSetConditionBasicType() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final DataType<Integer> indexedColumnValueType = BasicType.INTEGER;
                final ColumnName<String, Integer> indexedColumnName = ColumnName.valueOf("indexed column name");
                SecondaryIndex secondaryIndex = new SecondaryIndex(indexedColumnName, indexedColumnValueType);

                ColumnFamily<String, String> indexedColumnFamily = new ColumnFamily<String, String>("cf_test_indexed",
                        BasicType.STRING_UTF8, BasicType.STRING_UTF8, "Test indexed column family", secondaryIndex);

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";
                String rowKey3 = "row key 3";

                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, Integer> indexedColumnRow1 = new IntegerColumn<String>(indexedColumnName, Integer
                        .valueOf(1));
                Column<String, Integer> indexedColumnRow2 = new IntegerColumn<String>(indexedColumnName, Integer
                        .valueOf(2));
                Column<String, Integer> indexedColumnRow3 = new IntegerColumn<String>(indexedColumnName, Integer
                        .valueOf(3));

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");
                Column<String, String> column2Row3 = new StringColumn<String>(columnName2, "Value 3");

                SetCondition<Integer> condition = SetCondition.in(Integer.valueOf(1), Integer.valueOf(3));

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameDataTypeButOneColumnValueTypeProvider<String, String, Integer>(
                        BasicType.STRING_UTF8, indexedColumnName, indexedColumnValueType);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);
                CassandraRow<String, String> row3 = new CassandraRow<String, String>(rowKey3);

                row1.setColumn(indexedColumnRow1);
                row1.setColumn(column2Row1);

                row2.setColumn(indexedColumnRow2);
                row2.setColumn(column2Row2);

                row3.setColumn(indexedColumnRow3);
                row3.setColumn(column2Row3);

                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row1, indexedColumnFamily, context);
                getCassandraClient().insert(row2, indexedColumnFamily, context);
                getCassandraClient().insert(row3, indexedColumnFamily, context);

                List<CassandraRow<String, String>> queryResult = getCassandraClient().searchWithIndex(
                        indexedColumnName, condition, indexedColumnFamily, columnValueTypeProvider,
                        indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());

                CassandraRow<String, String> readRowA = queryResult.get(0);
                CassandraRow<String, String> readRowB = queryResult.get(1);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));

                Assert.assertNotNull(readRowA.getColumn(indexedColumnName));
                Assert.assertNotNull(readRowB.getColumn(indexedColumnName));

                Assert.assertNotNull(readRowA.getColumn(columnName2));
                Assert.assertNotNull(readRowB.getColumn(columnName2));

                // *********************************************************************************************
                // Updates row2 to be part of the filter too

                row2.setColumn(new IntegerColumn<String>(indexedColumnName, Integer.valueOf(1)));
                getCassandraClient().insert(row2, indexedColumnFamily, context);

                queryResult = getCassandraClient().searchWithIndex(indexedColumnName, condition, indexedColumnFamily,
                        columnValueTypeProvider, indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(3, queryResult.size());

                readRowA = queryResult.get(0);
                readRowB = queryResult.get(1);
                CassandraRow<String, String> readRowC = queryResult.get(2);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey2)
                        || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey2)
                        || readRowB.getKey().equals(rowKey3));
                Assert.assertTrue(readRowC.getKey().equals(rowKey1) || readRowC.getKey().equals(rowKey2)
                        || readRowC.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));
                Assert.assertFalse(readRowA.getKey().equals(readRowC.getKey()));
                Assert.assertFalse(readRowB.getKey().equals(readRowC.getKey()));

                // *********************************************************************************************

                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testSearchWithIndexSetConditionEnum() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final DataType<EnumMock> indexedColumnValueType = EnumType.valueOf(EnumMock.class);
                final ColumnName<String, EnumMock> indexedColumnName = ColumnName.valueOf("indexed column name");
                SecondaryIndex secondaryIndex = new SecondaryIndex(indexedColumnName, indexedColumnValueType);

                ColumnFamily<String, String> indexedColumnFamily = new ColumnFamily<String, String>("cf_test_indexed",
                        BasicType.STRING_UTF8, BasicType.STRING_UTF8, "Test indexed column family", secondaryIndex);

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";
                String rowKey3 = "row key 3";

                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, EnumMock> indexedColumnRow1 = new EnumColumn<String, EnumMock>(indexedColumnName,
                        EnumMock.ELEMENT_1);
                Column<String, EnumMock> indexedColumnRow2 = new EnumColumn<String, EnumMock>(indexedColumnName,
                        EnumMock.ELEMENT_2);
                Column<String, EnumMock> indexedColumnRow3 = new EnumColumn<String, EnumMock>(indexedColumnName,
                        EnumMock.ELEMENT_3);

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");
                Column<String, String> column2Row3 = new StringColumn<String>(columnName2, "Value 3");

                // NOT_IN is used because it is translated to IN by inverting the values.
                SetCondition<EnumMock> condition = SetCondition.notIn(EnumMock.ELEMENT_2);

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameDataTypeButOneColumnValueTypeProvider<String, String, EnumMock>(
                        BasicType.STRING_UTF8, indexedColumnName, indexedColumnValueType);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);
                CassandraRow<String, String> row3 = new CassandraRow<String, String>(rowKey3);

                row1.setColumn(indexedColumnRow1);
                row1.setColumn(column2Row1);

                row2.setColumn(indexedColumnRow2);
                row2.setColumn(column2Row2);

                row3.setColumn(indexedColumnRow3);
                row3.setColumn(column2Row3);

                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row1, indexedColumnFamily, context);
                getCassandraClient().insert(row2, indexedColumnFamily, context);
                getCassandraClient().insert(row3, indexedColumnFamily, context);

                List<CassandraRow<String, String>> queryResult = getCassandraClient().searchWithIndex(
                        indexedColumnName, condition, EnumMock.class, indexedColumnFamily, columnValueTypeProvider,
                        indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());

                CassandraRow<String, String> readRowA = queryResult.get(0);
                CassandraRow<String, String> readRowB = queryResult.get(1);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));

                Assert.assertNotNull(readRowA.getColumn(indexedColumnName));
                Assert.assertNotNull(readRowB.getColumn(indexedColumnName));

                Assert.assertNotNull(readRowA.getColumn(columnName2));
                Assert.assertNotNull(readRowB.getColumn(columnName2));

                // *********************************************************************************************
                // Updates row2 to be part of the filter too

                row2.setColumn(new EnumColumn<String, EnumMock>(indexedColumnName, EnumMock.ELEMENT_1));
                getCassandraClient().insert(row2, indexedColumnFamily, context);

                queryResult = getCassandraClient().searchWithIndex(indexedColumnName, condition, EnumMock.class,
                        indexedColumnFamily, columnValueTypeProvider, indexedColumnValueType, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(3, queryResult.size());

                readRowA = queryResult.get(0);
                readRowB = queryResult.get(1);
                CassandraRow<String, String> readRowC = queryResult.get(2);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey2)
                        || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey2)
                        || readRowB.getKey().equals(rowKey3));
                Assert.assertTrue(readRowC.getKey().equals(rowKey1) || readRowC.getKey().equals(rowKey2)
                        || readRowC.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));
                Assert.assertFalse(readRowA.getKey().equals(readRowC.getKey()));
                Assert.assertFalse(readRowB.getKey().equals(readRowC.getKey()));

                // *********************************************************************************************

                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    @Test
    public void testSearchWithIndexSetConditionCompositeColumn() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {

                final ColumnName<String, CompositeValue> indexedColumnName = ColumnName.valueOf("indexed column name");
                SecondaryIndex secondaryIndex = new SecondaryIndex(indexedColumnName, COMPOSITE_TYPE);

                ColumnFamily<String, String> indexedColumnFamily = new ColumnFamily<String, String>("cf_test_indexed",
                        BasicType.STRING_UTF8, BasicType.STRING_UTF8, "Test indexed column family", secondaryIndex);

                String rowKey1 = "row key 1";
                String rowKey2 = "row key 2";
                String rowKey3 = "row key 3";

                ColumnName<String, String> columnName2 = ColumnName.valueOf("column name 2");

                Column<String, CompositeValue> indexedColumnRow1 = new CustomColumn<String, CompositeValue>(
                        indexedColumnName, new CompositeValue("value 1", 1), COMPOSITE_TYPE);
                Column<String, CompositeValue> indexedColumnRow2 = new CustomColumn<String, CompositeValue>(
                        indexedColumnName, new CompositeValue("value 2", 2), COMPOSITE_TYPE);
                Column<String, CompositeValue> indexedColumnRow3 = new CustomColumn<String, CompositeValue>(
                        indexedColumnName, new CompositeValue("value 3", 3), COMPOSITE_TYPE);

                Column<String, String> column2Row1 = new StringColumn<String>(columnName2, "Value 1");
                Column<String, String> column2Row2 = new StringColumn<String>(columnName2, "Value 2");
                Column<String, String> column2Row3 = new StringColumn<String>(columnName2, "Value 3");

                SetCondition<CompositeValue> condition = SetCondition.in(new CompositeValue("value 1", 1),
                        new CompositeValue("value 3", 3));

                ColumnValueTypeProvider<String> columnValueTypeProvider = new SameDataTypeButOneColumnValueTypeProvider<String, String, CompositeValue>(
                        BasicType.STRING_UTF8, indexedColumnName, COMPOSITE_TYPE);

                CassandraRow<String, String> row1 = new CassandraRow<String, String>(rowKey1);
                CassandraRow<String, String> row2 = new CassandraRow<String, String>(rowKey2);
                CassandraRow<String, String> row3 = new CassandraRow<String, String>(rowKey3);

                row1.setColumn(indexedColumnRow1);
                row1.setColumn(column2Row1);

                row2.setColumn(indexedColumnRow2);
                row2.setColumn(column2Row2);

                row3.setColumn(indexedColumnRow3);
                row3.setColumn(column2Row3);

                getCassandraClient().createColumnFamily(indexedColumnFamily, context.getKeyspace(), context);
                getCassandraClient().insert(row1, indexedColumnFamily, context);
                getCassandraClient().insert(row2, indexedColumnFamily, context);
                getCassandraClient().insert(row3, indexedColumnFamily, context);

                List<CassandraRow<String, String>> queryResult = getCassandraClient().searchWithIndex(
                        indexedColumnName, condition, indexedColumnFamily, columnValueTypeProvider, COMPOSITE_TYPE,
                        context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(2, queryResult.size());

                CassandraRow<String, String> readRowA = queryResult.get(0);
                CassandraRow<String, String> readRowB = queryResult.get(1);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));

                Assert.assertNotNull(readRowA.getColumn(indexedColumnName));
                Assert.assertNotNull(readRowB.getColumn(indexedColumnName));

                Assert.assertNotNull(readRowA.getColumn(columnName2));
                Assert.assertNotNull(readRowB.getColumn(columnName2));

                // *********************************************************************************************
                // Updates row2 to be part of the filter too

                row2.setColumn(new CustomColumn<String, CompositeValue>(indexedColumnName, new CompositeValue(
                        "value 1", 1), COMPOSITE_TYPE));
                getCassandraClient().insert(row2, indexedColumnFamily, context);

                queryResult = getCassandraClient().searchWithIndex(indexedColumnName, condition, indexedColumnFamily,
                        columnValueTypeProvider, COMPOSITE_TYPE, context);

                Assert.assertNotNull(queryResult);
                Assert.assertEquals(3, queryResult.size());

                readRowA = queryResult.get(0);
                readRowB = queryResult.get(1);
                CassandraRow<String, String> readRowC = queryResult.get(2);

                Assert.assertTrue(readRowA.getKey().equals(rowKey1) || readRowA.getKey().equals(rowKey2)
                        || readRowA.getKey().equals(rowKey3));
                Assert.assertTrue(readRowB.getKey().equals(rowKey1) || readRowB.getKey().equals(rowKey2)
                        || readRowB.getKey().equals(rowKey3));
                Assert.assertTrue(readRowC.getKey().equals(rowKey1) || readRowC.getKey().equals(rowKey2)
                        || readRowC.getKey().equals(rowKey3));
                Assert.assertFalse(readRowA.getKey().equals(readRowB.getKey()));
                Assert.assertFalse(readRowA.getKey().equals(readRowC.getKey()));
                Assert.assertFalse(readRowB.getKey().equals(readRowC.getKey()));

                // *********************************************************************************************

                getCassandraClient().dropColumnFamily(indexedColumnFamily, context.getKeyspace(), context);

                return null;
            }
        });
    }

    private static enum EnumMock {
        ELEMENT_1, ELEMENT_2, ELEMENT_3
    }

    private static class CompositeValue implements Serializable, Comparable<CompositeValue> {
        private static final long serialVersionUID = 1L;

        private String strAttr;
        private int intAttr;

        public CompositeValue(String strAttr, int intAttr) {
            this.strAttr = strAttr;
            this.intAttr = intAttr;
        }

        public String getStrAttr() {
            return this.strAttr;
        }

        public int getIntAttr() {
            return this.intAttr;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.intAttr;
            result = prime * result + ((this.strAttr == null) ? 0 : this.strAttr.hashCode());
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

            CompositeValue other = (CompositeValue) obj;

            if (this.intAttr != other.intAttr) {
                return false;
            }

            if (this.strAttr == null) {
                if (other.strAttr != null) {
                    return false;
                }
            }
            else if (!this.strAttr.equals(other.strAttr)) {
                return false;
            }

            return true;
        }

        @Override
        public int compareTo(CompositeValue other) {
            int comparison = 0;

            if (other.strAttr != null) {
                comparison = this.strAttr.compareTo(other.strAttr);
            }

            if (comparison == 0) {
                comparison = Integer.valueOf(this.intAttr).compareTo(Integer.valueOf(other.intAttr));
            }

            return comparison;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(this, Property.valueOf("strAttr", this.strAttr),
                    Property.valueOf("intAttr", Integer.valueOf(this.intAttr)));
        }
    }

    private static class CompositeValueSerializer implements CompositeTypeSerializer<CompositeValue> {

        @Override
        public List<Component<CompositeValue, ?>> serialize(CompositeValue compositeValue) {
            List<Component<CompositeValue, ?>> components = new ArrayList<Component<CompositeValue, ?>>();
            components.add(new Component<CompositeValue, String>(BasicType.STRING_UTF8, compositeValue.getStrAttr()));
            components.add(new Component<CompositeValue, Integer>(BasicType.INTEGER, Integer.valueOf(compositeValue
                    .getIntAttr())));
            return components;
        }

        @Override
        public CompositeValue deserialize(List<Component<CompositeValue, ?>> components) {
            String strAttr = (String) components.get(0).getValue();
            Integer intAttr = (Integer) components.get(1).getValue();
            return new CompositeValue(strAttr, intAttr.intValue());
        }
    }

    private static class SameDataTypeButOneColumnValueTypeProvider<C extends Serializable & Comparable<C>, D, E>
            implements ColumnValueTypeProvider<C> {

        /*
         * DataTypeProvider when all columns are of the same value type except one
         */

        private final DataType<D> defaultColumnValueType;
        private final ColumnName<C, E> specificColumn;
        private final DataType<E> specificType;

        public SameDataTypeButOneColumnValueTypeProvider(DataType<D> defaultColumnValueType,
                ColumnName<C, E> specificColumn, DataType<E> specificType) {
            this.defaultColumnValueType = defaultColumnValueType;
            this.specificColumn = specificColumn;
            this.specificType = specificType;
        }

        @Override
        public <I> void getColumnValueType(ColumnName<C, ?> columnName, ColumnValueTypeHandler<C, I> dataTypeHandler,
                I handlerInput) throws PersistenceException {
            if (this.specificColumn.equals(columnName)) {
                dataTypeHandler.handle(this.specificColumn, this.specificType, handlerInput);
            }
            // All other columns are of the same type
            @SuppressWarnings("unchecked")
            ColumnName<C, D> typedColumnName = (ColumnName<C, D>) columnName;
            dataTypeHandler.handle(typedColumnName, this.defaultColumnValueType, handlerInput);
        }
    }
}
