/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import com.hp.util.model.persistence.PersistenceException;

/**
 * Groups several {@link CassandraClient}'s write operations into a batch.
 * <p>
 * A batch is used in case Write Ahead Log strategies are implemented in the future. There is no
 * difference between using or not a Batch operation other than write ahead log strategies since a
 * batch operation is not atomic.
 * <p>
 * This batch will record {@link CassandraClient}'s write operations to be executed later together.
 * Read operations won't be affected and they will be executed right away by the
 * {@link CassandraClient}.
 * <p>
 * All write operations included in a batch should be part of the same unit or work or same
 * {@link CassandraContext} and in the same thread ({@link Batch} is not thread-safe). All the
 * operations will be executed using the same consistency level.
 * <p>
 * Example:
 * 
 * <pre>
 * CassandraContext&lt;N&gt; context = ...;
 * 
 * // Regular isolated operation
 * cassandraClient.insert(..., context);
 * 
 * // Batch operations
 * Batch&lt;N&gt; batch = cassandraClient.prepareBatch(context);
 * cassandraContext&lt;N&gt; batchContext = batch.start();
 * cassandraClient.insert(..., batchContext);
 * ...
 * cassandraClient.delete(..., batchContext);
 * batch.execute();
 * 
 * <pre>
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public interface Batch<N> {

    /**
     * Starts the batch.
     * 
     * @return the batch {@link CassandraContext} to use on {@link CassandraClient}'s write
     *         operations. This context will record write operations so they are executed as a batch
     *         later using {@link #execute()}. This context may also be used to perform read
     *         operations, however it is recommended to use it just on write operations: once the
     *         batch is executed this context should no longer be used on write operations (again it
     *         could still be used on read operations but it is recommended to dispose it after
     *         executing the batch). It is not recommended to keep the context after executing the
     *         batch because it could mistakenly be used on write operations.
     */
    public CassandraContext<N> start();

    /**
     * Executes the mutation batch.
     * <p>
     * After calling this method the {@link CassandraContext} returned by {@link #start()} should no
     * longer be used on write operations. It may however be used to execute read operations but it
     * is not recommended. It is not recommended to keep the context after executing the batch
     * because it could mistakenly be used on write operations.
     * 
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public void execute() throws PersistenceException;
}
