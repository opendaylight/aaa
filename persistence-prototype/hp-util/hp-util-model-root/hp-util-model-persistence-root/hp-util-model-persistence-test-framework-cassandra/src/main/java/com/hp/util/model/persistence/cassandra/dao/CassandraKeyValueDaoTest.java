/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao;

import java.io.Serializable;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

import com.hp.util.common.Identifiable;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraTestDataStoreProvider;
import com.hp.util.model.persistence.cassandra.CassandraTestUtil;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.dao.KeyValueDao;
import com.hp.util.model.persistence.dao.KeyValueDaoTest;

/**
 * Integration test for {@link CassandraBaseDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class CassandraKeyValueDaoTest<I extends Serializable, T extends Identifiable<? super T, I>, D extends KeyValueDao<I, T, CassandraContext<Astyanax>> & ColumnFamilyHandler>
        extends KeyValueDaoTest<I, T, CassandraContext<Astyanax>, D> {

    /**
     * Creates a new DAO integration test.
     */
    public CassandraKeyValueDaoTest() {
        super(CassandraTestDataStoreProvider.getDataStore());
    }

    /**
     * Method executed before running the class tests.
     *
     * @throws Exception if errors occur
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        CassandraTestUtil.beforeTestClass();
    }

    /**
     * Method executed after running the class tests.
     *
     * @throws Exception if errors occur
     */
    @AfterClass
    public static void afterClass() throws Exception {
        CassandraTestUtil.afterTestClass();
    }

    /**
     * Method executed before running each test.
     *
     * @throws Exception if any errors occur during execution
     */
    @Before
    @Override
    public void beforeTest() throws Exception {
        Assume.assumeTrue(CassandraTestUtil.isIntegrationTestSupported());
        super.beforeTest();
        CassandraTestUtil.beforeTest();
        CassandraTestUtil.createColumnFamilies(createDaoInstance());
    }

    /**
     * Method executed after running each test.
     *
     * @throws Exception if any errors occur during execution
     */
    @Before
    @Override
    public void afterTest() throws Exception {
        if (!CassandraTestUtil.isIntegrationTestSupported()) {
            return;
        }
        super.afterTest();
        CassandraTestUtil.dropColumnFamilies(createDaoInstance());
        CassandraTestUtil.afterTest();
    }

    @Override
    protected void clear() throws Exception {
        CassandraTestUtil.clearColumnFamilies(createDaoInstance());
    }

    @Override
    protected boolean isPrimaryKeyIntegrityConstraintViolationSupported() {
        return false;
    }
}
