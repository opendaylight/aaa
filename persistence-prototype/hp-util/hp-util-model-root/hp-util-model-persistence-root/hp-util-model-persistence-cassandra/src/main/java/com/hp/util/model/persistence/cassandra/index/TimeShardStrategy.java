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

import com.hp.util.common.type.Date;
import com.hp.util.common.type.TimePeriod;

/**
 * Shard strategy to distribute column names into several row keys based on time.
 * <p>
 * The following iformation was taken from
 * http://rubyscale.com/blog/2011/03/06/basic-time-series-with-cassandra/ and
 * http://www.datastax.com/dev/blog/advanced-time-series-with-cassandra.
 * <p>
 * Cassandra is an excellent fit for time series data, and it's widely used for storing many types
 * of data that follow the time series pattern: performance metrics, fleet tracking, sensor data,
 * logs, financial data (pricing and ratings histories), user activity, and so on.
 * <p>
 * The most basic and intuitive way to go about storing time-series data is to use a column family
 * that has TimeUUID columns or composite columns (Date, Id), or Long if you know that no two
 * entries will happen at the same timestamp. Use the name of the thing you are monitoring as the
 * row_key (server1-load for example), column_name as the timestamp, and the column_value would be
 * the actual value of the thing (0.75 for example):
 * 
 * <pre>
 * Inserting data ' {:key => 'server1-load', :column_name => TimeUUID(now), :column_value => 0.75}
 * </pre>
 * 
 * Using this method, one uses a column_slice to get the data in question:
 * 
 * <pre>
 * Load at Time X '  {:key => 'server1-load', :start => TimeUUID(X), :count => 1}
 * Load between X and Y ' {:key => 'server1-load', :start => TimeUUID(X), :end => TimeUUID(Y)}
 * </pre>
 * 
 * This works well enough for a while, but over time, this row will get very large. If you are
 * storing sensor data that updates hundreds of times per second, that row will quickly become
 * gigantic and unusable. The answer to that is to shard the data up in some way. For example, we'll
 * pick a day as our shard interval. The only change we make when we insert our data is to add a day
 * to the row-key:
 * 
 * <pre>
 * Inserting data ' {:key => 'server1-load-20110306', :column_name => TimeUUID(now), :column_value => 0.75}
 * </pre>
 * 
 * Using this method, one still uses a column slice, but you have to then also specify a different
 * row_key depending on what you are querying:
 * 
 * <pre>
 * Load at Time X ' {:key => 'server1-load-[X.strftime]', :start => TimeUUID(X), :count => 1}
 * Load between Time X and Y (if X and Y are on the same day) ' {:key => 'server1-load-[X.strftime]', :start => TimeUUID(X), :end => TimeUUID(Y)}
 * </pre>
 * 
 * If X and Y are not on the same day you can use a multi-get to fetch more than one key at a time
 * (or issue parallel gets for maximum performance). If your X and Y span two days, you just need to
 * generate keys for those two days and issue them in a multiget:
 * 
 * <pre>
 * Load between Time X and Y ' {:key => ['server1-load-[X.strftime]', 'server1-load-[Y.strftime]'], :start => TimeUUID(X), :end => TimeUUID(Y)}
 * </pre>
 * 
 * Then in your application, you will need to aggregate/concatenate/iterate those two rows however
 * you see fit. If your data spans 3 or more days, you'll need to also generate every key in
 * between. Don't be tempted to use the Order-Preserving Partitioner here, it won't save you that
 * much typing and it'll will make managing your cluster much more difficult. </pre> <b>About time
 * series:</b>
 * <p>
 * <b>Timeline Starting Points:</b>
 * <p>
 * To support queries that ask for all events before a given time, your application usually needs to
 * know when the timeline was first started. Otherwise, if you aren't guarenteed to have events in
 * every bucket, you cannot just fetch buckets further and further back in time until you get back
 * an empty row; there's no way to distinguish between a bucket that just happens to contain no
 * events and one that falls before the timeline even began.
 * <p>
 * To prevent uneccessary searching through empty rows, we can keep track of when the earliest event
 * was inserted for a given timeline using a metadata row. When an application writes to a timeline
 * for the first time after starting up, it can read the metadata row, find out the current earliest
 * timestamp, and write a new timestamp if it ever inserts an earlier event. To avoid race
 * conditions, add a new column to the metadata row each time a new earliest event is inserted. I
 * suggest using TimeUUIDs with a timestamp matching the event's timestamp for the column name so
 * that the earliest timestamp will always be at the beginning of the metadata row.
 * <p>
 * After reading only the first column from the metadata row (either on startup or the first time
 * it's required, refreshing periodically), the application can know exactly how far in the past it
 * should look for events in a given timeline.
 * <p>
 * <b>High Throughput Timelines:</b>
 * <p>
 * Each row in a timeline will be handled by a single set of replicas, so they may become hotspots
 * while the row holding the current time bucket falls in their range. It's not very common, but
 * occasionally a single timeline may grow at such a rate that a single node cannot easily handle
 * it. This may happen if tens of thousands of events are being inserted per second or at a lower
 * rate if the column values are large. Sometimes, by reducing the size of the time bucket enough, a
 * single set of replicas will only have to ingest writes for a short enough period of time that the
 * throughput is sustainable, but this isn't always a feasible option.
 * <p>
 * In order to spread the write load among more nodes in the cluster, we can split each time bucket
 * into multiple rows. We can use row keys of the form
 * &lt;timeline&gt;:&lt;bucket&gt;:&lt;partition&gt;, where partition is a number between 1 and the
 * number of rows we want to split the bucket across. When writing, clients should append new events
 * to each of the partitions in round robin fashion so that all partitions grow at a similar rate.
 * When reading, clients should fetch slices from all of the partition rows for the time bucket they
 * are interested in and merge the results client-side, similar to the merge step of merge-sort.
 * <p>
 * If some timelines require splitting while others do not, or if you need to be able to adjust the
 * number of rows a timeline is split across periodically, It is suggested storing info about the
 * splits in a metadata row for the timeline in a separate column family. The metadata row might
 * have one column for each time the splitting factor is adjusted, something like
 * {&lt;timestamp&gt;: &lt;splitting_factor&gt;}, where timestamp should align with the beginning of
 * a time bucket after which clients should use the new splitting factor. When reading a time slice,
 * clients can know how many partition rows to ask for during a given range of time based on this
 * metadata.
 * <p>
 * <b>Variable Time Bucket Sizes:</b>
 * <p>
 * For some applications, the rate of events for different timelines may differ drastically. If some
 * timelines have an incoming event rate that is 100x or 1000x higher than other timelines, you may
 * want to use a different time bucket size for different timelines to prevent extremely wide rows
 * for the busy timelines or a very sparse set of rows for the slow timelines. In other cases, a
 * single timeline may increase or decrease its rate of events over time; eventually, this timeline
 * may need to change its bucket size to keep rows from growing too wide or too sparse.
 * <p>
 * Similar to the timeline metadata suggestion for high throughput timelines (above), we can track
 * time bucket sizes and their changes for individual timelines with a metadata row. Use a column of
 * the form {&lt;timestamp&gt;: &lt;bucket_size&gt;}, where timestamp aligns with the start of a
 * time bucket, and bucket_size is the bucket size to use after that point in time, measured in a
 * number of seconds. When reading a time slice of events, calculate the appropriate set of row keys
 * based on the bucket size during that time period.
 * 
 * @author Fabiel Zuniga
 */
public class TimeShardStrategy implements ColumnShardStrategy<String, Date> {

    private final String rowKeyPrefix;
    private final ShardSize shardSize;
    private final TimeZone timeZone;

    /**
     * Creates a shard strategy.
     * 
     * @param shardSize shard size
     * @param timeZone time zone
     */
    public TimeShardStrategy(ShardSize shardSize, TimeZone timeZone) {
        this(shardSize, null, timeZone);
    }

    /**
     * Creates a shard strategy.
     * 
     * @param shardSize shard size
     * @param rowKeyPrefix row key prefix
     * @param timeZone time zone
     */
    public TimeShardStrategy(ShardSize shardSize, String rowKeyPrefix, TimeZone timeZone) {
        if (shardSize == null) {
            throw new NullPointerException("shardSize cannot be null");
        }

        if (timeZone == null) {
            throw new NullPointerException("timeZone cannot be null");
        }

        this.rowKeyPrefix = rowKeyPrefix != null && !rowKeyPrefix.isEmpty() ? rowKeyPrefix + "_" : "";
        this.shardSize = shardSize;
        this.timeZone = timeZone;
    }

    @Override
    public String getShard(Date time) {
        if (time == null) {
            throw new NullPointerException("time cannot be null");
        }

        return this.rowKeyPrefix + this.shardSize.getSuffix(time, this.timeZone);
    }

    /**
     * Shard size.
     */
    public static enum ShardSize {
        /** Uses a row to group time from the same second */
        SECONDLY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(time.toDate());

                StringBuilder str = new StringBuilder(14);
                str.append(String.valueOf(calendar.get(Calendar.YEAR)));
                // January = 0, December = 11
                str.append(addLeadingZeros(calendar.get(Calendar.MONTH) + 1));
                str.append(addLeadingZeros(calendar.get(Calendar.DATE)));
                str.append(addLeadingZeros(calendar.get(Calendar.HOUR)));
                str.append(addLeadingZeros(calendar.get(Calendar.MINUTE)));
                str.append(addLeadingZeros(calendar.get(Calendar.SECOND)));

                return str.toString();
            }
        },
        /** Uses a row to group time from the same minute */
        MINUTELY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(time.toDate());

                StringBuilder str = new StringBuilder(12);
                str.append(String.valueOf(calendar.get(Calendar.YEAR)));
                // January = 0, December = 11
                str.append(addLeadingZeros(calendar.get(Calendar.MONTH) + 1));
                str.append(addLeadingZeros(calendar.get(Calendar.DATE)));
                str.append(addLeadingZeros(calendar.get(Calendar.HOUR)));
                str.append(addLeadingZeros(calendar.get(Calendar.MINUTE)));

                return str.toString();
            }
        },
        /** Uses a row to group time from the same hour */
        HOURLY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(time.toDate());

                StringBuilder str = new StringBuilder(10);
                str.append(String.valueOf(calendar.get(Calendar.YEAR)));
                // January = 0, December = 11
                str.append(addLeadingZeros(calendar.get(Calendar.MONTH) + 1));
                str.append(addLeadingZeros(calendar.get(Calendar.DATE)));
                str.append(addLeadingZeros(calendar.get(Calendar.HOUR)));

                return str.toString();
            }
        },
        /** Uses a row to group time from the same day */
        DAILY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(time.toDate());

                StringBuilder str = new StringBuilder(8);
                str.append(String.valueOf(calendar.get(Calendar.YEAR)));
                // January = 0, December = 11
                str.append(addLeadingZeros(calendar.get(Calendar.MONTH) + 1));
                str.append(addLeadingZeros(calendar.get(Calendar.DATE)));

                return str.toString();
            }
        },
        /** Uses a row to group time from the same week */
        WEEKLY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                TimePeriod timePeriod = TimePeriod.getWeekPeriod(time, timeZone);
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(timePeriod.getStartTime().toDate());

                StringBuilder str = new StringBuilder(9);
                str.append(String.valueOf(calendar.get(Calendar.YEAR)));
                // January = 0, December = 11
                str.append(addLeadingZeros(calendar.get(Calendar.MONTH) + 1));
                str.append(addLeadingZeros(calendar.get(Calendar.DATE)));
                // Makes a difference with 'daily' in case the same column family keeps different granularity of sharding
                str.append('w');

                return str.toString();
            }
        },
        /** Uses a row to group time from the same month */
        MONTHLY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(time.toDate());

                StringBuilder str = new StringBuilder(6);
                str.append(String.valueOf(calendar.get(Calendar.YEAR)));
                // January = 0, December = 11
                str.append(addLeadingZeros(calendar.get(Calendar.MONTH) + 1));

                return str.toString();
            }
        },
        /** Uses a row to group time from the same year */
        YEARLY{
            @Override
            String getSuffix(Date time, TimeZone timeZone) {
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTime(time.toDate());
                return String.valueOf(calendar.get(Calendar.YEAR));
            }
        }

        ;

        /**
         * Gets the row key suffix to use.
         * 
         * @param time time
         * @param timeZone time zone
         * @return a suffix to use for the row key
         */
        abstract String getSuffix(Date time, TimeZone timeZone);

        private static String addLeadingZeros(int value) {
            // Adds two leading zeros
            return String.format("%02d", Integer.valueOf(value));
        }
    }
}
