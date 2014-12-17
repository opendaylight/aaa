/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

import java.util.Set;

import javax.persistence.Query;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.filter.SetCondition;

/**
 * Set condition predicate.
 * 
 * @param <P> type of the entity type of the entity (an object annotated with
 *            {@link javax.persistence.Entity})
 * @param <D> type of the attribute
 * @author Fabiel Zuniga
 */
class JpqlSetCondition<P, D> implements JpqlPredicate {

    private JpqlPredicate delegate;

    /**
     * Creates an equality predicate.
     * 
     * @param condition set condition
     * @param attribute attribute to apply the condition to
     * @param entityClass persistent object class
     */
    public JpqlSetCondition(SetCondition<D> condition, SingularAttribute<? super P, D> attribute, Class<P> entityClass) {
        if (condition == null) {
            throw new NullPointerException("condition cannot be null");
        }

        if (attribute == null) {
            throw new NullPointerException("attribute cannot be null");
        }

        if (entityClass == null) {
            throw new NullPointerException("entityClass cannot be null");
        }

        this.delegate = new SetPredicate<P, D>(condition.getValues(), attribute, entityClass);
        if (condition.getMode().equals(SetCondition.Mode.NOT_IN)) {
            this.delegate = new JpqlNot(this.delegate);
        }
    }

    @Override
    public String getPredicate() {
        return this.delegate.getPredicate();
    }

    @Override
    public void addParameters(Query query) {
        this.delegate.addParameters(query);
    }

    private static class SetPredicate<P, D> implements JpqlPredicate {

        private Set<D> set;
        private SingularAttribute<? super P, D> attribute;
        private Class<P> entityClass;
        private String valueParameter;

        protected SetPredicate(Set<D> set, SingularAttribute<? super P, D> attribute, Class<P> entityClass) {
            this.set = set;
            this.attribute = attribute;
            this.entityClass = entityClass;
            this.valueParameter = attribute.getName() + "Values";
        }

        @Override
        public String getPredicate() {
            String attributeNameInQuery = JpqlUtil.getNameInQuery(this.attribute, this.entityClass);
            StringBuilder str = new StringBuilder(32);
            str.append(attributeNameInQuery);
            str.append(" In ");
            str.append(JpqlUtil.getValueParameterInQuery(this.valueParameter));
            return str.toString();
        }

        @Override
        public void addParameters(Query query) {
            query.setParameter(this.valueParameter, this.set);
        }
    }
}
