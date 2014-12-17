/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.hp.util.common.Converter;
import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.Batch;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.ColumnFamilyHandler;
import com.hp.util.model.persistence.cassandra.MainColumnFamily;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.dao.BaseDao;

/**
 * Cassandra {@link BaseDao}.
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
public abstract class CassandraBaseDao<I extends Serializable, K extends Serializable, T extends Identifiable<? super T, I>, N>
        implements BaseDao<I, T, CassandraContext<N>>, ColumnFamilyHandler, Converter<CassandraRow<I, String>, T> {

    /*
     * NOTE: Public methods use Id<T, I>; internal methods can just use I.
     */

    private MainColumnFamily<I, K, T> mainColumnFamily;

    /**
     * Creates a DAO.
     * 
     * @param mainColumnFamily column family to store the identifiable object
     */
    protected CassandraBaseDao(MainColumnFamily<I, K, T> mainColumnFamily) {
        if (mainColumnFamily == null) {
            throw new NullPointerException("mainColumnFamily cannot be null");
        }

        this.mainColumnFamily = mainColumnFamily;
    }

    @Override
    public Collection<ColumnFamily<?, ?>> getColumnFamilies() {
        Collection<ColumnFamily<?, ?>> columnFamilyDefinitions = new ArrayList<ColumnFamily<?, ?>>();
        columnFamilyDefinitions.addAll(this.mainColumnFamily.getColumnFamilies());
        Collection<ColumnFamily<?, ?>> customSecondaryIndexesColumnFamilyDefinitions = getCustomSecondaryIndexesColumnFamilyDefinitions();
        if (customSecondaryIndexesColumnFamilyDefinitions != null) {
            columnFamilyDefinitions.addAll(customSecondaryIndexesColumnFamilyDefinitions);
        }
        return columnFamilyDefinitions;
    }

    /**
     * Inserts (Creates or updates) an identifiable object.
     * 
     * @param identifiable object to insert
     * @param context data store context
     * @return inserted storable
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected T insert(T identifiable, CassandraContext<N> context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        // A batch is used in case Write Ahead Log strategies are implemented in the future.
        Batch<N> batch = context.getCassandraClient().prepareBatch(context);
        batch.start();
        prepareInsertion(identifiable, context);
        CassandraRow<I, String> inserted = this.mainColumnFamily.insert(identifiable, context);
        batch.execute();
        return convert(inserted);
    }

    @Override
    public T create(T identifiable, CassandraContext<N> context) throws PersistenceException {
        // In Cassandra create and update are considered the same operation.
        return insert(identifiable, context);
    }

    @Override
    public void update(T identifiable, CassandraContext<N> context) throws PersistenceException {
        // In Cassandra create and update are considered the same operation.
        insert(identifiable, context);
    }

    @Override
    public void delete(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        // A batch is used in case Write Ahead Log strategies are implemented in the future.
        // TODO:
        Batch<N> batch = context.getCassandraClient().prepareBatch(context);
        batch.start();
        prepareDeletion(id, context);
        this.mainColumnFamily.delete(id, context);
        batch.execute();
    }

    @Override
    public T get(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        return convert(this.mainColumnFamily.read(id, context));
    }

    @Override
    public boolean exist(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {
        return this.mainColumnFamily.exist(id, context);
    }

    /**
     * Gets the column family object are stored in.
     * 
     * @return the main column family
     */
    protected MainColumnFamily<I, K, T> getMainColumnFamily() {
        return this.mainColumnFamily;
    }

    @Override
    public T convert(CassandraRow<I, String> row) {
        if (row == null) {
            return null;
        }

        T target = this.mainColumnFamily.convert(row);

        //---------
        // setId was removed from Identifiable, now it is up to the subclasses to make sure the id
        // is correctly set.
        // identifiable.setId(Id.<T, I> valueOf(source.getId()));
        assert (row.getKey() == null || row.getKey().equals(target.getId().getValue()));
        //---------

        return target;
    }

    /**
     * Gets the definition of all column families used by secondary indexes.
     * 
     * @return column family definitions
     */
    @SuppressWarnings("static-method")
    protected Collection<ColumnFamily<?, ?>> getCustomSecondaryIndexesColumnFamilyDefinitions() {
        return Collections.emptyList();
    }

    /**
     * Updates any secondary index before an identifiable object is inserted (Added or updated).
     * 
     * @see CustomSecondaryIndex
     * @see IndexEntryHandler
     * @param identifiable object to insert
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected void prepareInsertion(T identifiable, CassandraContext<N> context) throws PersistenceException {

    }

    /**
     * Updates any secondary index before an identifiable object is deleted.
     * 
     * @see CustomSecondaryIndex
     * @see IndexEntryHandler
     * @param id id of the object to delete
     * @param context data store context
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    protected void prepareDeletion(Id<T, I> id, CassandraContext<N> context) throws PersistenceException {

    }

    /**
     * Re-throws {@link Throwable#getCause() exception.getCause()} if it is a
     * {@link PersistenceException}.
     * <p>
     * This is a convenient method to use when filter visitors are used to generate the result of a
     * query and thus a {@link PersistenceException} needs to be wrapped in {@link RuntimeException}.
     * 
     * @param exception exception to re-throw
     * @throws PersistenceException if the cause is a {@link PersistenceException}
     * @throws RuntimeException otherwise
     */
    protected static void rethrow(RuntimeException exception) throws PersistenceException {
        if (exception.getCause() != null && PersistenceException.class.isInstance(exception.getCause())) {
            PersistenceException persistenceException = (PersistenceException) exception.getCause();
            throw persistenceException;
        }
        throw exception;
    }
}
