/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.DependentDao;
import com.hp.util.model.persistence.query.DependentTestCase.DependentIdentifiable;
import com.hp.util.model.persistence.query.DependentTestCase.Owner;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class GetDependentQueryTest {

    private DependentIdentifiable dependentDtoMock;
    private DependentDao<Long, DependentIdentifiable, ?, ?, Long, Owner, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.dependentDtoMock = EasyMock.createMock(DependentIdentifiable.class);
        this.daoMock = EasyMock.createMock(DependentDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        Id<DependentIdentifiable, Long> internalId = Id.valueOf(Long.valueOf(1));
        Id<DependentIdentifiable, Long> id = Id.valueOf(Long.valueOf(1));

        EasyMock.expect(this.daoMock.get(EasyMock.eq(internalId), EasyMock.same(this.contextMock))).andReturn(
                this.dependentDtoMock);

        EasyMock.replay(this.daoMock);

        Query<DependentIdentifiable, Object> query = GetDependentQuery.createQuery(id, this.daoMock);
        Assert.assertSame(this.dependentDtoMock, query.execute(this.contextMock));

        EasyMock.verify(this.daoMock);
    }
}
