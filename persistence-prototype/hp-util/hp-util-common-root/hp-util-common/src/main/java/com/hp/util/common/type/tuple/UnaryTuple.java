/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.tuple;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * 1-Tuple.
 * <p>
 * In mathematics and computer science a tuple captures the intuitive notion of an ordered list of
 * elements. Depending on the mathematical foundation chosen, the formal notion differs slightly. In
 * set theory, an (ordered) n-tuple is a sequence (or ordered list) of n elements, where n is a
 * positive integer.
 * <p>
 * The main properties that distinguish a tuple from, for example, a set are that
 * <ol>
 * <li>It can contain an object more than once</li>
 * <li>The objects appear in a certain order</li>
 * <li>It has finite size</li>
 * </ol>
 * <p>
 * Tuples are convenient data structures to use with self-described types. For example the tuple
 * Tuple&lt;Address, PhoneNumber, Email&gt; makes easy to know what each element represents; in the
 * other hand, the tuple Tuple&lt;String, String, Integer&gt; makes hard to figure out what each
 * element represents.
 * 
 * @param <T1> Type for the first element of the tuple
 * @author Fabiel Zuniga
 */
public class UnaryTuple<T1> implements Serializable {
    private static final long serialVersionUID = 1L;

    private T1 first;

    /**
     * Creates a new 1-tuple using the given values.
     *
     * @param <TT1> the type for the first element of the tuple
     * @param first First element in the tuple
     * @return a new 1-tuple using the given values
     */
    public static <TT1> UnaryTuple<TT1> valueOf(TT1 first) {
        return new UnaryTuple<TT1>(first);
    }

    /**
     * Constructs a 1-tuple.
     *
     * @param first First element in the tuple
     */
    protected UnaryTuple(T1 first) {
        this.first = first;
    }

    /**
     * Gets the first element of the tuple.
     *
     * @return The first element
     */
    public T1 getFirst() {
        return this.first;
    }

    /**
     * Sets the first element of the tuple.
     *
     * @param first The first element
     */
    public void setFirst(T1 first) {
        this.first = first;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        UnaryTuple<?> other = (UnaryTuple<?>)obj;

        if (this.first == null) {
            if (other.first != null) {
                return false;
            }
        }
        else if (!this.first.equals(other.first)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("first", this.first)
        );
    }
}
