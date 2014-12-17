/*
 * Copyright (c) 2010 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.tuple;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * 4-Tuple.
 * <p>
 * Tuples are convenient data structures to use with self-described types. For example the tuple
 * Tuple&lt;Address, PhoneNumber, Email&gt; makes easy to know what each element represents; in the
 * other hand, the tuple Tuple&lt;String, String, Integer&gt; makes hard to figure out what each
 * element represents.
 * 
 * @param <T1> Type for the first element of the tuple
 * @param <T2> Type for the second element of the tuple
 * @param <T3> Type for the third element of the tuple
 * @param <T4> Type for the fourth element of the tuple
 * @author Fabiel Zuniga
 */
public class Quadruplet<T1, T2, T3, T4> extends Triplet<T1, T2, T3> {
    private static final long serialVersionUID = 1L;

    private T4 fourth;

    /**
     * Creates a new 4-tuple using the given values.
     *
     * @param <TT1> the type for the first element of the tuple
     * @param <TT2> the type for the second element of the tuple
     * @param <TT3> the type for the third element of the tuple
     * @param <TT4> the type for the fourth element of the tuple
     * @param first First element in the tuple
     * @param second Second element in the tuple
     * @param third Third element in the tuple
     * @param fourth Fourth element in the tuple
     * @return a new 4-tuple using the given values
     */
    public static <TT1, TT2, TT3, TT4> Quadruplet<TT1, TT2, TT3, TT4> valueOf(TT1 first, TT2 second, TT3 third,
        TT4 fourth) {
        return new Quadruplet<TT1, TT2, TT3, TT4>(first, second, third, fourth);
    }

    /**
     * Constructs a 4-tuple.
     *
     * @param first First element in the tuple
     * @param second Second element in the tuple
     * @param third Third element in the tuple
     * @param fourth Fourth element in the tuple
     */
    protected Quadruplet(T1 first, T2 second, T3 third, T4 fourth) {
        super(first, second, third);
        this.fourth = fourth;
    }

    /**
     * Gets the fourth element of the tuple.
     *
     * @return The fourth element
     */
    public T4 getFourth() {
        return this.fourth;
    }

    /**
     * Sets the fourth element of the tuple.
     *
     * @param fourth The fourth element
     */
    public void setFourth(T4 fourth) {
        this.fourth = fourth;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.fourth == null) ? 0 : this.fourth.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        Quadruplet<?, ?, ?, ?> other = (Quadruplet<?, ?, ?, ?>)obj;

        if (this.fourth == null) {
            if (other.fourth != null) {
                return false;
            }
        }
        else if (!this.fourth.equals(other.fourth)) {
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
                Property.valueOf("third", getThird()),
                Property.valueOf("fourth", this.fourth)
        );
    }
}
