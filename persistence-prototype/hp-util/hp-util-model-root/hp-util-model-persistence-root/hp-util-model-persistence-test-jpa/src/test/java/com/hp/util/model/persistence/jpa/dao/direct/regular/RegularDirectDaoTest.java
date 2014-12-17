/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.direct.regular;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Interval;
import com.hp.util.common.type.SortOrder;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.model.persistence.dao.SearchCase;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDaoTest;
import com.hp.util.model.persistence.jpa.dao.direct.regular.RegularDirectEntity.EmbeddableCustomValueType;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class RegularDirectDaoTest extends
        JpaOffsetPageDaoTest<Long, RegularDirectEntity, FilterMock, SortKeyMock, RegularDirectDao> {

    @Override
    protected RegularDirectDao createDaoInstance() {
        return new RegularDirectDao();
    }

    @Override
    protected boolean isPrimaryKeyIntegrityConstraintViolationSupported() {
        return false;
    }

    @Override
    protected boolean isVersioned() {
        return false;
    }

    @Override
    protected RegularDirectEntity createIdentifiable(Id<RegularDirectEntity, Long> id) {
        return null;
    }

    @Override
    protected List<RegularDirectEntity> createIdentifiables(int count) {
        List<RegularDirectEntity> identifiables = new ArrayList<RegularDirectEntity>(count);

        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        for (int i = 0; i < count; i++) {

            RegularDirectEntity entity = new RegularDirectEntity("String attribute " + dataGenerator.getInt(),
                    dataGenerator.getBoolean(), Long.valueOf(dataGenerator.getLong()), Date.currentTime(),
                    dataGenerator.getEnum(EnumMock.class), new EmbeddableCustomValueType("Custom value type " + i,
                            Long.valueOf(i)));

            identifiables.add(entity);
        }

        return identifiables;
    }

    @Override
    protected void modify(RegularDirectEntity identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
        identifiable.setAttributeCustomValueType(new EmbeddableCustomValueType("Custom value type", Long
                .valueOf(dataGenerator.getLong())));
    }

    @Override
    protected void assertEqualState(RegularDirectEntity expected, RegularDirectEntity actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
        Assert.assertEquals(expected.getAttributeCustomValueType(), actual.getAttributeCustomValueType());
    }

    @Override
    protected List<SearchCase<RegularDirectEntity, FilterMock, SortKeyMock>> getSearchCases() {
        List<SearchCase<RegularDirectEntity, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<RegularDirectEntity, FilterMock, SortKeyMock>>();

        /*
         * Each search case is persisted and then deleted after the test, thus the same objects are
         * be persisted multiple times. Since the entity is used directly (no data transfer patter -
         * Identifiable and the entity is the same object) when the entities are persisted the first
         * time, an Id will be auto-generated, and even though nothing is in the database, since the
         * entities will have an id then entityManager.persist(...) fails with the following
         * message: "org.hibernate.PersistentObjectException: detached entity passed to persist".
         * Thus, in order to overcome this problem new instances of the entities have to be created
         * for each search case. This is not the case if natural keys are used.
         */

        searchCases.add(case1());
        searchCases.add(case2());
        searchCases.add(case3());
        searchCases.add(case4());
        searchCases.add(case5());
        searchCases.add(case6());
        searchCases.add(case7());
        searchCases.add(case8());

        return searchCases;
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case1() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = null;
        SortSpecification<SortKeyMock> sortSpecification = null;

        return SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case2() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        return SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case3() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.STRING_ATTRIBUTE, SortOrder.DESCENDING);

        return SearchCase.forCase(searchSpace, filter, sortSpecification, entity5, entity4, entity3, entity2, entity1);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case4() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        return SearchCase.forCase(searchSpace, filter, sortSpecification, entity5, entity4, entity3, entity2, entity1);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case5() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(StringCondition.contain("Object"));
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        return SearchCase.forCase(searchSpace, filter, sortSpecification, entity5, entity4, entity3, entity2, entity1);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case6() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(StringCondition.endWith("Model Object 3"));
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        return SearchCase.forCase(searchSpace, filter, sortSpecification, entity3);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case7() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        filter.setAttributeStringCondition(StringCondition.endWith("Object 4"));
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        return SearchCase.forCase(searchSpace, filter, sortSpecification, entity4);
    }

    private static SearchCase<RegularDirectEntity, FilterMock, SortKeyMock> case8() {
        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDirectEntity entity1 = new RegularDirectEntity("Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity2 = new RegularDirectEntity("Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1, null);
        RegularDirectEntity entity3 = new RegularDirectEntity("Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity4 = new RegularDirectEntity("Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2, null);
        RegularDirectEntity entity5 = new RegularDirectEntity("Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3, null);

        List<RegularDirectEntity> searchSpace = new ArrayList<RegularDirectEntity>(5);
        searchSpace.add(entity1);
        searchSpace.add(entity2);
        searchSpace.add(entity3);
        searchSpace.add(entity4);
        searchSpace.add(entity5);

        FilterMock filter = new FilterMock();
        filter.setAttributeDateCondition(IntervalCondition.in(Interval.leftClosedRightUnbounded(Date
                .valueOf(new GregorianCalendar(2012, 1, 3).getTime()))));
        SortSpecification<SortKeyMock> sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        return SearchCase.forCase(searchSpace, filter, sortSpecification, entity5, entity4, entity3);
    }
}
