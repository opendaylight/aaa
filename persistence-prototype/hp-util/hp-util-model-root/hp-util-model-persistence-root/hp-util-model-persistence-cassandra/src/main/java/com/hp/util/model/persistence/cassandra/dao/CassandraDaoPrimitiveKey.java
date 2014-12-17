/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao;

import java.io.Serializable;

import com.hp.util.common.Identifiable;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.MainColumnFamilyPrimitiveKey;

/**
 * {@link CassandraDao} where the key is a primitive or basic type understood by Cassandra: String,
 * Long Integer, etc.
 * <p>
 * This class must remain state-less so it is thread safe.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter. A DAO is responsible for translating this filter to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            predicates in JPA-based implementations, or WHERE clauses in SQL-base implementations.
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications. A DAO is responsible for translating this specification to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            ORDER BY clauses in SQL-based implementations.
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public abstract class CassandraDaoPrimitiveKey<I extends Serializable, T extends Identifiable<? super T, I>, F, S, N>
        extends CassandraDao<I, I, T, F, S, N> {

    /**
     * Creates a DAO.
     * 
     * @param mainColumnFamily column family to store the identifiable objects
     */
    protected CassandraDaoPrimitiveKey(MainColumnFamilyPrimitiveKey<I, T> mainColumnFamily) {
        super(mainColumnFamily);
    }
}
