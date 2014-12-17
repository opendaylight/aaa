/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;

/**
 * Column command visitor.
 * 
 * @param <C> type of the column name or column key
 * @param <E> type of the result of the visit
 * @param <I> type of the visitor's input. This type allows making visitors thread safe (and thus
 *            allowing reusing the visitor instance) when they require input to do their job.
 * @author Fabiel Zuniga
 */
public interface ColumnCommandVisitor<C extends Serializable & Comparable<C>, E, I> {

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(VoidColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(BooleanColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(ByteColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(ByteArrayColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(DateColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(DoubleColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(FloatColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(IntegerColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(LongColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(StringColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(EnumColumn<C, ? extends Enum<?>> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(CustomColumn<C, ?> column, I input);
}
