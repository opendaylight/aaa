/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.DataTypeCommandVisitor;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;

/**
 * Column factory. This class is useful to reconstruct columns from its type when they are read from
 * the database.
 * 
 * @author Fabiel Zuniga
 */
public class ColumnFactory {

    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    private static final ColumnFactory INSTANCE = new ColumnFactory();

    private final Map<BasicType<?>, TypeColumnFactory<?, ?>> basicTypeFactories;

    @SuppressWarnings("rawtypes")
    private ColumnFactory() {
        this.basicTypeFactories = new HashMap<BasicType<?>, TypeColumnFactory<?, ?>>();

        this.basicTypeFactories.put(BasicType.VOID, new VoidColumnFactory());
        this.basicTypeFactories.put(BasicType.BYTE, new ByteColumnFactory());
        this.basicTypeFactories.put(BasicType.BYTE_ARRAY, new ByteArrayColumnFactory());
        this.basicTypeFactories.put(BasicType.STRING_ASCII, new StringColumnFactory());
        this.basicTypeFactories.put(BasicType.STRING_UTF8, new StringColumnFactory());
        this.basicTypeFactories.put(BasicType.INTEGER, new IntegerColumnFactory());
        this.basicTypeFactories.put(BasicType.LONG, new LongColumnFactory());
        // this.basicTypeDecoders.put(BasicType.UUID, null);
        // this.basicTypeDecoders.put(BasicType.TIME_UUID, null);
        this.basicTypeFactories.put(BasicType.DATE, new DateColumnFactory());
        this.basicTypeFactories.put(BasicType.BOOLEAN, new BooleanColumnFactory());
        this.basicTypeFactories.put(BasicType.FLOAT, new FloatColumnFactory());
        this.basicTypeFactories.put(BasicType.DOUBLE, new DoubleColumnFactory());
        // this.basicTypeDecoders.put(BasicType.DECIMAL, null);
        // this.basicTypeDecoders.put(BasicType.BIG_INTEGER, null);
        // this.basicTypeDecoders.put(BasicType.CHAR, null);
        // this.basicTypeDecoders.put(BasicType.SHORT, null);
        // this.basicTypeDecoders.put(BasicType.COUNTER_COLUMN, null);
    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    public static ColumnFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a column instance.
     * 
     * @param name column name
     * @param value column value
     * @param columnValueType column value type
     * @return a column instance
     */
    public <C extends Serializable & Comparable<C>, D> Column<C, D> create(final ColumnName<C, D> name, final D value,
            DataType<D> columnValueType) {

        DataTypeCommandVisitor<D, Column<C, D>, Void> commandVisitor = new DataTypeCommandVisitor<D, Column<C, D>, Void>() {

            @Override
            public Column<C, D> visit(BasicType<D> dataType, Void input) {
                return createBasicTypeColumn(name, value, dataType);
            }

            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Column<C, D> visit(EnumType<D> dataType, Void input) {
                Enum enumValue = (Enum) value;
                return new EnumColumn(name, enumValue);
            }

            @Override
            public Column<C, D> visit(CompositeType<D> dataType, Void input) {
                return createCompositeTypeColumn(name, value, dataType);
            }

        };

        return columnValueType.accept(commandVisitor, null);
    }

    @SuppressWarnings("unchecked")
    private <C extends Serializable & Comparable<C>, D> Column<C, D> createBasicTypeColumn(ColumnName<C, D> name,
            D value, BasicType<D> columnValueType) {
        TypeColumnFactory<C, D> factory = (TypeColumnFactory<C, D>) this.basicTypeFactories.get(columnValueType);
        if (factory == null) {
            throw new RuntimeException("Unknown basic type " + columnValueType);
        }
        return factory.create(name, value);
    }

    private static <C extends Serializable & Comparable<C>, D> Column<C, D> createCompositeTypeColumn(
            ColumnName<C, D> name, D value, CompositeType<D> columnValueType) {
        return new CustomColumn<C, D>(name, value, columnValueType);
    }

    private static interface TypeColumnFactory<C extends Serializable & Comparable<C>, D> {

        public Column<C, D> create(ColumnName<C, D> name, D value);
    }

    private static final class BooleanColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Boolean> {

        @Override
        public Column<C, Boolean> create(ColumnName<C, Boolean> name, Boolean value) {
            return new BooleanColumn<C>(name, value);
        }
    }

    private static final class ByteArrayColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, byte[]> {

        @Override
        public Column<C, byte[]> create(ColumnName<C, byte[]> name, byte[] value) {
            return new ByteArrayColumn<C>(name, value);
        }
    }

    private static final class ByteColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Byte> {

        @Override
        public Column<C, Byte> create(ColumnName<C, Byte> name, Byte value) {
            return new ByteColumn<C>(name, value);
        }
    }

    private static final class DateColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Date> {

        @Override
        public Column<C, Date> create(ColumnName<C, Date> name, Date value) {
            return new DateColumn<C>(name, value);
        }
    }

    private static final class DoubleColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Double> {

        @Override
        public Column<C, Double> create(ColumnName<C, Double> name, Double value) {
            return new DoubleColumn<C>(name, value);
        }
    }

    private static final class FloatColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Float> {

        @Override
        public Column<C, Float> create(ColumnName<C, Float> name, Float value) {
            return new FloatColumn<C>(name, value);
        }
    }

    private static final class IntegerColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Integer> {

        @Override
        public Column<C, Integer> create(ColumnName<C, Integer> name, Integer value) {
            return new IntegerColumn<C>(name, value);
        }
    }

    private static final class LongColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Long> {

        @Override
        public Column<C, Long> create(ColumnName<C, Long> name, Long value) {
            return new LongColumn<C>(name, value);
        }
    }

    private static final class StringColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, String> {

        @Override
        public Column<C, String> create(ColumnName<C, String> name, String value) {
            return new StringColumn<C>(name, value);
        }
    }

    private static final class VoidColumnFactory<C extends Serializable & Comparable<C>> implements
            TypeColumnFactory<C, Void> {

        @Override
        public Column<C, Void> create(ColumnName<C, Void> name, Void value) {
            return new VoidColumn<C>(name);
        }
    }
}
