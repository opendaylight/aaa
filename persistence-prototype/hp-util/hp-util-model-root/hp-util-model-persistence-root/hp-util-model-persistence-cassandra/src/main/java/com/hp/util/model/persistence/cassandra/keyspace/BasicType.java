/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;

/**
 * Basic data type.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public final class BasicType<D> implements DataType<D> {

    /**
     * Byte type.
     */
    public static final BasicType<Void> VOID = new BasicType<Void>(Void.class);

    /**
     * Byte type.
     */
    public static final BasicType<Byte> BYTE = new BasicType<Byte>(Byte.class);

    /**
     * (CQL Name: blob) Arbitrary hexadecimal bytes (no validation).
     */
    public static final BasicType<byte[]> BYTE_ARRAY = new BasicType<byte[]>(byte[].class);

    /**
     * (CQL Name: ascii) US-ASCII character string.
     */
    public static final BasicType<String> STRING_ASCII = new BasicType<String>(String.class);

    /**
     * (CQL Name: text, varchar) UTF-8 encoded string.
     */
    public static final BasicType<String> STRING_UTF8 = new BasicType<String>(String.class);

    /**
     * (CQL Name: varint) Arbitrary-precision integer.
     */
    public static final BasicType<Integer> INTEGER = new BasicType<Integer>(Integer.class);

    /**
     * (CQL Name: int, bigint) 8-byte long.
     */
    public static final BasicType<Long> LONG = new BasicType<Long>(Long.class);

    /**
     * (CQL Name: uuid) Type 1 or type 4 UUID.
     */
    public static final BasicType<java.util.UUID> UUID = new BasicType<java.util.UUID>(java.util.UUID.class);

    /**
     * (CQL Name: timeuuid) Type 1 UUID only (CQL3).
     */
    public static final BasicType<java.util.UUID> TIME_UUID = new BasicType<java.util.UUID>(java.util.UUID.class);

    /**
     * (CQL Name: timestamp) Date plus time, encoded as 8 bytes since epoch.
     */
    public static final BasicType<Date> DATE = new BasicType<Date>(Date.class);

    /**
     * (CQL Name: boolean) true or false.
     */
    public static final BasicType<Boolean> BOOLEAN = new BasicType<Boolean>(Boolean.class);

    /**
     * (CQL Name: float) 4-byte floating point.
     */
    public static final BasicType<Float> FLOAT = new BasicType<Float>(Float.class);

    /**
     * (CQL Name: double) 8-byte floating point.
     */
    public static final BasicType<Double> DOUBLE = new BasicType<Double>(Double.class);

    /**
     * (CQL Name: decimal) Variable-precision decimal.
     */
    public static final BasicType<BigDecimal> DECIMAL = new BasicType<BigDecimal>(BigDecimal.class);

    /**
     * (CQL Name: TODO) Big integer.
     */
    public static final BasicType<BigInteger> BIG_INTEGER = new BasicType<BigInteger>(BigInteger.class);

    /**
     * (CQL Name: TODO) Char.
     */
    public static final BasicType<Character> CHAR = new BasicType<Character>(Character.class);

    /**
     * (CQL Name: TODO) Short.
     */
    public static final BasicType<Short> SHORT = new BasicType<Short>(Short.class);

    /**
     * (CQL Name: counter) Distributed counter value (8-byte long).
     */
    public static final BasicType<Long> COUNTER_COLUMN = new BasicType<Long>(Long.class);

    private Class<D> typeClass;
    
    private BasicType(Class<D> typeClass) {
        this.typeClass = typeClass;
    }

    @Override
    public <I> void accept(DataTypeVisitor<D, I> visitor, I visitorInput) {
        visitor.visit(this, visitorInput);

    }

    @Override
    public <E, I> E accept(DataTypeCommandVisitor<D, E, I> visitor, I visitorInput) {
        return visitor.visit(this, visitorInput);
    }
    
    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("typeClass", this.typeClass)
        );
    }
}
