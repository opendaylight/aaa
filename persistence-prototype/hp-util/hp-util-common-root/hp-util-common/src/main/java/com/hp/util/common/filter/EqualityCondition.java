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
 * Equality filter condition.
 * 
 * @param <D> type of the attribute to apply the filter to
 * @author Fabiel Zuniga
 */
public final class EqualityCondition<D> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final D value;
    private final Mode mode;

    private EqualityCondition(D value, Mode mode) {
        if (mode == null) {
            throw new NullPointerException("mode cannot be null");
        }

        this.value = value;
        this.mode = mode;
    }

    /**
     * Creates an "equal" filter condition.
     * 
     * @param value value to compare to
     * @return an equality condition
     */
    public static <D> EqualityCondition<D> equalTo(D value) {
        return new EqualityCondition<D>(value, Mode.EQUAL);
    }

    /**
     * Creates a "unequal" filter condition.
     * 
     * @param value value to compare to
     * @return an equality condition
     */
    public static <D> EqualityCondition<D> unequalTo(D value) {
        return new EqualityCondition<D>(value, Mode.UNEQUAL);
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
    public <T> EqualityCondition<T> convert(Converter<D, T> converter) throws NullPointerException {
        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }
        return new EqualityCondition<T>(converter.convert(this.value), this.mode);
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
         * Elements that are equal to the specified value are selected by the filter.
         */
        EQUAL,
        /**
         * Elements that are not equal to the specified value are selected by the filter.
         */
        UNEQUAL
    }
}
