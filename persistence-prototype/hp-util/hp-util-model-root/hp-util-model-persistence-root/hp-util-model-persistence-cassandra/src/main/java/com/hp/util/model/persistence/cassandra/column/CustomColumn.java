/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;

import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;

/**
 * Custom column.
 * <p>
 * A custom column is normally used to create a composite column which is normally used to implement
 * filtering with a secondary index. For example, to filter persons by Status and Sex ("Get all
 * female single persons") a composite column that stores the status and sex can be created and a
 * secondary index can be set. Thus the column family to store persons would have a column for
 * status (to support filtering by status), a column for sex (to support filtering by sex) and a
 * composite column for status-sex.
 * <p>
 * A composite column is also used to implement super columns. Astyanax deprecated
 * {@code com.netflix.astyanax.model.ColumnPath} with the following comment: "Super columns should
 * be replaced with composite columns". All super columns support was dropped in favor of composite
 * columns. Cassandra will eventually replace the super column implementation with composite
 * columns.
 * <p>
 * Note that composite columns that will act as super columns are normally not indexed since they
 * will tend to store several values.
 * 
 * @param <C> type of the column name or column key
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
public class CustomColumn<C extends Serializable & Comparable<C>, D> extends Column<C, D> {

    private final CompositeType<D> dataType;

    /**
     * Creates a boolean column with no value.
     * 
     * @param name column's name
     */
    public CustomColumn(ColumnName<C, D> name) {
        super(name);
        this.dataType = null;
    }

    /**
     * Creates a boolean column.
     * 
     * @param name column's name
     * @param value column's value
     * @param dataType data type
     */
    public CustomColumn(ColumnName<C, D> name, D value, CompositeType<D> dataType) {
        super(name, value);

        if (dataType == null) {
            throw new NullPointerException("dataType cannot be null");
        }

        this.dataType = dataType;
    }

    @Override
    public <I> void accept(ColumnVisitor<C, I> visitor, I visitorInput) {
        visitor.visit(this, visitorInput);
    }

    @Override
    public <E, I> E accept(ColumnCommandVisitor<C, E, I> visitor, I visitorInput) {
        return visitor.visit(this, visitorInput);
    }

    /**
     * Accepts a custom column visitor.
     * 
     * @param visitor visitor
     */
    public void accept(CustomColumnVisitor<C> visitor) {
        /*
         * NOTE: This visitor wouldn't be needed if the operation was part of this class. Assume
         * there is an operation that uses type and value and thus require them to be of the
         * same type.
         */ 
         /*
         * void serialize(CompositeType<D> type, D value);
         */
        /*
         * If such operation was added as part of the custom column then the operation would work
         * even with wildcard column:
         */
        /*
         * CustomColumn<C, ?> column = ...; 
         * column.serialize();
         */
        /*
         * However, since this custom column is independent of implementations, it is not possible
         * to add methods that are particular to an implementation (Astyanax methods for example).
         */
        /*
         * See com.hp.demo.pattern.visitor.wildcard example.
         */
        visitor.visit(this);
    }
    
    /**
     * Accepts a custom column command visitor.
     * 
     * @param visitor visitor
     * @return the command result
     */
    public <E> E accept(CustomColumnCommandVisitor<C, E> visitor) {
        /*
         * This visitor wouldn't be needed either. See comments above.
         */
        return visitor.visit(this);
    }

    /**
     * Returns the data type of this column's value.
     * 
     * @return the data type of the column's value
     */
    public CompositeType<D> getDataType() {
        return this.dataType;
    }

    /**
     * Custom column visitor.
     * <p>
     * This visitor is useful when a custom column is read from Cassandra in the form
     * CustomColumn<C, ?>. The visitor won't receive the actual type, but it will be able to match
     * the value and the dataType (getting rid of the wildcard ?).
     * @param <C> type of the column name
     */
    public static interface CustomColumnVisitor<C extends Serializable & Comparable<C>> {

        /**
         * Visits a custom column.
         * 
         * @param column custom column
         */
        public <D> void visit(CustomColumn<C, D> column);
    }

    /**
     * Custom column visitor.
     * <p>
     * This visitor is useful when a custom column is read from Cassandra in the form
     * CustomColumn<C, ?>. The visitor won't receive the actual type, but it will be able to match
     * the value and the dataType (getting rid of the wildcard ?).
     * 
     * @param <C> type of the column name
     * @param <E> type of the result of the visit
     */
    public static interface CustomColumnCommandVisitor<C extends Serializable & Comparable<C>, E> {

        /**
         * Visits a custom column.
         * 
         * @param column custom column
         * @return the result of the visit
         */
        public <D> E visit(CustomColumn<C, D> column);
    }
}
