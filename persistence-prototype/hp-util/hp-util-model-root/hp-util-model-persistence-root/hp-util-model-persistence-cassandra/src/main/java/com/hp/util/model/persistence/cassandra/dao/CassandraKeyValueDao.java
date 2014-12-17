/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao;

import java.io.Serializable;
import java.util.Collection;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.MainColumnFamily;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.dao.KeyValueDao;

/**
 * Cassandra {@link KeyValueDao}.
 * <p>
 * This class must remain state-less so it is thread safe.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <K> type of the row key the id is mapped to
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public abstract class CassandraKeyValueDao<I extends Serializable, K extends Serializable, T extends Identifiable<? super T, I>, N>
        extends CassandraBaseDao<I, K, T, N> implements KeyValueDao<I, T, CassandraContext<N>> {

    /**
     * Creates a DAO.
     * 
     * @param mainColumnFamily column family to store the identifiable objects
     */
    protected CassandraKeyValueDao(MainColumnFamily<I, K, T> mainColumnFamily) {
        super(mainColumnFamily);
    }

    @Override
    protected Collection<ColumnFamily<?, ?>> getCustomSecondaryIndexesColumnFamilyDefinitions() {
        return getIndexesColumnFamilyDefinitions();
    }

    @Override
    protected void prepareInsertion(T identifiable, CassandraContext<N> context) throws PersistenceException {
        super.prepareInsertion(identifiable, context);
        updateIndexesBeforeInsertion(identifiable, context);
    }

    @Override
    protected void prepareDeletion(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        super.prepareDeletion(id, context);
        updateIndexesBeforeDeletion(id, context);
    }

    /**
     * Gets the definition of all column families used by secondary indexes.
     * 
     * @return column family definitions
     */
    protected abstract Collection<ColumnFamily<?, ?>> getIndexesColumnFamilyDefinitions();

    /**
     * Updates any secondary index before an identifiable object is inserted (Added or updated).
     * 
     * @see CustomSecondaryIndex
     * @see IndexEntryHandler
     * @param identifiable object to insert
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected abstract void updateIndexesBeforeInsertion(T identifiable, CassandraContext<N> context)
            throws PersistenceException;

    /**
     * Updates any secondary index before an identifiable object is deleted.
     * 
     * @see CustomSecondaryIndex
     * @see IndexEntryHandler
     * @param id id of the object to delete
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected abstract void updateIndexesBeforeDeletion(Id<T, I> id, CassandraContext<N> context)
            throws PersistenceException;
}
