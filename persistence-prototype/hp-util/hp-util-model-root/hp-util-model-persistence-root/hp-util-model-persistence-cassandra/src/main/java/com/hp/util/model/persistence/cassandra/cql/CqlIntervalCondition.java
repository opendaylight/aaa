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
import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.type.Interval;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * Interval condition predicate.
 * 
 * @param <C> type of the column name or column key
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
class CqlIntervalCondition<C extends Serializable & Comparable<C>, D extends Comparable<D>> implements CqlPredicate {

    private CqlPredicate delegate;

    /**
     * Creates an equality predicate.
     * 
     * @param condition interval condition
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query
     */
    public CqlIntervalCondition(IntervalCondition<D> condition, ColumnName<C, D> columnName,
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

        this.delegate = new IntervalPredicate<C, D>(condition.getValue(), columnName, columnNameConverter,
            columnValueConverter);
        if (condition.getMode().equals(IntervalCondition.Mode.NOT_IN)) {
            this.delegate = new CqlNot(this.delegate);
        }
    }

    @Override
    public String getPredicate() {
        return this.delegate.getPredicate();
    }

    private static class IntervalPredicate<C extends Serializable & Comparable<C>, D extends Comparable<D>> implements
        CqlPredicate {

        private CqlPredicate delegate;

        protected IntervalPredicate(Interval<D> interval, ColumnName<C, D> columnName,
            Converter<C, String> columnNameConverter, Converter<D, String> columnValueConverter) {

            switch (interval.getType()) {
                case OPEN: {
                    ComparabilityCondition<D> leftCondition = ComparabilityCondition.greaterThan(interval
                            .getLeftEndpoint());
                    ComparabilityCondition<D> rightCondition = ComparabilityCondition.lessThan(interval
                            .getRightEndpoint());
                    this.delegate = new CqlAnd(new CqlComparability<C, D>(leftCondition, columnName,
                        columnNameConverter, columnValueConverter), new CqlComparability<C, D>(rightCondition,
                        columnName, columnNameConverter, columnValueConverter));
                }
                    break;
                case CLOSED: {
                    ComparabilityCondition<D> leftCondition = ComparabilityCondition.greaterThanOrEqualTo(interval
                            .getLeftEndpoint());
                    ComparabilityCondition<D> rightCondition = ComparabilityCondition.lessThanOrEqualTo(interval
                            .getRightEndpoint());
                    this.delegate = new CqlAnd(new CqlComparability<C, D>(leftCondition, columnName,
                        columnNameConverter, columnValueConverter), new CqlComparability<C, D>(rightCondition,
                        columnName, columnNameConverter, columnValueConverter));
                }
                    break;
                case LEFT_CLOSED_RIGHT_OPEN: {
                    ComparabilityCondition<D> leftCondition = ComparabilityCondition.greaterThanOrEqualTo(interval
                            .getLeftEndpoint());
                    ComparabilityCondition<D> rightCondition = ComparabilityCondition.lessThan(interval
                            .getRightEndpoint());
                    this.delegate = new CqlAnd(new CqlComparability<C, D>(leftCondition, columnName,
                        columnNameConverter, columnValueConverter), new CqlComparability<C, D>(rightCondition,
                        columnName, columnNameConverter, columnValueConverter));
                }
                    break;
                case LEFT_OPEN_RIGHT_CLOSED: {
                    ComparabilityCondition<D> leftCondition = ComparabilityCondition.greaterThan(interval
                            .getLeftEndpoint());
                    ComparabilityCondition<D> rightCondition = ComparabilityCondition.lessThanOrEqualTo(interval
                            .getRightEndpoint());
                    this.delegate = new CqlAnd(new CqlComparability<C, D>(leftCondition, columnName,
                        columnNameConverter, columnValueConverter), new CqlComparability<C, D>(rightCondition,
                        columnName, columnNameConverter, columnValueConverter));
                }
                    break;
                case LEFT_OPEN_RIGHT_UNBOUNDED: {
                    ComparabilityCondition<D> leftCondition = ComparabilityCondition.greaterThan(interval
                            .getLeftEndpoint());
                    this.delegate = new CqlComparability<C, D>(leftCondition, columnName, columnNameConverter,
                        columnValueConverter);
                }
                    break;
                case LEFT_CLOSED_RIGHT_UNBOUNDED: {
                    ComparabilityCondition<D> leftCondition = ComparabilityCondition.greaterThanOrEqualTo(interval
                            .getLeftEndpoint());
                    this.delegate = new CqlComparability<C, D>(leftCondition, columnName, columnNameConverter,
                        columnValueConverter);
                }
                    break;
                case LEFT_UNBOUNDED_RIGHT_OPEN: {
                    ComparabilityCondition<D> rightCondition = ComparabilityCondition.lessThan(interval
                            .getRightEndpoint());
                    this.delegate = new CqlComparability<C, D>(rightCondition, columnName, columnNameConverter,
                        columnValueConverter);
                }
                    break;
                case LEFT_UNBOUNDED_RIGHT_CLOSED: {
                    ComparabilityCondition<D> rightCondition = ComparabilityCondition.lessThanOrEqualTo(interval
                            .getRightEndpoint());
                    this.delegate = new CqlComparability<C, D>(rightCondition, columnName, columnNameConverter,
                        columnValueConverter);
                }
                    break;
                case UNBOUNDED: {
                    this.delegate = new CqlTautology();
                }
                    break;
            }

            assert (this.delegate != null);
        }

        @Override
        public String getPredicate() {
            return this.delegate.getPredicate();
        }
    }
}
