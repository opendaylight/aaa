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
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class AllRowsSecondaryIndexTest {

    private AllRowsSecondaryIndex<Long, String> index;
    private AllRowsSecondaryIndex<SortedByDate, String> indexSortedByDate;

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

        CompositeTypeSerializer<SortedByDate> sortedByDateSerializer = new SortedByDateSerializer();
        CompositeType<SortedByDate> sortedByDateType = new CompositeType<SortedByDate>(sortedByDateSerializer,
                BasicType.DATE, BasicType.LONG);

        this.index = new AllRowsSecondaryIndex<Long, String>("cf_test_all_rows_secondary_index", BasicType.LONG,
                BasicType.STRING_UTF8);

        this.indexSortedByDate = new AllRowsSecondaryIndex<SortedByDate, String>(
                "cf_test_all_rows_sorted_secondary_index", sortedByDateType, BasicType.STRING_UTF8);

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                for (ColumnFamily<?, ?> definition : AllRowsSecondaryIndexTest.this.index.getColumnFamilies()) {
                    context.getCassandraClient().createColumnFamily(definition, context.getKeyspace(), context);
                }
                for (ColumnFamily<?, ?> definition : AllRowsSecondaryIndexTest.this.indexSortedByDate
                        .getColumnFamilies()) {
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
                for (ColumnFamily<?, ?> columnFamily : AllRowsSecondaryIndexTest.this.index.getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(columnFamily, context.getKeyspace(), context);
                }
                for (ColumnFamily<?, ?> definition : AllRowsSecondaryIndexTest.this.indexSortedByDate
                        .getColumnFamilies()) {
                    context.getCassandraClient().dropColumnFamily(definition, context.getKeyspace(), context);
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

    @Test
    public void testIndexSortedByDate() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testIndexSortedByDate(context);
                return null;
            }
        });
    }

    protected void testIndex(CassandraContext<Astyanax> context) throws PersistenceException {
        // Data sets definitions
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
        Assert.assertEquals(0, this.index.count(context));

        // Test update after insert
        this.index.insert(id1, null, context);
        Assert.assertEquals(1, this.index.count(context));

        this.index.insert(id1, null, context);
        Assert.assertEquals(1, this.index.count(context));

        // Test update after delete
        this.index.delete(id1, context);
        Assert.assertEquals(0, this.index.count(context));

        // Test update after delete all
        this.index.insert(id1, null, context);
        this.index.insert(id2, null, context);
        Assert.assertEquals(2, this.index.count(context));

        this.index.clear(context);
        Assert.assertEquals(0, this.index.count(context));

        // Test read
        this.index.insert(id1, denormilizedData1, context);
        this.index.insert(id2, denormilizedData2, context);
        this.index.insert(id3, denormilizedData3, context);
        this.index.insert(id4, denormilizedData4, context);
        this.index.insert(id5, denormilizedData5, context);

        List<Column<Long, String>> indexes = this.index.read(context);
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
        indexes = this.index.read(Arrays.asList(id1, id5, Long.valueOf(100)), context);
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
        MarkPage<Column<Long, String>> page = this.index.read(pageRequest, context);
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(id2, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData2, page.getRequest().getMark().getValue());
        Assert.assertEquals(id3, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData3, page.getData().get(0).getValue());
        Assert.assertEquals(id4, page.getData().get(1).getName().getValue());
        Assert.assertEquals(denormilizedData4, page.getData().get(1).getValue());
    }

    protected void testIndexSortedByDate(CassandraContext<Astyanax> context) throws PersistenceException {
        // Data sets definitions
        Long id1 = Long.valueOf(1);
        Long id2 = Long.valueOf(2);
        Long id3 = Long.valueOf(3);
        Long id4 = Long.valueOf(4);
        Long id5 = Long.valueOf(5);

        Date date1 = Date.valueOf(5);
        Date date2 = Date.valueOf(4);
        Date date3 = Date.valueOf(3);
        Date date4 = Date.valueOf(2);
        Date date5 = Date.valueOf(1);

        SortedByDate sortedByDate1 = new SortedByDate(id1, date1);
        SortedByDate sortedByDate2 = new SortedByDate(id2, date2);
        SortedByDate sortedByDate3 = new SortedByDate(id3, date3);
        SortedByDate sortedByDate4 = new SortedByDate(id4, date4);
        SortedByDate sortedByDate5 = new SortedByDate(id5, date5);

        String denormilizedData1 = "denormilized data for id 1";
        String denormilizedData2 = "denormilized data for id 2";
        String denormilizedData3 = "denormilized data for id 3";
        String denormilizedData4 = "denormilized data for id 4";
        String denormilizedData5 = "denormilized data for id 5";

        // Test initial state
        Assert.assertEquals(0, this.indexSortedByDate.count(context));

        // Test update after insert
        this.indexSortedByDate.insert(sortedByDate1, null, context);
        Assert.assertEquals(1, this.indexSortedByDate.count(context));

        this.indexSortedByDate.insert(sortedByDate1, null, context);
        Assert.assertEquals(1, this.indexSortedByDate.count(context));

        // Test update after delete
        this.indexSortedByDate.delete(sortedByDate1, context);
        Assert.assertEquals(0, this.indexSortedByDate.count(context));

        // Test update after delete all
        this.indexSortedByDate.insert(sortedByDate1, null, context);
        this.indexSortedByDate.insert(sortedByDate2, null, context);
        Assert.assertEquals(2, this.indexSortedByDate.count(context));

        this.indexSortedByDate.clear(context);
        Assert.assertEquals(0, this.indexSortedByDate.count(context));

        // Test read
        this.indexSortedByDate.insert(sortedByDate1, denormilizedData1, context);
        this.indexSortedByDate.insert(sortedByDate2, denormilizedData2, context);
        this.indexSortedByDate.insert(sortedByDate3, denormilizedData3, context);
        this.indexSortedByDate.insert(sortedByDate4, denormilizedData4, context);
        this.indexSortedByDate.insert(sortedByDate5, denormilizedData5, context);

        List<Column<SortedByDate, String>> indexes = this.indexSortedByDate.read(context);
        Assert.assertEquals(5, indexes.size());
        Assert.assertEquals(sortedByDate5, indexes.get(0).getName().getValue());
        Assert.assertEquals(denormilizedData5, indexes.get(0).getValue());
        Assert.assertEquals(sortedByDate4, indexes.get(1).getName().getValue());
        Assert.assertEquals(denormilizedData4, indexes.get(1).getValue());
        Assert.assertEquals(sortedByDate3, indexes.get(2).getName().getValue());
        Assert.assertEquals(denormilizedData3, indexes.get(2).getValue());
        Assert.assertEquals(sortedByDate2, indexes.get(3).getName().getValue());
        Assert.assertEquals(denormilizedData2, indexes.get(3).getValue());
        Assert.assertEquals(sortedByDate1, indexes.get(4).getName().getValue());
        Assert.assertEquals(denormilizedData1, indexes.get(4).getValue());

        // Test read columns. Columns are still returned in order.
        indexes = this.indexSortedByDate.read(
                Arrays.asList(sortedByDate1, sortedByDate5, new SortedByDate(Long.valueOf(100), date5)), context);
        Assert.assertEquals(2, indexes.size());
        Assert.assertEquals(id5, indexes.get(0).getName().getValue().getRowKey());
        Assert.assertEquals(denormilizedData5, indexes.get(0).getValue());
        Assert.assertEquals(id1, indexes.get(1).getName().getValue().getRowKey());
        Assert.assertEquals(denormilizedData1, indexes.get(1).getValue());

        // Test paged read

        // No need to do extensive paging testing because AstyanaxFacade is used to read the column
        // page,
        // and extensive paging testing is performed in AstyanaxFacadeTest.

        MarkPageRequest<SortedByDate> pageRequest = new MarkPageRequest<SortedByDate>(sortedByDate3, Navigation.NEXT, 2);
        MarkPage<Column<SortedByDate, String>> page = this.indexSortedByDate.read(pageRequest, context);
        Assert.assertEquals(2, page.getData().size());
        Assert.assertEquals(sortedByDate3, page.getRequest().getMark().getName().getValue());
        Assert.assertEquals(denormilizedData3, page.getRequest().getMark().getValue());
        Assert.assertEquals(sortedByDate2, page.getData().get(0).getName().getValue());
        Assert.assertEquals(denormilizedData2, page.getData().get(0).getValue());
        Assert.assertEquals(sortedByDate1, page.getData().get(1).getName().getValue());
        Assert.assertEquals(denormilizedData1, page.getData().get(1).getValue());
    }

    private static class SortedByDate implements Serializable, Comparable<SortedByDate> {
        private static final long serialVersionUID = 1L;

        private Date date;
        private Long rowKey;

        public SortedByDate(Long rowKey, Date date) {
            this.rowKey = rowKey;
            this.date = date;
        }

        public Long getRowKey() {
            return this.rowKey;
        }

        public Date getDate() {
            return this.date;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.rowKey.hashCode();
            result = prime * result + this.date.hashCode();
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

            SortedByDate other = (SortedByDate) obj;

            if (!this.rowKey.equals(other.rowKey)) {
                return false;
            }

            if (!this.date.equals(other.date)) {
                return false;
            }

            return true;
        }

        @Override
        public int compareTo(SortedByDate other) {
            int comparison = this.date.compareTo(other.date);

            // Note last comparison must be the id.
            if (comparison == 0) {
                comparison = this.rowKey.compareTo(other.rowKey);
            }

            return comparison;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(this, Property.valueOf("rowKey", this.rowKey),
                    Property.valueOf("date", this.date));
        }
    }

    private static class SortedByDateSerializer implements CompositeTypeSerializer<SortedByDate> {

        @Override
        public List<Component<SortedByDate, ?>> serialize(SortedByDate compositeValue) {
            // Note the id must be the last ordinal
            List<Component<SortedByDate, ?>> components = new ArrayList<Component<SortedByDate, ?>>();
            components.add(new Component<SortedByDate, Date>(BasicType.DATE, compositeValue.getDate()));
            components.add(new Component<SortedByDate, Long>(BasicType.LONG, compositeValue.getRowKey()));
            return components;
        }

        @Override
        public SortedByDate deserialize(List<Component<SortedByDate, ?>> components) {
            Date date = (Date) components.get(0).getValue();
            Long id = (Long) components.get(1).getValue();
            return new SortedByDate(id, date);
        }
    }
}
