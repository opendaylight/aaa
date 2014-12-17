/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;

/**
 * Immutable version of {@link java.util.Date}.
 * <p>
 * This version of Date can be safely returned in getters without making a copy.
 * 
 * @author Fabiel Zuniga
 */
public final class Date implements Serializable, Comparable<Date> {
    private static final long serialVersionUID = 1L;

    private final long value;

    private Date(long date) {
        this.value = date;
    }

    /**
     * Creates a date using the same implementation than {@link java.util.Date#Date(long)}.
     * 
     * @param date the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @return a date
     */
    public static Date valueOf(long date) {
        return new Date(date);
    }

    /**
     * Creates a Date from {@link java.util.Date}.
     * <p>
     * If the argument {@code date} is {@code null} this method returns {@code null} as a
     * convenience for dates conversions.
     * 
     * @param date {@link java.util.Date date}
     * @return a date object if the argument is not {@code null}, {@code null} otherwise
     */
    public static Date valueOf(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime());
    }

    /**
     * Creates a date with the current time.
     * 
     * @return a date
     */
    public static Date currentTime() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * Returns the time using the same implementation than {@link java.util.Date#getTime()}.
     * 
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this
     *         date.
     */
    public long getTime() {
        return this.value;
    }

    /**
     * Converts this Date to {@link java.util.Date}.
     * 
     * @return a {@link java.util.Date date}
     */
    public java.util.Date toDate() {
        return new java.util.Date(this.value);
    }

    @Override
    public int compareTo(Date o) {
        return Long.compare(this.value, o.value);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.value ^ (this.value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Date)) {
            return false;
        }

        Date other = (Date) obj;

        if (this.value != other.value) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("value", toDate())
        );
    }
}
