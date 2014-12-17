/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra;

import com.netflix.astyanax.model.ConsistencyLevel;

/**
 * Read consistency level.
 * <p>
 * In Cassandra, consistency refers to how up-to-date and synchronized a row of data is on all of
 * its replicas. Cassandra extends the concept of eventual consistency by offering tunable
 * consistency. For any given read or write operation, the client application decides how consistent
 * the requested data should be.
 * <P>
 * In addition to tunable consistency, Cassandra has a number of built-in repair mechanisms to
 * ensure that data remains consistent across replicas.
 * <P>
 * Consistency levels in Cassandra can be set on any read or write query. This allows application
 * developers to tune consistency on a per-query basis depending on their requirements for response
 * time versus data accuracy. Cassandra offers a number of consistency levels for both reads and
 * writes.
 * <P>
 * When you do a read in Cassandra, the consistency level specifies how many replicas must respond
 * before a result is returned to the client application.
 * <P>
 * Cassandra checks the specified number of replicas for the most recent data to satisfy the read
 * request (based on the timestamp).
 * <P>
 * The following consistency levels are available, with ONE being the lowest consistency (but
 * highest availability), and ALL being the highest consistency (but lowest availability). QUORUM is
 * a good middle-ground ensuring strong consistency, yet still tolerating some level of failure.
 * <P>
 * A quorum is calculated as (rounded down to a whole number): {@code (replication_factor / 2) + 1}
 * <P>
 * For example, with a replication factor of 3, a quorum is 2 (can tolerate 1 replica down). With a
 * replication factor of 6, a quorum is 4 (can tolerate 2 replicas down).
 * 
 * @author Fabiel Zuniga
 */
public enum ReadConsistencyLevel {
    /**
     * Returns a response from the closest replica. By default, a read repair runs
     * in the background to make the other replicas consistent.
     */
    ONE(ConsistencyLevel.CL_ONE),
    /**
     * Returns the record with the most recent timestamp once a quorum of replicas has responded.
     */
    QUORUM(ConsistencyLevel.CL_QUORUM),
    /**
     * Returns the record with the most recent timestamp once a quorum of replicas in the current
     * data center as the coordinator node has reported. Avoids latency of inter-data center
     * communication.
     */
    LOCAL_QUORUM(ConsistencyLevel.CL_LOCAL_QUORUM),
    /**
     * Returns the record with the most recent timestamp once a quorum of replicas in each data
     * center of the cluster has responded.
     */
    EACH_QUORUM(ConsistencyLevel.CL_EACH_QUORUM),
    /**
     * Returns the record with the most recent timestamp once all replicas have responded. The
     * read operation will fail if a replica does not respond.
     */
    ALL(ConsistencyLevel.CL_ALL);

    private ConsistencyLevel astyanaxLevel;

    private ReadConsistencyLevel(ConsistencyLevel astyanaxLevel) {
        this.astyanaxLevel = astyanaxLevel;
    }

    /**
     * Converts this enumeration to the corresponding in Astyanax.
     * 
     * @return astyanax consistency level
     */
    public ConsistencyLevel toAstyanaxLevel() {
        return this.astyanaxLevel;
    }
}
