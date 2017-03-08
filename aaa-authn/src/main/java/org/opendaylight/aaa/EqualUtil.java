/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa;

/**
 * Simple class to aide in implementing equals.
 *
 * <p>
 * <em>Arrays are not handled by this class</em>. This is because the
 * <code>Arrays.equals</code> methods should be used for array fields.
 */
public final class EqualUtil {

    private EqualUtil() {
    }

    public static boolean areEqual(boolean c1, boolean c2) {
        return c1 == c2;
    }

    public static boolean areEqual(char c1, char c2) {
        return c1 == c2;
    }

    public static boolean areEqual(long c1, long c2) {
        return c1 == c2;
    }

    public static boolean areEqual(float c1, float c2) {
        return Float.floatToIntBits(c1) == Float.floatToIntBits(c2);
    }

    public static boolean areEqual(double c1, double c2) {
        return Double.doubleToLongBits(c1) == Double.doubleToLongBits(c2);
    }

    public static boolean areEqual(Object c1, Object c2) {
        return c1 == null ? c2 == null : c1.equals(c2);
    }
}
