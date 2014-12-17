/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;

/**
 * Workaround to support filter combinations in Cassandra similar to the relational model.
 * For example: Assume we have a {@code Person} entity with the following attributes: {@code name},
 * {@code lastName}, {@code birthdate}, {@code gender} and {@code status}.
 * <p>
 * In a relational system it would be relatively simple to create queries involving any combination
 * of filters applied to the {@code Person}'s attributes:
 * <p>
 * {@code Select * from Person Where name='SomeName' AND lastName='someLastName'}
 * <p>
 * {@code Select * from Person Where birthdate < 'someDate' AND gender='FEMALE' AND status in ['SINGLE', 'DIVORCED']}
 * <p>
 * In Cassandra this is not possible because one column family for each expected type of query has to
 * be created. Thus, in order to support any combination of the {@code Person}'s attributes at least
 * 32 column families would have to be created (Using natural order, involving sorting increases the
 * number of column families).
 * <p>
 * In general the number of column families needed to support any combination of filtering and sorting is
 * given by:
 * <p>
 * <code>column families = 2<sup>f</sup> * s!</code>
 * <p>
 * where
 * <ul>
 * <li>{@code f} is the number of filter-able attributes</li>.
 * <li>{@code s} is the number of sort-able attributes combinations.</li>.
 * </ul>
 * <p>
 * Using {@code SecondaryIndexIntegrator} the number of column families is given by:
 * <p>
 * <code>column families = (f * s!) + w</code>
 * <p>
 * where
 * <ul>
 * <li>
 * {@code f} is the number of filter-able attributes</li>. One column family for each attribute for
 * each sorting combination is created. Note that this column families are already optimized to satisfy
 * queries involving single attributes.
 * </li>
 * <li>
 * {@code s} is the number of sort-able attributes. Note that if not all sorting combinations are needed
 * then this number would be smaller.
 * </li>
 * <li>{@code w} is the number of optimized well-known queries that involve combination of attributes.</li>
 * </ul>
 * <p>
 * <b>Some key points about {@code SecondaryIndexIntegrator}</b>
 * <ul>
 * <li>
 * A column family (native secondary index or custom secondary index) should be created for each
 * well-known query to optimize for such query. This should be the default data modeling approach.
 * </li>
 * <li>
 * In order to support filter combinations, one column family (secondary index) should be created for
 * each attribute and then use {@code SecondaryIndexIntegrator} to combine results.
 * </li>
 * <li>
 * Sorting is supported by {@code SecondaryIndexIntegrator}. All integrated indexes are forced to have
 * the same column name, thus sorting is part of the integration: It isn't possible to integrate indexes
 * using different sorting criteria.
 * </li>
 * <li>
 * {@code SecondaryIndexIntegrator} is a workaround and should be used just to support legacy code.
 * If you have to support any combination of queries then you have to ask yourself if Cassandra is the
 * right technology to use. In Cassandra is expected to satisfy a query with a single read (Reading a
 * single row). {@code SecondaryIndexIntegrator} performs multiple reads to combine filters and thus
 * will access multiple nodes. Seconday index combination will be extremely low compared to an optimized
 * secondary index query.
 * </li>
 * </ul>
 * <p>
 *
 * <h5>Data Model Summary</h5>
 * <p>
 * In relational systems entities and its relationships are modeled and then indexes are created to
 * support whatever queries become necessary. In a relational database, data is stored in tables and
 * the tables comprising an application are typically related to each other. Data is usually normalized
 * to reduce redundant entries, and tables are joined on common keys to satisfy a given query.
 * <p>
 * Cassandra does not enforce relationships between column families the way relational databases do
 * between tables: there are no formal foreign keys in Cassandra, and joining column families at query
 * time is not supported.
 * <p>
 * With Cassandra it is primary to think about what queries the system needs to support efficiently
 * ahead of time, and model appropriately. Since there are no automatically-provided indexes, the
 * application will be much closer to one Column Family per query than it'd be with tables-queries
 * relationally. There shouldn't be concerns with this denormalization; Cassandra is much faster at
 * writes than relational systems, without giving up speed on reads.
 * <p>
 * In Cassandra, denormalization is the norm. A standard and very efficient way of working with the
 * Cassandra data model is to create one column family for each expected type of query. With this approach,
 * data is denormalized and structured so that one or multiple rows in a single column family are used to
 * answer each query.
 * <p>
 * Unlike the fully-relational model, where data is normalized for storage in the database and then joined
 * during queries, Cassandra is at its best when there is approximately one column family per expected type
 * of query. This sacrifices disk space (one of the cheapest resources for a server) in order to reduce the
 * number of disk seeks and the amount of network traffic.
 * <p>
 * The Cassandra data model is a dynamic schema, column-oriented data model. This means that, unlike a
 * relational database, there isn't need to model all of the columns required by the application up front, as
 * each row is not required to have the same set of columns. Columns and their metadata can be added by the
 * application as they are needed without incurring downtime to the application. Planning a data model in
 * Cassandra has different design considerations than one may be used to from relational databases. Ultimately,
 * the data model design depends on the data to capture and how such data is accessed. However, there are some
 * common design considerations for Cassandra data model planning.
 * 
 * @author Fabiel Zuniga
 */
public final class SecondaryIndexIntegrator {

    private SecondaryIndexIntegrator() {

    }

    /**
     * Combines the indexes using {@code AND} operation.
     * <p>
     * Denormalized data is not part of the result because different indexes might use different
     * denormalization.
     * 
     * @param indexes index readers to combine
     * @param <C> type of the column name in the secondary index column family (row key in the main
     *            column family or composite value when sorting information is included). Note how
     *            all indexes must have the same column name, thus sorting is part of the
     *            integration: It isn't possible to integrate indexes using different sorting
     *            criteria.
     * @return resultant integration of the given indexes
     */
    public static <C extends Serializable & Comparable<C>> List<ColumnName<C, ?>> intersect(
        Collection<SecondaryIndexReader<C>> indexes) {
        if (indexes == null) {
            throw new NullPointerException("indexes cannot be null");
        }

        // Calculates the smallest single query result
        // The biggest possible result will be the smallest individual query because the operation is intersection.

        long smallestQuerySize = 0;
        SecondaryIndexReader<C> baseIndex = null;

        for (SecondaryIndexReader<C> index : indexes) {
            if (baseIndex == null) {
                smallestQuerySize = index.count();
                baseIndex = index;
            }
            else {
                long currentQuerySize = index.count();
                if (currentQuerySize < smallestQuerySize) {
                    smallestQuerySize = currentQuerySize;
                    baseIndex = index;
                }
            }

            if (smallestQuerySize <= 0) {
                return Collections.emptyList();
            }
        }

        if (baseIndex == null) {
            return Collections.emptyList();
        }

        // Reads the smallest result

        // Linked list is used in case the result is big
        List<ColumnName<C, ?>> result = new LinkedList<ColumnName<C, ?>>();
        for (Column<C, ?> column : baseIndex.read()) {
            result.add(column.getName());
        }

        // Reads the remaining indexes and combines the results
        applyIntersection(indexes, baseIndex, result);

        return result;
    }

    /**
     * Combines the indexes using {@code AND} operation.
     * <p>
     * Denormalized data is not part of the result because different indexes might use different
     * denormalization.
     * 
     * @param indexes index readers to combine
     * @param pageRequest page request
     * @param <C> type of the column name in the secondary index column family (row key in the main
     *            column family or composite value when sorting information is included). Note how
     *            all indexes must have the same column name, thus sorting is part of the
     *            integration: It isn't possible to integrate indexes using different sorting
     *            criteria.
     * @return a page of the resultant integration of the given indexes
     */
    public static <C extends Serializable & Comparable<C>> MarkPage<ColumnName<C, ?>> intersect(
        Collection<SecondaryIndexReader<C>> indexes, MarkPageRequest<ColumnName<C, ?>> pageRequest) {
        if (indexes == null) {
            throw new NullPointerException("indexes cannot be null");
        }

        // Calculates the smallest single query result
        // The biggest possible result will be the smallest individual query because the operation is intersection.

        long smallestQuerySize = 0;
        SecondaryIndexReader<C> baseIndex = null;

        for (SecondaryIndexReader<C> index : indexes) {
            if (baseIndex == null) {
                smallestQuerySize = index.count();
                baseIndex = index;
            }
            else {
                long currentQuerySize = index.count();
                if (currentQuerySize < smallestQuerySize) {
                    smallestQuerySize = currentQuerySize;
                    baseIndex = index;
                }
            }

            if (smallestQuerySize <= 0) {
                return new MarkPage<ColumnName<C, ?>>(pageRequest, Collections.<ColumnName<C, ?>> emptyList());
            }
        }

        if (baseIndex == null) {
            return new MarkPage<ColumnName<C, ?>>(pageRequest, Collections.<ColumnName<C, ?>> emptyList());
        }

        // A page is read from the base query index, but since intersection will be performed with the other indexes,
        // the pageData might be decreasing in size. So we read more records and we'll keep reading until the expected
        // page size is reached or no more columns are available.

        // Linked list is used in case the result is big
        List<ColumnName<C, ?>> result = new LinkedList<ColumnName<C, ?>>();

        MarkPageRequest<ColumnName<C, ?>> baseIndexPageRequest = new MarkPageRequest<ColumnName<C, ?>>(
            pageRequest.getMark(), pageRequest.getNavigation(), pageRequest.getSize() * 2);
        MarkPage<Column<C, ?>> baseIndexPage = null;

        do {
            baseIndexPage = baseIndex.read(baseIndexPageRequest, null);

            for (Column<C, ?> column : baseIndexPage.getData()) {
                result.add(column.getName());
            }

            // Reads the remaining indexes and combines the results
            applyIntersection(indexes, baseIndex, result);

            // Updates baseIndexPageRequest in case another read is needed
            MarkPageRequest<Column<C, ?>> request = null;
            if (pageRequest.getNavigation() == Navigation.NEXT) {
                request = baseIndexPage.getNextPageRequest();
            }
            else {
                request = baseIndexPage.getPreviousPageRequest();
            }
            baseIndexPageRequest = null;
            if (request != null) {
                baseIndexPageRequest = request.<ColumnName<C, ?>> convert(request.getMark() != null
                    ? request.getMark().getName() : null);
            }
        }
        while (result.size() < pageRequest.getSize() && !baseIndexPage.getData().isEmpty());

        // Removes excedent
        if (pageRequest.getNavigation() == Navigation.NEXT) {
            for (int i = result.size() - 1; i >= pageRequest.getSize(); i--) {
                result.remove(i);
            }
        }
        else {
            int excedent = result.size() - pageRequest.getSize();
            for (int i = 0; i < excedent; i++) {
                result.remove(0);
            }
        }

        return new MarkPage<ColumnName<C, ?>>(pageRequest, result);
    }

    /**
     * Updates the result obtained from the base index applying intersection with the other indexes'
     * result.
     * 
     * @param indexes indexes
     * @param baseIndex index used as the base. This index will be ignored
     * @param baseIndexResult result obtained from {@code baseIndex}. This result will be updated.
     */
    private static <C extends Serializable & Comparable<C>> void applyIntersection(
        Collection<SecondaryIndexReader<C>> indexes, SecondaryIndexReader<C> baseIndex,
        List<ColumnName<C, ?>> baseIndexResult) {

        for (SecondaryIndexReader<C> index : indexes) {
            if (baseIndexResult.isEmpty()) {
                break;
            }

            if (index != baseIndex) {
                List<Column<C, ?>> indexResult = index.read(baseIndexResult);
                baseIndexResult.clear();
                for (Column<C, ?> column : indexResult) {
                    baseIndexResult.add(column.getName());
                }
            }
        }
    }

    /**
     * Updates the result obtained from the base index applying intersection with the other indexes'
     * result.
     * 
     * @param indexes indexes
     * @param baseIndex index used as the base. This index will be ignored
     * @param baseIndexResult result obtained from {@code baseIndex}. This result will be updated.
     */
    // This implementation is not used since it is less efficient
    @SuppressWarnings("unused")
    private static <C extends Serializable & Comparable<C>> void applyIntersectionUsingPaging(
        Collection<SecondaryIndexReader<C>> indexes, SecondaryIndexReader<C> baseIndex,
        List<ColumnName<C, ?>> baseIndexResult) {
        // Used to check whether an entry belongs to the result (Faster implementation of
        // contains(Object)).
        Set<ColumnName<C, ?>> baseIndexResultSet = new LinkedHashSet<ColumnName<C, ?>>(baseIndexResult);

        for (SecondaryIndexReader<C> index : indexes) {
            if (baseIndexResult.isEmpty()) {
                break;
            }

            if (index != baseIndex) {
                List<ColumnName<C, ?>> indexResult = new LinkedList<ColumnName<C, ?>>();

                ColumnName<C, ?> start = baseIndexResult.isEmpty() ? null : baseIndexResult.get(0);
                ColumnName<C, ?> end = baseIndexResult.isEmpty() ? null
                    : baseIndexResult.get(baseIndexResult.size() - 1);

                MarkPageRequest<ColumnName<C, ?>> indexPageRequest = new MarkPageRequest<ColumnName<C, ?>>(start,
                    Navigation.NEXT, 10000);
                MarkPage<Column<C, ?>> indexPage = null;
                boolean addMark = true;
                do {
                    indexPage = index.read(indexPageRequest, end);

                    if (indexPage == null) {
                        break;
                    }

                    if (addMark && indexPage.getRequest().getMark() != null) {
                        ColumnName<C, ?> value = indexPage.getRequest().getMark().getName();
                        if (baseIndexResultSet.contains(value)) {
                            indexResult.add(value);
                        }
                    }

                    for (Column<C, ?> column : indexPage.getData()) {
                        ColumnName<C, ?> value = column.getName();
                        if (baseIndexResultSet.contains(value)) {
                            indexResult.add(value);
                        }
                    }

                    MarkPageRequest<Column<C, ?>> nextPageRequest = indexPage.getNextPageRequest();
                    indexPageRequest = null;
                    if (nextPageRequest != null) {
                        indexPageRequest = nextPageRequest.<ColumnName<C, ?>> convert(nextPageRequest.getMark() != null
                            ? nextPageRequest.getMark().getName() : null);
                    }

                    addMark = false;
                }
                while (!indexPage.getData().isEmpty());

                baseIndexResult.retainAll(indexResult);
                baseIndexResultSet = new LinkedHashSet<ColumnName<C, ?>>(baseIndexResult);
            }
        }
    }

    /**
     * Combines the indexes using {@code OR} operation.
     * <p>
     * Denormalized data is not part of the result because different indexes might use different
     * denormalization.
     * 
     * @param indexes index readers to combine
     * @param <C> type of the column name in the secondary index column family (row key in the main
     *            column family or composite value when sorting information is included). Note how
     *            all indexes must have the same column name, thus sorting is part of the
     *            integration: It isn't possible to integrate indexes using different sorting
     *            criteria.
     * @return resultant integration of the given indexes
     */
    public static <C extends Serializable & Comparable<C>> List<ColumnName<C, ?>> merge(
        Collection<SecondaryIndexReader<C>> indexes) {
        if (indexes == null) {
            throw new NullPointerException("indexes cannot be null");
        }

        // Linked list and linked set are used in case the result is big

        // Set to avoid duplicates
        Set<ColumnName<C, ?>> merge = new LinkedHashSet<ColumnName<C, ?>>();
        for (SecondaryIndexReader<C> index : indexes) {
            for (Column<C, ?> column : index.read()) {
                merge.add(column.getName());
            }
        }

        List<ColumnName<C, ?>> result = new LinkedList<ColumnName<C, ?>>(merge);
        Collections.sort(result);

        return result;
    }

    /**
     * Secondary index reader. This reader encapsulates the result to be combined with other
     * indexes.
     * 
     * @param <C> type of the column name in the secondary index column family (row key in the main
     *            column family or composite value when sorting information is included)
     */
    public static interface SecondaryIndexReader<C extends Serializable & Comparable<C>> {

        /**
         * Returns the size of the query for the particular index.
         * 
         * @return the size of the query
         */
        public long count();

        /**
         * Reads the index.
         * 
         * @return the index content
         */
        public List<Column<C, ?>> read();

        /**
         * Reads the index entries.
         * 
         * @param indexEntries index entries to read
         * @return the list of indexed columns with the denormalized data. Any entry that does not
         *         exist should not be included in the result.
         */
        public List<Column<C, ?>> read(List<ColumnName<C, ?>> indexEntries);

        /**
         * Reads a page from the index.
         * 
         * @param pageRequest page request
         * @param end last column to consider
         * @return the index content
         */
        public MarkPage<Column<C, ?>> read(MarkPageRequest<ColumnName<C, ?>> pageRequest, ColumnName<C, ?> end);
    }
}
