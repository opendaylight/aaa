/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.customid;

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
import com.hp.util.model.persistence.dao.OffsetPageDaoTest;
import com.hp.util.model.persistence.dao.SearchCase;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.JpaTestDataStoreProvider;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class CustomIdDaoTest extends
        OffsetPageDaoTest<String, CustomIdDto, FilterMock, SortKeyMock, JpaContext, CustomIdDao> {

    public CustomIdDaoTest() {
        super(JpaTestDataStoreProvider.getDataStore());
    }

    @Override
    protected CustomIdDao createDaoInstance() {
        return new CustomIdDao();
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
    protected CustomIdDto createIdentifiable(Id<CustomIdDto, String> id) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        return new CustomIdDto(id, "String attribute " + dataGenerator.getInt(), dataGenerator.getBoolean(),
                Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected List<CustomIdDto> createIdentifiables(int count) {
        List<CustomIdDto> identifiables = new ArrayList<CustomIdDto>(count);
        for (int i = 0; i < count; i++) {
            identifiables.add(createIdentifiable(getId(i)));
        }
        return identifiables;
    }

    @Override
    protected void modify(CustomIdDto identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected void assertEqualState(CustomIdDto expected, CustomIdDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
    }

    @Override
    protected List<SearchCase<CustomIdDto, FilterMock, SortKeyMock>> getSearchCases() {

        List<SearchCase<CustomIdDto, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<CustomIdDto, FilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        CustomIdDto dto1 = new CustomIdDto(getId(1), "Model Object 1", true, Long.valueOf(1), date1, EnumMock.ELEMENT_1);
        CustomIdDto dto2 = new CustomIdDto(getId(2), "Model Object 2", false, Long.valueOf(2), date2,
                EnumMock.ELEMENT_1);
        CustomIdDto dto3 = new CustomIdDto(getId(3), "Model Object 3", true, Long.valueOf(3), date3, EnumMock.ELEMENT_2);
        CustomIdDto dto4 = new CustomIdDto(getId(4), "Model Object 4", false, Long.valueOf(4), date4,
                EnumMock.ELEMENT_2);
        CustomIdDto dto5 = new CustomIdDto(getId(5), "Model Object 5", true, Long.valueOf(5), date5, EnumMock.ELEMENT_3);

        List<CustomIdDto> searchSpace = new ArrayList<CustomIdDto>(5);
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

    private static Id<CustomIdDto, String> getId(int i) {
        return Id.valueOf("id " + (i));
    }
}
