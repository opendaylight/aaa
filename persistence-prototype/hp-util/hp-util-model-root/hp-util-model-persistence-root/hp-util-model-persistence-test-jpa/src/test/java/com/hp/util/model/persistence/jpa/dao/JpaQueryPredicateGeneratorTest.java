/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.filter.TimePeriodCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Interval;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.TimePeriod;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.JpaTestDataStoreProvider;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.model.persistence.jpa.dao.regular.RegularDao;
import com.hp.util.model.persistence.jpa.dao.regular.RegularDto;
import com.hp.util.model.persistence.jpa.dao.regular.RegularEntity;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class JpaQueryPredicateGeneratorTest {

    // known Mock DTO objects

    private static final Long DTO_1_LONG_VALUE = Long.valueOf(500L);
    private static final String DTO_1_STRING_VALUE = "ABC";

    private static final Long DTO_2_LONG_VALUE = Long.valueOf(1000L);
    private static final String DTO_2_STRING_VALUE = "123456";

    private static final Long DTO_3_LONG_VALUE = Long.valueOf(1500L);
    private static final String DTO_3_STRING_VALUE = "Hello ABC Also World";

    private static final Long DTO_4_LONG_VALUE = Long.valueOf(2000L);
    private static final String DTO_4_STRING_VALUE = "Goodby World";

    private static final Long DTO_5_LONG_VALUE = Long.valueOf(2500L);
    private static final String DTO_5_STRING_VALUE = "123ABC456 and then some";
    private static final Date CURRENT_DATE;
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2012, 3, 15, 13, 30, 0);
        CURRENT_DATE = Date.valueOf(cal.getTime());
    }

    private static final Date FIVE_HOUR_AGO = hoursBefore(5, CURRENT_DATE);
    private static final Date FOUR_HOUR_AGO = hoursBefore(4, CURRENT_DATE);
    private static final Date THREE_HOUR_AGO = hoursBefore(3, CURRENT_DATE);
    private static final Date TWO_HOUR_AGO = hoursBefore(2, CURRENT_DATE);
    private static final Date ONE_HOUR_AGO = hoursBefore(1, CURRENT_DATE);

    private static RegularDto DTO_1 = new RegularDto(DTO_1_STRING_VALUE, true, DTO_1_LONG_VALUE, FIVE_HOUR_AGO,
            EnumMock.ELEMENT_1, null);
    private static RegularDto DTO_2 = new RegularDto(DTO_2_STRING_VALUE, false, DTO_2_LONG_VALUE, FOUR_HOUR_AGO,
            EnumMock.ELEMENT_1, null);
    private static RegularDto DTO_3 = new RegularDto(DTO_3_STRING_VALUE, false, DTO_3_LONG_VALUE, THREE_HOUR_AGO,
            EnumMock.ELEMENT_2, null);
    private static RegularDto DTO_4 = new RegularDto(DTO_4_STRING_VALUE, false, DTO_4_LONG_VALUE, TWO_HOUR_AGO,
            EnumMock.ELEMENT_2, null);
    private static RegularDto DTO_5 = new RegularDto(DTO_5_STRING_VALUE, true, DTO_5_LONG_VALUE, ONE_HOUR_AGO,
            EnumMock.ELEMENT_3, null);

    // This objects will be persisted and they will hold the DB id
    private RegularDto dto1;
    private RegularDto dto2;
    private RegularDto dto3;
    private RegularDto dto4;
    private RegularDto dto5;

    @Before
    public void beforeEachTest() throws Exception {
        clean();

        execute(new JpaQuery<Void>() {

            @Override
            protected Void execute(RegularDao dao, JpaContext context) throws PersistenceException {
                JpaQueryPredicateGeneratorTest.this.dto1 = dao.create(DTO_1, context);
                JpaQueryPredicateGeneratorTest.this.dto2 = dao.create(DTO_2, context);
                JpaQueryPredicateGeneratorTest.this.dto3 = dao.create(DTO_3, context);
                JpaQueryPredicateGeneratorTest.this.dto4 = dao.create(DTO_4, context);
                JpaQueryPredicateGeneratorTest.this.dto5 = dao.create(DTO_5, context);
                return null;
            }
        });

        assertEquals(5, count());
    }

    /*
     * Tautology and Contradiction
     */

    @Test
    public void testTautology() throws Exception {

        final JpaDao<Long, RegularDto, RegularEntity, FilterMock, SortKeyMock> daoMock = new RegularDao() {

            @Override
            protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<RegularEntity> root) {
                return getQueryPredicateGenerator().getTautology(builder);
            }

        };

        Query<List<RegularDto>, JpaContext> query = new Query<List<RegularDto>, JpaContext>() {

            @Override
            public List<RegularDto> execute(JpaContext context) throws PersistenceException {
                return daoMock.find(null, null, context);
            }

        };

        List<RegularDto> result = execute(query);

        assertFind(result, false, this.dto1, this.dto2, this.dto3, this.dto4, this.dto5);
    }

    @Test
    public void testContradiction() throws Exception {

        final JpaDao<Long, RegularDto, RegularEntity, FilterMock, SortKeyMock> daoMock = new RegularDao() {

            @Override
            protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<RegularEntity> root) {
                return getQueryPredicateGenerator().getContradiction(builder);
            }

        };

        Query<List<RegularDto>, JpaContext> query = new Query<List<RegularDto>, JpaContext>() {

            @Override
            public List<RegularDto> execute(JpaContext context) throws PersistenceException {
                return daoMock.find(null, null, context);
            }

        };

        List<RegularDto> result = execute(query);

        assertFind(result, false);
    }

    /*
     * Equality condition
     */

    @Test
    public void testEqual() throws Exception {
        FilterMock filter = new FilterMock();
        EqualityCondition<Boolean> equalityCondition = EqualityCondition.equalTo(Boolean.TRUE);
        filter.setAttributeBooleanCondition(equalityCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto5);
    }

    @Test
    public void testNotEqual() throws Exception {
        FilterMock filter = new FilterMock();
        EqualityCondition<Boolean> equalityCondition = EqualityCondition.unequalTo(Boolean.TRUE);
        filter.setAttributeBooleanCondition(equalityCondition);
        assertFind(find(filter, null), false, this.dto2, this.dto3, this.dto4);
    }

    /*
     * Set condition
     */

    @Test
    public void testSingleElementSetIn() throws Exception {
        SetCondition<EnumMock> setCondition = SetCondition.in(EnumMock.ELEMENT_1);
        FilterMock filter = new FilterMock();
        filter.setAttributeEnumCondition(setCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2);
    }

    @Test
    public void testMultipleElementSetIn() throws Exception {
        SetCondition<EnumMock> setCondition = SetCondition.in(EnumMock.ELEMENT_1, EnumMock.ELEMENT_3);
        FilterMock filter = new FilterMock();
        filter.setAttributeEnumCondition(setCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto5);
    }

    @Test
    public void testSingleElementSetNotIn() throws Exception {
        SetCondition<EnumMock> setCondition = SetCondition.notIn(EnumMock.ELEMENT_2);
        FilterMock filter = new FilterMock();
        filter.setAttributeEnumCondition(setCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto5);
    }

    @Test
    public void testMultipleElementSetNotIn() throws Exception {
        SetCondition<EnumMock> setCondition = SetCondition.notIn(EnumMock.ELEMENT_1, EnumMock.ELEMENT_3);
        FilterMock filter = new FilterMock();
        filter.setAttributeEnumCondition(setCondition);
        assertFind(find(filter, null), false, this.dto3, this.dto4);
    }

    /*
     * Comparability condition
     */

    @Test
    public void testLessThan() throws Exception {
        ComparabilityCondition<Long> comparabilityCondition = ComparabilityCondition.lessThan(DTO_4_LONG_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeLongCondition(comparabilityCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto3);
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        ComparabilityCondition<Long> comparabilityCondition = ComparabilityCondition
                .lessThanOrEqualTo(DTO_4_LONG_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeLongCondition(comparabilityCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto3, this.dto4);
    }

    @Test
    public void testComparableEqual() throws Exception {
        ComparabilityCondition<Long> comparabilityCondition = ComparabilityCondition.equalTo(DTO_4_LONG_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeLongCondition(comparabilityCondition);
        assertFind(find(filter, null), false, this.dto4);
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        ComparabilityCondition<Long> comparabilityCondition = ComparabilityCondition
                .greaterThanOrEqualTo(DTO_4_LONG_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeLongCondition(comparabilityCondition);
        assertFind(find(filter, null), false, this.dto4, this.dto5);
    }

    @Test
    public void testGreaterThan() throws Exception {
        ComparabilityCondition<Long> comparabilityCondition = ComparabilityCondition.greaterThan(DTO_4_LONG_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeLongCondition(comparabilityCondition);
        assertFind(find(filter, null), false, this.dto5);
    }

    /*
     * String condition
     */

    @Test
    public void testStringEqual() throws Exception {
        StringCondition stringCondition = StringCondition.equalTo(DTO_3_STRING_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(stringCondition);
        assertFind(find(filter, null), false, this.dto3);
    }

    @Test
    public void testStringNotEqual() throws Exception {
        StringCondition stringCondition = StringCondition.unequalTo(DTO_3_STRING_VALUE);
        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(stringCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto4, this.dto5);
    }

    @Test
    public void testStringContains() throws Exception {
        StringCondition stringCondition = StringCondition.contain("AB");
        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(stringCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto3, this.dto5);
    }

    @Test
    public void testStartsWith() throws Exception {
        StringCondition stringCondition = StringCondition.startWith("AB");
        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(stringCondition);
        assertFind(find(filter, null), false, this.dto1);
    }

    @Test
    public void testEndsWith() throws Exception {
        StringCondition stringCondition = StringCondition.endWith("World");
        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(stringCondition);
        assertFind(find(filter, null), false, this.dto3, this.dto4);
    }

    /*
     * Interval condition
     */

    @Test
    public void testOpenIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.open(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto3);
    }

    @Test
    public void testOpenIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.open(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto4, this.dto5);
    }

    @Test
    public void testClosedIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.closed(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto2, this.dto3, this.dto4);
    }

    @Test
    public void testClosedIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.closed(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto5);
    }

    @Test
    public void testLeftClosedRightOpenIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.leftClosedRightOpen(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto2, this.dto3);
    }

    @Test
    public void testLeftClosedRightOpenIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.leftClosedRightOpen(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto4, this.dto5);
    }

    @Test
    public void testLeftOpenRightCloseIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.leftOpenRightClosed(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto3, this.dto4);
    }

    @Test
    public void testLeftOpenRightCloseIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.leftOpenRightClosed(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto5);
    }

    @Test
    public void testLeftOpenRightUnboundedIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.leftOpenRightUnbounded(FOUR_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto3, this.dto4, this.dto5);
    }

    @Test
    public void testLeftOpenRightUnboundedIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.leftOpenRightUnbounded(FOUR_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2);
    }

    @Test
    public void testLeftClosedRightUnboundedIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.leftClosedRightUnbounded(FOUR_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto2, this.dto3, this.dto4, this.dto5);
    }

    @Test
    public void testLeftClosedRightUnboundedIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.leftClosedRightUnbounded(FOUR_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1);
    }

    @Test
    public void testLeftUnboundedRightOpenIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.leftUnboundedRightOpen(TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto3);
    }

    @Test
    public void testLeftUnboundedRightOpenIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.leftUnboundedRightOpen(TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto4, this.dto5);
    }

    @Test
    public void testLeftUnboundedRightClosedIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.leftUnboundedRightClosed(TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto3, this.dto4);
    }

    @Test
    public void testLeftUnboundedRightClosedIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.leftUnboundedRightClosed(TWO_HOUR_AGO);
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto5);
    }

    @Test
    public void testUnboundedIntervalModeIn() throws Exception {
        Interval<Date> interval = Interval.unbounded();
        IntervalCondition<Date> intervalCondition = IntervalCondition.in(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false, this.dto1, this.dto2, this.dto3, this.dto4, this.dto5);
    }

    @Test
    public void testUnboundedIntervalModeNotIn() throws Exception {
        Interval<Date> interval = Interval.unbounded();
        IntervalCondition<Date> intervalCondition = IntervalCondition.notIn(interval);
        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(intervalCondition);
        assertFind(find(filter, null), false);
    }

    /*
     * Time period condition
     */

    @Test
    public void testTimePeriodModeIn() throws Exception {
        TimePeriod period = new TimePeriod(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        TimePeriodCondition periodCondition = TimePeriodCondition.in(period);

        FilterMock filter = new FilterMock();
        filter.setAttributeDateConditionAsTimePeriod(periodCondition);

        assertFind(find(filter, null), false, this.dto2, this.dto3, this.dto4);
    }

    @Test
    public void testTimePeriodModeNotIn() throws Exception {
        TimePeriod period = new TimePeriod(FOUR_HOUR_AGO, TWO_HOUR_AGO);
        TimePeriodCondition periodCondition = TimePeriodCondition.notIn(period);

        FilterMock filter = new FilterMock();
        filter.setAttributeDateConditionAsTimePeriod(periodCondition);

        assertFind(find(filter, null), false, this.dto1, this.dto5);
    }

    /*
     * Id Condition
     */

    @Test
    public void testEqualId() throws Exception {
        /*
         * It is assumed the long value represents an ID in this test.
         */
        FilterMock filter = new FilterMock();
        Id<Object, Long> id = Id.valueOf(DTO_1_LONG_VALUE);
        EqualityCondition<Id<Object, Long>> equalityCondition = EqualityCondition.unequalTo(id);
        filter.setAttributeLongConditionAsId(equalityCondition);
        assertFind(find(filter, null), false, this.dto2, this.dto3, this.dto4, this.dto5);
    }

    @Test
    public void testNotEqualId() throws Exception {
        /*
         * It is assumed the long value represents an ID in this test.
         */
        FilterMock filter = new FilterMock();
        Id<Object, Long> id = Id.valueOf(DTO_1_LONG_VALUE);
        EqualityCondition<Id<Object, Long>> equalityCondition = EqualityCondition.equalTo(id);
        filter.setAttributeLongConditionAsId(equalityCondition);
        assertFind(find(filter, null), false, this.dto1);
    }

    /*
     * Logical operators
     */

    // TODO

    /*
     * 
     */

    private static <R> R execute(Query<R, JpaContext> query) throws Exception {
        return JpaTestDataStoreProvider.getDataStore().execute(query);
    }

    private static void clean() throws Exception {

        // Delete all rows in all tables.
        execute(new JpaQuery<Void>() {

            @Override
            protected Void execute(RegularDao dao, JpaContext context) throws PersistenceException {
                dao.delete((FilterMock)null, context);
                return null;
            }
        });

        Assert.assertEquals(0, count());
    }

    private static long count() throws Exception {
        Long count = execute(new JpaQuery<Long>() {

            @Override
            protected Long execute(RegularDao dao, JpaContext context) throws PersistenceException {
                return Long.valueOf(dao.count(null, context));
            }
        });

        return count.longValue();
    }

    private static List<RegularDto> find(final FilterMock filter, final SortSpecification<SortKeyMock> sortSpecification)
        throws Exception {
        return execute(new JpaQuery<List<RegularDto>>() {

            @Override
            protected List<RegularDto> execute(RegularDao dao, JpaContext context) throws PersistenceException {
                return dao.find(filter, sortSpecification, context);
            }
        });
    }

    private static void assertFind(List<RegularDto> searchResult, boolean assertOrder, RegularDto... expectedResult) {
        List<RegularDto> expectedResultList = Collections.emptyList();
        if (expectedResult != null) {
            expectedResultList = new ArrayList<RegularDto>(Arrays.asList(expectedResult));
        }

        Assert.assertEquals(expectedResultList.size(), searchResult.size());

        // Notice DtoMock overrides equals method

        if (assertOrder) {
            for (int i = expectedResultList.size() - 1; i >= 0; i--) {
                Assert.assertEquals(expectedResultList.get(i).getId(), searchResult.get(i).getId());
            }
        }
        else {
            for (RegularDto persistedIdentifiable : searchResult) {
                boolean foundInExpectedResult = false;
                for (RegularDto identifiable : expectedResultList) {
                    if (persistedIdentifiable.getId().equals(identifiable.getId())) {
                        foundInExpectedResult = true;
                        break;
                    }
                }
                Assert.assertTrue(foundInExpectedResult);
            }
        }
    }

    private static Date hoursBefore(int numHours, Date reference) {
        return Date.valueOf(reference.getTime() - numHours * 3600000);
    }

    private static abstract class JpaQuery<R> implements Query<R, JpaContext> {

        protected JpaQuery() {

        }

        @Override
        public R execute(JpaContext context) throws PersistenceException {
            return execute(new RegularDao(), context);
        }

        protected abstract R execute(RegularDao dao, JpaContext context) throws PersistenceException;
    }
}
