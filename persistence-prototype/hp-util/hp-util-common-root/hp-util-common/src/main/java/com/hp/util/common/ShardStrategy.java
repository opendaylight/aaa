/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Shard strategy.
 * <p>
 * http://en.wikipedia.org/wiki/Shard_(database_architecture):
 * <p>
 * A database shard is a horizontal partition of data in a database or search engine. Each
 * individual partition is referred to as a shard or database shard. Each shard is held on a
 * separate database server instance, to spread load. Some data within a database remains present in
 * all shards, but some only appears in a single shard. Each shard (or server) acts as the single
 * source for this subset of data
 * 
 * @param <S> type of the shard
 * @param <D> type of the data to partition
 * @author Fabiel Zuniga
 */
public interface ShardStrategy<S, D> {

    /**
     * Gets the shard or partition the data belongs to.
     * 
     * @param data data to calculate the shard for
     * @return the shard the data belongs to
     */
    public S getShard(D data);
}
