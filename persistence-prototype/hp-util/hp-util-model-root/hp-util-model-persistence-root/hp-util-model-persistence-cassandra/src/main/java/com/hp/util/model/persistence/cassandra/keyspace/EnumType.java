/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

/**
 * Enumeration type.
 * 
 * @param <D> type of the data
 * @author Fabiel Zuniga
 */
public class EnumType<D> implements DataType<D> {

    /*
     * NOTE: The type D in the class definition cannot be restricted to "D extends Enum<D>"
     * otherwise DataTypeVisitor<D> is not possible since the BasicType and CompositeType are not
     * restricted to be enumerations. As workaround, a factory method was added where the
     * restriction is added. It won't be possible to force the enumeration type at compile type, but
     * at least it is not possible to create an instance of this class with an invalid type (Some
     * unsafe code will have to be used).
     */

    private final Class<D> enumClass;

    private EnumType(Class<D> enumClass) {
        if (enumClass == null) {
            throw new NullPointerException("enumClass cannot be null");
        }

        this.enumClass = enumClass;
    }

    /**
     * Creates an enumeration type.
     * 
     * @param enumClass enumeration class
     * @return an enumeration type
     */
    public static <D extends Enum<D>> EnumType<D> valueOf(Class<D> enumClass) {
        return new EnumType<D>(enumClass);
    }

    /**
     * Returns the enumeration class.
     * 
     * @return the enumeration class
     */
    public Class<D> getEnumClass() {
        return this.enumClass;
    }

    @Override
    public <I> void accept(DataTypeVisitor<D, I> visitor, I visitorInput) {
        visitor.visit(this, visitorInput);

    }

    @Override
    public <E, I> E accept(DataTypeCommandVisitor<D, E, I> visitor, I visitorInput) {
        return visitor.visit(this, visitorInput);
    }
}
