/*****************************************************************************
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/

package org.opendaylight.aaa;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <tt>hashCode</tt>.
 *
 * <p>
 * Example use case:
 *
 * <pre>
 * public int hashCode() {
 *     int result = HashCodeUtil.SEED;
 *     // collect the contributions of various fields
 *     result = HashCodeUtil.hash(result, fPrimitive);
 *     result = HashCodeUtil.hash(result, fObject);
 *     result = HashCodeUtil.hash(result, fArray);
 *     return result;
 * }
 * </pre>
 */
public final class HashCodeUtil {

    /**
     * An initial value for a <tt>hashCode</tt>, to which is added contributions
     * from fields. Using a non-zero value decreases collisions of
     * <tt>hashCode</tt> values.
     */
    public static final int SEED = 23;

    private HashCodeUtil() {
    }

    /** booleans. */
    public static int hash(int seed, boolean booleanNumber) {
        return firstTerm(seed) + (booleanNumber ? 1 : 0);
    }

    /*** chars. */
    public static int hash(int seed, char character) {
        return firstTerm(seed) + character;
    }

    /** ints. */
    public static int hash(int seed, int integer) {
        return firstTerm(seed) + integer;
    }

    /** longs. */
    public static int hash(int seed, long longNumber) {
        return firstTerm(seed) + (int) (longNumber ^ longNumber >>> 32);
    }

    /** floats. */
    public static int hash(int seed, float floatNumber) {
        return hash(seed, Float.floatToIntBits(floatNumber));
    }

    /** doubles. */
    public static int hash(int seed, double doubleNumber) {
        return hash(seed, Double.doubleToLongBits(doubleNumber));
    }

    /**
     * <tt>aObject</tt> is a possibly-null object field, and possibly an array.
     *
     * <p>
     * If <tt>aObject</tt> is an array, then each element may be a primitive or
     * a possibly-null object.
     */
    public static int hash(int seed, Object object) {
        int result = seed;
        if (object == null) {
            result = hash(result, 0);
        } else if (!isArray(object)) {
            result = hash(result, object.hashCode());
        } else {
            int length = Array.getLength(object);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(object, idx);
                // if an item in the array references the array itself, prevent
                // infinite looping
                if (!(item == object)) {
                    result = hash(result, item);
                }
            }
        }
        return result;
    }

    // PRIVATE
    private static final int ODD_PRIME_NUMBER = 37;

    private static int firstTerm(int seed) {
        return ODD_PRIME_NUMBER * seed;
    }

    private static boolean isArray(Object object) {
        return object.getClass().isArray();
    }
}
