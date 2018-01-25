/*****************************************************************************
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *****************************************************************************/

package org.opendaylight.aaa.impl.shiro.tokenauthrealm.util;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of hashCode.
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
     * An initial value for a hashCode, to which is added contributions
     * from fields. Using a non-zero value decreases collisions of
     * hashCode values.
     */
    // PRIVATE
    private static final int ODD_PRIME_NUMBER = 37;

    public static final int SEED = 23;

    private HashCodeUtil() {
    }

    /**
     * Hashing functionality.
     *
     * @param seed the seed
     * @param booleanNumber the boolean number
     * @return the hash
     */
    public static int hash(int seed, boolean booleanNumber) {
        return firstTerm(seed) + (booleanNumber ? 1 : 0);
    }

    /**
     * Hashing functionality.
     *
     * @param seed the seed
     * @param character the character
     * @return the hash
     */
    public static int hash(int seed, char character) {
        return firstTerm(seed) + character;
    }

    /**
     * Hashing functionality.
     *
     * @param seed the seed
     * @param integer the integer
     * @return the hash
     */
    public static int hash(int seed, int integer) {
        return firstTerm(seed) + integer;
    }

    /**
     * Hashing functionality.
     *
     * @param seed the seed
     * @param longNumber the long number
     * @return the hash
     */
    public static int hash(int seed, long longNumber) {
        return firstTerm(seed) + (int) (longNumber ^ longNumber >>> 32);
    }

    /**
     * Hashing functionality.
     *
     * @param seed the seed
     * @param floatNumber the float number
     * @return the hash
     */
    public static int hash(int seed, float floatNumber) {
        return hash(seed, Float.floatToIntBits(floatNumber));
    }

    /**
     * Hashing functionality.
     *
     * @param seed the seed
     * @param doubleNumber the double number
     * @return the hash
     */
    public static int hash(int seed, double doubleNumber) {
        return hash(seed, Double.doubleToLongBits(doubleNumber));
    }

    /**
     * Object can be either a nullable object field or an array.
     *
     * <p>
     * If the object is an array, then each element may be a primitive or
     * a possibly-null object.
     *
     * @param seed the seed
     * @param object the object
     * @return the hash
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
                if (item != object) {
                    result = hash(result, item);
                }
            }
        }
        return result;
    }

    private static int firstTerm(int seed) {
        return ODD_PRIME_NUMBER * seed;
    }

    private static boolean isArray(Object object) {
        return object.getClass().isArray();
    }
}
