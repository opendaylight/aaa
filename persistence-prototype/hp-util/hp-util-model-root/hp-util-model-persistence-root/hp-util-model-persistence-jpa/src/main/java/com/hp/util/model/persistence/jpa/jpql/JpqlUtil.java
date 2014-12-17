/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Java Persistence Query Language utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class JpqlUtil {

    private JpqlUtil() {

    }

    /**
     * Gets the name of the table to use in a query.
     * <P>
     * For example: 'Person' is the name of the table in the following query.
     *
     * <Pre>
     * SELECT p FROM Person p WHERE p.name LIKE :paramValue
     * </Pre>
     *
     * @param entityClass persistent object class
     * @return the persistent object name in a query
     */
    public static <P> String getTableNameInQuery(Class<P> entityClass) {
        return entityClass.getSimpleName();
    }

    /**
     * Gets the alias of an entity in a query.
     * <P>
     * For example: 'p' is the name of the entity in the following query
     * 
     * <Pre>
     * SELECT p FROM Person p WHERE p.name LIKE :paramValue
     * </Pre>
     * 
     * @param entityClass persistent object class
     * @return the persistent object name in a query
     */
    public static <P> String getAliasInQuery(Class<P> entityClass) {
        return String.valueOf(entityClass.getSimpleName().toLowerCase().charAt(0));
    }

    /**
     * Gets the name of a persistent object's attribute in a query.
     * <P>
     * For example: 'p.name' is the name of the 'name' attribute in the following query.
     * 
     * <Pre>
     * SELECT p FROM Person p WHERE p.name LIKE :paramValue
     * </Pre>
     * 
     * @param attribute attribute to get the name for
     * @param entityClass persistent object class
     * @return the persistent object name in a query
     */
    public static <P> String getNameInQuery(SingularAttribute<? super P, ?> attribute,
        Class<P> entityClass) {
        return getAliasInQuery(entityClass) + '.' + attribute.getName();
    }

    /**
     * Gets a value parameter to use in a query.
     * <P>
     * For example: ':paramValue' is the value parameter for 'name' in the following query.
     * 
     * <Pre>
     * SELECT p FROM Person p WHERE p.name LIKE :paramValue
     * </Pre>
     * 
     * @param valueParameter value parameter
     * @return the value parameter to use in a query
     */
    public static String getValueParameterInQuery(String valueParameter) {
        return ":" + valueParameter;
    }
}
