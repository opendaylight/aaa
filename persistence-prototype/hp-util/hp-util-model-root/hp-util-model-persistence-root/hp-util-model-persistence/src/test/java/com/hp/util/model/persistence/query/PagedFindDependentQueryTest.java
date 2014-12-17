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

import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.page.Page;
import com.hp.util.common.type.page.PageRequest;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.PagedDependentDao;
import com.hp.util.model.persistence.query.DependentTestCase.DependentIdentifiable;
import com.hp.util.model.persistence.query.DependentTestCase.Owner;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class PagedFindDependentQueryTest {

    private Object filterMock;
    private SortSpecification<Object> sortSpecMock;
    private PageRequest pageRequestMock;
    private Page<PageRequest, DependentIdentifiable> pageMock;
    private PagedDependentDao<?, DependentIdentifiable, Object, Object, PageRequest, Page<PageRequest, DependentIdentifiable>, Long, Owner, Object> daoMock;
    private Object contextMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        this.filterMock = EasyMock.createMock(Object.class);
        this.sortSpecMock = EasyMock.createMock(SortSpecification.class);
        this.pageRequestMock = EasyMock.createMock(PageRequest.class);
        this.pageMock = EasyMock.createMock(Page.class);
        this.daoMock = EasyMock.createMock(PagedDependentDao.class);
        this.contextMock = EasyMock.createMock(Object.class);
    }

    @Test
    public void testExecute() throws Exception {
        EasyMock.expect(
                this.daoMock.find(EasyMock.same(this.filterMock), EasyMock.same(this.sortSpecMock),
                        EasyMock.same(this.pageRequestMock), EasyMock.same(this.contextMock))).andReturn(this.pageMock);

        EasyMock.replay(this.daoMock);

        Query<Page<PageRequest, DependentIdentifiable>, Object> query = PagedFindDependentQuery.createQuery(
                this.filterMock, this.sortSpecMock, this.pageRequestMock, this.daoMock);
        Assert.assertSame(this.pageMock, query.execute(this.contextMock));

        EasyMock.verify(this.daoMock);
    }
}
