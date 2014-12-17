/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.query;

import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.WriteConsistencyLevel;

/**
 * Query decorator to tune the write consistency level.
 * 
 * @param <T> type of the query result
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public class TunableWriteConsistencyQueryDecorator<T, N> implements Query<T, CassandraContext<N>> {

    private Query<T, CassandraContext<N>> delegate;
    private WriteConsistencyLevel consistencyLevel;

    /**
     * Creates a write query decorator.
     * 
     * @param delegate query delegate
     * @param consistencyLevel write consistency level
     */
    public TunableWriteConsistencyQueryDecorator(Query<T, CassandraContext<N>> delegate,
        WriteConsistencyLevel consistencyLevel) {
        this.delegate = delegate;
        this.consistencyLevel = consistencyLevel;
    }

    @Override
    public T execute(CassandraContext<N> context) throws PersistenceException {
        context.setWriteConsistencyLevel(this.consistencyLevel);
        return this.delegate.execute(context);
    }
}
