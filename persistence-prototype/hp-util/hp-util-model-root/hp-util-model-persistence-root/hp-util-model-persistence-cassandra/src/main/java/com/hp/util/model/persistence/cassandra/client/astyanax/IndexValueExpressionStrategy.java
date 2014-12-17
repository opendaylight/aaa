/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.io.Serializable;

import com.netflix.astyanax.query.IndexQuery;
import com.netflix.astyanax.query.IndexValueExpression;

/**
 * Index value expression strategy.
 * 
 * @param <K> type of the row key
 * @param <C> type of the column name or column key
 * @param <D> type of the column value
 * @author Fabiel Zuniga
 */
interface IndexValueExpressionStrategy<K extends Serializable, C extends Serializable & Comparable<C>, D> {

    /**
     * Gets the index query to execute.
     * 
     * @param indexValueExpression index value expression
     * @param value value to create the index query with. It is assumed value is not {@code null}.
     * @return the index query to execute
     */
    public IndexQuery<K, C> getIndexQuery(IndexValueExpression<K, C> indexValueExpression, D value);
}
