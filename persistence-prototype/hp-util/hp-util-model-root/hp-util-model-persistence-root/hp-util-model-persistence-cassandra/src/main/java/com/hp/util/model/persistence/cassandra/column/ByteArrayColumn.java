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
 * Byte array column.
 * 
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public final class ByteArrayColumn<C extends Serializable & Comparable<C>> extends Column<C, byte[]> {

    /**
     * Creates a byte array column with no value.
     * 
     * @param name column's name
     */
    public ByteArrayColumn(ColumnName<C, byte[]> name) {
        super(name);
    }

    /**
     * Creates a byte array column.
     * 
     * @param name column's name
     * @param value column's value
     */
    public ByteArrayColumn(ColumnName<C, byte[]> name, byte[] value) {
        super(name, value);
    }

    @Override
    public <I> void accept(ColumnVisitor<C, I> visitor, I visitorInput) {
        visitor.visit(this, visitorInput);
    }

    @Override
    public <E, I> E accept(ColumnCommandVisitor<C, E, I> visitor, I visitorInput) {
        return visitor.visit(this, visitorInput);
    }
}
