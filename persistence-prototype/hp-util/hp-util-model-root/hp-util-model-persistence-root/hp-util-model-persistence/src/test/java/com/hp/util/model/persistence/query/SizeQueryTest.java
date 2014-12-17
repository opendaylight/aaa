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

import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.Dao;
import com.hp.util.model.persistence.dao.KeyValueDao;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class SizeQueryTest {

    private KeyValueDao<?, ?, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.daoMock = EasyMock.createMock(Dao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    @SuppressWarnings("boxing")
    public void testExecute() throws Exception {
        Long result = Long.valueOf(10);
        EasyMock.expect(this.daoMock.size(EasyMock.same(this.contextMock))).andReturn(result);

        EasyMock.replay(this.daoMock);

        Query<Long, Object> query = SizeQuery.createQuery(this.daoMock);
        Assert.assertEquals(result, query.execute(this.contextMock));

        EasyMock.verify(this.daoMock);
    }
}
