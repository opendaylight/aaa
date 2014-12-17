/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.dependent;

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
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class IndependentDaoTest
        extends
        JpaOffsetPageDaoTest<Long, IndependentDto, FilterMock, SortKeyMock, IndependentDao> {

    @Override
    protected IndependentDao createDaoInstance() {
        return new IndependentDao(new DependentDao());
    }

    @Override
    protected boolean isPrimaryKeyIntegrityConstraintViolationSupported() {
        return false;
    }

    @Override
    protected boolean isVersioned() {
        return true;
    }

    @Override
    protected IndependentDto createIdentifiable(Id<IndependentDto, Long> id) {
        return null;
    }

    @Override
    protected List<IndependentDto> createIdentifiables(int count) {
        List<IndependentDto> identifiables = new ArrayList<IndependentDto>(count);

        long naturaulKeyCount = 0;
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        for (int i = 0; i < count; i++) {
            IndependentDto independent = new IndependentDto("String attribute " + dataGenerator.getInt(),
                    dataGenerator.getBoolean(), Long.valueOf(dataGenerator.getLong()), Date.currentTime(),
                    dataGenerator.getEnum(EnumMock.class));

            for (int j = 0; j < 3; j++) {
                long naturalKey = ++naturaulKeyCount;
                DependentDto dependent = new DependentDto(independent.getId(),
                        Long.valueOf(naturalKey), "String attribute " + dataGenerator.getInt() + " " + naturalKey,
                        dataGenerator.getBoolean(), Long.valueOf(dataGenerator.getLong()), Date.currentTime(),
                        dataGenerator.getEnum(EnumMock.class));
                independent.addDependent(dependent);
            }

            identifiables.add(independent);
        }
        return identifiables;
    }

    @Override
    protected void modify(IndependentDto independent) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        independent.setAttributeString("String attribute " + dataGenerator.getInt());
        independent.setAttributeBoolean(!independent.getAttributeBoolean());
        independent.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        independent.setAttributeDate(Date.currentTime());
        independent.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));

        // Deletes a dependent

        if (!independent.getDependents().isEmpty()) {
            DependentDto dependentToRemove = independent.getDependents().iterator().next();
            independent.removeDependent(dependentToRemove);
        }

        // Updates dependents

        int index = 0;
        if (!independent.getDependents().isEmpty()) {
            for (DependentDto dependent : independent.getDependents()) {
                dependent.setAttributeString("String attribute " + dataGenerator.getInt() + " " + index);
                independent.setAttributeBoolean(!independent.getAttributeBoolean());
                independent.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
                independent.setAttributeDate(Date.currentTime());
                independent.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
                index++;
            }
        }

        // Adds a dependent

        DependentDto dependent = new DependentDto(independent.getId(), Long.valueOf(Long.MAX_VALUE),
                "String attribute " + dataGenerator.getInt() + " ", dataGenerator.getBoolean(),
                Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
        independent.addDependent(dependent);
    }

    @Override
    protected void assertEqualState(IndependentDto expected, IndependentDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());

        Assert.assertEquals(expected.getDependents().size(), actual.getDependents().size());

        for (DependentDto expectedDependent : expected.getDependents()) {
            boolean found = false;
            for (DependentDto actualDependent : actual.getDependents()) {
                /*
                 * Note that dependent objects might not have a valid id. See documentation of
                 * BaseDaoTest.assertEqualState(T, T). It is preferable to use a natural key to
                 * match dependent objects from expected to actual.
                 */
                if (expectedDependent.getNaturalKey().equals(actualDependent.getNaturalKey())) {
                    found = true;

                    Assert.assertEquals(expectedDependent.getAttributeString(), actualDependent.getAttributeString());
                    Assert.assertEquals(expectedDependent.getAttributeBoolean(), actualDependent.getAttributeBoolean());
                    Assert.assertEquals(expectedDependent.getAttributeLong(), actualDependent.getAttributeLong());
                    Assert.assertEquals(expectedDependent.getAttributeDate(), actualDependent.getAttributeDate());
                    Assert.assertEquals(expectedDependent.getAttributeEnum(), actualDependent.getAttributeEnum());

                    break;
                }
            }
            Assert.assertTrue(found);
        }
    }

    @Override
    protected List<SearchCase<IndependentDto, FilterMock, SortKeyMock>> getSearchCases() {

        List<SearchCase<IndependentDto, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<IndependentDto, FilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        IndependentDto dto1 = new IndependentDto("Model Object 1", true, Long.valueOf(1), date1, EnumMock.ELEMENT_1);
        IndependentDto dto2 = new IndependentDto("Model Object 2", false, Long.valueOf(2), date2, EnumMock.ELEMENT_1);
        IndependentDto dto3 = new IndependentDto("Model Object 3", true, Long.valueOf(3), date3, EnumMock.ELEMENT_2);
        IndependentDto dto4 = new IndependentDto("Model Object 4", false, Long.valueOf(4), date4, EnumMock.ELEMENT_2);
        IndependentDto dto5 = new IndependentDto("Model Object 5", true, Long.valueOf(5), date5, EnumMock.ELEMENT_3);

        List<IndependentDto> searchSpace = new ArrayList<IndependentDto>(5);
        searchSpace.add(dto1);
        searchSpace.add(dto2);
        searchSpace.add(dto3);
        searchSpace.add(dto4);
        searchSpace.add(dto5);

        //

        FilterMock filter = null;
        SortSpecification<SortKeyMock> sortSpecification = null;

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        //

        filter = new FilterMock();
        sortSpecification = new SortSpecification<SortKeyMock>();

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        //

        filter = new FilterMock();
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.STRING_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3, dto2, dto1));

        //

        filter = new FilterMock();
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3, dto2, dto1));

        //

        filter = new FilterMock();
        filter.setAttributeStringCondition(StringCondition.contain("Object"));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3, dto2, dto1));

        //

        filter = new FilterMock();
        filter.setAttributeStringCondition(StringCondition.equalTo("Model Object 3"));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto3));

        //

        filter = new FilterMock();
        filter.setAttributeStringCondition(StringCondition.endWith("Object 4"));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto4));

        //

        filter = new FilterMock();
        filter.setAttributeDateCondition(IntervalCondition.in(Interval.leftClosedRightUnbounded(Date
                .valueOf(new GregorianCalendar(2012, 1, 3).getTime()))));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3));

        //

        return searchCases;
    }
}
