/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import com.hp.util.common.converter.ObjectToStringConverter;

/**
 * Abstract value type that hides the internal representation, for example the internal
 * representation of an IpAddress could be a String or bytes.
 * <P>
 * This class is immutable and so should subclasses.
 * 
 * @param <E> type of the internal representation of the value type
 * @See AbstractValueType
 * @author Fabiel Zuniga
 */
public abstract class ValueType<E> {

    private final E value;

    /**
     * Creates a new value type.
     * 
     * @param value value
     * @throws NullPointerException if {@code value} is {@code null}
     */
    protected ValueType(E value) throws NullPointerException {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }

        this.value = value;
    }

    /**
     * Gets the internal representation.
     *
     * @return internal representation
     */
    public E getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        ValueType<?> other = (ValueType<?>)obj;

        if (!this.value.equals(other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("value", this.value.toString())
        );
    }

    /**
     * Converts a value type to its internal representation.
     * 
     * @param valueType value type to convert to its internal representation
     * @return internal representation if {@code valueType} is not {@code null}, {@code null}
     *         otherwise
     */
    public static <E> E toValue(ValueType<E> valueType) {
        if (valueType == null) {
            return null;
        }
        return valueType.getValue();
    }
}
