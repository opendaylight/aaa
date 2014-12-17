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

import com.hp.util.common.Identifiable;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.BaseDao;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class AddQueryTest {

    private Identifiable<Object, Long> toCreateMock;
    private Identifiable<Object, Long> createdMock;
    private BaseDao<Long, Identifiable<Object, Long>, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.toCreateMock = EasyMock.createMock(Identifiable.class);
        this.createdMock = EasyMock.createMock(Identifiable.class);
        this.daoMock = EasyMock.createMock(BaseDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        EasyMock.expect(this.daoMock.create(EasyMock.same(this.toCreateMock), EasyMock.same(this.contextMock)))
                .andReturn(this.createdMock);

        EasyMock.replay(this.daoMock);

        Query<Identifiable<Object, Long>, Object> query = AddQuery.createQuery(this.toCreateMock, this.daoMock);
        Assert.assertSame(this.createdMock, query.execute(this.contextMock));

        EasyMock.verify(this.daoMock);
    }
}
