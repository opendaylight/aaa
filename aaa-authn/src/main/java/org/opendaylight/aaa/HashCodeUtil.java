/*****************************************************************************
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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

    /** booleans. */
    public static int hash(int aSeed, boolean aBoolean) {
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    /*** chars. */
    public static int hash(int aSeed, char aChar) {
        return firstTerm(aSeed) + aChar;
    }

    /** ints. */
    public static int hash(int aSeed, int aInt) {
        return firstTerm(aSeed) + aInt;
    }

    /** longs. */
    public static int hash(int aSeed, long aLong) {
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    /** floats. */
    public static int hash(int aSeed, float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    /** doubles. */
    public static int hash(int aSeed, double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    /**
     * <tt>aObject</tt> is a possibly-null object field, and possibly an array.
     *
     * If <tt>aObject</tt> is an array, then each element may be a primitive or
     * a possibly-null object.
     */
    public static int hash(int aSeed, Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!isArray(aObject)) {
            result = hash(result, aObject.hashCode());
        } else {
            int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(aObject, idx);
                // if an item in the array references the array itself, prevent
                // infinite looping
                if (!(item == aObject)) {
                    result = hash(result, item);
                }
            }
        }
        return result;
    }

    // PRIVATE
    private static final int fODD_PRIME_NUMBER = 37;

    private static int firstTerm(int aSeed) {
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(Object aObject) {
        return aObject.getClass().isArray();
    }
}