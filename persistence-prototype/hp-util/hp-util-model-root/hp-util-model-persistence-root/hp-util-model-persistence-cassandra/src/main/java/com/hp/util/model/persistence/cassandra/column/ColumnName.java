/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;

import com.hp.util.common.type.SerializableValueType;

/**
 * Value type object for the column name. This class is used to prohibit setting a value of the
 * wrong type to a column. There is no harm in memory usage because column names will normally be
 * constants.
 * 
 * @param <C> type of the column name or column key. This type should be immutable. It is critical
 *            this type implements equals() and hashCode() correctly.
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
public final class ColumnName<C extends Serializable & Comparable<C>, D> extends SerializableValueType<C> implements
    Comparable<ColumnName<C, ?>> {
    private static final long serialVersionUID = 1L;

    private ColumnName(C value) {
        super(value);
    }

    /**
     * Creates a column name.
     *
     * @param name name of the column
     * @return a column name
     */
    public static <C extends Serializable & Comparable<C>, D> ColumnName<C, D> valueOf(C name) {
        return new ColumnName<C, D>(name);
    }

    @Override
    public int compareTo(ColumnName<C, ?> other) {
        return getValue().compareTo(other.getValue());
    }
}
