/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.util.common.Mutator;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraTestUtil;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.index.StringSecondaryIndex.StringIndexEntry;
import com.hp.util.model.persistence.cassandra.index.StringSecondaryIndex.StringIndexEntryFactory;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class StringSecondaryIndexTest {

    private StringSecondaryIndex<Long, IndexEntryGroupedByDate, String> index;

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

        Mutator<IndexEntryGroupedByDate, String> mutator = new Mutator<IndexEntryGroupedByDate, String>() {
            @Override
            public IndexEntryGroupedByDate mutate(IndexEntryGroupedByDate target, String mutation) {
                return new IndexEntryGroupedByDate(target.getGroupBy(), mutation, target.getRowKey());
            }
        };

        CompositeTypeSerializer<IndexEntryGroupedByDate> indexEntryGroupedByDateSerializer = new SortedByDateSerializer();
        CompositeType<IndexEntryGroupedByDate> compositeType = new CompositeType<IndexEntryGroupedByDate>(
                indexEntryGroupedByDateSerializer, BasicType.DATE, BasicType.STRING_UTF8, BasicType.LONG);

        this.index = new StringSecondaryIndex<Long, IndexEntryGroupedByDate, String>("cf_test_string_secondary_index",
                compositeType, RangeLimit.LONG_RANGE_LIMIT, mutator, BasicType.STRING_UTF8);

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> definition : StringSecondaryIndexTest.this.index.getColumnFamilies()) {
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
                for (ColumnFamily<?, ?> columnFamily : StringSecondaryIndexTest.this.index.getColumnFamilies()) {
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
        // Test data

        Long id1 = Long.valueOf(1);
        Long id2 = Long.valueOf(2);
        Long id3 = Long.valueOf(3);
        Long id4 = Long.valueOf(4);
        Long id5 = Long.valueOf(5);
        Long id6 = Long.valueOf(6);
        Long id7 = Long.valueOf(7);
        Long id8 = Long.valueOf(8);
        Long id9 = Long.valueOf(9);
        Long id10 = Long.valueOf(10);

        String indexedStringValue1 = "abc xyz";
        String indexedStringValue2 = "ab yz";
        String indexedStringValue3 = "bca cxy";
        String indexedStringValue4 = "bcb bxy";
        String indexedStringValue5 = "bcc axy";
        String indexedStringValue6 = "bc xy";
        String indexedStringValue7 = "Duplicated value";
        String indexedStringValue8 = "Duplicated value";
        String indexedStringValue9 = "Duplicated value";
        String indexedStringValue10 = "Duplicated value";

        final Date groupBy = Date.valueOf(1);

        IndexEntryGroupedByDate indexEntry1 = new IndexEntryGroupedByDate(groupBy, indexedStringValue1, id1);
        IndexEntryGroupedByDate indexEntry2 = new IndexEntryGroupedByDate(groupBy, indexedStringValue2, id2);
        IndexEntryGroupedByDate indexEntry3 = new IndexEntryGroupedByDate(groupBy, indexedStringValue3, id3);
        IndexEntryGroupedByDate indexEntry4 = new IndexEntryGroupedByDate(groupBy, indexedStringValue4, id4);
        IndexEntryGroupedByDate indexEntry5 = new IndexEntryGroupedByDate(groupBy, indexedStringValue5, id5);
        IndexEntryGroupedByDate indexEntry6 = new IndexEntryGroupedByDate(groupBy, indexedStringValue6, id6);
        IndexEntryGroupedByDate indexEntry7 = new IndexEntryGroupedByDate(groupBy, indexedStringValue7, id7);
        IndexEntryGroupedByDate indexEntry8 = new IndexEntryGroupedByDate(groupBy, indexedStringValue8, id8);
        IndexEntryGroupedByDate indexEntry9 = new IndexEntryGroupedByDate(groupBy, indexedStringValue9, id9);
        IndexEntryGroupedByDate indexEntry10 = new IndexEntryGroupedByDate(groupBy, indexedStringValue10, id10);

        String denormilizedData1 = "denormilized data for id 1";
        String denormilizedData2 = "denormilized data for id 2";
        String denormilizedData3 = "denormilized data for id 3";
        String denormilizedData4 = "denormilized data for id 4";
        String denormilizedData5 = "denormilized data for id 5";
        String denormilizedData6 = "denormilized data for id 6";
        String denormilizedData7 = "denormilized data for id 7";
        String denormilizedData8 = "denormilized data for id 8";
        String denormilizedData9 = "denormilized data for id 9";
        String denormilizedData10 = "denormilized data for id 10";

        this.index.insert(indexEntry1, denormilizedData1, context);
        this.index.insert(indexEntry2, denormilizedData2, context);
        this.index.insert(indexEntry3, denormilizedData3, context);
        this.index.insert(indexEntry4, denormilizedData4, context);
        this.index.insert(indexEntry5, denormilizedData5, context);
        this.index.insert(indexEntry6, denormilizedData6, context);
        this.index.insert(indexEntry7, denormilizedData7, context);
        this.index.insert(indexEntry8, denormilizedData8, context);
        this.index.insert(indexEntry9, denormilizedData9, context);
        this.index.insert(indexEntry10, denormilizedData10, context);

        // indexEntryFactory is used to include grouping and sorting information. In this example
        // groupBy is included.
        StringIndexEntryFactory<Long, IndexEntryGroupedByDate> indexEntryFactory = new StringIndexEntryFactory<Long, IndexEntryGroupedByDate>() {
            @Override
            public IndexEntryGroupedByDate create(String indexedStringValue, Long rowKey) {
                return new IndexEntryGroupedByDate(groupBy, indexedStringValue, rowKey);
            }
        };

        // Test equals

        Assert.assertEquals(1, this.index.count("ab yz", indexEntryFactory, context));

        List<Column<IndexEntryGroupedByDate, String>> result = this.index.read("Duplicated value", indexEntryFactory,
                context);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(indexEntry7, result.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData7, result.get(0).getValue());
        Assert.assertEquals(indexEntry8, result.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData8, result.get(1).getValue());
        Assert.assertEquals(indexEntry9, result.get(2).getName().getValue());
        Assert.assertEquals(denormilizedData9, result.get(2).getValue());
        Assert.assertEquals(indexEntry10, result.get(3).getName().getValue());
        Assert.assertEquals(denormilizedData10, result.get(3).getValue());

        MarkPageRequest<IndexEntryGroupedByDate> pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(1);
        MarkPage<Column<IndexEntryGroupedByDate, String>> page = this.index.read("Duplicated value", pageRequest,
                indexEntryFactory, context);
        Assert.assertEquals(1, page.getData().size());
        Assert.assertNull(page.getRequest().getMark());
        Assert.assertEquals(indexEntry7, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData7, page.getData().get(0).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(indexEntry8, Navigation.NEXT, 1);
        page = this.index.read("Duplicated value", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(1, page.getData().size());
        Assert.assertEquals(indexEntry8, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData8, page.getRequest().getMark().getValue());
        Assert.assertEquals(indexEntry9, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData9, page.getData().get(0).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(indexEntry9, Navigation.PREVIOUS, 2);
        page = this.index.read("Duplicated value", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(indexEntry9, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData9, page.getRequest().getMark().getValue());
        Assert.assertEquals(indexEntry7, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData7, page.getData().get(0).getValue());
        Assert.assertEquals(indexEntry8, page.getData().get(1).getName().getValue());
        Assert.assertEquals(denormilizedData8, page.getData().get(1).getValue());

        List<IndexEntryGroupedByDate> indexEntries = new ArrayList<IndexEntryGroupedByDate>();
        indexEntries.add(indexEntry7);
        indexEntries.add(indexEntry9);
        indexEntries.add(indexEntry10);
        result = this.index.read("Duplicated value", indexEntries, indexEntryFactory, context);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(indexEntry7, result.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData7, result.get(0).getValue());
        Assert.assertEquals(indexEntry9, result.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData9, result.get(1).getValue());
        Assert.assertEquals(indexEntry10, result.get(2).getName().getValue());
        Assert.assertEquals(denormilizedData10, result.get(2).getValue());

        // Test starts with

        Assert.assertEquals(4, this.index.countStartsWith("bc", indexEntryFactory, context));

        result = this.index.readStartsWith("bc", indexEntryFactory, context);
        Assert.assertEquals(indexEntry6, result.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData6, result.get(0).getValue());
        Assert.assertEquals(indexEntry3, result.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData3, result.get(1).getValue());
        Assert.assertEquals(indexEntry4, result.get(2).getName().getValue());
        Assert.assertEquals(denormilizedData4, result.get(2).getValue());
        Assert.assertEquals(indexEntry5, result.get(3).getName().getValue());
        Assert.assertEquals(denormilizedData5, result.get(3).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(1);
        page = this.index.readStartsWith("bc", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(1, page.getData().size());
        Assert.assertNull(page.getRequest().getMark());
        Assert.assertEquals(indexEntry6, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData6, page.getData().get(0).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(indexEntry3, Navigation.NEXT, 1);
        page = this.index.readStartsWith("bc", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(1, page.getData().size());
        Assert.assertEquals(indexEntry3, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData3, page.getRequest().getMark().getValue());
        Assert.assertEquals(indexEntry4, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData4, page.getData().get(0).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(indexEntry5, Navigation.PREVIOUS, 2);
        page = this.index.readStartsWith("bc", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(indexEntry5, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData5, page.getRequest().getMark().getValue());
        Assert.assertEquals(indexEntry3, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData3, page.getData().get(0).getValue());
        Assert.assertEquals(indexEntry4, page.getData().get(1).getName().getValue());
        Assert.assertEquals(denormilizedData4, page.getData().get(1).getValue());

        indexEntries = new ArrayList<IndexEntryGroupedByDate>();
        indexEntries.add(indexEntry4);
        indexEntries.add(indexEntry5);
        result = this.index.readStartsWith("bc", indexEntries, indexEntryFactory, context);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(indexEntry4, result.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData4, result.get(0).getValue());
        Assert.assertEquals(indexEntry5, result.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData5, result.get(1).getValue());

        // Test ends with

        Assert.assertEquals(4, this.index.countEndsWith("xy", indexEntryFactory, context));

        result = this.index.readEndsWith("xy", indexEntryFactory, context);
        Assert.assertEquals(indexEntry6, result.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData6, result.get(0).getValue());
        Assert.assertEquals(indexEntry5, result.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData5, result.get(1).getValue());
        Assert.assertEquals(indexEntry4, result.get(2).getName().getValue());
        Assert.assertEquals(denormilizedData4, result.get(2).getValue());
        Assert.assertEquals(indexEntry3, result.get(3).getName().getValue());
        Assert.assertEquals(denormilizedData3, result.get(3).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(1);
        page = this.index.readEndsWith("xy", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(1, page.getData().size());
        Assert.assertNull(page.getRequest().getMark());
        Assert.assertEquals(indexEntry6, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData6, page.getData().get(0).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(indexEntry5, Navigation.NEXT, 1);
        page = this.index.readEndsWith("xy", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(1, page.getData().size());
        Assert.assertEquals(indexEntry5, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData5, page.getRequest().getMark().getValue());
        Assert.assertEquals(indexEntry4, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData4, page.getData().get(0).getValue());

        pageRequest = new MarkPageRequest<IndexEntryGroupedByDate>(indexEntry3, Navigation.PREVIOUS, 2);
        page = this.index.readEndsWith("xy", pageRequest, indexEntryFactory, context);
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(indexEntry3, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData3, page.getRequest().getMark().getValue());
        Assert.assertEquals(indexEntry5, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData5, page.getData().get(0).getValue());
        Assert.assertEquals(indexEntry4, page.getData().get(1).getName().getValue());
        Assert.assertEquals(denormilizedData4, page.getData().get(1).getValue());

        indexEntries = new ArrayList<IndexEntryGroupedByDate>();
        indexEntries.add(indexEntry5);
        indexEntries.add(indexEntry4);
        result = this.index.readEndsWith("xy", indexEntries, indexEntryFactory, context);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(indexEntry5, result.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData5, result.get(0).getValue());
        Assert.assertEquals(indexEntry4, result.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData4, result.get(1).getValue());
    }

    private static class IndexEntryGroupedByDate implements Serializable, Comparable<IndexEntryGroupedByDate>,
            StringIndexEntry<Long> {
        private static final long serialVersionUID = 1L;

        // Note the id must be the last ordinal and the grouping and/or sorting the first ones.
        private Date groupBy;
        private String indexedValue;
        private Long rowKey;

        public IndexEntryGroupedByDate(Date groupBy, String indexedValue, Long rowKey) {
            this.groupBy = groupBy;
            this.indexedValue = indexedValue;
            this.rowKey = rowKey;
        }

        public Date getGroupBy() {
            return this.groupBy;
        }

        @Override
        public String getIndexedStringValue() {
            return this.indexedValue;
        }

        @Override
        public Long getRowKey() {
            return this.rowKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.rowKey.hashCode();
            result = prime * result + this.indexedValue.hashCode();
            result = prime * result + this.groupBy.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            IndexEntryGroupedByDate other = (IndexEntryGroupedByDate) obj;

            if (!this.rowKey.equals(other.rowKey)) {
                return false;
            }

            if (!this.indexedValue.equals(other.indexedValue)) {
                return false;
            }

            if (!this.groupBy.equals(other.groupBy)) {
                return false;
            }

            return true;
        }

        @Override
        public int compareTo(IndexEntryGroupedByDate other) {
            int comparison = this.groupBy.compareTo(other.groupBy);

            if (comparison == 0) {
                comparison = this.indexedValue.compareTo(other.indexedValue);
            }

            // Note last comparison must be the id.
            if (comparison == 0) {
                comparison = this.rowKey.compareTo(other.rowKey);
            }

            return comparison;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(this, Property.valueOf("rowKey", this.rowKey),
                    Property.valueOf("indexedValue", this.indexedValue), Property.valueOf("groupBy", this.groupBy));
        }
    }

    private static class SortedByDateSerializer implements CompositeTypeSerializer<IndexEntryGroupedByDate> {

        @Override
        public List<Component<IndexEntryGroupedByDate, ?>> serialize(IndexEntryGroupedByDate compositeValue) {
            // Note the id must be the last ordinal and the grouping and/or sorting the first ones.
            List<Component<IndexEntryGroupedByDate, ?>> components = new ArrayList<Component<IndexEntryGroupedByDate, ?>>();
            components.add(new Component<IndexEntryGroupedByDate, Date>(BasicType.DATE, compositeValue.getGroupBy()));
            components.add(new Component<IndexEntryGroupedByDate, String>(BasicType.STRING_UTF8, compositeValue
                    .getIndexedStringValue()));
            components.add(new Component<IndexEntryGroupedByDate, Long>(BasicType.LONG, compositeValue.getRowKey()));
            return components;
        }

        @Override
        public IndexEntryGroupedByDate deserialize(List<Component<IndexEntryGroupedByDate, ?>> components) {
            Date date = (Date) components.get(0).getValue();
            String indexedStringValue = (String) components.get(1).getValue();
            Long id = (Long) components.get(2).getValue();
            return new IndexEntryGroupedByDate(date, indexedStringValue, id);
        }
    }
}
