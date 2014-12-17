/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraTestUtil;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler.IndexEntry;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class IndexEntryHandlerTest {

    IndexEntryHandler<Long> indexEntries;

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

        this.indexEntries = new IndexEntryHandler<Long>("cf_test_index_entries", BasicType.LONG);

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> definition : IndexEntryHandlerTest.this.indexEntries.getColumnFamilies()) {
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
                for (ColumnFamily<?, ?> columnFamily : IndexEntryHandlerTest.this.indexEntries.getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);
                }
                return null;
            }
        });

        CassandraTestUtil.afterTest();
    }

    /**
     * @throws Exception if errors occur
     */
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

        ColumnName<String, String> columnName1 = ColumnName.valueOf("indexed column 1");
        ColumnName<String, EnumMock> columnName2 = ColumnName.valueOf("indexed column 2");

        DataType<String> indexColumn1ValueType = BasicType.STRING_UTF8;
        DataType<EnumMock> indexColumn2ValueType = EnumType.valueOf(EnumMock.class);

        Long id1 = Long.valueOf(1);
        Long id2 = Long.valueOf(2);
        Long id3 = Long.valueOf(3);

        String valueColumn1Id1 = "valueColumn1Id1";
        String valueColumn1Id2 = "valueColumn1Id2";

        EnumMock valueColumn2Id1 = EnumMock.ELEMENT_1;
        EnumMock valueColumn2Id2 = EnumMock.ELEMENT_2;

        // Test initial state

        Assert.assertTrue(this.indexEntries.getIndexedValues(id1, columnName1, indexColumn1ValueType, context).isEmpty());

        // Adds index entries

        this.indexEntries.addIndexedValue(id1, new StringColumn<String>(columnName1, valueColumn1Id1), context);
        this.indexEntries.addIndexedValue(id2, new StringColumn<String>(columnName1, valueColumn1Id2), context);

        this.indexEntries.addIndexedValue(id1, new EnumColumn<String, EnumMock>(columnName2, valueColumn2Id1), context);
        this.indexEntries.addIndexedValue(id2, new EnumColumn<String, EnumMock>(columnName2, valueColumn2Id2), context);

        // Test index entries

        Assert.assertTrue(this.indexEntries.getIndexedValues(id3, columnName1, indexColumn1ValueType, context).isEmpty());
        Assert.assertTrue(this.indexEntries.getIndexedValues(id3, columnName2, indexColumn2ValueType, context).isEmpty());

        Collection<Column<IndexEntry, String>> column1Id1IndexEntries = this.indexEntries.getIndexedValues(id1,
                columnName1, indexColumn1ValueType, context);

        Assert.assertEquals(1, column1Id1IndexEntries.size());
        Assert.assertEquals(valueColumn1Id1, column1Id1IndexEntries.iterator().next().getValue());

        Collection<Column<IndexEntry, String>> column1Id2IndexEntries = this.indexEntries.getIndexedValues(id2,
                columnName1, indexColumn1ValueType, context);

        Assert.assertEquals(1, column1Id2IndexEntries.size());
        Assert.assertEquals(valueColumn1Id2, column1Id2IndexEntries.iterator().next().getValue());

        Collection<Column<IndexEntry, EnumMock>> column2Id1IndexEntries = this.indexEntries.getIndexedValues(id1,
                columnName2, indexColumn2ValueType, context);

        Assert.assertEquals(1, column2Id1IndexEntries.size());
        Assert.assertEquals(valueColumn2Id1, column2Id1IndexEntries.iterator().next().getValue());

        Collection<Column<IndexEntry, EnumMock>> column2Id2IndexEntries = this.indexEntries.getIndexedValues(id2,
                columnName2, indexColumn2ValueType, context);

        Assert.assertEquals(1, column2Id2IndexEntries.size());
        Assert.assertEquals(valueColumn2Id2, column2Id2IndexEntries.iterator().next().getValue());

        // Adds more indexes

        this.indexEntries.addIndexedValue(id1, new StringColumn<String>(columnName1, "new indexed value"), context);
        column1Id1IndexEntries = this.indexEntries.getIndexedValues(id1, columnName1, indexColumn1ValueType, context);
        Assert.assertEquals(2, column1Id1IndexEntries.size());
        Set<String> values = new HashSet<String>();
        for (Column<IndexEntry, String> indexEntry : column1Id1IndexEntries) {
            Assert.assertTrue(indexEntry.getValue().endsWith(valueColumn1Id1)
                    || indexEntry.getValue().endsWith("new indexed value"));
            values.add(indexEntry.getValue());
        }
        Assert.assertEquals(2, values.size());

        // Test delete index entries

        this.indexEntries.deleteIndexedValues(id1, column1Id1IndexEntries, context);
        Assert.assertTrue(this.indexEntries.getIndexedValues(id1, columnName1, indexColumn1ValueType, context).isEmpty());

        this.indexEntries.deleteIndexedValues(id2, column1Id2IndexEntries, context);
        Assert.assertTrue(this.indexEntries.getIndexedValues(id2, columnName1, indexColumn1ValueType, context).isEmpty());

        this.indexEntries.deleteIndexedValues(id1, column2Id1IndexEntries, context);
        Assert.assertTrue(this.indexEntries.getIndexedValues(id1, columnName2, indexColumn2ValueType, context).isEmpty());

        this.indexEntries.deleteIndexedValues(id2, column2Id2IndexEntries, context);
        Assert.assertTrue(this.indexEntries.getIndexedValues(id2, columnName2, indexColumn2ValueType, context).isEmpty());
    }

    private static enum EnumMock {
        ELEMENT_1, ELEMENT_2, ELEMENT_3
    }
}
