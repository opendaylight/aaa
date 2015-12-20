/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa;

/**
 * Simple class to aide in implementing equals.
 * <p>
 *
 * <em>Arrays are not handled by this class</em>. This is because the
 * <code>Arrays.equals</code> methods should be used for array fields.
 */
public final class EqualUtil {
    static public boolean areEqual(boolean aThis, boolean aThat) {
        return aThis == aThat;
    }

    static public boolean areEqual(char aThis, char aThat) {
        return aThis == aThat;
    }

    static public boolean areEqual(long aThis, long aThat) {
        return aThis == aThat;
    }

    static public boolean areEqual(float aThis, float aThat) {
        return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
    }

    static public boolean areEqual(double aThis, double aThat) {
        return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
    }

    static public boolean areEqual(Object aThis, Object aThat) {
        return aThis == null ? aThat == null : aThis.equals(aThat);
    }
}
