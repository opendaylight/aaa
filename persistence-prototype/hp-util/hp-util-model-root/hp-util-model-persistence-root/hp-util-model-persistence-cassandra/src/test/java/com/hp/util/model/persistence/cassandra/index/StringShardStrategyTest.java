/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class StringShardStrategyTest {

    @Test
    public void testGetShardWithPrefix() {
        StringShardStrategy shardStrategy = new StringShardStrategy("my_prefix");

        Assert.assertEquals("my_prefix_h", shardStrategy.getShard("Hello World"));
        Assert.assertEquals("my_prefix_h", shardStrategy.getShard("hello world"));
        Assert.assertEquals("my_prefix_9", shardStrategy.getShard("916"));
        Assert.assertEquals("my_prefix_special", shardStrategy.getShard("_"));
        Assert.assertEquals("my_prefix_special", shardStrategy.getShard(" "));
        Assert.assertEquals("my_prefix_special", shardStrategy.getShard("~"));
        Assert.assertEquals("my_prefix_special", shardStrategy.getShard("  "));
        Assert.assertEquals("my_prefix_special", shardStrategy.getShard("\n"));
    }

    @Test
    public void testGetShardWithNoPrefix() {
        StringShardStrategy shardStrategy = new StringShardStrategy();

        Assert.assertEquals("h", shardStrategy.getShard("Hello World"));
        Assert.assertEquals("h", shardStrategy.getShard("hello world"));
        Assert.assertEquals("9", shardStrategy.getShard("916"));
        Assert.assertEquals("special", shardStrategy.getShard("_"));
        Assert.assertEquals("special", shardStrategy.getShard(" "));
        Assert.assertEquals("special", shardStrategy.getShard("~"));
        Assert.assertEquals("special", shardStrategy.getShard("  "));
        Assert.assertEquals("special", shardStrategy.getShard("\n"));
    }

    @Test
    public void testGetShardWithEmptyPrefix() {
        StringShardStrategy shardStrategy = new StringShardStrategy("");

        Assert.assertEquals("h", shardStrategy.getShard("Hello World"));
        Assert.assertEquals("h", shardStrategy.getShard("hello world"));
        Assert.assertEquals("9", shardStrategy.getShard("916"));
        Assert.assertEquals("special", shardStrategy.getShard("_"));
        Assert.assertEquals("special", shardStrategy.getShard(" "));
        Assert.assertEquals("special", shardStrategy.getShard("~"));
        Assert.assertEquals("special", shardStrategy.getShard("  "));
        Assert.assertEquals("special", shardStrategy.getShard("\n"));
    }

    @Test
    public void testGetShardInvalidColumnName() {
        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new StringShardStrategy("my_prefix").getShard(null);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new StringShardStrategy().getShard(null);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new StringShardStrategy("my_prefix").getShard("");
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new StringShardStrategy().getShard("");
            }
        });
    }
}
