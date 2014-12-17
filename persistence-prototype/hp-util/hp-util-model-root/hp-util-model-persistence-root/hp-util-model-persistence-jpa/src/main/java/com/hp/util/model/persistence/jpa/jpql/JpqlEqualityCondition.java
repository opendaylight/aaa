/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

import javax.persistence.Query;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.filter.EqualityCondition;

/**
 * Equality condition predicate.
 * 
 * @param <P> type of the entity type of the entity (an object annotated with
 *            {@link javax.persistence.Entity})
 * @param <D> type of the attribute
 * @author Fabiel Zuniga
 */
class JpqlEqualityCondition<P, D> implements JpqlPredicate {

    private Class<P> entityClass;
    private EqualityCondition<D> condition;
    private SingularAttribute<? super P, D> attribute;
    private String valueParameter;

    /**
     * Creates an equality predicate.
     * 
     * @param condition equality condition
     * @param attribute attribute to apply the condition to
     * @param entityClass persistent object class
     */
    public JpqlEqualityCondition(EqualityCondition<D> condition, SingularAttribute<? super P, D> attribute,
        Class<P> entityClass) {
        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (attribute == null) {
            throw new NullPointerException("attribute cannot be null");
        }

        if (entityClass == null) {
            throw new NullPointerException("entityClass cannot be null");
        }

        this.condition = condition;
        this.attribute = attribute;
        this.entityClass = entityClass;

        this.valueParameter = attribute.getName() + "Value";
    }

    @Override
    public String getPredicate() {
        String attributeNameInQuery = JpqlUtil.getNameInQuery(this.attribute, this.entityClass);

        StringBuilder str = new StringBuilder(64);

        str.append(attributeNameInQuery);

        if (this.condition.getValue() != null) {
            switch (this.condition.getMode()) {
                case EQUAL:
                    str.append(" = ");
                    break;
                case UNEQUAL:
                    str.append(" != ");
                    break;
            }
            str.append(JpqlUtil.getValueParameterInQuery(this.valueParameter));
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

    @Override
    public void addParameters(Query query) {
        if (this.condition.getValue() != null) {
            query.setParameter(this.valueParameter, this.condition.getValue());
        }
    }
}
