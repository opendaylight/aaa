/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao.regular;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.dao.CassandraMarkPageDaoTest;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDaoTest.AstyanaxRegularDao;
import com.hp.util.model.persistence.cassandra.mock.EnumMock;
import com.hp.util.model.persistence.dao.SearchCase;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class RegularDaoTest extends CassandraMarkPageDaoTest<String, RegularDto, RegularDtoFilter, Void, AstyanaxRegularDao> {

    @Override
    protected AstyanaxRegularDao createDaoInstance() {
        return new AstyanaxRegularDao();
    }

    @Override
    protected boolean isVersioned() {
        return false;
    }

    @Override
    protected RegularDto createIdentifiable(Id<RegularDto, String> id) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        return new RegularDto(id, "String attribute " + dataGenerator.getInt(), dataGenerator.getBoolean(),
                Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected List<RegularDto> createIdentifiables(int count) {
        List<RegularDto> identifiables = new ArrayList<RegularDto>(count);
        for (int i = 0; i < count; i++) {
            identifiables.add(createIdentifiable(getId(i)));
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
    }

    @Override
    protected void assertEqualState(RegularDto expected, RegularDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
    }

    @Override
    protected List<SearchCase<RegularDto, RegularDtoFilter, Void>> getSearchCases() {
        List<SearchCase<RegularDto, RegularDtoFilter, Void>> searchCases = new ArrayList<SearchCase<RegularDto, RegularDtoFilter, Void>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        RegularDto dto1 = new RegularDto(getId(1), "Model Object 1", true, Long.valueOf(1), date1, EnumMock.ELEMENT_1);
        RegularDto dto2 = new RegularDto(getId(2), "Model Object 2", false, Long.valueOf(2), date2, EnumMock.ELEMENT_1);
        RegularDto dto3 = new RegularDto(getId(3), "Model Object 3", true, Long.valueOf(3), date3, EnumMock.ELEMENT_2);
        RegularDto dto4 = new RegularDto(getId(4), "Model Object 4", false, Long.valueOf(4), date4, EnumMock.ELEMENT_2);
        RegularDto dto5 = new RegularDto(getId(5), "Model Object 5", true, Long.valueOf(5), date5, EnumMock.ELEMENT_3);

        List<RegularDto> searchSpace = new ArrayList<RegularDto>(5);
        searchSpace.add(dto1);
        searchSpace.add(dto2);
        searchSpace.add(dto3);
        searchSpace.add(dto4);
        searchSpace.add(dto5);

        // SQL systems normally use primary key as the default sorting

        //

        RegularDtoFilter filter = null;
        SortSpecification<Void> sortSpecification = null;

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        //

        filter = RegularDtoFilter.filterAll();
        sortSpecification = new SortSpecification<Void>();

        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        return searchCases;
    }

    private static Id<RegularDto, String> getId(int i) {
        return Id.valueOf("id " + (i));
    }

    public static class AstyanaxRegularDao extends RegularDao<Astyanax> {
        /*
         * Class to allow using Astyanax-based integration test.
         */
    }
}
