/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.valuetypeid;

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
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.model.persistence.dao.SearchCase;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDaoTest;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class ValueTypeIdDaoTest
        extends
        JpaOffsetPageDaoTest<MacAddress, ValueTypeIdDto, FilterMock, SortKeyMock, ValueTypeIdDao> {

    @Override
    protected ValueTypeIdDao createDaoInstance() {
        return new ValueTypeIdDao();
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
    protected ValueTypeIdDto createIdentifiable(Id<ValueTypeIdDto, MacAddress> id) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        return new ValueTypeIdDto(id, "String attribute " + dataGenerator.getInt(), dataGenerator.getBoolean(),
                Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected List<ValueTypeIdDto> createIdentifiables(int count) {
        List<ValueTypeIdDto> identifiables = new ArrayList<ValueTypeIdDto>(count);
        for (int i = 0; i < count; i++) {
            identifiables.add(createIdentifiable(getId(i)));
        }
        return identifiables;
    }

    @Override
    protected void modify(ValueTypeIdDto identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected void assertEqualState(ValueTypeIdDto expected, ValueTypeIdDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
    }

    @Override
    protected List<SearchCase<ValueTypeIdDto, FilterMock, SortKeyMock>> getSearchCases() {

        List<SearchCase<ValueTypeIdDto, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<ValueTypeIdDto, FilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        ValueTypeIdDto dto1 = new ValueTypeIdDto(getId(1), "Model Object 1", true, Long.valueOf(1), date1,
                EnumMock.ELEMENT_1);
        ValueTypeIdDto dto2 = new ValueTypeIdDto(getId(2), "Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1);
        ValueTypeIdDto dto3 = new ValueTypeIdDto(getId(3), "Model Object 3", true, Long.valueOf(3), date3,
                EnumMock.ELEMENT_2);
        ValueTypeIdDto dto4 = new ValueTypeIdDto(getId(4), "Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2);
        ValueTypeIdDto dto5 = new ValueTypeIdDto(getId(5), "Model Object 5", true, Long.valueOf(5), date5,
                EnumMock.ELEMENT_3);

        List<ValueTypeIdDto> searchSpace = new ArrayList<ValueTypeIdDto>(5);
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

    private static Id<ValueTypeIdDto, MacAddress> getId(int i) {
        MacAddress macAddress = MacAddress.valueOfOctets((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) i);
        return Id.valueOf(macAddress);
    }
}
