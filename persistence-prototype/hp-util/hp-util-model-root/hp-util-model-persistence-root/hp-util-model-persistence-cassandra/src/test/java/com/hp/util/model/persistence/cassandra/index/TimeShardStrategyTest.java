/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.util.Calendar;
import java.util.TimeZone;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.cassandra.index.TimeShardStrategy.ShardSize;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class TimeShardStrategyTest {

    @Test
    public void testConstruction() {
        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new TimeShardStrategy(null, TimeZone.getDefault());
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new TimeShardStrategy(ShardSize.DAILY, null);
            }
        });
    }

    @Test
    public void testGetShardInvalidColumnName() {
        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new TimeShardStrategy(ShardSize.HOURLY, TimeZone.getDefault()).getShard(null);
            }
        });
    }

    @Test
    public void testGetShardSecondly() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 0, 11, 9, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.SECONDLY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("20130111093059", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("20130111093059", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_20130111093059", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("20130111113059", shardStrategy.getShard(utcTime));
    }

    @Test
    public void testGetShardMinutely() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 0, 11, 9, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.MINUTELY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("201301110930", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("201301110930", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_201301110930", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("201301111130", shardStrategy.getShard(utcTime));
    }

    @Test
    public void testGetShardHourly() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 0, 11, 9, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.HOURLY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("2013011109", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("2013011109", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_2013011109", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("2013011111", shardStrategy.getShard(utcTime));
    }

    @Test
    public void testGetShardDaily() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 0, 11, 23, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.DAILY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("20130111", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("20130111", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_20130111", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("20130112", shardStrategy.getShard(utcTime));
    }

    @Test
    public void testGetShardWeekly() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 0, 12, 23, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.WEEKLY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("20130106w", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("20130106w", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_20130106w", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("20130113w", shardStrategy.getShard(utcTime));
    }

    @Test
    public void testGetShardMonthly() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 0, 31, 23, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.MONTHLY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("201301", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("201301", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_201301", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("201302", shardStrategy.getShard(utcTime));
    }

    @Test
    public void testGetShardYearly() {
        TimeZone timeZone = TimeZone.getDefault();

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.set(2013, 11, 31, 23, 30, 59);
        Date utcTime = Date.valueOf(calendar.getTime());

        ShardSize shardSize = ShardSize.YEARLY;

        // No prefix
        TimeShardStrategy shardStrategy = new TimeShardStrategy(shardSize, timeZone);
        Assert.assertEquals("2013", shardStrategy.getShard(utcTime));

        // Empty prefix
        shardStrategy = new TimeShardStrategy(shardSize, "", timeZone);
        Assert.assertEquals("2013", shardStrategy.getShard(utcTime));

        // Prefix
        shardStrategy = new TimeShardStrategy(shardSize, "my_prefix", timeZone);
        Assert.assertEquals("my_prefix_2013", shardStrategy.getShard(utcTime));

        // Changing type zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        shardStrategy = new TimeShardStrategy(shardSize, twoHoursAheadTimeZone);
        Assert.assertEquals("2014", shardStrategy.getShard(utcTime));
    }

    private static TimeZone getTimeZone(TimeZone reference, Measurable<Duration> offset) {
        int offsetMilliseconds = (int) offset.longValue(SI.MILLI(SI.SECOND));
        assert (offsetMilliseconds == offset.longValue(SI.MILLI(SI.SECOND)));
        String[] ids = TimeZone.getAvailableIDs(reference.getRawOffset() + offsetMilliseconds);
        if (ids == null || ids.length <= 0) {
            throw new RuntimeException("Invalid offeset");
        }
        return TimeZone.getTimeZone(ids[0]);
    }
}
