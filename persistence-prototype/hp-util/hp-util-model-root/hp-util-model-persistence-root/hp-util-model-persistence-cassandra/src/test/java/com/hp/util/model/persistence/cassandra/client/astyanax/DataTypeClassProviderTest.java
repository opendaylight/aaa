/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class DataTypeClassProviderTest {

    @Test
    public void testGetDataTypeClass() {
        DataTypeClassProvider dataTypeClassProvider = new DataTypeClassProvider();

        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.VOID), "ByteType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.BYTE), "ByteType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.BYTE_ARRAY), "BytesType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.STRING_ASCII), "AsciiType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.STRING_UTF8), "UTF8Type");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.INTEGER), "IntegerType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.LONG), "LongType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.UUID), "UUIDType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.TIME_UUID), "TimeUUIDType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.DATE), "DateType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.BOOLEAN), "BooleanType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.FLOAT), "FloatType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.DOUBLE), "DoubleType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.DECIMAL), "DecimalType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.BIG_INTEGER), "BigIntegerType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.CHAR), "CharType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.SHORT), "ShortType");
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(BasicType.COUNTER_COLUMN), "CounterColumnType");

        EnumType<EnumMock> enumType = EnumType.valueOf(EnumMock.class);
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(enumType), "UTF8Type");

        @SuppressWarnings("unchecked")
        CompositeTypeSerializer<CompositeValue> serializer = EasyMock.createMock(CompositeTypeSerializer.class);
        CompositeType<CompositeValue> compositeType = new CompositeType<CompositeValue>(serializer, BasicType.INTEGER,
                BasicType.LONG);
        Assert.assertEquals(dataTypeClassProvider.getDataTypeClass(compositeType),
                "CompositeType(IntegerType,LongType)");
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
