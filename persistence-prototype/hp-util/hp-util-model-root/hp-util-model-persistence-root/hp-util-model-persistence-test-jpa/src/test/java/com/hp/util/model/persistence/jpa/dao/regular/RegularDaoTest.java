/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.regular;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.hp.util.model.persistence.jpa.dao.regular.RegularDto.CustomValueType;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class RegularDaoTest extends JpaOffsetPageDaoTest<Long, RegularDto, FilterMock, SortKeyMock, RegularDao> {

    @Override
    protected RegularDao createDaoInstance() {
        return new RegularDao();
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
    protected RegularDto createIdentifiable(Id<RegularDto, Long> id) {
        return null;
    }

    @Override
    protected List<RegularDto> createIdentifiables(int count) {
        List<RegularDto> identifiables = new ArrayList<RegularDto>(count);

        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        for (int i = 0; i < count; i++) {

            RegularDto dtoMock = new RegularDto("String attribute " + dataGenerator.getInt(),
                    dataGenerator.getBoolean(), Long.valueOf(dataGenerator.getLong()), Date.currentTime(),
                    dataGenerator.getEnum(EnumMock.class), new CustomValueType("Custom value type " + i,
                            Long.valueOf(i)));

            List<String> valueTypeCollection = new ArrayList<String>(2);
            valueTypeCollection.add("Value type " + i + " 1");
            valueTypeCollection.add("Value type " + i + " 2");
            dtoMock.setValueTypeCollection(valueTypeCollection);

            List<CustomValueType> customValueTypeObjectCollection = new ArrayList<CustomValueType>();
            customValueTypeObjectCollection.add(new CustomValueType("Custom value type " + i + " 1", Long.valueOf(1)));
            customValueTypeObjectCollection.add(new CustomValueType("Custom value type " + i + " 2", Long.valueOf(2)));
            dtoMock.setCustomValueTypeCollection(customValueTypeObjectCollection);

            Map<Integer, String> valueTypeMap = new HashMap<Integer, String>(2);
            valueTypeMap.put(Integer.valueOf(1), "Value 1");
            valueTypeMap.put(Integer.valueOf(2), "Value 2");
            dtoMock.setValueTypeMap(valueTypeMap);

            Map<CustomValueType, CustomValueType> customValueTypeMap = new HashMap<CustomValueType, CustomValueType>(2);
            customValueTypeMap.put(new CustomValueType("Custom value type key 1", Long.valueOf(1)),
                    new CustomValueType("Custom value type value 1", Long.valueOf(1)));
            customValueTypeMap.put(new CustomValueType("Custom value type key 2", Long.valueOf(2)),
                    new CustomValueType("Custom value type value 2", Long.valueOf(2)));
            dtoMock.setCustomValueTypeMap(customValueTypeMap);

            identifiables.add(dtoMock);
        }
        return identifiables;
    }

    @Override
    protected void modify(RegularDto identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
        identifiable.setAttributeCustomValueType(new CustomValueType("Custom value type", Long.valueOf(dataGenerator
                .getLong())));

        List<String> valueTypeCollection = new ArrayList<String>(identifiable.getValueTypeCollection());
        valueTypeCollection.add("New value type: " + dataGenerator.getInt());
        identifiable.setValueTypeCollection(valueTypeCollection);

        List<CustomValueType> customValueTypeCollection = new ArrayList<CustomValueType>(
                identifiable.getCustomValueTypeCollection());
        customValueTypeCollection.clear();
        customValueTypeCollection.add(new CustomValueType("Custom value type", Long.valueOf(dataGenerator.getLong())));
        identifiable.setCustomValueTypeCollection(customValueTypeCollection);

        Map<Integer, String> valueTypeMap = new HashMap<Integer, String>(identifiable.getValueTypeMap());
        valueTypeMap.put(Integer.valueOf(Integer.MAX_VALUE), "New value: " + dataGenerator.getInt());
        identifiable.setValueTypeMap(valueTypeMap);

        Map<CustomValueType, CustomValueType> customValueTypeMap = new HashMap<CustomValueType, CustomValueType>(
                identifiable.getCustomValueTypeMap());
        customValueTypeMap.put(new CustomValueType("New custom value type key", Long.valueOf(1)), new CustomValueType(
                "New custom value type value", Long.valueOf(1)));
        identifiable.setCustomValueTypeMap(customValueTypeMap);
    }

    @Override
    protected void assertEqualState(RegularDto expected, RegularDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
        Assert.assertEquals(expected.getAttributeCustomValueType(), actual.getAttributeCustomValueType());
        Assert.assertTrue(expected.getValueTypeCollection().equals(actual.getValueTypeCollection()));
        Assert.assertTrue(expected.getCustomValueTypeCollection().equals(actual.getCustomValueTypeCollection()));
        Assert.assertTrue(expected.getValueTypeMap().equals(actual.getValueTypeMap()));
        Assert.assertTrue(expected.getCustomValueTypeMap().equals(actual.getCustomValueTypeMap()));
    }

    @Override
    protected List<SearchCase<RegularDto, FilterMock, SortKeyMock>> getSearchCases() {

        List<SearchCase<RegularDto, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<RegularDto, FilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDto dto1 = new RegularDto("Model Object 1", true, Long.valueOf(1), date1, EnumMock.ELEMENT_1, null);
        RegularDto dto2 = new RegularDto("Model Object 2", false, Long.valueOf(2), date2, EnumMock.ELEMENT_1, null);
        RegularDto dto3 = new RegularDto("Model Object 3", true, Long.valueOf(3), date3, EnumMock.ELEMENT_2, null);
        RegularDto dto4 = new RegularDto("Model Object 4", false, Long.valueOf(4), date4, EnumMock.ELEMENT_2, null);
        RegularDto dto5 = new RegularDto("Model Object 5", true, Long.valueOf(5), date5, EnumMock.ELEMENT_3, null);

        List<RegularDto> searchSpace = new ArrayList<RegularDto>(5);
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
        filter.setAttributeStringCondition(StringCondition.endWith("Model Object 3"));
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
