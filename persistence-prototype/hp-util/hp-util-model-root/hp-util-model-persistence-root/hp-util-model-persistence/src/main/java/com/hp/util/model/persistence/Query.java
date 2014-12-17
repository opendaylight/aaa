/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence;

/**
 * Query to perform persistence operations.
 * 
 * @param <T> type of the query's result
 * @param <C> type of the query's execution context. This context should provide everything the
 *            query needs to perform persistence operations (A Database connection for example).
 * @see DataStore
 * @author Robert Gagnon
 * @author Fabiel Zuniga
 */
public interface Query<T, C> {

    /*
     * Note: "ReadQuery" and "WriteQuery" were unified into just "Query" because the user could
     * define anything as ReadQuery or WriteQuery. It is not possible to make sure a ReadQuery will
     * just perform read operations. If the DataStore implementation has special considerations for
     * either read or writes (for example creating transactions just for write queries), then
     * decorators could be provided by the DataStore implementation. See the following classes for
     * examples: com.hp.util.model.persistence.query.QueryLoggerDecorator,
     * com.hp.util.model.persistence.query.QueryModelDecorator,
     * com.hp.util.model.persistence.cassandra.query.TunableReadConsistencyQueryDecorator and
     * com.hp.util.model.persistence.cassandra.query.TunableWriteConsistencyQueryDecorator.
     */

    /**
     * Executes the query.
     * 
     * @param context query execution context. This context should be considered valid just for the
     *            execution of this query. After the query finish execution the context may be
     *            destroyed.
     * @return the result of the query
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public T execute(C context) throws PersistenceException;
}
