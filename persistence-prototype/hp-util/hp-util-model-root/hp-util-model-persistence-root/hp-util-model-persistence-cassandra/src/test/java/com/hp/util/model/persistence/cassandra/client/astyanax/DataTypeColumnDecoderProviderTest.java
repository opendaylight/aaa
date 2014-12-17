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
public class DataTypeColumnDecoderProviderTest {

    @Test
    public void testGetDataTypeClass() {
        DataTypeColumnDecoderProvider dataTypeColumnDecoderProvider = new DataTypeColumnDecoderProvider(
                new DataTypeSerializerProvider());

        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.VOID));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.BYTE));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.BYTE_ARRAY));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.STRING_ASCII));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.STRING_UTF8));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.INTEGER));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.LONG));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.UUID));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.TIME_UUID));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.DATE));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.BOOLEAN));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.FLOAT));
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.DOUBLE));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.DECIMAL));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.BIG_INTEGER));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.CHAR));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.SHORT));
        // Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(BasicType.COUNTER_COLUMN));

        EnumType<EnumMock> enumType = EnumType.valueOf(EnumMock.class);
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(enumType));

        @SuppressWarnings("unchecked")
        CompositeTypeSerializer<CompositeValue> serializer = EasyMock.createMock(CompositeTypeSerializer.class);
        CompositeType<CompositeValue> compositeType = new CompositeType<CompositeValue>(serializer, BasicType.INTEGER,
                BasicType.LONG);
        Assert.assertNotNull(dataTypeColumnDecoderProvider.getColumnDecoder(compositeType));
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
