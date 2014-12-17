/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.filter;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * String filter condition.
 * 
 * @author Fabiel Zuniga
 */
public final class StringCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;
    private final Mode mode;

    private StringCondition(String value, Mode mode) {
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
     * @return a string condition
     */
    public static StringCondition equalTo(String value) {
        return new StringCondition(value, Mode.EQUAL);
    }

    /**
     * Creates an "unequal" filter condition.
     * 
     * @param value value to compare to
     * @return a string condition
     */
    public static StringCondition unequalTo(String value) {
        return new StringCondition(value, Mode.UNEQUAL);
    }

    /**
     * Creates a "start with" filter condition.
     * 
     * @param value value to compare to
     * @return a string condition
     */
    public static StringCondition startWith(String value) {
        return new StringCondition(value, Mode.STARTS_WITH);
    }

    /**
     * Creates a "contain" filter condition.
     * 
     * @param value value to compare to
     * @return a string condition
     */
    public static StringCondition contain(String value) {
        return new StringCondition(value, Mode.CONTAINS);
    }

    /**
     * Creates an "end with" filter condition.
     * 
     * @param value value to compare to
     * @return a string condition
     */
    public static StringCondition endWith(String value) {
        return new StringCondition(value, Mode.ENDS_WITH);
    }

    /**
     * Gets the value to filter by.
     * 
     * @return the value to filter by
     */
    public String getValue() {
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
        UNEQUAL,
        /**
         * Elements that start with the specified value are selected by the filter.
         */
        STARTS_WITH,
        /**
         * Elements that contain the specified value are selected by the filter.
         */
        CONTAINS,
        /**
         * Elements that ends with the specified value are selected by the filter.
         */
        ENDS_WITH,
    }
}
