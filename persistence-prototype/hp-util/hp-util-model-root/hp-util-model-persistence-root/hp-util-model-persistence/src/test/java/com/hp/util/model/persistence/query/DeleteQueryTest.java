/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.Dao;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class DeleteQueryTest {

    private Object filterMock;
    private Dao<?, ?, Object, ?, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.filterMock = EasyMock.createMock(Object.class);
        this.daoMock = EasyMock.createMock(Dao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        this.daoMock.delete(EasyMock.same(this.filterMock), EasyMock.same(this.contextMock));

        EasyMock.replay(this.daoMock);

        Query<Void, Object> query = DeleteQuery.createQuery(this.filterMock, this.daoMock);
        query.execute(this.contextMock);

        EasyMock.verify(this.daoMock);
    }
}
