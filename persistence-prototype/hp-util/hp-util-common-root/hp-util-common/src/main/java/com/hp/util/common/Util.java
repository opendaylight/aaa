/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class Util {

    private Util() {

    }

    /**
     * Verifies id the two objects are equals considering {@code null}.
     * 
     * @param obj1 one of the objects to compare
     * @param obj2 the other object to compare
     * @return {@code true} if both objects are either {@code null} or equals (as stated in
     *         {@link Object#equals(Object)}), {@code false} otherwise
     */
    public static <T> boolean equals(T obj1, T obj2) {
        if (obj1 == null) {
            if (obj2 != null) {
                return false;
            }
        }
        else if (!obj1.equals(obj2)) {
            return false;
        }
        return true;
    }
}
