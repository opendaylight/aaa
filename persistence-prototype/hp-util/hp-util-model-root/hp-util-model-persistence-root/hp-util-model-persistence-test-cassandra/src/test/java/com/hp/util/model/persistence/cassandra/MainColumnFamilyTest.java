/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.model.AbstractIdentifiable;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerializableValueType;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.DateColumn;
import com.hp.util.model.persistence.cassandra.column.DynamicColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.LongColumn;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;
import com.hp.util.model.persistence.cassandra.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class MainColumnFamilyTest {

    private static final ColumnName<String, String> ATTR_STRING = ColumnName.valueOf("attribute_string");
    private static final ColumnName<String, Boolean> ATTR_BOOLEAN = ColumnName.valueOf("attribute_boolean");
    private static final ColumnName<String, Long> ATTR_LONG = ColumnName.valueOf("attribute_long");
    private static final ColumnName<String, Date> ATTR_DATE = ColumnName.valueOf("attribute_date");
    private static final ColumnName<String, EnumMock> ATTR_ENUM = ColumnName.valueOf("attribute_enum");

    private static final DataType<EnumMock> ENUM_TYPE = EnumType.valueOf(EnumMock.class);

    private MainColumnFamily<MyKey, String, RegularDto> mainColumnFamily;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CassandraTestUtil.beforeTestClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        CassandraTestUtil.afterTestClass();
    }

    @Before
    public void beforeTest() throws Exception {
        Assume.assumeTrue(CassandraTestUtil.isIntegrationTestSupported());

        CassandraTestUtil.beforeTest();

        DynamicColumnValueTypeProvider<String> columnValueTypeProvider = new DynamicColumnValueTypeProvider<String>();
        columnValueTypeProvider.registerColumnValueType(ATTR_STRING, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(ATTR_BOOLEAN, BasicType.BOOLEAN);
        columnValueTypeProvider.registerColumnValueType(ATTR_LONG, BasicType.LONG);
        columnValueTypeProvider.registerColumnValueType(ATTR_DATE, BasicType.DATE);
        columnValueTypeProvider.registerColumnValueType(ATTR_ENUM, ENUM_TYPE);

        this.mainColumnFamily = new MainColumnFamily<MyKey, String, RegularDto>("cf_regular_dao_mapped_key",
                BasicType.STRING_UTF8, "Regular DAO main column family", columnValueTypeProvider, new KeyConverter(),
                new Converter());

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> definition : MainColumnFamilyTest.this.mainColumnFamily.getColumnFamilies()) {
                    context.getCassandraClient().createColumnFamily(definition, context.getKeyspace(), context);
                }
                for (ColumnFamily<?, ?> definition : MainColumnFamilyTest.this.mainColumnFamily.getColumnFamilies()) {
                    context.getCassandraClient().createColumnFamily(definition, context.getKeyspace(), context);
                }
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
                for (ColumnFamily<?, ?> columnfamily : MainColumnFamilyTest.this.mainColumnFamily.getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(columnfamily, context.getKeyspace(), context);
                }
                for (ColumnFamily<?, ?> definition : MainColumnFamilyTest.this.mainColumnFamily.getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(definition, context.getKeyspace(), context);
                }
                return null;
            }
        });

        CassandraTestUtil.afterTest();
    }

    @Test
    public void testInsertExistReadDelete() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testInsertExistReadDelete(context);
                return null;
            }
        });
    }

    @Test
    public void testReadIds() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testReadIds(context);
                return null;
            }
        });
    }

    private void testInsertExistReadDelete(CassandraContext<Astyanax> context) throws PersistenceException {
        Id<RegularDto, MyKey> id = Id.valueOf(MyKey.valueOf("id"));
        RegularDto expected = new RegularDto(id);

        expected.setAttributeBoolean(false);
        expected.setAttributeDate(Date.currentTime());
        expected.setAttributeEnum(EnumMock.ELEMENT_2);
        expected.setAttributeLong(Long.valueOf(2));
        expected.setAttributeString("String value");

        Assert.assertNull(this.mainColumnFamily.read(id, context));
        Assert.assertFalse(this.mainColumnFamily.exist(id, context));

        // Insert

        this.mainColumnFamily.insert(expected, context);

        // Exist

        Assert.assertTrue(this.mainColumnFamily.exist(id, context));

        // Read

        CassandraRow<MyKey, String> row = this.mainColumnFamily.read(id, context);
        RegularDto actual = this.mainColumnFamily.convert(row);
        assertEqualState(expected, actual);

        // Modify/Insert/Read
        expected.setAttributeBoolean(true);
        expected.setAttributeDate(Date.currentTime());
        expected.setAttributeEnum(EnumMock.ELEMENT_3);
        expected.setAttributeLong(Long.valueOf(3));
        expected.setAttributeString("new value");

        this.mainColumnFamily.insert(expected, context);

        row = this.mainColumnFamily.read(id, context);
        actual = this.mainColumnFamily.convert(row);
        assertEqualState(expected, actual);

        // Delete
        this.mainColumnFamily.delete(id, context);
        Assert.assertNull(this.mainColumnFamily.read(id, context));
    }

    private void testReadIds(CassandraContext<Astyanax> context) throws PersistenceException {
        Id<RegularDto, MyKey> id1 = Id.valueOf(MyKey.valueOf("id 1"));
        Id<RegularDto, MyKey> id2 = Id.valueOf(MyKey.valueOf("id 2"));
        List<MyKey> ids = Arrays.asList(id1.getValue(), id2.getValue());

        RegularDto expected1 = new RegularDto(id1);
        RegularDto expected2 = new RegularDto(id2);

        expected1.setAttributeBoolean(false);
        expected1.setAttributeDate(Date.currentTime());
        expected1.setAttributeEnum(EnumMock.ELEMENT_2);
        expected1.setAttributeLong(Long.valueOf(2));
        expected1.setAttributeString("String value");

        expected2.setAttributeBoolean(true);
        expected2.setAttributeDate(Date.currentTime());
        expected2.setAttributeEnum(EnumMock.ELEMENT_3);
        expected2.setAttributeLong(Long.valueOf(3));
        expected2.setAttributeString("new value");

        List<CassandraRow<MyKey, String>> actual = this.mainColumnFamily.read(ids, context);
        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.isEmpty());

        this.mainColumnFamily.insert(expected1, context);

        actual = this.mainColumnFamily.read(ids, context);
        Assert.assertNotNull(actual);
        Assert.assertEquals(1, actual.size());
        assertEqualState(expected1, this.mainColumnFamily.convert(actual.get(0)));

        this.mainColumnFamily.insert(expected2, context);

        actual = this.mainColumnFamily.read(ids, context);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, actual.size());
        assertEqualState(expected1, this.mainColumnFamily.convert(actual.get(0)));
        assertEqualState(expected2, this.mainColumnFamily.convert(actual.get(1)));
    }

    private static void assertEqualState(RegularDto a, RegularDto b) {
        Assert.assertEquals(a.getId(), b.getId());
        Assert.assertEquals(a.getAttributeString(), b.getAttributeString());
        Assert.assertEquals(a.getAttributeBoolean(), b.getAttributeBoolean());
        Assert.assertEquals(a.getAttributeLong(), b.getAttributeLong());
        Assert.assertEquals(a.getAttributeDate(), b.getAttributeDate());
        Assert.assertEquals(a.getAttributeEnum(), b.getAttributeEnum());
    }

    private static final class MyKey extends SerializableValueType<String> {
        private static final long serialVersionUID = 1L;

        private MyKey(String value) throws NullPointerException {
            super(value);
        }

        public static MyKey valueOf(String value) {
            return new MyKey(value);
        }
    }

    private static class RegularDto extends AbstractIdentifiable<RegularDto, MyKey> {

        private String attributeString;
        private boolean attributeBoolean;
        private Long attributeLong;
        private Date attributeDate;
        private EnumMock attributeEnum;

        public RegularDto(Id<RegularDto, MyKey> id) {
            super(id);
        }

        public String getAttributeString() {
            return this.attributeString;
        }

        public void setAttributeString(String attributeString) {
            this.attributeString = attributeString;
        }

        public boolean getAttributeBoolean() {
            return this.attributeBoolean;
        }

        public void setAttributeBoolean(boolean attributeBoolean) {
            this.attributeBoolean = attributeBoolean;
        }

        public Long getAttributeLong() {
            return this.attributeLong;
        }

        public void setAttributeLong(Long attributeLong) {
            this.attributeLong = attributeLong;
        }

        public Date getAttributeDate() {
            return this.attributeDate;
        }

        public void setAttributeDate(Date attributeDate) {
            this.attributeDate = attributeDate;
        }

        public EnumMock getAttributeEnum() {
            return this.attributeEnum;
        }

        public void setAttributeEnum(EnumMock attributeEnum) {
            this.attributeEnum = attributeEnum;
        }
    }

    private static class KeyConverter implements BidirectionalConverter<MyKey, String> {

        @Override
        public String convert(MyKey source) {
            return source.getValue();
        }

        @Override
        public MyKey restore(String target) throws IllegalArgumentException {
            return MyKey.valueOf(target);
        }
    }

    private static class Converter implements BidirectionalConverter<RegularDto, CassandraRow<MyKey, String>> {

        @Override
        public CassandraRow<MyKey, String> convert(RegularDto source) {
            CassandraRow<MyKey, String> row = new CassandraRow<MyKey, String>(source.getId().getValue());
            row.setColumn(new StringColumn<String>(ATTR_STRING, source.getAttributeString()));
            row.setColumn(new BooleanColumn<String>(ATTR_BOOLEAN, Boolean.valueOf(source.getAttributeBoolean())));
            row.setColumn(new LongColumn<String>(ATTR_LONG, source.getAttributeLong()));
            row.setColumn(new DateColumn<String>(ATTR_DATE, source.getAttributeDate()));
            row.setColumn(new EnumColumn<String, EnumMock>(ATTR_ENUM, source.getAttributeEnum()));

            return row;
        }

        @Override
        public RegularDto restore(CassandraRow<MyKey, String> row) throws IllegalArgumentException {
            RegularDto dto = new RegularDto(Id.<RegularDto, MyKey> valueOf(row.getKey()));
            dto.setAttributeBoolean(((Boolean) row.getColumn(ATTR_BOOLEAN).getValue()).booleanValue());
            dto.setAttributeDate(((Date) row.getColumn(ATTR_DATE).getValue()));
            dto.setAttributeLong(((Long) row.getColumn(ATTR_LONG).getValue()));
            dto.setAttributeString(((String) row.getColumn(ATTR_STRING).getValue()));
            dto.setAttributeEnum(((EnumMock) row.getColumn(ATTR_ENUM).getValue()));
            return dto;
        }
    }
}
