/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class HashShardStrategyTest {

    @Test
    public void testConstruction() {
        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new HashShardStrategy<String>(null, 10);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new HashShardStrategy<String>("", 10);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new HashShardStrategy<String>("my_prefix", 1);
            }
        });
    }

    @Test
    public void testGetShard() {
        final String prefix = "my_prefix";
        final int partition = 10;

        Set<String> expectedShardedKeys = new HashSet<String>();
        for (int i = 0; i < partition; i++) {
            expectedShardedKeys.add(prefix + "_" + i);
        }

        HashShardStrategy<Integer> shardStrategy1 = new HashShardStrategy<Integer>(prefix, partition);

        for (int i = 0; i < 1000; i++) {
            String shardedRowKey = shardStrategy1.getShard(Integer.valueOf(i));
            Assert.assertTrue(expectedShardedKeys.contains(shardedRowKey));

            shardedRowKey = shardStrategy1.getShard(Integer.valueOf(-i));
            Assert.assertTrue(expectedShardedKeys.contains(shardedRowKey));
        }

        HashShardStrategy<String> shardStrategy2 = new HashShardStrategy<String>(prefix, partition);

        for (int i = 0; i < 1000; i++) {
            String shardedRowKey = shardStrategy2.getShard("Some string " + Integer.valueOf(i));
            Assert.assertTrue(expectedShardedKeys.contains(shardedRowKey));

            shardedRowKey = shardStrategy1.getShard(Integer.valueOf(-i));
            Assert.assertTrue(expectedShardedKeys.contains(shardedRowKey));
        }
    }

    @Test
    public void testGetShardInvalidColumnName() {
        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new HashShardStrategy<String>("my_prefix", 10).getShard(null);
            }
        });
    }
}
