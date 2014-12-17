/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

/**
 * Data Type visitor.
 * 
 * @param <D> type of the data
 * @param <E> type of the result of the visit
 * @param <I> type of the visitor's input. This type allows making visitors thread safe (and thus
 *            allowing reusing the visitor instance) when they require input to do their job.
 * @author Fabiel Zuniga
 */
public interface DataTypeCommandVisitor<D, E, I> {

    /**
     * Visits a data type.
     * 
     * @param dataType data type to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(BasicType<D> dataType, I input);

    /**
     * Visits a data type.
     * 
     * @param dataType data type to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(EnumType<D> dataType, I input);

    /**
     * Visits a data type.
     * 
     * @param dataType data type to visit
     * @param input visitor's input
     * @return the result of the visit
     */
    public E visit(CompositeType<D> dataType, I input);
}
