/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import java.io.Serializable;
import java.util.List;

import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.column.Column;

/**
 * Custom secondary index column family that keep rows from another column family (or main column
 * family) as columns in wide rows.
 * <p>
 * Cassandra already provides native index, however is like a hashed index which means you can only
 * do equality query and not range query. One advantage though is that Cassandra's native secondary
 * indexes already handle updates.
 * <p>
 * Cassandra's index is only recommended for attributes with low cardinality, i.e. attributes that
 * have few unique values e.g., color of a product.
 * <p>
 * When a column family is created to keep custom secondary indexes, the row key in such column
 * family would typically be a value of the indexed column and the column names would be the row
 * keys in the main column family that has such value set plus any attribute included for sorting.
 * The column value will include any denormalized data.
 * <p>
 * When working with secondary indexes, one of two strategies is typically employed: either the
 * column values (or names if composite column namme is used) contain row keys pointing to a
 * separate column family which contains the actual data, or the complete (or partial) set of data
 * for each entity is stored in the secondary index itself (denormalization). With the first
 * strategy, which is similar to building an index, you first fetch a set of row keys from a index
 * and then multiget the matching data rows from a separate column family. This approach is
 * appealing to many at first because it is more normalized; it allows for easy updates of entities,
 * doesn't require you to repeat the same data in multiple column families. However, the second step
 * of the data fetching process, the multiget, is fairly expensive and slow. It requires querying
 * many nodes where each node will need to perform many disk seeks to fetch the rows if they aren't
 * well cached. This approach will not scale well with large data sets.
 * <p>
 * Examples:
 * <p>
 * 
 * <pre>
 * column_family_secondary_index {
 *     "indexed_value_1": {
 *         id_i: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         id_j: &lt;data provided by the denormalizer&gt;,
 *     }
 *     "indexed_value_2": {
 *         id_m: &lt;data provided by the denormalizer&gt;,
 *         ...
 *         id_n: &lt;data provided by the denormalizer&gt;,
 *     }
 * }
 * 
 * column_family_persons_by_status {
 *     "Single": {
 *         person_id_i: &lt;Name, Last Name&gt;,
 *         ...
 *         person_id_j: &lt;Name, Last Name&gt;,
 *     }
 *     "Married": {
 *         person_id_m: &lt;Name, Last Name&gt;,
 *         ...
 *         person_id_n: &lt;Name, Last Name&gt;,
 *     }
 * }
 * </pre>
 * 
 * If the columns need to be sorted by a different attribute the attribute is included as part of
 * the column name (composite column name). This does not affect the denormalizer.
 * <p>
 * Example:
 * 
 * <pre>
 * column_family_persons_by_status_sorted_by_last_name_and_name {
 *     "Single": {
 *         &lt;last_name_i, name_i, person_id_i&gt;: &lt;Birthdate_i&gt;,
 *         ...
 *         &lt;last_name_j, name_j, person_id_j&gt;: &lt;Birthdate_j&gt;,
 *     }
 *     "Married": {
 *         &lt;last_name_m, name_m, person_id_m&gt;: &lt;Birthdate_m&gt;,
 *         ...
 *         &lt;last_name_n, name_n, person_id_n&gt;: &lt;Birthdate_n&gt;,
 *     }
 * }
 * </pre>
 * 
 * @param <K> type of the value (or composite values) of the indexed column (or columns) in the main
 *            column family. This value (or composite value) will become the row key in the
 *            secondary index column.
 * @param <C> type of the column name in the secondary index column family (row key in the main
 *            column family or composite value when sorting information is included)
 * @param <D> type of the denormalized data to set as the value in the indexed columns
 * @author Fabiel Zuniga
 */
public interface CustomSecondaryIndex<K extends Serializable, C extends Serializable & Comparable<C>, D> extends
        ColumnFamilyHandler {

    /**
     * Updates the index after a row has been inserted into the main column family.
     * 
     * @param indexEntry row key in the main column family (or composite value when sorting
     *            information is included). Such row key (or composite value) will become the name
     *            of the column in the secondary index.
     * @param denormalizedData denormalized data to include as part of the indexed columns
     *            {@code null} if no denormalization is used.
     * @param indexKey value (or composite values) of the indexed column (or columns) in the main
     *            column family. This value (or composite value) will become the row key in the
     *            secondary index column.
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void insert(C indexEntry, D denormalizedData, K indexKey, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Updates the index after a row has been deleted from the main column family.
     * 
     * @param indexEntry key of the row removed from the main column family
     * @param indexKey value of the indexed column in the main column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void delete(C indexEntry, K indexKey, CassandraContext<N> context) throws PersistenceException;

    /**
     * Deletes a row in the secondary index column family.
     * <p>
     * This method should be the preferred way to clear a secondary index if it contains a small
     * well-know set of rows. Truncating a column family is an expensive operation.
     * 
     * @param indexKey value of the indexed column in the main column family
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void delete(K indexKey, CassandraContext<N> context) throws PersistenceException;

    /**
     * Updates the index after deleting all rows from the main column family.
     * 
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> void clear(CassandraContext<N> context) throws PersistenceException;

    /**
     * Counts the number of columns in the index.
     * 
     * @param indexKey value of the indexed column in the main column family or row key in the
     *            secondary index column family
     * @param context data store context
     * @return the number of entries in the index key
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> long count(K indexKey, CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads the index entries.
     * 
     * @param indexKey value of the indexed column in the main column family or row key in the
     *            secondary index column family
     * @param context data store context
     * @return the list of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> read(K indexKey, CassandraContext<N> context) throws PersistenceException;

    /**
     * Reads a page of index entries.
     * <p>
     * Note that implementations will normally execute a range query internally, thus if the mark in
     * {@link MarkPageRequest pageRequest} isn't null but doesn't exist, the page will not
     * necessarily be the first page. However, if the mark does not exist, then the mark in the
     * resultant {@link MarkPage} will be null.
     * 
     * @param indexKey value of the indexed column in the main column family or row key in the
     *            secondary index column family
     * @param pageRequest page request
     * @param context data store context
     * @return a page of indexed columns with the denormalized data
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> MarkPage<Column<C, D>> read(K indexKey, MarkPageRequest<C> pageRequest, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Reads the index entries.
     * <p>
     * An index is normally used to get rows (from the main column family) that match a specific
     * indexed value, not to load entries known to match the indexed value - like in this method.
     * This method has been defined to allow secondary indexes to be used by a
     * {@link SecondaryIndexIntegrator.SecondaryIndexReader}.
     * 
     * @param indexEntries index entries to read
     * @param indexKey value of the indexed column in the main column family or row key in the
     *            secondary index column family
     * @param context data store context
     * @return the list of indexed columns with the denormalized data. Any index entry from
     *         {@code indexEntries} that doesn't exist in the index given by {@code indexKey} is not
     *         included in the result.
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public <N> List<Column<C, D>> read(List<C> indexEntries, K indexKey, CassandraContext<N> context)
            throws PersistenceException;
}
