/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import com.netflix.astyanax.Cluster;

/**
 * Astyanax as native Cassandra client.
 * 
 * @author Fabiel Zuniga
 */
public class Astyanax {

    private final com.netflix.astyanax.Keyspace keyspaceClient;
    private final Cluster clusterClient;

    /**
     * Creates a native Cassandra client.
     * 
     * @param keyspaceClient keyspace client
     * @param clusterClient cluster client
     */
    // TODO: Make this package private when Batch implementations is moved to
    // AstyanaxCassandraClient
    public Astyanax(com.netflix.astyanax.Keyspace keyspaceClient, Cluster clusterClient) {
        this.keyspaceClient = keyspaceClient;
        this.clusterClient = clusterClient;
    }

    /**
     * Gets the Astyanax keyspace.
     * 
     * @return the keyspace
     */
    public com.netflix.astyanax.Keyspace getKeyspace() {
        return this.keyspaceClient;
    }

    /**
     * Gets the Astyanax cluster.
     * 
     * @return the cluster
     */
    public Cluster getCluster() {
        return this.clusterClient;
    }
}
