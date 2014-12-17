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
import com.hp.util.common.type.tuple.Pair;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.dao.SearchCase;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDependentDaoTest;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class DependentDaoTest extends
        JpaOffsetPageDependentDaoTest<Long, DependentDto, FilterMock, SortKeyMock, Long, IndependentDto, DependentDao> {

    private IndependentDao ownerDao;

    @Override
    protected DependentDao createDaoInstance() {
        DependentDao dao = new DependentDao();
        this.ownerDao = new IndependentDao(dao);
        return dao;
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
    protected DependentDto createIdentifiable(Id<DependentDto, Long> id) {
        return null;
    }

    @Override
    protected List<DependentDto> createIdentifiables(int count) {
        List<DependentDto> identifiables = new ArrayList<DependentDto>(count);
        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        for (int i = 0; i < count; i++) {
            IndependentDto owner = createOwnerInstance();
            Long naturalKey = Long.valueOf(i);
            DependentDto dependent = new DependentDto(owner.getId(), naturalKey, "String attribute "
                    + dataGenerator.getInt(), dataGenerator.getBoolean(), Long.valueOf(dataGenerator.getLong()),
                    Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
            owner.addDependent(dependent);
            identifiables.add(dependent);
        }
        return identifiables;
    }

    @Override
    protected void assertEqualState(DependentDto expected, DependentDto actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
    }

    @Override
    protected void modify(DependentDto identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
    }

    @Override
    protected Pair<DependentDto, IndependentDto> storeThroughOwner(
            final DependentDto dependent) throws Exception {
        return execute(new DaoQuery<Pair<DependentDto, IndependentDto>>() {
            @Override
            protected Pair<DependentDto, IndependentDto> execute(
                    DependentDao dao, JpaContext context) throws PersistenceException {
                IndependentDto ownerDto = createOwnerInstance();
                ownerDto.addDependent(dependent);
                ownerDto = DependentDaoTest.this.ownerDao.create(ownerDto, context);
                assert (ownerDto.getDependents().size() == 1);
                DependentDto dependentDto = ownerDto.getDependents().iterator().next();
                return Pair.valueOf(dependentDto, ownerDto);
            }
        });
    }

    @Override
    protected void clearThroughOwner() throws Exception {
        execute(new DaoQuery<Void>() {
            @Override
            protected Void execute(DependentDao dao, JpaContext context) throws PersistenceException {
                DependentDaoTest.this.ownerDao.clear(context);
                return null;
            }
        });

        Assert.assertEquals(0, size());
    }

    @Override
    protected List<SearchCase<DependentDto, FilterMock, SortKeyMock>> getSearchCases() {
        List<SearchCase<DependentDto, FilterMock, SortKeyMock>> searchCases = new ArrayList<SearchCase<DependentDto, FilterMock, SortKeyMock>>();

        Date date1 = Date.valueOf(new GregorianCalendar(2012, 1, 1).getTime());
        Date date2 = Date.valueOf(new GregorianCalendar(2012, 1, 2).getTime());
        Date date3 = Date.valueOf(new GregorianCalendar(2012, 1, 3).getTime());
        Date date4 = Date.valueOf(new GregorianCalendar(2012, 1, 4).getTime());
        Date date5 = Date.valueOf(new GregorianCalendar(2012, 1, 5).getTime());

        IndependentDto owner = createOwnerInstance();
        DependentDto dto1 = new DependentDto(owner.getId(), Long.valueOf(1), "Model Object 1", true, Long.valueOf(1),
                date1, EnumMock.ELEMENT_1);
        owner.addDependent(dto1);

        owner = createOwnerInstance();
        DependentDto dto2 = new DependentDto(owner.getId(), Long.valueOf(2), "Model Object 2", false, Long.valueOf(2),
                date2, EnumMock.ELEMENT_1);
        owner.addDependent(dto2);

        owner = createOwnerInstance();
        DependentDto dto3 = new DependentDto(owner.getId(), Long.valueOf(3), "Model Object 3", true, Long.valueOf(3),
                date3, EnumMock.ELEMENT_2);
        owner.addDependent(dto3);

        owner = createOwnerInstance();
        DependentDto dto4 = new DependentDto(owner.getId(), Long.valueOf(4), "Model Object 4", false, Long.valueOf(4),
                date4, EnumMock.ELEMENT_2);
        owner.addDependent(dto4);

        owner = createOwnerInstance();
        DependentDto dto5 = new DependentDto(owner.getId(), Long.valueOf(5), "Model Object 5", true, Long.valueOf(5),
                date5, EnumMock.ELEMENT_3);
        owner.addDependent(dto5);

        List<DependentDto> searchSpace = new ArrayList<DependentDto>(5);
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

    private static IndependentDto createOwnerInstance() {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        return new IndependentDto("String attribute " + dataGenerator.getInt(), dataGenerator.getBoolean(),
                Long.valueOf(dataGenerator.getLong()), Date.currentTime(), dataGenerator.getEnum(EnumMock.class));
    }
}
