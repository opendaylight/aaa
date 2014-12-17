/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraTestUtil;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class EnumSecondaryIndexTest {
    EnumSecondaryIndex<EnumMock, Long, String> index;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CassandraTestUtil.beforeTestClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        CassandraTestUtil.afterTestClass();
    }

    @Before
    public void beforeTest() throws Exception {
        Assume.assumeTrue(CassandraTestUtil.isIntegrationTestSupported());

        CassandraTestUtil.beforeTest();

        this.index = new EnumSecondaryIndex<EnumMock, Long, String>(EnumMock.class, "cf_test_enum_secondary_index",
                BasicType.LONG, BasicType.STRING_UTF8);

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> definition : EnumSecondaryIndexTest.this.index.getColumnFamilies()) {
                    context.getCassandraClient().createColumnFamily(definition, context.getKeyspace(), context);
                }
                return null;
            }
        });
    }

    @After
    public void afterTest() throws Exception {
        if (!CassandraTestUtil.isIntegrationTestSupported()) {
            return;
        }

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> columnFamily : EnumSecondaryIndexTest.this.index.getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);
                }
                return null;
            }
        });

        CassandraTestUtil.afterTest();
    }

    @Test
    public void testIndex() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testIndex(context);
                return null;
            }
        });
    }

    protected void testIndex(CassandraContext<Astyanax> context) throws PersistenceException {
        // Data sets definitions

        EnumMock indexKey = EnumMock.ELEMENT_2;

        Long id1 = Long.valueOf(1);
        Long id2 = Long.valueOf(2);
        Long id3 = Long.valueOf(3);
        Long id4 = Long.valueOf(4);
        Long id5 = Long.valueOf(5);

        String denormilizedData1 = "denormilized data for id 1";
        String denormilizedData2 = "denormilized data for id 2";
        String denormilizedData3 = "denormilized data for id 3";
        String denormilizedData4 = "denormilized data for id 4";
        String denormilizedData5 = "denormilized data for id 5";

        // Test initial state
        Assert.assertEquals(0, this.index.count(indexKey, context));

        // Test update after insert
        this.index.insert(id1, null, indexKey, context);
        Assert.assertEquals(1, this.index.count(indexKey, context));

        this.index.insert(id1, null, indexKey, context);
        Assert.assertEquals(1, this.index.count(indexKey, context));

        // Test update after delete
        this.index.delete(id1, indexKey, context);
        Assert.assertEquals(0, this.index.count(indexKey, context));

        // Test update after delete all
        this.index.insert(id1, null, indexKey, context);
        this.index.insert(id2, null, indexKey, context);
        Assert.assertEquals(2, this.index.count(indexKey, context));

        this.index.clear(context);
        Assert.assertEquals(0, this.index.count(indexKey, context));

        // Test read
        this.index.insert(id1, denormilizedData1, indexKey, context);
        this.index.insert(id2, denormilizedData2, indexKey, context);
        this.index.insert(id3, denormilizedData3, indexKey, context);
        this.index.insert(id4, denormilizedData4, indexKey, context);
        this.index.insert(id5, denormilizedData5, indexKey, context);

        List<Column<Long, String>> indexes = this.index.read(indexKey, context);
        Assert.assertEquals(5, indexes.size());
        Assert.assertEquals(id1, indexes.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData1, indexes.get(0).getValue());
        Assert.assertEquals(id2, indexes.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData2, indexes.get(1).getValue());
        Assert.assertEquals(id3, indexes.get(2).getName().getValue());
        Assert.assertEquals(denormilizedData3, indexes.get(2).getValue());
        Assert.assertEquals(id4, indexes.get(3).getName().getValue());
        Assert.assertEquals(denormilizedData4, indexes.get(3).getValue());
        Assert.assertEquals(id5, indexes.get(4).getName().getValue());
        Assert.assertEquals(denormilizedData5, indexes.get(4).getValue());

        // Test read columns
        indexes = this.index.read(Arrays.asList(id1, id5, Long.valueOf(100)), indexKey, context);
        Assert.assertEquals(2, indexes.size());
        Assert.assertEquals(id1, indexes.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData1, indexes.get(0).getValue());
        Assert.assertEquals(id5, indexes.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData5, indexes.get(1).getValue());

        // Test paged read

        // No need to do extensive paging testing because AstyanaxFacade is used to read the column
        // page,
        // and extensive paging testing is performed in AstyanaxFacadeTest.

        MarkPageRequest<Long> pageRequest = new MarkPageRequest<Long>(id2, Navigation.NEXT, 2);
        MarkPage<Column<Long, String>> page = this.index.read(indexKey, pageRequest, context);
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(id2, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData2, page.getRequest().getMark().getValue());
        Assert.assertEquals(id3, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData3, page.getData().get(0).getValue());
        Assert.assertEquals(id4, page.getData().get(1).getName().getValue());
        Assert.assertEquals(denormilizedData4, page.getData().get(1).getValue());
    }

    public static enum EnumMock {
        /** */
        ELEMENT_1,
        /** */
        ELEMENT_2,
        /** */
        ELEMENT_3
    }
}
