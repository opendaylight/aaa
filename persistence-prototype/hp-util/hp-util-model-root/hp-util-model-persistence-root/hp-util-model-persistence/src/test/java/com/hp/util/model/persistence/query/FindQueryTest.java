/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.Dao;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class FindQueryTest {

    private IdentifiableObject identifiableMock;
    private Object filterMock;
    private SortSpecification<Object> sortSpecMock;
    private Dao<?, IdentifiableObject, Object, Object, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.identifiableMock = EasyMock.createMock(IdentifiableObject.class);
        this.filterMock = EasyMock.createMock(Object.class);
        this.sortSpecMock = EasyMock.createMock(SortSpecification.class);
        this.daoMock = EasyMock.createMock(Dao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        int resultCount = 3;
        List<IdentifiableObject> resultMock = new ArrayList<IdentifiableObject>(resultCount);
        for (int i = 0; i < resultCount; i++) {
            resultMock.add(this.identifiableMock);
        }

        EasyMock.expect(
                this.daoMock.find(EasyMock.same(this.filterMock), EasyMock.same(this.sortSpecMock),
                        EasyMock.same(this.contextMock))).andReturn(resultMock);

        EasyMock.replay(this.daoMock);

        Query<List<IdentifiableObject>, Object> query = FindQuery.createQuery(this.filterMock, this.sortSpecMock,
                this.daoMock);

        List<IdentifiableObject> result = query.execute(this.contextMock);
        Assert.assertEquals(resultCount, result.size());
        for (IdentifiableObject identifiable : result) {
            Assert.assertSame(this.identifiableMock, identifiable);
        }

        EasyMock.verify(this.daoMock);
    }

    private static class IdentifiableObject implements Identifiable<IdentifiableObject, Long> {

        @Override
        public <E extends IdentifiableObject> Id<E, Long> getId() {
            return null;
        }
    }
}
