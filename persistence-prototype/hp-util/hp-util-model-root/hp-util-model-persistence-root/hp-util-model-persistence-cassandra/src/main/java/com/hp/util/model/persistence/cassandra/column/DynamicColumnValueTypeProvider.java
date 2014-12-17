/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.column;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;

/**
 * {@link ColumnValueTypeProvider} where column value types can be dynamically register.
 * 
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public class DynamicColumnValueTypeProvider<C extends Serializable & Comparable<C>> implements
        ColumnValueTypeProvider<C> {

    private final Map<ColumnName<C, ?>, TypeEntry<C, ?>> typeEntries;

    /**
     * Creates a {@link ColumnValueTypeProvider}.
     */
    public DynamicColumnValueTypeProvider() {
        this.typeEntries = new HashMap<ColumnName<C, ?>, TypeEntry<C, ?>>();
    }

    /**
     * Registers a column value type.
     * 
     * @param columnName column name
     * @param columnValueType column value type
     */
    public synchronized <D> void registerColumnValueType(ColumnName<C, D> columnName, DataType<D> columnValueType) {
        TypeEntry<C, D> typeEntry = TypeEntry.valueOf(columnName, columnValueType);
        this.typeEntries.put(columnName, typeEntry);
    }
    
    @Override
    public synchronized <I> void getColumnValueType(ColumnName<C, ?> columnName,
            ColumnValueTypeHandler<C, I> dataTypeHandler,
            I handlerInput) throws PersistenceException {
        TypeEntry<C, ?> typeEntry = this.typeEntries.get(columnName);
        if (typeEntry == null) {
            throw new PersistenceException("Unknown column value type " + columnName);
        }
        typeEntry.notify(dataTypeHandler, handlerInput);
    }

    private static class TypeEntry<C extends Serializable & Comparable<C>, D> {
        private final ColumnName<C, D> columnName;
        private final DataType<D> columnValueType;

        private TypeEntry(ColumnName<C, D> columnName, DataType<D> columnValueType) {
            this.columnName = columnName;
            this.columnValueType = columnValueType;
        }

        public static <C extends Serializable & Comparable<C>, D> TypeEntry<C, D> valueOf(ColumnName<C, D> columnName,
                DataType<D> columnValueType) {
            return new TypeEntry<C, D>(columnName, columnValueType);
        }

        public <I> void notify(ColumnValueTypeHandler<C, I> dataTypeHandler, I handlerInput)
                throws PersistenceException {
            dataTypeHandler.handle(this.columnName, this.columnValueType, handlerInput);
        }
    }
}
