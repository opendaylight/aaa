/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Composite type.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public class CompositeType<D> implements DataType<D> {

    private final List<BasicType<?>> basicTypes;
    private final CompositeTypeSerializer<D> serializer;

    /**
     * Creates a composite type.
     * 
     * @param serializer composite serializer
     * @param basicTypes basic types
     */
    public CompositeType(CompositeTypeSerializer<D> serializer, BasicType<?>... basicTypes) {
        if (serializer == null) {
            throw new NullPointerException("serializer cannot be null");
        }

        if (basicTypes.length < 2) {
            throw new IllegalArgumentException("A composite type must be composed by at least two types");
        }

        this.serializer = serializer;
        this.basicTypes = Collections.unmodifiableList(Arrays.asList(basicTypes));
    }

    /**
     * Returns the basic types that compose this type.
     * 
     * @return the basic types
     */
    public List<BasicType<?>> getBasicTypes() {
        return this.basicTypes;
    }

    /**
     * Returns the composite type serializer.
     * 
     * @return the composite type serializer
     */
    public CompositeTypeSerializer<D> getCompositeTypeSerializer() {
        return this.serializer;
    }

    @Override
    public <I> void accept(DataTypeVisitor<D, I> visitor, I visitorInput) {
        visitor.visit(this, visitorInput);

    }

    @Override
    public <E, I> E accept(DataTypeCommandVisitor<D, E, I> visitor, I visitorInput) {
        return visitor.visit(this, visitorInput);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(32);
        str.append("CompositeType(");

        for (BasicType<?> basicType : this.basicTypes) {
            if (basicType == null) {
                throw new NullPointerException("basicType cannot be null");
            }
            str.append(basicType);
            str.append(',');
        }

        str.delete(str.length() - 1, str.length());

        str.append(')');

        return str.toString();
    }
}
