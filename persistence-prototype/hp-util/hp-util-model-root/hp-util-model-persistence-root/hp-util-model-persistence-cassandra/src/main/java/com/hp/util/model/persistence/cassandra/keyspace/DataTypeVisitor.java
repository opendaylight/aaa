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
 * @param <I> type of the visitor's input. This type allows making visitors thread safe (and thus
 *            allowing reusing the visitor instance) when they require input to do their job.
 * @author Fabiel Zuniga
 */
public interface DataTypeVisitor<D, I> {

    /**
     * Visits a data type.
     * 
     * @param dataType data type to visit
     * @param input visitor's input
     */
    public void visit(BasicType<D> dataType, I input);

    /**
     * Visits a data type.
     * 
     * @param dataType data type to visit
     * @param input visitor's input
     */
    public void visit(EnumType<D> dataType, I input);

    /**
     * Visits a data type.
     * 
     * @param dataType data type to visit
     * @param input visitor's input
     */
    public void visit(CompositeType<D> dataType, I input);
}
