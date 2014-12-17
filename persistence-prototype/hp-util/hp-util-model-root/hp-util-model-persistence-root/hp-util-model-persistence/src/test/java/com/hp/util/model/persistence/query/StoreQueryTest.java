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

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.BaseDao;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class StoreQueryTest {

    private Id<IdentifiableObject, Long> idMock;
    private IdentifiableObject identifiableMock;
    private BaseDao<Long, IdentifiableObject, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.idMock = EasyMock.createMock(Id.class);
        this.identifiableMock = EasyMock.createMock(IdentifiableObject.class);
        this.daoMock = EasyMock.createMock(BaseDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testExecuteAdd() throws Exception {
        EasyMock.expect(this.identifiableMock.getId()).andReturn(this.idMock);
        EasyMock.expect(this.daoMock.exist(EasyMock.same(this.idMock), EasyMock.same(this.contextMock))).andReturn(
                false);
        EasyMock.expect(this.daoMock.create(EasyMock.same(this.identifiableMock), EasyMock.same(this.contextMock)))
                .andReturn(this.identifiableMock);

        EasyMock.replay(this.identifiableMock, this.daoMock);

        Query<Void, Object> query = StoreQuery.createQuery(this.identifiableMock, this.daoMock);
        query.execute(this.contextMock);

        EasyMock.verify(this.identifiableMock, this.daoMock);
    }

    @SuppressWarnings("boxing")
    @Test
    public void testExecuteUpdate() throws Exception {
        EasyMock.expect(this.identifiableMock.getId()).andReturn(this.idMock);
        EasyMock.expect(this.daoMock.exist(EasyMock.same(this.idMock), EasyMock.same(this.contextMock)))
                .andReturn(true);
        this.daoMock.update(EasyMock.same(this.identifiableMock), EasyMock.same(this.contextMock));

        EasyMock.replay(this.identifiableMock, this.daoMock);

        Query<Void, Object> query = StoreQuery.createQuery(this.identifiableMock, this.daoMock);
        query.execute(this.contextMock);

        EasyMock.verify(this.identifiableMock, this.daoMock);
    }

    private static class IdentifiableObject implements Identifiable<IdentifiableObject, Long> {

        @Override
        public <E extends IdentifiableObject> Id<E, Long> getId() {
            return null;
        }
    }
}
