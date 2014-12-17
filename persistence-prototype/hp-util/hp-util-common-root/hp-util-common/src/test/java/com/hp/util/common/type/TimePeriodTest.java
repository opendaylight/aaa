/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class TimePeriodTest {

    @Test
    public void testConstruction() {
        final Date startTime = Date.valueOf(1);
        final Date endTime = Date.valueOf(2);

        TimePeriod period = new TimePeriod(startTime, endTime);
        Assert.assertEquals(startTime, period.getStartTime());
        Assert.assertEquals(endTime, period.getEndTime());
    }

    @Test
    @SuppressWarnings("unused")
    public void testInvalidConstruction() {
        final Date validStartTime = Date.valueOf(1);
        final Date invalidStartTime = null;

        final Date validEndTime = Date.valueOf(2);
        final Date invalidEndTime = null;

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new TimePeriod(validEndTime, validStartTime);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new TimePeriod(invalidStartTime, validEndTime);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                new TimePeriod(validStartTime, invalidEndTime);
            }
        });
    }

    @Test
    public void testEqualsAndHashCode() {
        final Date startTime = Date.valueOf(1);
        final Date endTime = Date.valueOf(2);
        final Date otherTime = Date.valueOf(3);

        TimePeriod obj = new TimePeriod(startTime, endTime);
        TimePeriod equal1 = new TimePeriod(startTime, endTime);
        TimePeriod equal2 = new TimePeriod(startTime, endTime);
        TimePeriod unequal = new TimePeriod(startTime, otherTime);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<TimePeriod> semanticVerifier = new SemanticCompatibilityVerifier<TimePeriod>() {
            @Override
            public void assertSemanticCompatibility(TimePeriod original, TimePeriod replica) {
                Assert.assertEquals(original.getStartTime(), replica.getStartTime());
                Assert.assertEquals(original.getEndTime(), replica.getEndTime());
            }
        };

        SerializabilityTester.testSerialization(new TimePeriod(Date.valueOf(1), Date.valueOf(2)), semanticVerifier);
    }

    @Test
    public void testIsValidPeriod() {
        final Date startTime = Date.valueOf(1);
        final Date endTime = Date.valueOf(2);

        Assert.assertTrue(TimePeriod.isValidPeriod(startTime, endTime));
        Assert.assertFalse(TimePeriod.isValidPeriod(startTime, startTime));
        Assert.assertFalse(TimePeriod.isValidPeriod(endTime, startTime));
        Assert.assertFalse(TimePeriod.isValidPeriod(startTime, null));
        Assert.assertFalse(TimePeriod.isValidPeriod(null, endTime));
        Assert.assertFalse(TimePeriod.isValidPeriod(null, null));
    }

    @Test
    public void testGetHourPeriod() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar reference = Calendar.getInstance(timeZone);
        reference.set(2013, 1, 1, 5, 2, 1);

        Calendar expectedStartTime = Calendar.getInstance(timeZone);
        expectedStartTime.set(2013, 1, 1, 5, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        Calendar expectedEndTime = Calendar.getInstance(timeZone);
        expectedEndTime.set(2013, 1, 1, 5, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        TimePeriod period = TimePeriod.getHourPeriod(Date.valueOf(reference.getTime()), timeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());

        // Changing time zone
        // Since Java Date is in UTC time the result should be the same for hour period.
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));
        period = TimePeriod.getHourPeriod(Date.valueOf(reference.getTime()), twoHoursAheadTimeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());
    }

    @Test
    public void testGetDayPeriod() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar reference = Calendar.getInstance(timeZone);
        reference.set(2013, 1, 1, 23, 2, 1);

        Calendar expectedStartTime = Calendar.getInstance(timeZone);
        expectedStartTime.set(2013, 1, 1, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        Calendar expectedEndTime = Calendar.getInstance(timeZone);
        expectedEndTime.set(2013, 1, 1, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        TimePeriod period = TimePeriod.getDayPeriod(Date.valueOf(reference.getTime()), timeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());

        // Changing time zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));

        expectedStartTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedStartTime.set(2013, 1, 2, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        expectedEndTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedEndTime.set(2013, 1, 2, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        period = TimePeriod.getDayPeriod(Date.valueOf(reference.getTime()), twoHoursAheadTimeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());
    }

    @Test
    public void testGetWeekPeriod() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar reference = Calendar.getInstance(timeZone);
        reference.set(2013, 0, 12, 23, 30, 59);

        Calendar expectedStartTime = Calendar.getInstance(timeZone);
        expectedStartTime.set(2013, 0, 6, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        Calendar expectedEndTime = Calendar.getInstance(timeZone);
        expectedEndTime.set(2013, 0, 12, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        TimePeriod period = TimePeriod.getWeekPeriod(Date.valueOf(reference.getTime()), timeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());

        // Changing time zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));

        expectedStartTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedStartTime.set(2013, 0, 13, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        expectedEndTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedEndTime.set(2013, 0, 19, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        period = TimePeriod.getWeekPeriod(Date.valueOf(reference.getTime()), twoHoursAheadTimeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());
    }

    @Test
    public void testGetWeekDays() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar reference = Calendar.getInstance(timeZone);
        reference.set(2013, 1, 6, 5, 2, 1);

        Calendar sunday = Calendar.getInstance(timeZone);
        sunday.set(2013, 1, 3, 0, 0, 0);
        sunday.set(Calendar.MILLISECOND, 0);

        Calendar monday = Calendar.getInstance(timeZone);
        monday.set(2013, 1, 4, 0, 0, 0);
        monday.set(Calendar.MILLISECOND, 0);

        Calendar tuesday = Calendar.getInstance(timeZone);
        tuesday.set(2013, 1, 5, 0, 0, 0);
        tuesday.set(Calendar.MILLISECOND, 0);

        Calendar wednesday = Calendar.getInstance(timeZone);
        wednesday.set(2013, 1, 6, 0, 0, 0);
        wednesday.set(Calendar.MILLISECOND, 0);

        Calendar thrusday = Calendar.getInstance(timeZone);
        thrusday.set(2013, 1, 7, 0, 0, 0);
        thrusday.set(Calendar.MILLISECOND, 0);

        Calendar friday = Calendar.getInstance(timeZone);
        friday.set(2013, 1, 8, 0, 0, 0);
        friday.set(Calendar.MILLISECOND, 0);

        Calendar saturday = Calendar.getInstance(timeZone);
        saturday.set(2013, 1, 9, 0, 0, 0);
        saturday.set(Calendar.MILLISECOND, 0);

        Date[] week = TimePeriod.getWeekDays(Date.valueOf(reference.getTime()), timeZone);
        Assert.assertNotNull(week);
        Assert.assertEquals(7, week.length);
        Assert.assertEquals(sunday.getTime(), week[0].toDate());
        Assert.assertEquals(monday.getTime(), week[1].toDate());
        Assert.assertEquals(tuesday.getTime(), week[2].toDate());
        Assert.assertEquals(wednesday.getTime(), week[3].toDate());
        Assert.assertEquals(thrusday.getTime(), week[4].toDate());
        Assert.assertEquals(friday.getTime(), week[5].toDate());
        Assert.assertEquals(saturday.getTime(), week[6].toDate());

        // TODO: Add time zone change test
    }

    @Test
    public void testGetMonthPeriod() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar reference = Calendar.getInstance(timeZone);
        reference.set(2013, 1, 28, 23, 2, 1);

        Calendar expectedStartTime = Calendar.getInstance(timeZone);
        expectedStartTime.set(2013, 1, 1, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        Calendar expectedEndTime = Calendar.getInstance(timeZone);
        expectedEndTime.set(2013, 1, 28, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        TimePeriod period = TimePeriod.getMonthPeriod(Date.valueOf(reference.getTime()), timeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());

        // Changing time zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));

        expectedStartTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedStartTime.set(2013, 2, 1, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        expectedEndTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedEndTime.set(2013, 2, 31, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        period = TimePeriod.getMonthPeriod(Date.valueOf(reference.getTime()), twoHoursAheadTimeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());
    }

    @Test
    public void testGetYearPeriod() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar reference = Calendar.getInstance(timeZone);
        reference.set(2013, 11, 31, 23, 2, 1);

        Calendar expectedStartTime = Calendar.getInstance(timeZone);
        expectedStartTime.set(2013, 0, 1, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        Calendar expectedEndTime = Calendar.getInstance(timeZone);
        expectedEndTime.set(2013, 11, 31, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        TimePeriod period = TimePeriod.getYearPeriod(Date.valueOf(reference.getTime()), timeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());

        // Changing time zone
        TimeZone twoHoursAheadTimeZone = getTimeZone(timeZone, Measure.valueOf(2, NonSI.HOUR));

        expectedStartTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedStartTime.set(2014, 0, 1, 0, 0, 0);
        expectedStartTime.set(Calendar.MILLISECOND, 0);

        expectedEndTime = Calendar.getInstance(twoHoursAheadTimeZone);
        expectedEndTime.set(2014, 11, 31, 23, 59, 59);
        expectedEndTime.set(Calendar.MILLISECOND, 999);

        period = TimePeriod.getYearPeriod(Date.valueOf(reference.getTime()), twoHoursAheadTimeZone);
        Assert.assertEquals(expectedStartTime.getTime(), period.getStartTime().toDate());
        Assert.assertEquals(expectedEndTime.getTime(), period.getEndTime().toDate());
    }

    @Test
    public void testBreakDownHourly() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar startTime = Calendar.getInstance(timeZone);
        startTime.set(2013, 1, 3, 5, 3, 2);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance(timeZone);
        endTime.set(2013, 1, 3, 7, 25, 50);
        endTime.set(Calendar.MILLISECOND, 0);

        TimePeriod period = new TimePeriod(Date.valueOf(startTime.getTime()), Date.valueOf(endTime.getTime()));
        List<TimePeriod> daily = period.breakDownHourly(timeZone);

        Calendar firstHourStart = startTime;

        Calendar firstHourEnd = Calendar.getInstance(timeZone);
        firstHourEnd.set(2013, 1, 3, 5, 59, 59);
        firstHourEnd.set(Calendar.MILLISECOND, 999);

        Calendar secondHourStart = Calendar.getInstance(timeZone);
        secondHourStart.set(2013, 1, 3, 6, 0, 0);
        secondHourStart.set(Calendar.MILLISECOND, 0);

        Calendar secondHourEnd = Calendar.getInstance(timeZone);
        secondHourEnd.set(2013, 1, 3, 6, 59, 59);
        secondHourEnd.set(Calendar.MILLISECOND, 999);

        Calendar thirdHourStart = Calendar.getInstance(timeZone);
        thirdHourStart.set(2013, 1, 3, 7, 0, 0);
        thirdHourStart.set(Calendar.MILLISECOND, 0);

        Calendar thirdDayEnd = endTime;

        Assert.assertNotNull(daily);
        Assert.assertEquals(3, daily.size());
        Assert.assertEquals(firstHourStart.getTime(), daily.get(0).getStartTime().toDate());
        Assert.assertEquals(firstHourEnd.getTime(), daily.get(0).getEndTime().toDate());
        Assert.assertEquals(secondHourStart.getTime(), daily.get(1).getStartTime().toDate());
        Assert.assertEquals(secondHourEnd.getTime(), daily.get(1).getEndTime().toDate());
        Assert.assertEquals(thirdHourStart.getTime(), daily.get(2).getStartTime().toDate());
        Assert.assertEquals(thirdDayEnd.getTime(), daily.get(2).getEndTime().toDate());

        // TODO: Add time zone change test
    }

    @Test
    public void testBreakDownDaily() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar startTime = Calendar.getInstance(timeZone);
        startTime.set(2013, 1, 3, 5, 3, 2);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance(timeZone);
        endTime.set(2013, 1, 5, 11, 25, 50);
        endTime.set(Calendar.MILLISECOND, 0);

        TimePeriod period = new TimePeriod(Date.valueOf(startTime.getTime()), Date.valueOf(endTime.getTime()));
        List<TimePeriod> daily = period.breakDownDaily(timeZone);

        Calendar firstDayStart = startTime;

        Calendar firstDayEnd = Calendar.getInstance(timeZone);
        firstDayEnd.set(2013, 1, 3, 23, 59, 59);
        firstDayEnd.set(Calendar.MILLISECOND, 999);

        Calendar secondDayStart = Calendar.getInstance(timeZone);
        secondDayStart.set(2013, 1, 4, 0, 0, 0);
        secondDayStart.set(Calendar.MILLISECOND, 0);

        Calendar secondDayEnd = Calendar.getInstance(timeZone);
        secondDayEnd.set(2013, 1, 4, 23, 59, 59);
        secondDayEnd.set(Calendar.MILLISECOND, 999);

        Calendar thirdDayStart = Calendar.getInstance(timeZone);
        thirdDayStart.set(2013, 1, 5, 0, 0, 0);
        thirdDayStart.set(Calendar.MILLISECOND, 0);

        Calendar thirdDayEnd = endTime;

        Assert.assertNotNull(daily);
        Assert.assertEquals(3, daily.size());
        Assert.assertEquals(firstDayStart.getTime(), daily.get(0).getStartTime().toDate());
        Assert.assertEquals(firstDayEnd.getTime(), daily.get(0).getEndTime().toDate());
        Assert.assertEquals(secondDayStart.getTime(), daily.get(1).getStartTime().toDate());
        Assert.assertEquals(secondDayEnd.getTime(), daily.get(1).getEndTime().toDate());
        Assert.assertEquals(thirdDayStart.getTime(), daily.get(2).getStartTime().toDate());
        Assert.assertEquals(thirdDayEnd.getTime(), daily.get(2).getEndTime().toDate());

        // TODO: Add time zone change test
    }

    @Test
    public void testBreakDownWeekly() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar startTime = Calendar.getInstance(timeZone);
        startTime.set(2013, 1, 6, 5, 3, 2);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance(timeZone);
        endTime.set(2013, 1, 20, 11, 25, 50);
        endTime.set(Calendar.MILLISECOND, 0);

        TimePeriod period = new TimePeriod(Date.valueOf(startTime.getTime()), Date.valueOf(endTime.getTime()));
        List<TimePeriod> weekly = period.breakDownWeekly(timeZone);

        Calendar firstWeekStart = startTime;

        Calendar firstWeekEnd = Calendar.getInstance(timeZone);
        firstWeekEnd.set(2013, 1, 9, 23, 59, 59);
        firstWeekEnd.set(Calendar.MILLISECOND, 999);

        Calendar secondWeekStart = Calendar.getInstance(timeZone);
        secondWeekStart.set(2013, 1, 10, 0, 0, 0);
        secondWeekStart.set(Calendar.MILLISECOND, 0);

        Calendar secondWeekEnd = Calendar.getInstance(timeZone);
        secondWeekEnd.set(2013, 1, 16, 23, 59, 59);
        secondWeekEnd.set(Calendar.MILLISECOND, 999);

        Calendar thirdWeekStart = Calendar.getInstance(timeZone);
        thirdWeekStart.set(2013, 1, 17, 0, 0, 0);
        thirdWeekStart.set(Calendar.MILLISECOND, 0);

        Calendar thirdWeekEnd = endTime;

        Assert.assertNotNull(weekly);
        Assert.assertEquals(3, weekly.size());
        Assert.assertEquals(firstWeekStart.getTime(), weekly.get(0).getStartTime().toDate());
        Assert.assertEquals(firstWeekEnd.getTime(), weekly.get(0).getEndTime().toDate());
        Assert.assertEquals(secondWeekStart.getTime(), weekly.get(1).getStartTime().toDate());
        Assert.assertEquals(secondWeekEnd.getTime(), weekly.get(1).getEndTime().toDate());
        Assert.assertEquals(thirdWeekStart.getTime(), weekly.get(2).getStartTime().toDate());
        Assert.assertEquals(thirdWeekEnd.getTime(), weekly.get(2).getEndTime().toDate());

        // TODO: Add time zone change test
    }

    @Test
    public void testBreakDownMonthly() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar startTime = Calendar.getInstance(timeZone);
        startTime.set(2013, 1, 6, 5, 3, 2);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance(timeZone);
        endTime.set(2013, 3, 20, 11, 25, 50);
        endTime.set(Calendar.MILLISECOND, 0);

        TimePeriod period = new TimePeriod(Date.valueOf(startTime.getTime()), Date.valueOf(endTime.getTime()));
        List<TimePeriod> monthly = period.breakDownMonthly(timeZone);

        Calendar firstMonthStart = startTime;

        Calendar firstMonthEnd = Calendar.getInstance(timeZone);
        firstMonthEnd.set(2013, 1, 28, 23, 59, 59);
        firstMonthEnd.set(Calendar.MILLISECOND, 999);

        Calendar secondMonthStart = Calendar.getInstance(timeZone);
        secondMonthStart.set(2013, 2, 1, 0, 0, 0);
        secondMonthStart.set(Calendar.MILLISECOND, 0);

        Calendar secondMonthEnd = Calendar.getInstance(timeZone);
        secondMonthEnd.set(2013, 2, 31, 23, 59, 59);
        secondMonthEnd.set(Calendar.MILLISECOND, 999);

        Calendar thirdMonthStart = Calendar.getInstance(timeZone);
        thirdMonthStart.set(2013, 3, 1, 0, 0, 0);
        thirdMonthStart.set(Calendar.MILLISECOND, 0);

        Calendar thirdMonthEnd = endTime;

        Assert.assertNotNull(monthly);
        Assert.assertEquals(3, monthly.size());
        Assert.assertEquals(firstMonthStart.getTime(), monthly.get(0).getStartTime().toDate());
        Assert.assertEquals(firstMonthEnd.getTime(), monthly.get(0).getEndTime().toDate());
        Assert.assertEquals(secondMonthStart.getTime(), monthly.get(1).getStartTime().toDate());
        Assert.assertEquals(secondMonthEnd.getTime(), monthly.get(1).getEndTime().toDate());
        Assert.assertEquals(thirdMonthStart.getTime(), monthly.get(2).getStartTime().toDate());
        Assert.assertEquals(thirdMonthEnd.getTime(), monthly.get(2).getEndTime().toDate());

        // TODO: Add time zone change test
    }

    @Test
    public void testBreakDownYearly() {
        TimeZone timeZone = getDefaultTimeZone();

        Calendar startTime = Calendar.getInstance(timeZone);
        startTime.set(2011, 1, 6, 5, 3, 2);
        startTime.set(Calendar.MILLISECOND, 0);

        Calendar endTime = Calendar.getInstance(timeZone);
        endTime.set(2013, 3, 20, 11, 25, 50);
        endTime.set(Calendar.MILLISECOND, 0);

        TimePeriod period = new TimePeriod(Date.valueOf(startTime.getTime()), Date.valueOf(endTime.getTime()));
        List<TimePeriod> yearly = period.breakDownYearly(timeZone);

        Calendar firstYearStart = startTime;

        Calendar firstYearEnd = Calendar.getInstance(timeZone);
        firstYearEnd.set(2011, 11, 31, 23, 59, 59);
        firstYearEnd.set(Calendar.MILLISECOND, 999);

        Calendar secondYearStart = Calendar.getInstance(timeZone);
        secondYearStart.set(2012, 0, 1, 0, 0, 0);
        secondYearStart.set(Calendar.MILLISECOND, 0);

        Calendar secondYearEnd = Calendar.getInstance(timeZone);
        secondYearEnd.set(2012, 11, 31, 23, 59, 59);
        secondYearEnd.set(Calendar.MILLISECOND, 999);

        Calendar thirdYearStart = Calendar.getInstance(timeZone);
        thirdYearStart.set(2013, 0, 1, 0, 0, 0);
        thirdYearStart.set(Calendar.MILLISECOND, 0);

        Calendar thirdYearEnd = endTime;

        Assert.assertNotNull(yearly);
        Assert.assertEquals(3, yearly.size());
        Assert.assertEquals(firstYearStart.getTime(), yearly.get(0).getStartTime().toDate());
        Assert.assertEquals(firstYearEnd.getTime(), yearly.get(0).getEndTime().toDate());
        Assert.assertEquals(secondYearStart.getTime(), yearly.get(1).getStartTime().toDate());
        Assert.assertEquals(secondYearEnd.getTime(), yearly.get(1).getEndTime().toDate());
        Assert.assertEquals(thirdYearStart.getTime(), yearly.get(2).getStartTime().toDate());
        Assert.assertEquals(thirdYearEnd.getTime(), yearly.get(2).getEndTime().toDate());

        // TODO: Add time zone change test
    }

    private static TimeZone getDefaultTimeZone() {
        // Not all time zones have a time zone that is two hours ahead (Used in some tests)
        // Thus, if TimeZone.getDefault() is used, tests could fail in some environments.
        return TimeZone.getTimeZone("GMT");
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
