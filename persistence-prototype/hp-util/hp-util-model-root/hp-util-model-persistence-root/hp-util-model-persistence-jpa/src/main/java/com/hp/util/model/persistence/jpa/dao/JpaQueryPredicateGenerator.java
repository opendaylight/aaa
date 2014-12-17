/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.IdValueConverter;
import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.filter.TimePeriodCondition;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Interval;

/**
 * Query predicate generator.
 * 
 * @param <P> type of the entity (an object annotated with {@link javax.persistence.Entity})
 * @author Fabiel Zuniga
 */
@SuppressWarnings("static-method")
public class JpaQueryPredicateGenerator<P> {

    private static final String SQL_WILDCARD = "%";

    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    @SuppressWarnings("rawtypes")
    private static final JpaQueryPredicateGenerator INSTANCE = new JpaQueryPredicateGenerator();

    private JpaQueryPredicateGenerator() {

    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    @SuppressWarnings("unchecked")
    public static <P> JpaQueryPredicateGenerator<P> getInstance() {
        return INSTANCE;
    }

    /**
     * Generates a predicate that is true.
     *
     * @param builder criteria builder
     * @return a predicate that is true
     */
    public Predicate getTautology(CriteriaBuilder builder) {
        // Negation of disjunction is not working with EclipseLink, it produces the same result.
        return builder.not(getContradiction(builder));
    }

    /**
     * Generates a predicate that is false.
     *
     * @param builder criteria builder
     * @return a predicate that is false
     */
    public Predicate getContradiction(CriteriaBuilder builder) {
        return builder.disjunction();
    }

    /**
     * Different providers might treat {@code null} predicates differently (EclipseLink and
     * Hibernate do). This is a convenient method to unify such behavior. In this implementation a
     * {@code null} predicate represents a tautology (it is considered {@code true}).
     * 
     * @param builder criteria builder
     * @param predicates input
     * @return the result of the logical disjunction
     */
    public Predicate and(CriteriaBuilder builder, Predicate... predicates) {
        List<Predicate> nonNullPredicates = new ArrayList<Predicate>(predicates.length);
        for (Predicate predicate : predicates) {
            if (predicate != null) {
                nonNullPredicates.add(predicate);
            }
        }
        return builder.and(nonNullPredicates.toArray(new Predicate[0]));
    }

    /**
     * Different providers might treat {@code null} predicates differently (EclipseLink and
     * Hibernate do). This is a convenient method to unify such behavior. In this implementation a
     * {@code null} predicate represents a tautology (it is considered {@code true}).
     * 
     * @param builder criteria builder
     * @param predicates input
     * @return the result of the logical conjunction
     */
    public Predicate or(CriteriaBuilder builder, Predicate... predicates) {
        for (Predicate predicate : predicates) {
            if (predicate == null) {
                return getTautology(builder);
            }
        }

        return builder.or(predicates);
    }

    /**
     * Different providers might treat {@code null} predicates differently (EclipseLink and
     * Hibernate do). This is a convenient method to unify such behavior. In this implementation a
     * {@code null} predicate represents a tautology (it is considered {@code true}).
     * 
     * @param builder criteria builder
     * @param predicate input
     * @return the result of the logical negation
     */
    public Predicate not(CriteriaBuilder builder, Predicate predicate) {
        if (predicate == null) {
            return getContradiction(builder);
        }

        return builder.not(predicate);
    }

    /**
     * Generates a predicate to satisfy the given equality condition.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicate to use in a query
     */
    public <D> Predicate getPredicate(EqualityCondition<D> condition, SingularAttribute<? super P, D> attribute,
        CriteriaBuilder builder, Root<P> root) {
        if (condition == null) {
            return getTautology(builder);
        }

        Predicate predicate = null;

        switch (condition.getMode()) {
            case EQUAL:
                if (condition.getValue() != null) {
                    predicate = builder.equal(root.get(attribute), condition.getValue());
                }
                else {
                    predicate = builder.isNull(root.get(attribute));
                }
                break;
            case UNEQUAL:
                if (condition.getValue() != null) {
                    predicate = builder.notEqual(root.get(attribute), condition.getValue());
                }
                else {
                    predicate = builder.isNotNull(root.get(attribute));

                }
                break;
        }

        return predicate;
    }

    /**
     * Generates predicates to satisfy the given comparable condition.
     *
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicate to use in a query
     */
    public <D extends Comparable<D>> Predicate getPredicate(ComparabilityCondition<D> condition,
        SingularAttribute<? super P, D> attribute, CriteriaBuilder builder, Root<P> root) {
        if (condition == null) {
            return getTautology(builder);
        }

        Predicate predicate = null;

        switch (condition.getMode()) {
            case LESS_THAN:
                predicate = builder.lessThan(root.get(attribute), condition.getValue());
                break;
            case LESS_THAN_OR_EQUAL_TO:
                predicate = builder.lessThanOrEqualTo(root.get(attribute), condition.getValue());
                break;
            case EQUAL:
                predicate = builder.equal(root.get(attribute), condition.getValue());
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                predicate = builder.greaterThanOrEqualTo(root.get(attribute), condition.getValue());
                break;
            case GREATER_THAN:
                predicate = builder.greaterThan(root.get(attribute), condition.getValue());
                break;
        }

        return predicate;
    }

    /**
     * Generates predicates to satisfy the given interval condition.
     *
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicates to use in a query
     */
    public <D extends Comparable<D>> Predicate getPredicate(IntervalCondition<D> condition,
        SingularAttribute<? super P, D> attribute, CriteriaBuilder builder, Root<P> root) {
        if (condition == null) {
            return getTautology(builder);
        }

        Interval<D> interval = condition.getValue();

        if (condition.getMode() == IntervalCondition.Mode.NOT_IN && interval.getType() == Interval.Type.UNBOUNDED) {
            return getContradiction(builder);
        }

        Predicate predicate = null;
        Predicate rightSideOfInterval = null;
        Predicate leftSideOfInterval = null;

        switch (interval.getType()) {
            case OPEN:
                leftSideOfInterval = builder.greaterThan(root.get(attribute), interval.getLeftEndpoint());
                rightSideOfInterval = builder.lessThan(root.get(attribute), interval.getRightEndpoint());
                predicate = builder.and(leftSideOfInterval, rightSideOfInterval);
                break;
            case CLOSED:
                leftSideOfInterval = builder.greaterThanOrEqualTo(root.get(attribute), interval.getLeftEndpoint());
                rightSideOfInterval = builder.lessThanOrEqualTo(root.get(attribute), interval.getRightEndpoint());
                predicate = builder.and(leftSideOfInterval, rightSideOfInterval);
                break;
            case LEFT_CLOSED_RIGHT_OPEN:
                leftSideOfInterval = builder.greaterThanOrEqualTo(root.get(attribute), interval.getLeftEndpoint());
                rightSideOfInterval = builder.lessThan(root.get(attribute), interval.getRightEndpoint());
                predicate = builder.and(leftSideOfInterval, rightSideOfInterval);
                break;
            case LEFT_OPEN_RIGHT_CLOSED:
                leftSideOfInterval = builder.greaterThan(root.get(attribute), interval.getLeftEndpoint());
                rightSideOfInterval = builder.lessThanOrEqualTo(root.get(attribute), interval.getRightEndpoint());
                predicate = builder.and(leftSideOfInterval, rightSideOfInterval);
                break;
            case LEFT_OPEN_RIGHT_UNBOUNDED:
                predicate = builder.greaterThan(root.get(attribute), interval.getLeftEndpoint());
                break;
            case LEFT_CLOSED_RIGHT_UNBOUNDED:
                predicate = builder.greaterThanOrEqualTo(root.get(attribute), interval.getLeftEndpoint());
                break;
            case LEFT_UNBOUNDED_RIGHT_OPEN:
                predicate = builder.lessThan(root.get(attribute), interval.getRightEndpoint());
                break;
            case LEFT_UNBOUNDED_RIGHT_CLOSED:
                predicate = builder.lessThanOrEqualTo(root.get(attribute), interval.getRightEndpoint());
                break;
            case UNBOUNDED:
                predicate = getTautology(builder);
                break;
        }

        switch (condition.getMode()) {
            case IN:
                // Nothing to do
                break;
            case NOT_IN:
                predicate = builder.not(predicate);
                break;
        }

        return predicate;
    }

    /**
     * Generates predicates to satisfy the given time period condition.
     * <p>
     * NOTE: {@link TimePeriodCondition} uses {@link com.hp.util.common.type} but the
     * {@link SingularAttribute} must be of type {@link Date}. This because entities uses
     * {@link Date} because JPA understands it.
     * 
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicates to use in a query
     */
    public Predicate getPredicate(TimePeriodCondition condition, SingularAttribute<? super P, Date> attribute,
        CriteriaBuilder builder, Root<P> root) {

        IntervalCondition<Date> intervalCondition = null;

        if (condition != null) {
            Interval<Date> interval = Interval.closed(condition.getValue().getStartTime().toDate(), condition
                    .getValue().getEndTime().toDate());
            if (condition.getMode() == TimePeriodCondition.Mode.IN) {
                intervalCondition = IntervalCondition.in(interval);
            }
            else {
                intervalCondition = IntervalCondition.notIn(interval);
            }
        }

        return getPredicate(intervalCondition, attribute, builder, root);
    }

    /**
     * Generates predicates to satisfy the given Set condition.
     *
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicates to use in a query
     */
    public <D> Predicate getPredicate(SetCondition<D> condition, SingularAttribute<? super P, D> attribute,
        CriteriaBuilder builder, Root<P> root) {
        if (condition == null) {
            return getTautology(builder);
        }

        Predicate predicate = null;

        In<D> in = builder.in(root.get(attribute));
        for (D value : condition.getValues()) {
            in = in.value(value);
        }
        switch (condition.getMode()) {
            case IN:
                predicate = in;
                break;
            case NOT_IN:
                predicate = builder.not(in);
                break;
        }

        return predicate;
    }

    /**
     * Generates predicates to satisfy the given string condition.
     *
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicates to use in a query
     */
    public Predicate getPredicate(StringCondition condition, SingularAttribute<? super P, String> attribute,
        CriteriaBuilder builder, Root<P> root) {
        if (condition == null) {
            return getTautology(builder);
        }

        Predicate predicate = null;

        switch (condition.getMode()) {
            case EQUAL:
                predicate = builder.equal(root.get(attribute), condition.getValue());
                break;
            case UNEQUAL:
                predicate = builder.notEqual(root.get(attribute), condition.getValue());
                break;
            case STARTS_WITH:
                StringBuilder startWithBldr = new StringBuilder(condition.getValue());
                startWithBldr.append(SQL_WILDCARD);
                predicate = builder.like(root.get(attribute), startWithBldr.toString());
                break;
            case CONTAINS:
                StringBuilder containBldr = new StringBuilder(SQL_WILDCARD);
                containBldr.append(condition.getValue());
                containBldr.append(SQL_WILDCARD);
                predicate = builder.like(root.get(attribute), containBldr.toString());
                break;
            case ENDS_WITH:
                StringBuilder endWithBldr = new StringBuilder(SQL_WILDCARD);
                endWithBldr.append(condition.getValue());
                predicate = builder.like(root.get(attribute), endWithBldr.toString());
                break;
        }

        return predicate;
    }

    /**
     * Generates a predicate to satisfy the given ID equality condition.
     *
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicate to use in a query
     */
    public <T, I extends Serializable> Predicate getPredicateForId(EqualityCondition<Id<T, I>> condition,
        SingularAttribute<? super P, I> attribute, CriteriaBuilder builder, Root<P> root) {
        if (condition == null) {
            return getTautology(builder);
        }

        EqualityCondition<I> idValueCondition = condition.convert(IdValueConverter.<T, I> getInstance());
        return getPredicate(idValueCondition, attribute, builder, root);
    }

    /**
     * Generates a predicate to satisfy the given ID equality condition.
     *
     * @param condition condition to apply
     * @param attribute JPA entity attribute (table column)
     * @param builder criteriaBuider object for creating the predicate
     * @param root the root element type of the table
     * @return predicate to use in a query
     */
    public <T, I extends Serializable, R> Predicate getPredicateForRelatedEntity(EqualityCondition<Id<T, I>> condition,
            SingularAttribute<? super P, R> attribute, CriteriaBuilder builder, Root<P> root) {
        /*
         * TODO: R should be an entity. Before it was SingularAttribute<? super P, ? extends
         * Storable<I>>
         */
        if (condition == null) {
            return getTautology(builder);
        }

        Predicate predicate = null;

        switch (condition.getMode()) {
            case EQUAL:
                if (condition.getValue() != null) {
                    predicate = builder.equal(root.get(attribute), condition.getValue().getValue());
                }
                else {
                    predicate = builder.isNull(root.get(attribute));
                }
                break;
            case UNEQUAL:
                if (condition.getValue() != null) {
                    predicate = builder.notEqual(root.get(attribute), condition.getValue().getValue());
                }
                else {
                    predicate = builder.isNull(root.get(attribute));
                }
                break;
        }

        return predicate;
    }
}
