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
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * Column value data type provider used to construct columns when they are read from the database.
 * 
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public interface ColumnValueTypeProvider<C extends Serializable & Comparable<C>> {

    /**
     * Retrieves the column value data type for the given column name.
     * 
     * @param columnName column name to get the value's type for
     * @param dataTypeHandler a callback method to pass the data type to (In the same calling
     *            thread)
     * @param handlerInput input to pass to the handler when the data type is retrieved and passed
     *            to {@code dataTypeHandler} pro processing
     * @throws PersistenceException if the value type for {@code columnName} is unknown, or if the
     *             {@code dataTypeHandler} throws such exception
     */
    public <I> void getColumnValueType(ColumnName<C, ?> columnName, ColumnValueTypeHandler<C, I> dataTypeHandler,
            I handlerInput)
            throws PersistenceException;

    /**
     * A callback for column data type provision that matches the column name value and data type.
     * 
     * @param <C> type of the column name or column key
     * @param <I> type of input needed by the handler
     */
    public static interface ColumnValueTypeHandler<C extends Serializable & Comparable<C>, I> {
        
        /**
         * Callback method to to pass the data type to
         * 
         * @param columnName column name
         * @param dataType the column value's type
         * @param input input needed by the handler
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public <D> void handle(ColumnName<C, D> columnName, DataType<D> dataType, I input) throws PersistenceException;
    }
}
