/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Column.
 * 
 * @param <C> type of the column name or column key
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
public abstract class Column<C extends Serializable & Comparable<C>, D> {

    private final ColumnName<C, D> name;
    private final D value;

    /**
     * Creates a column with no value.
     *
     * @param name column's name
     */
    protected Column(ColumnName<C, D> name) {
        this(name, null);
    }

    /**
     * Creates a column.
     *
     * @param name column's name
     * @param value column's value
     */
    protected Column(ColumnName<C, D> name, D value) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        this.name = name;
        this.value = value;
    }

    /**
     * Gets the column's name.
     *
     * @return the column's name
     */
    public ColumnName<C, D> getName() {
        return this.name;
    }

    /**
     * Gets the column's value.
     *
     * @return the column's value
     */
    public D getValue() {
        return this.value;
    }

    /**
     * Accepts a visitor.
     * 
     * @param visitor visitor
     * @param visitorInput visitor's input
     */
    public abstract <I> void accept(ColumnVisitor<C, I> visitor, I visitorInput);

    /**
     * Accepts a command visitor.
     * 
     * @param visitor visitor
     * @param visitorInput visitor's input
     * @return the command result
     */
    public abstract <E, I> E accept(ColumnCommandVisitor<C, E, I> visitor, I visitorInput);

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

        Column<?, ?> other = (Column<?, ?>)obj;

        if (!this.name.equals(other.name)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("name", this.name),
                Property.valueOf("value", this.value)
        );
    }
}
