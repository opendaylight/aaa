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
public class DeleteByIdQueryTest {

    private BaseDao<Long, IdentifiableObject, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.daoMock = EasyMock.createMock(BaseDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        Id<IdentifiableObject, Long> id = Id.valueOf(Long.valueOf(1));
        this.daoMock.delete(EasyMock.eq(id), EasyMock.same(this.contextMock));

        EasyMock.replay(this.daoMock);

        Query<Void, Object> query = DeleteByIdQuery.createQuery(id, this.daoMock);
        query.execute(this.contextMock);

        EasyMock.verify(this.daoMock);
    }

    private static class IdentifiableObject implements Identifiable<IdentifiableObject, Long> {

        @Override
        public <E extends IdentifiableObject> Id<E, Long> getId() {
            return null;
        }
    }
}
