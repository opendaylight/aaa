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
import com.hp.util.common.type.Property;

/**
 * Comparability filter condition.
 * 
 * @param <D> type of the attribute to apply the filter to
 * @author Fabiel Zuniga
 */
public final class ComparabilityCondition<D extends Comparable<D>> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final D value;
    private final Mode mode;

    private ComparabilityCondition(D value, Mode mode) {
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
     * Creates a "less than" filter condition.
     * 
     * @param value value to compare to
     * @return a comparability condition
     */
    public static <D extends Comparable<D>> ComparabilityCondition<D> lessThan(D value) {
        return new ComparabilityCondition<D>(value, Mode.LESS_THAN);
    }

    /**
     * Creates a "less than or equal to" filter condition.
     * 
     * @param value value to compare to
     * @return a comparability condition
     */
    public static <D extends Comparable<D>> ComparabilityCondition<D> lessThanOrEqualTo(D value) {
        return new ComparabilityCondition<D>(value, Mode.LESS_THAN_OR_EQUAL_TO);
    }

    /**
     * Creates an "equal" filter condition.
     * 
     * @param value value to compare to
     * @return a comparability condition
     */
    public static <D extends Comparable<D>> ComparabilityCondition<D> equalTo(D value) {
        return new ComparabilityCondition<D>(value, Mode.EQUAL);
    }

    /**
     * Creates a "greater than or equal to" filter condition.
     * 
     * @param value value to compare to
     * @return a comparability condition
     */
    public static <D extends Comparable<D>> ComparabilityCondition<D> greaterThanOrEqualTo(D value) {
        return new ComparabilityCondition<D>(value, Mode.GREATER_THAN_OR_EQUAL_TO);
    }

    /**
     * Creates a "greater than" filter condition.
     * 
     * @param value value to compare to
     * @return a comparability condition
     */
    public static <D extends Comparable<D>> ComparabilityCondition<D> greaterThan(D value) {
        return new ComparabilityCondition<D>(value, Mode.GREATER_THAN);
    }

    /**
     * Gets the value to filter by.
     * 
     * @return the value to filter by
     */
    public D getValue() {
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
    public <T extends Comparable<T>> ComparabilityCondition<T> convert(Converter<D, T> converter)
            throws NullPointerException {
        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }
        return new ComparabilityCondition<T>(converter.convert(this.value), this.mode);
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
         * Elements that are less than the specified value are selected by the filter.
         */
        LESS_THAN,
        /**
         * Elements that are less than or equal to the specified value are selected by the filter.
         */
        LESS_THAN_OR_EQUAL_TO,
        /**
         * Elements that are equal to the specified value are selected by the filter.
         */
        EQUAL,
        /**
         * Elements that are greater than or equal to the specified value are selected by the filter.
         */
        GREATER_THAN_OR_EQUAL_TO,
        /**
         * Elements that are greater than the specified value are selected by the filter.
         */
        GREATER_THAN
    }
}
