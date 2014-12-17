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
import com.hp.util.model.persistence.dao.DependentDao;
import com.hp.util.model.persistence.query.DependentTestCase.DependentIdentifiable;
import com.hp.util.model.persistence.query.DependentTestCase.Owner;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class UpdateDependentQueryTest {

    private DependentIdentifiable dependentDtoToUpdateMock;
    private DependentDao<Long, DependentIdentifiable, ?, ?, Long, Owner, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.dependentDtoToUpdateMock = EasyMock.createMock(DependentIdentifiable.class);
        this.daoMock = EasyMock.createMock(DependentDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        this.daoMock.update(EasyMock.same(this.dependentDtoToUpdateMock), EasyMock.same(this.contextMock));

        EasyMock.replay(this.daoMock);

        Query<Void, Object> query = UpdateDependentQuery.createQuery(this.dependentDtoToUpdateMock, this.daoMock);
        query.execute(this.contextMock);

        EasyMock.verify(this.daoMock);
    }
}
