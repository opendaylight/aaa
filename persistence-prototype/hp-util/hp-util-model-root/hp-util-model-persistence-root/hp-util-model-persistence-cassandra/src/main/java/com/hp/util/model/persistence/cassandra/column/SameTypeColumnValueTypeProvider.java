/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;

import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * {@link ColumnValueTypeProvider} to use when all columns have the same value type; In
 * {@link CustomSecondaryIndex custom secondary indexes} for example.
 * 
 * @param <C> type of the column name or column key
 * @param <D> type of the common column value type
 * @author Fabiel Zuniga
 */
public class SameTypeColumnValueTypeProvider<C extends Serializable & Comparable<C>, D> implements
        ColumnValueTypeProvider<C> {

    /*
     * DataTypeProvider when all columns are of the same value type
     */

    private final DataType<D> columnValueType;

    /**
     * Creates a {@link ColumnValueTypeProvider}.
     * 
     * @param columnValueType common column value type
     */
    public SameTypeColumnValueTypeProvider(DataType<D> columnValueType) {
        this.columnValueType = columnValueType;
    }

    @Override
    public <I> void getColumnValueType(ColumnName<C, ?> columnName, ColumnValueTypeHandler<C, I> dataTypeHandler,
            I handlerInput) throws PersistenceException {
        // All columns are of the same type
        @SuppressWarnings("unchecked")
        ColumnName<C, D> typedColumnName = (ColumnName<C, D>) columnName;
        dataTypeHandler.handle(typedColumnName, this.columnValueType, handlerInput);
    }
}
