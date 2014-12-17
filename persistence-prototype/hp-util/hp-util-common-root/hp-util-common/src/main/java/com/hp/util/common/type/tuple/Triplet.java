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
 * 3-Tuple.
 * <p>
 * Tuples are convenient data structures to use with self-described types. For example the tuple
 * Tuple&lt;Address, PhoneNumber, Email&gt; makes easy to know what each element represents; in the
 * other hand, the tuple Tuple&lt;String, String, Integer&gt; makes hard to figure out what each
 * element represents.
 * 
 * @param <T1> Type for the first element of the tuple
 * @param <T2> Type for the second element of the tuple
 * @param <T3> Type for the third element of the tuple
 * @author Fabiel Zuniga
 */
public class Triplet<T1, T2, T3> extends Pair<T1, T2> {
    private static final long serialVersionUID = 1L;

    private T3 third;

    /**
     * Creates a new 3-tuple using the given values.
     *
     * @param <TT1> the type for the first element of the tuple
     * @param <TT2> the type for the second element of the tuple
     * @param <TT3> the type for the third element of the tuple
     * @param first First element in the tuple
     * @param second Second element in the tuple
     * @param third Third element in the tuple
     * @return a new 3-tuple using the given values
     */
    public static <TT1, TT2, TT3> Triplet<TT1, TT2, TT3> valueOf(TT1 first, TT2 second, TT3 third) {
        return new Triplet<TT1, TT2, TT3>(first, second, third);
    }

    /**
     * Constructs a 3-tuple.
     *
     * @param first First element in the tuple
     * @param second Second element in the tuple
     * @param third Third element in the tuple
     */
    protected Triplet(T1 first, T2 second, T3 third) {
        super(first, second);
        this.third = third;
    }

    /**
     * Gets the third element of the tuple.
     *
     * @return The third element.
     */
    public T3 getThird() {
        return this.third;
    }

    /**
     * Sets the third element of the tuple.
     *
     * @param third The third element
     */
    public void setThird(T3 third) {
        this.third = third;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.third == null) ? 0 : this.third.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        Triplet<?, ?, ?> other = (Triplet<?, ?, ?>)obj;

        if (this.third == null) {
            if (other.third != null) {
                return false;
            }
        }
        else if (!this.third.equals(other.third)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("first", getFirst()),
                Property.valueOf("second", getSecond()),
                Property.valueOf("third", this.third)
        );
    }
}
