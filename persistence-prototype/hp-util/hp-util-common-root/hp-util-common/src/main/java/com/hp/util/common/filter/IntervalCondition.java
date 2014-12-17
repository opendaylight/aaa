/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.filter;

import java.io.Serializable;

import com.hp.util.common.Converter;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Interval;
import com.hp.util.common.type.Property;

/**
 * Interval filter condition.
 * 
 * @param <D> type of the attribute to apply the filter to
 * @author Fabiel Zuniga
 */
public final class IntervalCondition<D extends Comparable<D>> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Interval<D> value;
    private final Mode mode;

    private IntervalCondition(Interval<D> value, Mode mode) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }

        if (mode == null) {
            throw new NullPointerException("mode cannot be null");
        }

        this.value = value;
        this.mode = mode;
    }

    /**
     * Creates an "in" filter condition.
     * 
     * @param value value to compare to
     * @return an equality condition
     */
    public static <D extends Comparable<D>> IntervalCondition<D> in(Interval<D> value) {
        return new IntervalCondition<D>(value, Mode.IN);
    }

    /**
     * Creates a "not in" filter condition.
     * 
     * @param value value to compare to
     * @return an equality condition
     */
    public static <D extends Comparable<D>> IntervalCondition<D> notIn(Interval<D> value) {
        return new IntervalCondition<D>(value, Mode.NOT_IN);
    }

    /**
     * Gets the value to filter by.
     * 
     * @return the value to filter by
     */
    public Interval<D> getValue() {
        return this.value;
    }

    /**
     * Gets the filter mode.
     *
     * @return the filter mode
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Converts this condition to a different value type.
     * 
     * @param converter value converter
     * @return converted condition
     * @throws NullPointerException if {@code converter is null}
     */
    public <T extends Comparable<T>> IntervalCondition<T> convert(Converter<D, T> converter) throws NullPointerException {
        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }
        
        return new IntervalCondition<T>(this.value.convert(converter), this.mode);
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("value", this.value),
                Property.valueOf("mode", this.mode)
        );
    }

    /**
     * Filter mode.
     */
    public static enum Mode {
        /**
         * Elements inside the interval are selected by the filter.
         */
        IN,
        /**
         * Elements outside the interval are selected by the filter.
         */
        NOT_IN
    }
}
