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
public class DataTypeSerializerProviderTest {

    @Test
    public void testGetDataTypeClass() {
        DataTypeSerializerProvider dataTypeSerializerProvider = new DataTypeSerializerProvider();

        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.VOID));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.BYTE));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.BYTE_ARRAY));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.STRING_ASCII));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.STRING_UTF8));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.INTEGER));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.LONG));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.UUID));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.TIME_UUID));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.DATE));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.BOOLEAN));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.FLOAT));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.DOUBLE));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.DECIMAL));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.BIG_INTEGER));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.CHAR));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.SHORT));
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(BasicType.COUNTER_COLUMN));

        EnumType<EnumMock> enumType = EnumType.valueOf(EnumMock.class);
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(enumType));

        @SuppressWarnings("unchecked")
        CompositeTypeSerializer<CompositeValue> serializer = EasyMock.createMock(CompositeTypeSerializer.class);
        CompositeType<CompositeValue> compositeType = new CompositeType<CompositeValue>(serializer, BasicType.INTEGER,
                BasicType.LONG);
        Assert.assertNotNull(dataTypeSerializerProvider.getSerializer(compositeType));
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
