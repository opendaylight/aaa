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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.VoidColumn;
import com.hp.util.model.persistence.cassandra.index.SecondaryIndexIntegrator.SecondaryIndexReader;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class SecondaryIndexIntegratorTest {

    private static final int DEFAULT_PAGE_SIZE = 10000; // Page size internally used by SecondaryuIndexIntegrator

    @Test
    @SuppressWarnings("boxing")
    public void testIntersectEmptyWithZeroQuerySize() {
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        EasyMock.expect(indexReader1.count()).andReturn(Long.valueOf(10));
        EasyMock.expect(indexReader2.count()).andReturn(Long.valueOf(0));

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> intersection = SecondaryIndexIntegrator.intersect(indexes);
        Assert.assertTrue(intersection.isEmpty());

        EasyMock.verify(indexReader1, indexReader2);
    }

    @Test
    @SuppressWarnings("boxing")
    public void testIntersect() {
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        List<Column<Long, ?>> reader1Data = new ArrayList<Column<Long, ?>>();
        List<ColumnName<Long, ?>> reader1DataColumnNames = new ArrayList<ColumnName<Long, ?>>();
        List<Column<Long, ?>> reader2Intersection = new ArrayList<Column<Long, ?>>();

        for (int i = 0; i < 10; i++) {
            ColumnName<Long, Void> columnName = ColumnName.valueOf(Long.valueOf(i));
            reader1Data.add(new VoidColumn<Long>(columnName));
            reader1DataColumnNames.add(columnName);
        }

        reader2Intersection.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(3))));
        reader2Intersection.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(5))));
        reader2Intersection.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(9))));

        EasyMock.expect(indexReader1.count()).andReturn(Long.valueOf(reader1Data.size()));
        EasyMock.expect(indexReader2.count()).andReturn(Long.valueOf(10000));
        EasyMock.expect(indexReader1.read()).andReturn(reader1Data);
        EasyMock.expect(indexReader2.read(EasyMock.eq(reader1DataColumnNames))).andReturn(reader2Intersection);

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> intersection = SecondaryIndexIntegrator.intersect(indexes);
        Assert.assertEquals(3, intersection.size());
        Assert.assertEquals(ColumnName.valueOf(Long.valueOf(3)), intersection.get(0));
        Assert.assertEquals(ColumnName.valueOf(Long.valueOf(5)), intersection.get(1));
        Assert.assertEquals(ColumnName.valueOf(Long.valueOf(9)), intersection.get(2));

        EasyMock.verify(indexReader1, indexReader2);
    }

    /*

     This tests assume the alternative implementation of intersection

    public void testIntersectConsecutiveColumns() {
        SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        List<Column<Long, ?>> reader1PageData = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Data = new ArrayList<Column<Long, ?>>();

        for (int i = 0; i < 1000; i++) {
            reader1PageData.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i + 10))));
        }

        for (int i = 0; i < 3000; i++) {
            reader2Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i))));
        }

        ColumnName<Long, ?> start = reader2Data.get(0).getName();
        ColumnName<Long, ?> end = reader2Data.get(reader2Data.size() - 1).getName();

        MarkPageRequest<ColumnName<Long, ?>> pageRequest1 = new MarkPageRequest<ColumnName<Long, ?>>(start,
            Navigation.NEXT, DEFAULT_PAGE_SIZE);
        MarkPageRequest<ColumnName<Long, ?>> pageRequest2 = new MarkPageRequest<ColumnName<Long, ?>>(
            reader1PageData.get(reader1PageData.size() - 1).getName(), Navigation.NEXT, DEFAULT_PAGE_SIZE);

        MarkPage<Column<Long, ?>> reader1Page1 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            DEFAULT_PAGE_SIZE), reader1PageData);

        MarkPage<Column<Long, ?>> reader1Page2 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            reader1PageData.get(reader1PageData.size() - 1), Navigation.NEXT, DEFAULT_PAGE_SIZE),
            Collections.<Column<Long, ?>> emptyList());

        EasyMock.expect(indexReader1.count()).andReturn(Long.valueOf(reader1PageData.size() + 5000));
        EasyMock.expect(indexReader2.count()).andReturn(Long.valueOf(reader2Data.size()));
        EasyMock.expect(indexReader2.read()).andReturn(reader2Data);
        EasyMock.expect(indexReader1.read(EasyMock.eq(pageRequest1), EasyMock.eq(end))).andReturn(reader1Page1);
        EasyMock.expect(indexReader1.read(EasyMock.eq(pageRequest2), EasyMock.eq(end))).andReturn(reader1Page2);

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> intersection = SecondaryIndexIntegrator.intersect(indexes);
        Assert.assertEquals(1000, intersection.size());
        for (int i = 0; i < intersection.size(); i++) {
            Assert.assertEquals(ColumnName.valueOf(Long.valueOf(i + 10)), intersection.get(i));
        }

        EasyMock.verify(indexReader1, indexReader2);
    }

    @Test
    @SuppressWarnings("boxing")
    public void testIntersectNonConsecutiveColumns() {
        SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        List<Column<Long, ?>> reader1Data = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Page1Data = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Page2Data = new ArrayList<Column<Long, ?>>();

        for (int i = 0; i < 5; i++) {
            reader1Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i
                * DEFAULT_PAGE_SIZE))));
        }

        for (int i = 0; i < DEFAULT_PAGE_SIZE; i++) {
            reader2Page1Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i))));
            reader2Page2Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i
                + DEFAULT_PAGE_SIZE))));
        }

        ColumnName<Long, ?> start = reader1Data.get(0).getName();
        ColumnName<Long, ?> end = reader1Data.get(reader1Data.size() - 1).getName();

        MarkPageRequest<ColumnName<Long, ?>> pageRequest1 = new MarkPageRequest<ColumnName<Long, ?>>(start,
            Navigation.NEXT, DEFAULT_PAGE_SIZE);
        MarkPageRequest<ColumnName<Long, ?>> pageRequest2 = new MarkPageRequest<ColumnName<Long, ?>>(
            reader2Page1Data.get(reader2Page1Data.size() - 1).getName(), Navigation.NEXT, DEFAULT_PAGE_SIZE);
        MarkPageRequest<ColumnName<Long, ?>> pageRequest3 = new MarkPageRequest<ColumnName<Long, ?>>(
            reader2Page2Data.get(reader2Page2Data.size() - 1).getName(), Navigation.NEXT, DEFAULT_PAGE_SIZE);

        MarkPage<Column<Long, ?>> reader2Page1 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            DEFAULT_PAGE_SIZE), reader2Page1Data);

        MarkPage<Column<Long, ?>> reader2Page2 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            reader2Page1Data.get(reader2Page1Data.size() - 1), Navigation.NEXT, DEFAULT_PAGE_SIZE), reader2Page2Data);

        MarkPage<Column<Long, ?>> reader2Page3 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            reader2Page2Data.get(reader2Page2Data.size() - 1), Navigation.NEXT, DEFAULT_PAGE_SIZE),
            Collections.<Column<Long, ?>> emptyList());

        EasyMock.expect(indexReader1.count()).andReturn(Long.valueOf(reader1Data.size()));
        EasyMock.expect(indexReader2.count()).andReturn(Long.valueOf(reader2Page1Data.size() + reader2Page2Data.size()));
        EasyMock.expect(indexReader1.read()).andReturn(reader1Data);
        EasyMock.expect(indexReader2.read(EasyMock.eq(pageRequest1), EasyMock.eq(end))).andReturn(reader2Page1);
        EasyMock.expect(indexReader2.read(EasyMock.eq(pageRequest2), EasyMock.eq(end))).andReturn(reader2Page2);
        EasyMock.expect(indexReader2.read(EasyMock.eq(pageRequest3), EasyMock.eq(end))).andReturn(reader2Page3);

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> intersection = SecondaryIndexIntegrator.intersect(indexes);
        Assert.assertEquals(2, intersection.size());
        for (int i = 0; i < intersection.size(); i++) {
            Assert.assertEquals(ColumnName.valueOf(Long.valueOf(i * DEFAULT_PAGE_SIZE)), intersection.get(i));
        }

        EasyMock.verify(indexReader1, indexReader2);
    }

    @Test
    @SuppressWarnings("boxing")
    public void testIntersectEmpty() {
        @SuppressWarnings("unchecked") SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        @SuppressWarnings("unchecked") SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        List<Column<Long, ?>> reader1Data = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Page1Data = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Page2Data = new ArrayList<Column<Long, ?>>();

        reader1Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(0))));
        reader1Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(DEFAULT_PAGE_SIZE * 2))));

        for (int i = 0; i < DEFAULT_PAGE_SIZE; i++) {
            reader2Page1Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i))));
            reader2Page2Data.add(new ValuelessColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i
                + DEFAULT_PAGE_SIZE))));
        }

        reader2Page1Data.remove(0);
        reader2Page1Data.add(reader2Page2Data.remove(0));

        ColumnName<Long, ?> start = reader1Data.get(0).getName();
        ColumnName<Long, ?> end = reader1Data.get(reader1Data.size() - 1).getName();

        MarkPageRequest<ColumnName<Long, ?>> pageRequest1 = new MarkPageRequest<ColumnName<Long, ?>>(start,
            Navigation.NEXT, DEFAULT_PAGE_SIZE);
        MarkPageRequest<ColumnName<Long, ?>> pageRequest2 = new MarkPageRequest<ColumnName<Long, ?>>(
            reader2Page1Data.get(reader2Page1Data.size() - 1).getName(), Navigation.NEXT, DEFAULT_PAGE_SIZE);
        MarkPageRequest<ColumnName<Long, ?>> pageRequest3 = new MarkPageRequest<ColumnName<Long, ?>>(
            reader2Page2Data.get(reader2Page2Data.size() - 1).getName(), Navigation.NEXT, DEFAULT_PAGE_SIZE);

        MarkPage<Column<Long, ?>> reader2Page1 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            DEFAULT_PAGE_SIZE), reader2Page1Data);

        MarkPage<Column<Long, ?>> reader2Page2 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            reader2Page1Data.get(reader2Page1Data.size() - 1), Navigation.NEXT, DEFAULT_PAGE_SIZE), reader2Page2Data);

        MarkPage<Column<Long, ?>> reader2Page3 = new MarkPage<Column<Long, ?>>(new MarkPageRequest<Column<Long, ?>>(
            reader2Page2Data.get(reader2Page2Data.size() - 1), Navigation.NEXT, DEFAULT_PAGE_SIZE),
            Collections.<Column<Long, ?>> emptyList());

        EasyMock.expect(indexReader1.count()).andReturn(Long.valueOf(reader1Data.size()));
        EasyMock.expect(indexReader2.count()).andReturn(Long.valueOf(reader2Page1Data.size() + reader2Page2Data.size()));
        EasyMock.expect(indexReader1.read()).andReturn(reader1Data);
        EasyMock.expect(indexReader2.read(EasyMock.eq(pageRequest1), EasyMock.eq(end))).andReturn(reader2Page1);
        EasyMock.expect(indexReader2.read(EasyMock.eq(pageRequest2), EasyMock.eq(end))).andReturn(reader2Page2);
        EasyMock.expect(indexReader2.read(EasyMock.eq(pageRequest3), EasyMock.eq(end))).andReturn(reader2Page3);

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> intersection = SecondaryIndexIntegrator.intersect(indexes);
        Assert.assertTrue(intersection.isEmpty());

        EasyMock.verify(indexReader1, indexReader2);
    }
    */

    @Test
    public void testMerge() {
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        List<Column<Long, ?>> reader1Data = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Data = new ArrayList<Column<Long, ?>>();

        reader1Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(1))));
        reader1Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(3))));
        reader1Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(5))));

        reader2Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(1)))); // duplicated
        reader2Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(2))));
        reader2Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(4))));

        EasyMock.expect(indexReader1.read()).andReturn(reader1Data);
        EasyMock.expect(indexReader2.read()).andReturn(reader2Data);

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> union = SecondaryIndexIntegrator.merge(indexes);
        Assert.assertEquals(5, union.size());
        for (int i = 0; i < union.size(); i++) {
            Assert.assertEquals(ColumnName.valueOf(Long.valueOf(i + 1)), union.get(i));
        }

        EasyMock.verify(indexReader1, indexReader2);
    }

    @Test
    @SuppressWarnings("boxing")
    public void testIntersectPagedEmptyWithZeroQuerySize() {
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader1 = EasyMock.createMock(SecondaryIndexReader.class);
        @SuppressWarnings("unchecked")
        SecondaryIndexReader<Long> indexReader2 = EasyMock.createMock(SecondaryIndexReader.class);

        EasyMock.expect(indexReader1.count()).andReturn(Long.valueOf(10));
        EasyMock.expect(indexReader2.count()).andReturn(Long.valueOf(0));

        EasyMock.replay(indexReader1, indexReader2);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        MarkPage<ColumnName<Long, ?>> intersection = SecondaryIndexIntegrator.intersect(indexes,
            new MarkPageRequest<ColumnName<Long, ?>>(10));
        Assert.assertTrue(intersection.isEmpty());

        EasyMock.verify(indexReader1, indexReader2);
    }

    @Test
    public void testIntersectPaged() {
        List<Column<Long, ?>> reader1Data = new ArrayList<Column<Long, ?>>();
        List<Column<Long, ?>> reader2Data = new ArrayList<Column<Long, ?>>();

        for (int i = 0; i < 4; i++) {
            reader1Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i
                * DEFAULT_PAGE_SIZE))));
        }

        for (int i = 0; i < DEFAULT_PAGE_SIZE * reader1Data.size(); i++) {
            reader2Data.add(new VoidColumn<Long>(ColumnName.<Long, Void> valueOf(Long.valueOf(i))));
        }

        SecondaryIndexReader<Long> indexReader1 = new SecondaryIndexReaderImpl<Long>(reader1Data);
        SecondaryIndexReader<Long> indexReader2 = new SecondaryIndexReaderImpl<Long>(reader2Data);

        Collection<SecondaryIndexReader<Long>> indexes = new ArrayList<SecondaryIndexReader<Long>>();
        indexes.add(indexReader1);
        indexes.add(indexReader2);
        List<ColumnName<Long, ?>> expectedCompleteIntersection = new ArrayList<ColumnName<Long, ?>>();
        for (Column<Long, ?> column : reader1Data) {
            expectedCompleteIntersection.add(column.getName());
        }

        testPagedIntersection(indexes, expectedCompleteIntersection);
    }

    protected <C extends Serializable & Comparable<C>> void testPagedIntersection(
        Collection<SecondaryIndexReader<C>> indexes, List<ColumnName<C, ?>> expectedCompleteIntersection) {
        int totalColumns = expectedCompleteIntersection.size();

        // Next Page

        for (int size = 1; size <= totalColumns; size++) {
            int totalPages = totalColumns / size;

            // handle extra non-full page at the end
            if (totalPages * size < totalColumns) {
                totalPages = totalPages + 1;
            }

            // Search result will contain the aggregated records from all pages to compare at the end
            List<ColumnName<C, ?>> aggregatedResult = new ArrayList<ColumnName<C, ?>>(totalColumns);

            MarkPage<ColumnName<C, ?>> page;
            MarkPageRequest<ColumnName<C, ?>> pageRequest = new MarkPageRequest<ColumnName<C, ?>>(size);

            do {
                page = SecondaryIndexIntegrator.intersect(indexes, pageRequest);
                aggregatedResult.addAll(page.getData());
                pageRequest = page.getNextPageRequest();
            }
            while (!page.getData().isEmpty());

            Assert.assertEquals(expectedCompleteIntersection, aggregatedResult);
        }

        // Previous Page

        for (int size = 1; size <= totalColumns; size++) {
            int totalPages = totalColumns / size;

            // handle extra non-full page at the end
            if (totalPages * size < totalColumns) {
                totalPages = totalPages + 1;
            }

            // Search result will contain the aggregated records from all pages to compare at the end
            List<ColumnName<C, ?>> aggregatedResult = new ArrayList<ColumnName<C, ?>>(totalColumns);

            MarkPage<ColumnName<C, ?>> page;
            ColumnName<C, ?> mark = expectedCompleteIntersection.get(totalColumns - 1);
            MarkPageRequest<ColumnName<C, ?>> pageRequest = new MarkPageRequest<ColumnName<C, ?>>(mark,
                Navigation.PREVIOUS, size);

            // The mark is not included in the page
            aggregatedResult.add(mark);

            do {
                page = SecondaryIndexIntegrator.intersect(indexes, pageRequest);
                aggregatedResult.addAll(page.getData());
                pageRequest = page.getPreviousPageRequest();
            }
            while (!page.getData().isEmpty());

            Assert.assertEquals(expectedCompleteIntersection.size(), aggregatedResult.size());
            Assert.assertTrue(expectedCompleteIntersection.containsAll(aggregatedResult));
            Assert.assertTrue(aggregatedResult.containsAll(expectedCompleteIntersection));
        }
    }

    private static class SecondaryIndexReaderImpl<C extends Serializable & Comparable<C>> implements
        SecondaryIndexReader<C> {
        private List<Column<C, ?>> data;

        protected SecondaryIndexReaderImpl(List<Column<C, ?>> data) {
            this.data = Collections.unmodifiableList(new LinkedList<Column<C, ?>>(data));
        }

        @Override
        public long count() {
            return this.data.size();
        }

        @Override
        public List<Column<C, ?>> read() {
            return this.data;
        }

        @Override
        public MarkPage<Column<C, ?>> read(MarkPageRequest<ColumnName<C, ?>> pageRequest, ColumnName<C, ?> end) {

            Column<C, ?> mark = null;
            if (pageRequest.getMark() != null) {
                for (Column<C, ?> column : this.data) {
                    if (column.getName().equals(pageRequest.getMark())) {
                        mark = column;
                    }
                }

                if (mark == null) {
                    throw new IllegalArgumentException("This implementation assumes the mark exists if it is not null");
                }
            }

            int markPos = mark != null ? this.data.indexOf(mark) : -1;

            List<Column<C, ?>> pageData = new LinkedList<Column<C, ?>>();

            if (pageRequest.getNavigation() == Navigation.NEXT) {
                for (int i = markPos + 1; i < this.data.size() && pageData.size() < pageRequest.getSize(); i++) {
                    Column<C, ?> column = this.data.get(i);
                    if (end != null) {
                        if (column.getName().compareTo(end) > 0) {
                            break;
                        }
                    }
                    pageData.add(column);
                }
            }
            else {
                for (int i = markPos - 1; i >= 0 && pageData.size() < pageRequest.getSize(); i--) {
                    Column<C, ?> column = this.data.get(i);
                    if (end != null) {
                        if (column.getName().compareTo(end) < 0) {
                            break;
                        }
                    }
                    pageData.add(column);
                }
                Collections.reverse(pageData);
            }

            return new MarkPage<Column<C, ?>>(pageRequest.<Column<C, ?>> convert(mark), pageData);
        }

        @Override
        public List<Column<C, ?>> read(List<ColumnName<C, ?>> indexEntries) {
            List<Column<C, ?>> result = new ArrayList<Column<C, ?>>();

            for (Column<C, ?> column : this.data) {
                if (indexEntries.contains(column.getName())) {
                    result.add(column);
                }
            }

            return result;
        }
    }
}
