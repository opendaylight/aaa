/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

/**
 * Shard strategy to distribute column names into several row keys based on a hash function applied
 * to the column value.
 * 
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public class HashShardStrategy<C> implements ColumnShardStrategy<String, C> {

    private final String rowKeyPrefix;
    private final int partition;

    /**
     * Creates a shard strategy.
     * 
     * @param rowKeyPrefix row key prefix
     * @param partition the number of rows we want to split the data across
     */
    public HashShardStrategy(String rowKeyPrefix, int partition) {
        if (rowKeyPrefix == null || rowKeyPrefix.isEmpty()) {
            throw new IllegalArgumentException("rowKeyPrefix cannot be null nor empty");
        }

        if (partition <= 1) {
            throw new IllegalArgumentException("partition must be greater than 1");
        }

        this.rowKeyPrefix = rowKeyPrefix + "_";
        this.partition = partition;
    }

    @Override
    public String getShard(C columnName) {
        if (columnName == null) {
            throw new NullPointerException("columnName cannot be null");
        }

        int rowKeyIndex = Math.abs(columnName.hashCode() % this.partition);
        return this.rowKeyPrefix + rowKeyIndex;
    }
}
