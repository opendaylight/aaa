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
 * When you do a write in Cassandra, the consistency level specifies on how many replicas the write
 * must succeed before returning an acknowledgement to the client application.
 * <P>
 * The following consistency levels are available, with ANY being the lowest consistency (but
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
public enum WriteConsistencyLevel {
    /**
     * A write must be written to at least one node. If all replica nodes for the given row key are
     * down, the write can still succeed once a hinted handoff has been written. Note that if all
     * replica nodes are down at write time, an ANY write will not be readable until the replica
     * nodes for that row key have recovered.
     */
    ANY(ConsistencyLevel.CL_ANY),
    /**
     * A write must be written to the commit log and memory table of at least one replica node.
     */
    ONE(ConsistencyLevel.CL_ONE),
    /**
     * A write must be written to the commit log and memory table on a quorum of replica nodes.
     */
    QUORUM(ConsistencyLevel.CL_QUORUM),
    /**
     * A write must be written to the commit log and memory table on a quorum of replica nodes
     * in the same data center as the coordinator node. Avoids latency of inter-data center
     * communication.
     */
    LOCAL_QUORUM(ConsistencyLevel.CL_LOCAL_QUORUM),
    /**
     * A write must be written to the commit log and memory table on a quorum of replica nodes
     * in all data centers.
     */
    EACH_QUORUM(ConsistencyLevel.CL_EACH_QUORUM),
    /**
     * A write must be written to the commit log and memory table on all replica nodes in the
     * cluster for that row key.
     */
    ALL(ConsistencyLevel.CL_ALL);

    private ConsistencyLevel astyanaxLevel;

    private WriteConsistencyLevel(ConsistencyLevel astyanaxLevel) {
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
