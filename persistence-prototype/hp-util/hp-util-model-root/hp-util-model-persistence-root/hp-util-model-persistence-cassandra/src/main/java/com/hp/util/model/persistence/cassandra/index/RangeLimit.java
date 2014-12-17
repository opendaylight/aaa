/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;

/**
 * Range limit.
 * 
 * @param <T> type of the range (column name)
 * @author Fabiel Zuniga
 */
public class RangeLimit<T extends Serializable & Comparable<T>> {

    /** String range limit */
    public static final RangeLimit<String> STRING_RANGE_LIMIT = new RangeLimit<String>(" ", "~");
    /** Boolean range limit */
    public static final RangeLimit<Boolean> BOOLEAN_RANGE_LIMIT = new RangeLimit<Boolean>(Boolean.FALSE, Boolean.TRUE);
    /** Byte range limit */
    public static final RangeLimit<Byte> BYTE_RANGE_LIMIT = new RangeLimit<Byte>(Byte.valueOf(Byte.MIN_VALUE),
        Byte.valueOf(Byte.MAX_VALUE));
    /** Date range limit */
    public static final RangeLimit<Date> DATE_RANGE_LIMIT = new RangeLimit<Date>(Date.valueOf(1),
            Date.valueOf(Long.MAX_VALUE));
    /** Double range limit */
    public static final RangeLimit<Double> DOUBLE_RANGE_LIMIT = new RangeLimit<Double>(
        Double.valueOf(Double.MIN_VALUE), Double.valueOf(Double.MAX_VALUE));
    /** Float range limit */
    public static final RangeLimit<Float> FLOAT_RANGE_LIMIT = new RangeLimit<Float>(Float.valueOf(Float.MIN_VALUE),
        Float.valueOf(Float.MAX_VALUE));
    /** Integer range limit */
    public static final RangeLimit<Integer> INTEGER_RANGE_LIMIT = new RangeLimit<Integer>(
        Integer.valueOf(Integer.MIN_VALUE), Integer.valueOf(Integer.MAX_VALUE));
    /** Long range limit */
    public static final RangeLimit<Long> LONG_RANGE_LIMIT = new RangeLimit<Long>(Long.valueOf(Long.MIN_VALUE),
        Long.valueOf(Long.MAX_VALUE));

    private T start;
    private T end;

    /**
     * Creates a range limit.
     *
     * @param start value to ensure the first column in a row is included in a range query
     * @param end value to ensure the last column in a row is included in a range query
     */
    public RangeLimit(T start, T end) {
        if (start == null) {
            throw new NullPointerException("start cannot be null");
        }

        if (end == null) {
            throw new NullPointerException("end cannot be null");
        }

        if (start.compareTo(end) >= 0) {
            throw new IllegalArgumentException("start must be less than end");
        }

        this.start = start;
        this.end = end;
    }

    /**
     * Gets the value to ensure the first column in a row is included in a range query.
     *
     * @return the first value in the range
     */
    public T getStart() {
        return this.start;
    }

    /**
     * Gets the value to ensure the last column in a row is included in a range query.
     *
     * @return the last value in the range
     */
    public T getEnd() {
        return this.end;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("start", this.start),
                Property.valueOf("end", this.end)
        );
    }
}
