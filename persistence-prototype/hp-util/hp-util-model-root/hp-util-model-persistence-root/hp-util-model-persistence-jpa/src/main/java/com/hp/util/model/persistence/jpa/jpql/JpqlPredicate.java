/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

import javax.persistence.Query;

/**
 * Java Persistence Query Language predicate.
 * <P>
 * This predicate is used to create queries using JPQL, for example:
 * <P>
 * This type-unsafe way of creating queries is needed because Kundera does not support
 * CriteriaBuilder. CriteriaBuilder is used to create type safe queries in JPA 2.0.
 * 
 * <Pre>
 * EntityManager entityManager = ...;
 * Query query = entityManager.createQuery("SELECT c FROM Customer c WHERE c.name LIKE :custName");
 * query.setParameter("custName", name);
 * query.setMaxResults(10);
 * query.getResultList();
 * </Pre>
 * 
 * @author Fabiel Zuniga
 */
public interface JpqlPredicate {

    /**
     * Gets the predicate to be included in a query,
     * 
     * @return the predicate to include in the query
     */
    public String getPredicate();

    /**
     * Sets any parameter used in the predicate.
     * 
     * @param query query to set parameters to
     */
    public void addParameters(Query query);
}
