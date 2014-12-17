/*
 * Copyright (c) 2009 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.tuple;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * 2-Tuple.
 * <p>
 * Tuples are convenient data structures to use with self-described types. For example the tuple
 * Tuple&lt;Address, PhoneNumber, Email&gt; makes easy to know what each element represents; in the
 * other hand, the tuple Tuple&lt;String, String, Integer&gt; makes hard to figure out what each
 * element represents.
 * 
 * @param <T1> Type for the first element of the tuple
 * @param <T2> Type for the second element of the tuple
 * @author Fabiel Zuniga
 */
public class Pair<T1, T2> extends UnaryTuple<T1> {
    private static final long serialVersionUID = 1L;

    private T2 second;

    /**
     * Creates a new 2-tuple using the given values.
     *
     * @param <TT1> the type for the first element of the tuple
     * @param <TT2> the type for the second element of the tuple
     * @param first First element in the tuple
     * @param second Second element in the tuple
     * @return a new 2-tuple using the given values
     */
    public static <TT1, TT2> Pair<TT1, TT2> valueOf(TT1 first, TT2 second) {
        return new Pair<TT1, TT2>(first, second);
    }

    /**
     * Constructs a 2-tuple.
     *
     * @param first First element in the tuple
     * @param second Second element in the tuple
     */
    protected Pair(T1 first, T2 second) {
        super(first);
        this.second = second;
    }

    /**
     * Gets the second element of the tuple.
     *
     * @return The second element
     */
    public T2 getSecond() {
        return this.second;
    }

    /**
     * Sets the second element of the tuple.
     *
     * @param second The Second element
     */
    public void setSecond(T2 second) {
        this.second = second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        Pair<?, ?> other = (Pair<?, ?>)obj;

        if (this.second == null) {
            if (other.second != null) {
                return false;
            }
        }
        else if (!this.second.equals(other.second)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("first", getFirst()),
                Property.valueOf("second", this.second)
        );
    }
}
