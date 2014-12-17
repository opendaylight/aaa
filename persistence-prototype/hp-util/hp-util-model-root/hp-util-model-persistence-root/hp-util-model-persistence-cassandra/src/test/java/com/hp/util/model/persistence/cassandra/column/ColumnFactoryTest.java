/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ColumnFactoryTest {

    private static <C extends Serializable & Comparable<C>, D> void testCreate(ColumnName<C, D> name, D value,
            DataType<D> columnValueType, Class<?> expectedColumnClass) {
        Column<C, D> column = ColumnFactory.getInstance().create(name, value, columnValueType);
        Assert.assertNotNull(column);
        Assert.assertTrue(expectedColumnClass.isInstance(column));
        Assert.assertEquals(name, column.getName());
        Assert.assertEquals(value, column.getValue());
    }

    @Test
    public void testCreateVoidColumn() {
        ColumnName<String, Void> name = ColumnName.valueOf("name");
        Void value = null;
        testCreate(name, value, BasicType.VOID, VoidColumn.class);
    }

    @Test
    public void testCreateByteColumn() {
        ColumnName<String, Byte> name = ColumnName.valueOf("name");
        Byte value = Byte.valueOf((byte) 1);
        testCreate(name, value, BasicType.BYTE, ByteColumn.class);
        testCreate(name, null, BasicType.BYTE, ByteColumn.class);
    }

    @Test
    public void testCreateByteArrayColumn() {
        ColumnName<String, byte[]> name = ColumnName.valueOf("name");
        byte[] value = new byte[] { (byte) 1 };
        testCreate(name, value, BasicType.BYTE_ARRAY, ByteArrayColumn.class);
        testCreate(name, null, BasicType.BYTE_ARRAY, ByteArrayColumn.class);
    }

    @Test
    public void testCreateAsciiStringColumn() {
        ColumnName<String, String> name = ColumnName.valueOf("name");
        String value = "value";
        testCreate(name, value, BasicType.STRING_ASCII, StringColumn.class);
        testCreate(name, null, BasicType.STRING_ASCII, StringColumn.class);
    }

    @Test
    public void testCreateUtf8StringColumn() {
        ColumnName<String, String> name = ColumnName.valueOf("name");
        String value = "value";
        testCreate(name, value, BasicType.STRING_UTF8, StringColumn.class);
        testCreate(name, null, BasicType.STRING_UTF8, StringColumn.class);
    }

    @Test
    public void testCreateIntegerColumn() {
        ColumnName<String, Integer> name = ColumnName.valueOf("name");
        Integer value = Integer.valueOf(1);
        testCreate(name, value, BasicType.INTEGER, IntegerColumn.class);
        testCreate(name, null, BasicType.INTEGER, IntegerColumn.class);
    }

    @Test
    public void testCreateLongColumn() {
        ColumnName<String, Long> name = ColumnName.valueOf("name");
        Long value = Long.valueOf(1);
        testCreate(name, value, BasicType.LONG, LongColumn.class);
        testCreate(name, null, BasicType.LONG, LongColumn.class);
    }

    @Test
    public void testCreateDateColumn() {
        ColumnName<String, Date> name = ColumnName.valueOf("name");
        Date value = Date.currentTime();
        testCreate(name, value, BasicType.DATE, DateColumn.class);
        testCreate(name, null, BasicType.DATE, DateColumn.class);
    }

    @Test
    public void testCreateBooleanColumn() {
        ColumnName<String, Boolean> name = ColumnName.valueOf("name");
        Boolean value = Boolean.TRUE;
        testCreate(name, value, BasicType.BOOLEAN, BooleanColumn.class);
        testCreate(name, null, BasicType.BOOLEAN, BooleanColumn.class);
    }

    @Test
    public void testCreateFloatColumn() {
        ColumnName<String, Float> name = ColumnName.valueOf("name");
        Float value = Float.valueOf(1);
        testCreate(name, value, BasicType.FLOAT, FloatColumn.class);
        testCreate(name, null, BasicType.FLOAT, FloatColumn.class);
    }

    @Test
    public void testCreateDoubleColumn() {
        ColumnName<String, Double> name = ColumnName.valueOf("name");
        Double value = Double.valueOf(1);
        testCreate(name, value, BasicType.DOUBLE, DoubleColumn.class);
        testCreate(name, null, BasicType.DOUBLE, DoubleColumn.class);
    }

    @Test
    public void testCreateEnumColumn() {
        ColumnName<String, EnumMock> name = ColumnName.valueOf("name");
        EnumMock value = EnumMock.ELEMENT_1;
        testCreate(name, value, EnumType.valueOf(EnumMock.class), EnumColumn.class);
        testCreate(name, null, EnumType.valueOf(EnumMock.class), EnumColumn.class);
    }

    @Test
    public void testCreateCustomColumn() {
        ColumnName<String, CompositeValue> name = ColumnName.valueOf("name");
        CompositeValue value = new CompositeValue();
        @SuppressWarnings("unchecked")
        CompositeTypeSerializer<CompositeValue> serializer = EasyMock.createMock(CompositeTypeSerializer.class);
        CompositeType<CompositeValue> type = new CompositeType<CompositeValue>(serializer, BasicType.INTEGER,
                BasicType.LONG);
        testCreate(name, value, type, CustomColumn.class);
        testCreate(name, null, type, CustomColumn.class);
    }

    private static enum EnumMock {
        ELEMENT_1
    }

    private static class CompositeValue {
        @SuppressWarnings("unused")
        private int attr1;
        @SuppressWarnings("unused")
        private long attr2;
    }
}
