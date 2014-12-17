/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.cql;

import java.io.Serializable;

import com.hp.util.common.Converter;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * Equality condition predicate.
 * 
 * @param <C> type of the column name or column key
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
class CqlEqualityCondition<C extends Serializable & Comparable<C>, D> implements CqlPredicate {

    private EqualityCondition<D> condition;
    private ColumnName<C, D> columnName;
    private Converter<C, String> columnNameConverter;
    private Converter<D, String> columnValueConverter;

    /**
     * Creates an equality predicate.
     * 
     * @param condition equality condition
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query
     */
    public CqlEqualityCondition(EqualityCondition<D> condition, ColumnName<C, D> columnName,
        Converter<C, String> columnNameConverter, Converter<D, String> columnValueConverter) {
        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        if (columnNameConverter == null) {
            throw new NullPointerException("columnNameConverter cannot be null");
        }

        if (columnValueConverter == null) {
            throw new NullPointerException("columnValueConverter cannot be null");
        }

        this.condition = condition;
        this.columnName = columnName;
        this.columnNameConverter = columnNameConverter;
        this.columnValueConverter = columnValueConverter;
    }

    @Override
    public String getPredicate() {
        StringBuilder str = new StringBuilder(64);

        str.append(this.columnNameConverter.convert(this.columnName.getValue()));

        if (this.condition.getValue() != null) {
            switch (this.condition.getMode()) {
                case EQUAL:
                    str.append(" = ");
                    break;
                case UNEQUAL:
                    str.append(" != ");
                    break;
            }
            str.append(this.columnValueConverter.convert(this.condition.getValue()));
        }
        else {
            switch (this.condition.getMode()) {
                case EQUAL:
                    str.append(" is Null ");
                    break;
                case UNEQUAL:
                    str.append(" is not Null ");
                    break;
            }
        }

        return str.toString();
    }
}
