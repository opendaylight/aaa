/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.DataTypeCommandVisitor;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;
import com.netflix.astyanax.Serializer;
import com.netflix.astyanax.query.IndexQuery;
import com.netflix.astyanax.query.IndexValueExpression;

/**
 * @author Fabiel Zuniga
 */
class DataTypeIndexQueryProvider {

    private final DataTypeSerializerProvider serializerProvider;
    private final Map<BasicType<?>, IndexValueExpressionStrategy<?, ?, ?>> basicTypeStrategies;

    @SuppressWarnings("rawtypes")
    private final DataTypeVisitor dataTypeVisitor = new DataTypeVisitor();

    @SuppressWarnings("rawtypes")
    public DataTypeIndexQueryProvider(DataTypeSerializerProvider serializerProvider) {
        this.serializerProvider = serializerProvider;
        this.basicTypeStrategies = new HashMap<BasicType<?>, IndexValueExpressionStrategy<?, ?, ?>>();

        /*
         * TODO: Complete strategies
         */

        this.basicTypeStrategies.put(BasicType.VOID, null);
        this.basicTypeStrategies.put(BasicType.BYTE, new ByteIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.BYTE_ARRAY, new ByteArrayIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.STRING_ASCII, new StringIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.STRING_UTF8, new StringIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.INTEGER, new IntegerIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.LONG, new LongIndexValueExpressionStrategy());
        // this.basicTypeDecoders.put(BasicType.UUID, null);
        // this.basicTypeDecoders.put(BasicType.TIME_UUID, null);
        this.basicTypeStrategies.put(BasicType.DATE, new DateIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.BOOLEAN, new BooleanIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.FLOAT, new FloatIndexValueExpressionStrategy());
        this.basicTypeStrategies.put(BasicType.DOUBLE, new DoubleIndexValueExpressionStrategy());
        // this.basicTypeDecoders.put(BasicType.DECIMAL, null);
        // this.basicTypeDecoders.put(BasicType.BIG_INTEGER, null);
        // this.basicTypeDecoders.put(BasicType.CHAR, null);
        // this.basicTypeDecoders.put(BasicType.SHORT, null);
        // this.basicTypeDecoders.put(BasicType.COUNTER_COLUMN, null);
    }

    public <K extends Serializable, C extends Serializable & Comparable<C>, D> IndexValueExpressionStrategy<K, C, D> getStrategy(
            DataType<D> columnValueType) {
        @SuppressWarnings("unchecked")
        DataTypeCommandVisitor<D, IndexValueExpressionStrategy<K, C, D>, Void> commandVisitor = this.dataTypeVisitor;
        return columnValueType.accept(commandVisitor, null);
    }

    @SuppressWarnings("unchecked")
    private <K extends Serializable, C extends Serializable & Comparable<C>, D> IndexValueExpressionStrategy<K, C, D> getBasicTypeStrategy(
            BasicType<D> columnValueType) {
        IndexValueExpressionStrategy<?, ?, ?> strategy = this.basicTypeStrategies.get(columnValueType);
        if (strategy == null) {
            throw new RuntimeException("Unknown basic type " + columnValueType);
        }
        return (IndexValueExpressionStrategy<K, C, D>) strategy;
    }

    private <K extends Serializable, C extends Serializable & Comparable<C>, D> IndexValueExpressionStrategy<K, C, D> getCompositeTypeStrategy(
            CompositeType<D> columnValueType) {
        Serializer<D> serializer = this.serializerProvider.getSerializer(columnValueType);
        return new CustomIndexValueExpressionStrategy<K, C, D>(serializer);
    }

    private class DataTypeVisitor<K extends Serializable, C extends Serializable & Comparable<C>, D> implements
            DataTypeCommandVisitor<D, IndexValueExpressionStrategy<K, C, D>, Void> {

        @Override
        public IndexValueExpressionStrategy<K, C, D> visit(BasicType<D> dataType, Void input) {
            return getBasicTypeStrategy(dataType);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public IndexValueExpressionStrategy<K, C, D> visit(EnumType<D> dataType, Void input) {
            return new EnumIndexValueExpressionStrategy();
        }

        @Override
        public IndexValueExpressionStrategy<K, C, D> visit(CompositeType<D> dataType, Void input) {
            return getCompositeTypeStrategy(dataType);
        }
    }

    public static final class BooleanIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Boolean> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Boolean value) {
            return indexValueExpression.value(value.booleanValue());
        }
    }

    public static final class ByteArrayIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, byte[]> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, byte[] value) {
            return indexValueExpression.value(value);
        }
    }

    public static final class ByteIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Byte> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Byte value) {
            return indexValueExpression.value(new byte[] { value.byteValue() });
        }
    }

    public static final class DateIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Date> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Date value) {
            return indexValueExpression.value(value.toDate());
        }
    }

    public static final class DoubleIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Double> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Double value) {
            return indexValueExpression.value(value.doubleValue());
        }
    }

    public static final class FloatIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Float> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Float value) {
            return indexValueExpression.value(value.doubleValue());
        }
    }

    public static final class IntegerIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Integer> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Integer value) {
            return indexValueExpression.value(value.intValue());
        }
    }

    public static final class LongIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, Long> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, Long value) {
            return indexValueExpression.value(value.longValue());
        }
    }

    public static final class StringIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>>
            implements IndexValueExpressionStrategy<K, C, String> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, String value) {
            return indexValueExpression.value(value);
        }
    }

    public static final class EnumIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>, D extends Enum<D>>
            implements IndexValueExpressionStrategy<K, C, D> {

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, D value) {
            return indexValueExpression.value(DataTypeSerializerProvider.EnumSerializer.encode(value));
        }
    }

    public static final class CustomIndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>, D>
            implements IndexValueExpressionStrategy<K, C, D> {

        private final Serializer<D> serializer;

        public CustomIndexValueExpressionStrategy(Serializer<D> serializer) {
            if (serializer == null) {
                throw new NullPointerException("serializer cannot be null");
            }
            this.serializer = serializer;
        }

        @Override
        public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, D value) {
            return indexValueExpression.value(value, this.serializer);
        }
    }
}
