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
 * Column visitor.
 * 
 * @param <C> type of the column name or column key
 * @param <I> type of the visitor's input. This type allows making visitors thread safe (and thus
 *            allowing reusing the visitor instance) when they require input to do their job.
 * @author Fabiel Zuniga
 */
public interface ColumnVisitor<C extends Serializable & Comparable<C>, I> {

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(VoidColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(BooleanColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(ByteColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(ByteArrayColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(DateColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(DoubleColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(FloatColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(IntegerColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(LongColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(StringColumn<C> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(EnumColumn<C, ? extends Enum<?>> column, I input);

    /**
     * Visits a column.
     * 
     * @param column column to visit
     * @param input visitor's input
     */
    public void visit(CustomColumn<C, ?> column, I input);
}
