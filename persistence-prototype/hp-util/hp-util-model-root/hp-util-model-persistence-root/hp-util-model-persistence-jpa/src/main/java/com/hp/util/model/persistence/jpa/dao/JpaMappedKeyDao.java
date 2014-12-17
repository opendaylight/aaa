/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.io.Serializable;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.dao.UpdateStrategy;

/**
 * JPA {@link JpaOffsetPageDao} where the primary key is a mapped to a different type (MacAddress,
 * IpAddress, etc) that must be converted to a type that JPA understands (String, Long, Integer,
 * etc).
 * <p>
 * This class must remain state-less so it is thread safe.
 * <p>
 * A DAO should be used by {@link Query queries}.
 * 
 * @param <I> type of the identifiable object's id. This type should be immutable and it is critical
 *            it implements {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <B> type of the id used by JPA (Example: String, Long, Integer).
 * @param <P> type of the entity (an object annotated with {@link javax.persistence.Entity})
 * @param <F> type of the associated filter. A DAO is responsible for translating this filter to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            predicates in JPA-based implementations, or WHERE clauses in SQL-base implementations.
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications. A DAO is responsible for translating this specification to any
 *            mechanism understood by the underlying data store or database technology. For example,
 *            ORDER BY clauses in SQL-based implementations.
 * @author Fabiel Zuniga
 */
public abstract class JpaMappedKeyDao<I extends Serializable, T extends Identifiable<? super T, I>, B extends Serializable, P, F, S>
        extends JpaOffsetPageDao<I, T, P, F, S> {

    /**
     * Construct a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     */
    protected JpaMappedKeyDao(Class<P> entityClass) {
        super(entityClass);
    }

    /**
     * Creates a DAO.
     * 
     * @param entityClass class of the object annotated with {@link javax.persistence.Entity}
     * @param updateStrategy update strategy
     */
    protected JpaMappedKeyDao(Class<P> entityClass, UpdateStrategy<P, T> updateStrategy) {
        super(entityClass, updateStrategy);
    }

    @Override
    protected Object getEntityId(Id<T, I> id) {
        return mapKey(id.getValue());
    }

    /**
     * Maps the custom key to the type that JPA understands.
     * 
     * @param key key to convert
     * @return key as stored by JPA in the database
     */
    protected abstract B mapKey(I key);
}
