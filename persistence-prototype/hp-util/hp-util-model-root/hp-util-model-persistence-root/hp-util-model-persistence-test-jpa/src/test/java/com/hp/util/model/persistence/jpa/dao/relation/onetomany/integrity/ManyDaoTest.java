/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.relation.onetomany.integrity;

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
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class ManyDaoTest extends JpaOffsetPageDaoTest<Long, ManyDto, ManyFilterMock, SortKeyMock, ManyDao> {

    @Override
    protected ManyDao createDaoInstance() {
        return new ManyDao(new OneDao());
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
    protected ManyDto createIdentifiable(Id<ManyDto, Long> id) {
        return null;
    }

    @Override
    protected List<ManyDto> createIdentifiables(int count) {
        List<ManyDto> identifiables = new ArrayList<ManyDto>(count);

        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        for (int i = 0; i < count; i++) {
            ManyDto dtoMock = new ManyDto("String attribute " + dataGenerator.getInt(), dataGenerator.getBoolean(),
                    Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class),
                    null);

            identifiables.add(dtoMock);
        }

        return identifiables;
    }

    @Override
    protected void modify(ManyDto identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected void assertEqualState(ManyDto expected, ManyDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());

        if (expected.getRelative() == null) {
            Assert.assertNull(actual.getRelative());
        }
        else {
            Assert.assertEquals(expected.getRelative(), actual.getRelative());
        }
    }

    @Override
    protected List<SearchCase<ManyDto, ManyFilterMock, SortKeyMock>> getSearchCases() {
        // TODO: Test relative condition

        List<SearchCase<ManyDto, ManyFilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<ManyDto, ManyFilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        ManyDto dto1 = new ManyDto("Model Object 1", true, Long.valueOf(1), date1, EnumMock.ELEMENT_1, null);
        ManyDto dto2 = new ManyDto("Model Object 2", false, Long.valueOf(2), date2, EnumMock.ELEMENT_1, null);
        ManyDto dto3 = new ManyDto("Model Object 3", true, Long.valueOf(3), date3, EnumMock.ELEMENT_2, null);
        ManyDto dto4 = new ManyDto("Model Object 4", false, Long.valueOf(4), date4, EnumMock.ELEMENT_2, null);
        ManyDto dto5 = new ManyDto("Model Object 5", true, Long.valueOf(5), date5, EnumMock.ELEMENT_3, null);

        List<ManyDto> searchSpace = new ArrayList<ManyDto>(5);
        searchSpace.add(dto1);
        searchSpace.add(dto2);
        searchSpace.add(dto3);
        searchSpace.add(dto4);
        searchSpace.add(dto5);

        //

        ManyFilterMock filter = null;
        SortSpecification<SortKeyMock> sortSpecification = null;

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        //

        filter = new ManyFilterMock();
        sortSpecification = new SortSpecification<SortKeyMock>();

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        //

        filter = new ManyFilterMock();
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.STRING_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3, dto2, dto1));

        //

        filter = new ManyFilterMock();
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3, dto2, dto1));

        //

        filter = new ManyFilterMock();
        filter.setAttributeStringCondition(StringCondition.contain("Object"));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3, dto2, dto1));

        //

        filter = new ManyFilterMock();
        filter.setAttributeStringCondition(StringCondition.equalTo("Model Object 3"));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto3));

        //

        filter = new ManyFilterMock();
        filter.setAttributeStringCondition(StringCondition.endWith("Object 4"));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto4));

        //

        filter = new ManyFilterMock();
        filter.setAttributeDateCondition(IntervalCondition.in(Interval.leftClosedRightUnbounded(Date
                .valueOf(new GregorianCalendar(2012, 1, 3).getTime()))));
        sortSpecification = new SortSpecification<SortKeyMock>();
        sortSpecification.addSortComponent(SortKeyMock.DATE_ATTRIBUTE, SortOrder.DESCENDING);

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto5, dto4, dto3));

        //

        return searchCases;
    }
}
