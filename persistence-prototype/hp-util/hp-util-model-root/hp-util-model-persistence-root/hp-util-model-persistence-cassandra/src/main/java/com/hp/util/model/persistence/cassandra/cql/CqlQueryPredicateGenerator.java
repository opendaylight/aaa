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
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.filter.TimePeriodCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Interval;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * CQL predicate generator.
 * 
 * @author Fabiel Zuniga
 */
public class CqlQueryPredicateGenerator {
    private static final CqlPredicate TAUTOLOGY = new CqlTautology();
    private static final CqlPredicate CONTRADICTION = new CqlContradiction();

    /**
     * Generates a predicate that is true.
     *
     * @return a predicate
     */
    public static CqlPredicate getTautology() {
        return TAUTOLOGY;
    }

    /**
     * Generates a predicate that is false.
     *
     * @return a predicate
     */
    public static CqlPredicate getContradiction() {
        return CONTRADICTION;
    }

    /**
     * Operates the given operands with the AND operator.
     *
     * @param operands operands
     * @return a predicate
     */
    public static CqlPredicate and(CqlPredicate... operands) {
        return new CqlAnd(operands);
    }

    /**
     * Operates the given operands with the OR operator.
     *
     * @param operands operands
     * @return a predicate
     */
    public static CqlPredicate or(CqlPredicate... operands) {
        return new CqlOr(operands);
    }

    /**
     * Operates the given operand with the NOT operator.
     *
     * @param operand operand
     * @return a predicate
     */
    public static CqlPredicate not(CqlPredicate operand) {
        return new CqlNot(operand);
    }

    /**
     * Generates a predicate to satisfy the given equality condition.
     *
     * @param condition condition to apply
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @return a predicate
     */
    public static <C extends Serializable & Comparable<C>, D> CqlPredicate getPredicate(EqualityCondition<D> condition,
        ColumnName<C, D> columnName, Converter<C, String> columnNameConverter, Converter<D, String> columnValueConverter) {
        if (condition == null) {
            return getTautology();
        }
        return new CqlEqualityCondition<C, D>(condition, columnName, columnNameConverter, columnValueConverter);
    }

    /**
     * Generates predicates to satisfy the given comparability condition.
     * 
     * @param condition condition to apply
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @return a predicate
     */
    public static <C extends Serializable & Comparable<C>, D extends Comparable<D>> CqlPredicate getPredicate(
        ComparabilityCondition<D> condition, ColumnName<C, D> columnName, Converter<C, String> columnNameConverter,
        Converter<D, String> columnValueConverter) {
        if (condition == null) {
            return getTautology();
        }
        return new CqlComparability<C, D>(condition, columnName, columnNameConverter, columnValueConverter);
    }

    /**
     * Generates predicates to satisfy the given interval condition.
     * 
     * @param condition condition to apply
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @return a predicate
     */
    public static <C extends Serializable & Comparable<C>, D extends Comparable<D>> CqlPredicate getPredicate(
        IntervalCondition<D> condition, ColumnName<C, D> columnName, Converter<C, String> columnNameConverter,
        Converter<D, String> columnValueConverter) {
        if (condition == null) {
            return getTautology();
        }
        return new CqlIntervalCondition<C, D>(condition, columnName, columnNameConverter, columnValueConverter);
    }

    /**
     * Generates predicates to satisfy the given time period condition.
     * 
     * @param condition condition to apply
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @return predicates to use in a query
     */
    public static <C extends Serializable & Comparable<C>> CqlPredicate getPredicate(TimePeriodCondition condition,
        ColumnName<C, Date> columnName, Converter<C, String> columnNameConverter) {

        IntervalCondition<Date> intervalCondition = null;

        if (condition != null) {
            Interval<Date> interval = Interval.closed(condition.getValue().getStartTime(), condition.getValue()
                    .getEndTime());
            if (condition.getMode() == TimePeriodCondition.Mode.IN) {
                intervalCondition = IntervalCondition.in(interval);
            }
            else {
                intervalCondition = IntervalCondition.notIn(interval);
            }
        }

        return getPredicate(intervalCondition, columnName, columnNameConverter,
            CqlConverter.CqlStringConverter.<Date> getInstance());
    }

    /**
     * Generates predicates to satisfy the given set condition.
     * 
     * @param condition condition to apply
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @param columnValueConverter converts the column value to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @return a predicate
     */
    public static <C extends Serializable & Comparable<C>, D> CqlPredicate getPredicate(SetCondition<D> condition,
        ColumnName<C, D> columnName, Converter<C, String> columnNameConverter, Converter<D, String> columnValueConverter) {
        if (condition == null) {
            return getTautology();
        }
        return new CqlSetCondition<C, D>(condition, columnName, columnNameConverter, columnValueConverter);
    }

    /**
     * Generates predicates to satisfy the given string condition.
     * 
     * @param condition condition to apply
     * @param columnName column to apply the condition to
     * @param columnNameConverter converts the column name to a {@link String} ready to use in the
     *            CQL query. See {@link CqlConverter}.
     * @return a predicate
     */
    public static <C extends Serializable & Comparable<C>> CqlPredicate getPredicate(StringCondition condition,
        ColumnName<C, String> columnName, Converter<C, String> columnNameConverter) {
        if (condition == null) {
            return getTautology();
        }
        return new CqlStringCondition<C>(condition, columnName, columnNameConverter);
    }
}
