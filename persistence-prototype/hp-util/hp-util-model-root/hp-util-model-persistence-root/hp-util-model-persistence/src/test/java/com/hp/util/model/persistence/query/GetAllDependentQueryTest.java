/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.DependentDao;
import com.hp.util.model.persistence.query.DependentTestCase.DependentIdentifiable;
import com.hp.util.model.persistence.query.DependentTestCase.Owner;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class GetAllDependentQueryTest {

    private DependentIdentifiable dependentMock;
    private DependentDao<?, DependentIdentifiable, ?, ?, Long, Owner, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.dependentMock = EasyMock.createMock(DependentIdentifiable.class);
        this.daoMock = EasyMock.createMock(DependentDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        int resultCount = 3;
        List<DependentIdentifiable> resultMock = new ArrayList<DependentIdentifiable>(resultCount);
        for (int i = 0; i < resultCount; i++) {
            resultMock.add(this.dependentMock);
        }

        EasyMock.expect(this.daoMock.getAll(EasyMock.same(this.contextMock))).andReturn(resultMock);

        EasyMock.replay(this.daoMock);

        Query<Collection<DependentIdentifiable>, Object> query = GetAllDependentQuery.createQuery(this.daoMock);

        Collection<DependentIdentifiable> result = query.execute(this.contextMock);
        Assert.assertEquals(resultCount, result.size());
        for (DependentIdentifiable identifiable : result) {
            Assert.assertSame(this.dependentMock, identifiable);
        }

        EasyMock.verify(this.daoMock);
    }
}
