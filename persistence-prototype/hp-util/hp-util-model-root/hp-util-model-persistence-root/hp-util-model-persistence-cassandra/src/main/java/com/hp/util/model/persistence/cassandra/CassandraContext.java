/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import com.hp.util.model.persistence.cassandra.client.astyanax.AstyanaxCassandraContextAccessor;
import com.hp.util.model.persistence.cassandra.keyspace.Keyspace;

/**
 * Cassandra query context.
 * 
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public class CassandraContext<N> {

    static {
        AstyanaxCassandraContextAccessor.setDefault(new CassandraContextAccessorAstyanaxImpl());
    }

    private final Keyspace keyspace;
    private final CassandraClient<N> cassandraClient;
    private final N nativeClient;
    private ReadConsistencyLevel readConsistencyLevel;
    private WriteConsistencyLevel writeConsistencyLevel;

    CassandraContext(Keyspace keyspace, CassandraClient<N> cassandraClient, N nativeClient) {
        this.keyspace = keyspace;
        this.cassandraClient = cassandraClient;
        this.nativeClient = nativeClient;
        this.readConsistencyLevel = ReadConsistencyLevel.ONE;
        this.writeConsistencyLevel = WriteConsistencyLevel.ANY;
    }

    /**
     * Returns the keyspace.
     * 
     * @return the keyspace
     */
    public Keyspace getKeyspace() {
        return this.keyspace;
    }

    /**
     * Returns the cassandra client.
     * 
     * @return the cassandra client
     */
    public CassandraClient<N> getCassandraClient() {
        return this.cassandraClient;
    }

    /**
     * Returns the native Cassandra client.
     * 
     * @return the native Cassandra client.
     */
    public N getNativeClient() {
        return this.nativeClient;
    }

    /**
     * Returns the consistency level for reads.
     * 
     * @return read consistency level
     */
    public ReadConsistencyLevel getReadConsistencyLevel() {
        return this.readConsistencyLevel;
    }

    /**
     * Sets the consistency level for reads.
     * 
     * @param level read consistency level
     */
    public void setReadConsistencyLevel(ReadConsistencyLevel level) {
        this.readConsistencyLevel = level;
    }

    /**
     * Returns the consistency level for writes.
     * 
     * @return write consistency level
     */
    public WriteConsistencyLevel getWriteConsistencyLevel() {
        return this.writeConsistencyLevel;
    }

    /**
     * Sets the consistency level for writes.
     * 
     * @param level write consistency level
     */
    public void setWriteConsistencyLevel(WriteConsistencyLevel level) {
        this.writeConsistencyLevel = level;
    }
}
