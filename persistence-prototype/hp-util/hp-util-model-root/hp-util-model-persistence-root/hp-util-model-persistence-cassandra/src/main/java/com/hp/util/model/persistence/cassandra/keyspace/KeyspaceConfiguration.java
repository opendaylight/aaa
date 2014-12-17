/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Keyspace configuration.
 * 
 * @author Fabiel Zuniga
 */
public class KeyspaceConfiguration {

    private final Strategy strategy;
    private final int replicationFactor;

    /**
     * Creates a keyspace configuration.
     *
     * @param strategy strategy
     * @param replicationFactor replication factor
     */
    public KeyspaceConfiguration(Strategy strategy, int replicationFactor) {
        if (strategy == null) {
            throw new NullPointerException("strategy cannot be null");
        }

        if (replicationFactor <= 0) {
            throw new IllegalArgumentException("replicationFactor must be greater than zero");
        }

        this.strategy = strategy;
        this.replicationFactor = replicationFactor;
    }

    /**
     * Gets the strategy.
     *
     * @return the strategy
     */
    public Strategy getStrategy() {
        return this.strategy;
    }

    /**
     * Gets the replication factor.
     *
     * @return the replication factor
     */
    public int getReplicationFactor() {
        return this.replicationFactor;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("strategy", this.strategy),
                Property.valueOf("replicationFactor", Integer.valueOf(this.replicationFactor))
        );
    }
}
