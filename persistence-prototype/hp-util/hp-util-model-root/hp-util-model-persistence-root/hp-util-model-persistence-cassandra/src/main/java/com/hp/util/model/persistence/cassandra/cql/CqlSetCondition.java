/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.cql;

import java.io.Serializable;
import java.util.Set;

import com.hp.util.common.Converter;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * Set condition predicate.
 * 
 * @param <C> type of the column name or column key
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
class CqlSetCondition<C extends Serializable & Comparable<C>, D> implements CqlPredicate {

    private CqlPredicate delegate;

    /**
     * Creates an equality predicate.
     * 
     * @param condition set condition
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query
     */
    public CqlSetCondition(SetCondition<D> condition, ColumnName<C, D> columnName,
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

        this.delegate = new SetPredicate<C, D>(condition.getValues(), columnName, columnNameConverter,
            columnValueConverter);
        if (condition.getMode().equals(SetCondition.Mode.NOT_IN)) {
            this.delegate = new CqlNot(this.delegate);
        }
    }

    @Override
    public String getPredicate() {
        return this.delegate.getPredicate();
    }

    private static class SetPredicate<C extends Serializable & Comparable<C>, D> implements CqlPredicate {

        private Set<D> set;
        private ColumnName<C, D> columnName;
        private Converter<C, String> columnNameConverter;
        private Converter<D, String> columnValueConverter;

        protected SetPredicate(Set<D> set, ColumnName<C, D> columnName, Converter<C, String> columnNameConverter,
            Converter<D, String> columnValueConverter) {
            this.set = set;
            this.columnName = columnName;
            this.columnNameConverter = columnNameConverter;
            this.columnValueConverter = columnValueConverter;
        }

        @Override
        public String getPredicate() {
            StringBuilder str = new StringBuilder(32);
            str.append(this.columnNameConverter.convert(this.columnName.getValue()));
            str.append(" In ");
            str.append('[');
            if (!this.set.isEmpty()) {
                for (D element : this.set) {
                    str.append(this.columnValueConverter.convert(element));
                    str.append(',');
                }
                str.delete(str.length() - 1, str.length());
            }
            str.append(']');


            return str.toString();
        }
    }
}
