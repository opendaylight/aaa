/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.cql;

/**
 * Cassandra Query Language predicate.
 * <P>
 * This predicate is used to create queries using CQL.
 * <P>
 * CQL is the Cassandra equivalent to SQL but only supports a small subset of the SQL syntax.
 * 
 * @author Fabiel Zuniga
 */
public interface CqlPredicate {

    /**
     * Gets the predicate to be included in a query.
     * 
     * @return the predicate to include in the query
     */
    public String getPredicate();
}
