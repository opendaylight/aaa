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
import com.hp.util.common.type.TimePeriod;

/**
 * Time period filter condition.
 * 
 * @author Fabiel Zuniga
 */
public final class TimePeriodCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TimePeriod value;
    private final Mode mode;

    private TimePeriodCondition(TimePeriod value, Mode mode) {
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
    public static TimePeriodCondition in(TimePeriod value) {
        return new TimePeriodCondition(value, Mode.IN);
    }

    /**
     * Creates a "not in" filter condition.
     * 
     * @param value value to compare to
     * @return an equality condition
     */
    public static TimePeriodCondition notIn(TimePeriod value) {
        return new TimePeriodCondition(value, Mode.NOT_IN);
    }

    /**
     * Gets the value to filter by.
     * 
     * @return the value to filter by
     */
    public TimePeriod getValue() {
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
         * Elements inside the time period are selected by the filter.
         */
        IN,
        /**
         * Elements outside the time period are selected by the filter.
         */
        NOT_IN
    }
}
