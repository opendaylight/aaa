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
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.ByteArrayColumn;
import com.hp.util.model.persistence.cassandra.column.ByteColumn;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.CustomColumn;
import com.hp.util.model.persistence.cassandra.column.DateColumn;
import com.hp.util.model.persistence.cassandra.column.DoubleColumn;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.FloatColumn;
import com.hp.util.model.persistence.cassandra.column.IntegerColumn;
import com.hp.util.model.persistence.cassandra.column.LongColumn;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.column.VoidColumn;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.DataTypeCommandVisitor;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;
import com.netflix.astyanax.Serializer;

/**
 * @author Fabiel Zuniga
 */
class DataTypeColumnDecoderProvider {

    private final DataTypeSerializerProvider serializerProvider;
    private final Map<BasicType<?>, ColumnDecoder<?, ?>> basicTypeDecoders;

    @SuppressWarnings("rawtypes")
    private final DataTypeVisitor dataTypeVisitor = new DataTypeVisitor();

    @SuppressWarnings("rawtypes")
    public DataTypeColumnDecoderProvider(DataTypeSerializerProvider serializerProvider) {
        this.serializerProvider = serializerProvider;
        this.basicTypeDecoders = new HashMap<BasicType<?>, ColumnDecoder<?, ?>>();

        /*
         * TODO: Complete decoders
         */

        this.basicTypeDecoders.put(BasicType.VOID, new VoidColumnDecoder());
        this.basicTypeDecoders.put(BasicType.BYTE, new ByteColumnDecoder());
        this.basicTypeDecoders.put(BasicType.BYTE_ARRAY, new ByteArrayColumnDecoder());
        this.basicTypeDecoders.put(BasicType.STRING_ASCII, new StringColumnDecoder());
        this.basicTypeDecoders.put(BasicType.STRING_UTF8, new StringColumnDecoder());
        this.basicTypeDecoders.put(BasicType.INTEGER, new IntegerColumnDecoder());
        this.basicTypeDecoders.put(BasicType.LONG, new LongColumnDecoder());
        // this.basicTypeDecoders.put(BasicType.UUID, null);
        // this.basicTypeDecoders.put(BasicType.TIME_UUID, null);
        this.basicTypeDecoders.put(BasicType.DATE, new DateColumnDecoder());
        this.basicTypeDecoders.put(BasicType.BOOLEAN, new BooleanColumnDecoder());
        this.basicTypeDecoders.put(BasicType.FLOAT, new FloatColumnDecoder());
        this.basicTypeDecoders.put(BasicType.DOUBLE, new DoubleColumnDecoder());
        // this.basicTypeDecoders.put(BasicType.DECIMAL, null);
        // this.basicTypeDecoders.put(BasicType.BIG_INTEGER, null);
        // this.basicTypeDecoders.put(BasicType.CHAR, null);
        // this.basicTypeDecoders.put(BasicType.SHORT, null);
        // this.basicTypeDecoders.put(BasicType.COUNTER_COLUMN, null);
    }

    public <C extends Serializable & Comparable<C>, D> ColumnDecoder<C, D> getColumnDecoder(DataType<D> columnValueType) {
        @SuppressWarnings("unchecked")
        DataTypeCommandVisitor<D, ColumnDecoder<C, D>, Void> commandVisitor = this.dataTypeVisitor;
        return columnValueType.accept(commandVisitor, null);
    }

    @SuppressWarnings("unchecked")
    private <C extends Serializable & Comparable<C>, D> ColumnDecoder<C, D> getBasicTypeDecoder(
            BasicType<D> columnValueType) {
        ColumnDecoder<?, ?> columnDecoder = this.basicTypeDecoders.get(columnValueType);
        if (columnDecoder == null) {
            throw new RuntimeException("Unknown basic type " + columnValueType);
        }
        return (ColumnDecoder<C, D>) columnDecoder;
    }

    private <C extends Serializable & Comparable<C>, D> ColumnDecoder<C, D> getCompositeTypeDecoder(
            CompositeType<D> columnValueType) {
        Serializer<D> serializer = this.serializerProvider.getSerializer(columnValueType);
        return new CustomColumnDecoder<C, D>(columnValueType, serializer);
    }

    private class DataTypeVisitor<C extends Serializable & Comparable<C>, D> implements
            DataTypeCommandVisitor<D, ColumnDecoder<C, D>, Void> {

        @Override
        public ColumnDecoder<C, D> visit(BasicType<D> dataType, Void input) {
            return getBasicTypeDecoder(dataType);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public ColumnDecoder<C, D> visit(EnumType<D> dataType, Void input) {
            return new EnumColumnDecoder(dataType.getEnumClass());
        }

        @Override
        public ColumnDecoder<C, D> visit(CompositeType<D> dataType, Void input) {
            return getCompositeTypeDecoder(dataType);
        }
    }

    private static final class BooleanColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Boolean> {

        @Override
        public Column<C, Boolean> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            BooleanColumn<C> column = null;
            if (code.hasValue()) {
                column = new BooleanColumn<C>(ColumnName.<C, Boolean> valueOf(code.getName()), Boolean.valueOf(code
                        .getBooleanValue()));
            }
            else {
                column = new BooleanColumn<C>(ColumnName.<C, Boolean> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class ByteArrayColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, byte[]> {

        @Override
        public Column<C, byte[]> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            ByteArrayColumn<C> column = null;
            if (code.hasValue()) {
                column = new ByteArrayColumn<C>(ColumnName.<C, byte[]> valueOf(code.getName()),
                        code.getByteArrayValue());
            }
            else {
                column = new ByteArrayColumn<C>(ColumnName.<C, byte[]> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class ByteColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Byte> {

        @Override
        public Column<C, Byte> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            ByteColumn<C> column = null;
            if (code.hasValue()) {
                Byte value = Byte.valueOf(code.getByteValue());
                column = new ByteColumn<C>(ColumnName.<C, Byte> valueOf(code.getName()), value);
            }
            else {
                column = new ByteColumn<C>(ColumnName.<C, Byte> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class DateColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Date> {

        @Override
        public Column<C, Date> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            DateColumn<C> column = null;
            if (code.hasValue()) {
                column = new DateColumn<C>(ColumnName.<C, Date> valueOf(code.getName()), Date.valueOf(code
                        .getDateValue()));
            }
            else {
                column = new DateColumn<C>(ColumnName.<C, Date> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class DoubleColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Double> {

        @Override
        public Column<C, Double> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            DoubleColumn<C> column = null;
            if (code.hasValue()) {
                column = new DoubleColumn<C>(ColumnName.<C, Double> valueOf(code.getName()), Double.valueOf(code
                        .getDoubleValue()));
            }
            else {
                column = new DoubleColumn<C>(ColumnName.<C, Double> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class FloatColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Float> {

        @Override
        public Column<C, Float> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            // TODO: com.netflix.astyanax.model.Column.getFloatValue() is supported in 1.56
            FloatColumn<C> column = null;
            if (code.hasValue()) {
                column = new FloatColumn<C>(ColumnName.<C, Float> valueOf(code.getName()), Float.valueOf((float) code
                        .getDoubleValue()));
            }
            else {
                column = new FloatColumn<C>(ColumnName.<C, Float> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class IntegerColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Integer> {

        @Override
        public Column<C, Integer> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            IntegerColumn<C> column = null;
            if (code.hasValue()) {
                column = new IntegerColumn<C>(ColumnName.<C, Integer> valueOf(code.getName()), Integer.valueOf(code
                        .getIntegerValue()));
            }
            else {
                column = new IntegerColumn<C>(ColumnName.<C, Integer> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class LongColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Long> {

        @Override
        public Column<C, Long> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            LongColumn<C> column = null;
            if (code.hasValue()) {
                column = new LongColumn<C>(ColumnName.<C, Long> valueOf(code.getName()), Long.valueOf(code
                        .getLongValue()));
            }
            else {
                column = new LongColumn<C>(ColumnName.<C, Long> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class StringColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, String> {

        @Override
        public Column<C, String> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            StringColumn<C> column = null;
            if (code.hasValue()) {
                column = new StringColumn<C>(ColumnName.<C, String> valueOf(code.getName()), String.valueOf(code
                        .getStringValue()));
            }
            else {
                column = new StringColumn<C>(ColumnName.<C, String> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class VoidColumnDecoder<C extends Serializable & Comparable<C>> implements
            ColumnDecoder<C, Void> {

        @Override
        public Column<C, Void> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            return new VoidColumn<C>(ColumnName.<C, Void> valueOf(code.getName()));
        }
    }

    private static final class EnumColumnDecoder<C extends Serializable & Comparable<C>, D extends Enum<D>> implements
            ColumnDecoder<C, D> {

        private Class<D> enumClass;

        /**
         * Creates an enumeration decoder.
         * 
         * @param enumClass enumeration class
         */
        public EnumColumnDecoder(Class<D> enumClass) {
            if (enumClass == null) {
                throw new NullPointerException("enumClass cannot be null");
            }
            this.enumClass = enumClass;
        }

        // The enum is encoded to String and decoded from String using the constant name.

        @Override
        public Column<C, D> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            EnumColumn<C, D> column = null;
            if (code.hasValue()) {
                D value = DataTypeSerializerProvider.EnumSerializer.decode(code.getStringValue(), this.enumClass);
                column = new EnumColumn<C, D>(ColumnName.<C, D> valueOf(code.getName()), value);
            }
            else {
                column = new EnumColumn<C, D>(ColumnName.<C, D> valueOf(code.getName()));
            }
            return column;
        }
    }

    private static final class CustomColumnDecoder<C extends Serializable & Comparable<C>, D> implements
            ColumnDecoder<C, D> {

        private final CompositeType<D> dataType;
        private final Serializer<D> serializer;

        public CustomColumnDecoder(CompositeType<D> dataType, Serializer<D> serializer) {
            if (dataType == null) {
                throw new NullPointerException("dataType cannot be null");
            }

            if (serializer == null) {
                throw new NullPointerException("serializer cannot be null");
            }

            this.dataType = dataType;
            this.serializer = serializer;
        }

        @Override
        public Column<C, D> decode(com.netflix.astyanax.model.Column<C> code) throws IllegalArgumentException {
            CustomColumn<C, D> column = null;
            if (code.hasValue()) {
                column = new CustomColumn<C, D>(ColumnName.<C, D> valueOf(code.getName()),
                        code.getValue(this.serializer), this.dataType);
            }
            else {
                column = new CustomColumn<C, D>(ColumnName.<C, D> valueOf(code.getName()));
            }
            return column;
        }
    }
}
