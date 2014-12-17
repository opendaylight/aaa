/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.filter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.hp.util.common.Converter;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Set filter condition.
 * 
 * @param <D> type of the attribute to apply the filter to
 * @author Fabiel Zuniga
 */
public final class SetCondition<D> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Set<D> values;
    private final Mode mode;

    private SetCondition(Set<D> values, Mode mode) {
        if (values == null) {
            throw new NullPointerException("values cannot be null");
        }

        if (mode == null) {
            throw new NullPointerException("mode cannot be null");
        }

        this.values = Collections.unmodifiableSet(new HashSet<D>(values));
        this.mode = mode;
    }

    /**
     * Creates an "in" filter condition.
     * 
     * @param values value to compare to
     * @return a set condition
     */
    public static <D> SetCondition<D> in(Set<D> values) {
        return new SetCondition<D>(values, Mode.IN);
    }

    /**
     * Creates an "in" filter condition.
     * 
     * @param values value to compare to
     * @return a set condition
     */
    @SafeVarargs
    public static <D> SetCondition<D> in(D... values) {
        return SetCondition.in(new HashSet<D>(Arrays.asList(values)));
    }

    /**
     * Creates a "not in" filter condition.
     * 
     * @param values value to compare to
     * @return a set condition
     */
    public static <D> SetCondition<D> notIn(Set<D> values) {
        return new SetCondition<D>(values, Mode.NOT_IN);
    }

    /**
     * Creates a "not in" filter condition.
     * 
     * @param values value to compare to
     * @return a set condition
     */
    @SafeVarargs
    public static <D> SetCondition<D> notIn(D... values) {
        return SetCondition.notIn(new HashSet<D>(Arrays.asList(values)));
    }

    /**
     * Gets the value to filter by.
     * 
     * @return the value to filter by
     */
    public Set<D> getValues() {
        return this.values;
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
    public <T> SetCondition<T> convert(Converter<D, T> converter) throws NullPointerException {
        if (converter == null) {
            throw new NullPointerException("converter cannot be null");
        }

        Set<T> convertedValues = new HashSet<T>();
        for (D value : this.values) {
            convertedValues.add(converter.convert(value));
        }

        return new SetCondition<T>(convertedValues, this.mode);
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("values", this.values),
                Property.valueOf("mode", this.mode)
        );
    }


    /**
     * Filter mode.
     */
    public static enum Mode {
        /**
         * Elements that are in to the specified values are selected by the filter.
         */
        IN,
        /**
         * Elements that are not in the specified values are selected by the filter.
         */
        NOT_IN
    }
}
