/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

import java.io.Serializable;

import com.hp.util.common.type.Id;

/**
 * Identifiable.
 * 
 * @param <T> type of the identified object
 * @param <I> type of the id. This type should be immutable and it is critical it implements
 *            {@link Object#equals(Object)} and {@link Object#hashCode()} correctly.
 * @author Fabiel Zuniga
 */
public interface Identifiable<T, I extends Serializable> {

    /**
     * Gets the id of this object.
     * <P>
     * A type for the identified must be specified to retrieve the id because identified objects
     * might be inheritable. For example, assume {@code Employee} extends from {@code Person} and
     * {@code Person} implements {@code Identifiable<Person, Long>}. The following code would be
     * possible.
     * 
     * <pre>
     * Id&lt;Person, Long&gt; id = employee.getId();
     * Id&lt;Employee, Long&gt; id = employee.getId();
     * 
     * <pre>
     * 
     * @return the id of this object
     */
    public <E extends T> Id<E, I> getId();
}
