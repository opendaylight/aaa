/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;

import com.hp.util.common.ShardStrategy;

/**
 * Strategy used to distribute columns into multiple rows so they are handled by different nodes in
 * Cassandra.
 * <p>
 * Writing always to one key (when using wide rows in secondary indexes) means that all writes for
 * that key will go to one node.
 * <p>
 * Wide rows also have a 2 billion size restriction. So if a row could have more than 2 billion
 * columns applying a shard strategy is mandatory.
 * 
 * @param <K> type of the row key to implement the shard strategy on
 * @param <C> type of the column name or column key
 * @author Fabiel Zuniga
 */
public interface ColumnShardStrategy<K extends Serializable, C> extends ShardStrategy<K, C> {

}
