/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.compositeid;

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
public class CompositeIdDaoTest extends
        JpaOffsetPageDaoTest<CompositeId, CompositeIdDto, FilterMock, SortKeyMock, CompositeIdDao> {

    @Override
    protected CompositeIdDao createDaoInstance() {
        return new CompositeIdDao();
    }

    @Override
    protected boolean isPrimaryKeyIntegrityConstraintViolationSupported() {
        return true;
    }

    @Override
    protected boolean isVersioned() {
        return false;
    }

    @Override
    protected CompositeIdDto createIdentifiable(Id<CompositeIdDto, CompositeId> id) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        return new CompositeIdDto(id, "String attribute " + dataGenerator.getInt(), dataGenerator.getBoolean(),
                Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected List<CompositeIdDto> createIdentifiables(int count) {
        List<CompositeIdDto> identifiables = new ArrayList<CompositeIdDto>(count);
        for (int i = 0; i < count; i++) {
            CompositeId idValue = new CompositeId("id " + i, i);
            Id<CompositeIdDto, CompositeId> id = Id.valueOf(idValue);
            identifiables.add(createIdentifiable(id));
        }
        return identifiables;
    }

    @Override
    protected void modify(CompositeIdDto identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected void assertEqualState(CompositeIdDto a, CompositeIdDto b) {
        Assert.assertEquals(a.getAttributeString(), b.getAttributeString());
        Assert.assertEquals(a.getAttributeBoolean(), b.getAttributeBoolean());
        Assert.assertEquals(a.getAttributeLong(), b.getAttributeLong());
        Assert.assertEquals(a.getAttributeDate(), b.getAttributeDate());
        Assert.assertEquals(a.getAttributeEnum(), b.getAttributeEnum());
    }

    @Override
    protected List<SearchCase<CompositeIdDto, FilterMock, SortKeyMock>> getSearchCases() {

        List<SearchCase<CompositeIdDto, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<CompositeIdDto, FilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        CompositeIdDto dto1 = new CompositeIdDto(
                Id.<CompositeIdDto, CompositeId> valueOf(new CompositeId("id " + 1, 1)), "Model Object 1", true,
                Long.valueOf(1), date1, EnumMock.ELEMENT_1);
        CompositeIdDto dto2 = new CompositeIdDto(
                Id.<CompositeIdDto, CompositeId> valueOf(new CompositeId("id " + 2, 2)), "Model Object 2", false,
                Long.valueOf(2), date2, EnumMock.ELEMENT_1);
        CompositeIdDto dto3 = new CompositeIdDto(
                Id.<CompositeIdDto, CompositeId> valueOf(new CompositeId("id " + 3, 3)), "Model Object 3", true,
                Long.valueOf(3), date3, EnumMock.ELEMENT_2);
        CompositeIdDto dto4 = new CompositeIdDto(
                Id.<CompositeIdDto, CompositeId> valueOf(new CompositeId("id " + 4, 4)), "Model Object 4", false,
                Long.valueOf(4), date4, EnumMock.ELEMENT_2);
        CompositeIdDto dto5 = new CompositeIdDto(
                Id.<CompositeIdDto, CompositeId> valueOf(new CompositeId("id " + 5, 5)), "Model Object 5", true,
                Long.valueOf(5), date5, EnumMock.ELEMENT_3);

        List<CompositeIdDto> searchSpace = new ArrayList<CompositeIdDto>(5);
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
