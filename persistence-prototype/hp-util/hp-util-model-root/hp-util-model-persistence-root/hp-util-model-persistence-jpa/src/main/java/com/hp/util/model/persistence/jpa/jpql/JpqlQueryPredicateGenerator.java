/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;

/**
 * JPQL predicate generator.
 * 
 * @param <P> type of the entity type of the entity (an object annotated with
 *            {@link javax.persistence.Entity})
 * @author Fabiel Zuniga
 */
@SuppressWarnings("static-method")
public class JpqlQueryPredicateGenerator<P> {
    private static final JpqlPredicate TAUTOLOGY = new JpqlTautology();
    private static final JpqlPredicate CONTRADICTION = new JpqlContradiction();

    /**
     * Generates a predicate that is true.
     * 
     * @return a predicate
     */
    public JpqlPredicate getTautology() {
        return TAUTOLOGY;
    }

    /**
     * Generates a predicate that is false.
     * 
     * @return a predicate
     */
    public JpqlPredicate getContradiction() {
        return CONTRADICTION;
    }

    /**
     * Operates the given operands with the AND operator.
     * 
     * @param operands operands
     * @return a predicate
     */
    public JpqlPredicate and(JpqlPredicate... operands) {
        return new JpqlAnd(operands);
    }

    /**
     * Operates the given operands with the OR operator.
     * 
     * @param operands operands
     * @return a predicate
     */
    public JpqlPredicate or(JpqlPredicate... operands) {
        return new JpqlOr(operands);
    }

    /**
     * Operates the given operand with the NOT operator.
     * 
     * @param operand operand
     * @return a predicate
     */
    public JpqlPredicate not(JpqlPredicate operand) {
        return new JpqlNot(operand);
    }

    /**
     * Generates a predicate to satisfy the given equality condition.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param entityClass persistent object class
     * @return a predicate
     */
    public <D> JpqlPredicate getPredicate(EqualityCondition<D> condition, SingularAttribute<? super P, D> attribute,
        Class<P> entityClass) {
        if (condition == null) {
            return getTautology();
        }
        return new JpqlEqualityCondition<P, D>(condition, attribute, entityClass);
    }

    /**
     * Generates predicates to satisfy the given comparability condition.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param entityClass persistent object class
     * @return a predicate
     */
    public <D extends Comparable<D>> JpqlPredicate getPredicate(ComparabilityCondition<D> condition,
        SingularAttribute<? super P, D> attribute, Class<P> entityClass) {
        if (condition == null) {
            return getTautology();
        }
        return new JpqlComparability<P, D>(condition, attribute, entityClass);
    }

    /**
     * Generates predicates to satisfy the given interval condition.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param entityClass persistent object class
     * @return a predicate
     */
    public <D extends Comparable<D>> JpqlPredicate getPredicate(IntervalCondition<D> condition,
        SingularAttribute<? super P, D> attribute, Class<P> entityClass) {
        if (condition == null) {
            return getTautology();
        }
        return new JpqlIntervalCondition<P, D>(condition, attribute, entityClass);
    }

    /**
     * Generates predicates to satisfy the given set condition.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param entityClass persistent object class
     * @return a predicate
     */
    public <D> JpqlPredicate getPredicate(SetCondition<D> condition, SingularAttribute<? super P, D> attribute,
        Class<P> entityClass) {
        if (condition == null) {
            return getTautology();
        }
        return new JpqlSetCondition<P, D>(condition, attribute, entityClass);
    }

    /**
     * Generates predicates to satisfy the given string condition.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param entityClass persistent object class
     * @return a predicate
     */
    public JpqlPredicate getPredicate(StringCondition condition, SingularAttribute<? super P, String> attribute,
        Class<P> entityClass) {
        if (condition == null) {
            return getTautology();
        }
        return new JpqlStringCondition<P>(condition, attribute, entityClass);
    }
}
