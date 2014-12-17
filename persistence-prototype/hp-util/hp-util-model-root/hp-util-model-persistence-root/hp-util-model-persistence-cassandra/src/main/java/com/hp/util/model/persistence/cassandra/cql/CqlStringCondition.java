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
import com.hp.util.common.filter.StringCondition;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * String condition predicate.
 * 
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
class CqlStringCondition<C extends Serializable & Comparable<C>> implements CqlPredicate {

    private CqlPredicate delegate;

    /**
     * Creates an equality predicate.
     * 
     * @param condition string condition
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query
     */
    public CqlStringCondition(StringCondition condition, ColumnName<C, String> columnName,
        Converter<C, String> columnNameConverter) {
        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        if (columnNameConverter == null) {
            throw new NullPointerException("columnNameConverter cannot be null");
        }

        switch (condition.getMode()) {
            case EQUAL: {
                EqualityCondition<String> equalityCondition = EqualityCondition.equalTo(condition.getValue());
                this.delegate = new CqlEqualityCondition<C, String>(equalityCondition, columnName, columnNameConverter,
                    CqlConverter.CqlStringConverter.<String> getInstance());
            }
                break;
            case UNEQUAL: {
                EqualityCondition<String> equalityCondition = EqualityCondition.unequalTo(condition.getValue());
                this.delegate = new CqlEqualityCondition<C, String>(equalityCondition, columnName, columnNameConverter,
                    CqlConverter.CqlStringConverter.<String> getInstance());
            }
                break;
            case STARTS_WITH:
            case CONTAINS:
            case ENDS_WITH:
                this.delegate = new SubstringPredicate<C>(condition, columnName, columnNameConverter,
                    CqlConverter.CqlStringConverter.<String> getInstance());
                break;
        }
    }

    @Override
    public String getPredicate() {
        return this.delegate.getPredicate();
    }

    private static class SubstringPredicate<C extends Serializable & Comparable<C>> implements CqlPredicate {

        private static final char WILDCARD = '%';

        private StringCondition condition;
        private ColumnName<C, String> columnName;
        private Converter<C, String> columnNameConverter;
        private Converter<String, String> columnValueConverter;

        protected SubstringPredicate(StringCondition condition, ColumnName<C, String> columnName,
            Converter<C, String> columnNameConverter, Converter<String, String> columnValueConverter) {
            this.condition = condition;
            this.columnName = columnName;
            this.columnNameConverter = columnNameConverter;
            this.columnValueConverter = columnValueConverter;
        }

        @Override
        public String getPredicate() {
            StringBuilder str = new StringBuilder(64);

            str.append(this.columnNameConverter.convert(this.columnName.getValue()));

            String conditionValue = this.columnValueConverter.convert(this.condition.getValue());

            str.append(" Like '");
            switch (this.condition.getMode()) {
                case EQUAL:
                    // Ignored since this case is not possible
                    break;
                case UNEQUAL:
                    // Ignored since this case is not possible
                    break;
                case STARTS_WITH:
                    str.append(conditionValue);
                    str.append(WILDCARD);
                    break;
                case CONTAINS:
                    str.append(WILDCARD);
                    str.append(conditionValue);
                    str.append(WILDCARD);
                    break;
                case ENDS_WITH:
                    str.append(WILDCARD);
                    str.append(conditionValue);
                    break;
            }
            str.append('\'');

            return str.toString();
        }
    }
}
