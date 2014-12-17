/*
 * Copyright (c) 2011 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import org.junit.Assert;

/**
 * Tester to test objects which override {@link Object#equals(Object)} and {@link Object#hashCode()}
 * 
 * @author Fabiel Zuniga .
 */
public final class EqualityTester {

    private static final DifferetType DIFFERENT_TYPE = new DifferetType();

    private EqualityTester() {

    }

    /**
     * Tests {@link Object#equals(Object)} and {@link Object#hashCode()} methods partially testing
     * consistency property: Fields that are not part of the {@link Object#equals(Object)} are not
     * modified - no {@link Exerciser} is used.
     * 
     * @param <T> type of the object to test
     * @param obj base object
     * @param equal1 an object expected to be equal to the base
     * @param equal2 an object expected to be equal to the base
     * @param unequals a set of objects expected not to be equal to the base
     */
    @SafeVarargs
    public static <T> void testEqualsAndHashCode(T obj, T equal1, T equal2, T... unequals) {
        testEqualsAndHashCode(obj, equal1, equal2, null, unequals);
    }

    /**
     * Tests {@link Object#equals(Object)} and {@link Object#hashCode()} methods using the given
     * {@code exerciser} to test consistency.
     * 
     * @param <T> type of the object to test
     * @param obj base object
     * @param equal1 an object expected to be equal to the base
     * @param equal2 an object expected to be equal to the base
     * @param exerciser exerciser used to test consistency
     * @param unequals a set of objects expected not to be equal to the base
     */
    @SafeVarargs
    public static <T> void testEqualsAndHashCode(T obj, T equal1, T equal2, Exerciser<T> exerciser, T... unequals) {

        String testedObjectClassName = obj.getClass().getCanonicalName();

        // Equals contract

        // Reflexive
        Assert.assertTrue("Reflexive property broken for " + testedObjectClassName, obj.equals(obj));

        // Symmetric
        Assert.assertTrue("Symmetric property broken for " + testedObjectClassName, obj.equals(equal1)
                && equal1.equals(obj));

        // Transitive
        Assert.assertTrue("Transitive property broken for " + testedObjectClassName, obj.equals(equal1)
                && equal1.equals(equal2) && obj.equals(equal2));

        // Null reference
        Assert.assertFalse("Null reference property broken for " + testedObjectClassName, obj.equals(null));

        // Different type parameter
        Assert.assertFalse("Different type parameter consideration broken for " + testedObjectClassName,
                obj.equals(DIFFERENT_TYPE));

        // Inequality test
        if (unequals != null) {
            for (T unequal : unequals) {
                Assert.assertFalse("Inequality test broken for " + testedObjectClassName, obj.equals(unequal));
                Assert.assertFalse("Inequality test broken for " + testedObjectClassName, unequal.equals(obj));
            }
        }

        // Hash code
        Assert.assertEquals("Hashcode broken for " + testedObjectClassName, obj.hashCode(), equal1.hashCode());
        Assert.assertEquals("Hashcode broken for " + testedObjectClassName, obj.hashCode(), equal2.hashCode());

        // Consistent property

        if (exerciser != null) {
            exerciser.exercise(obj);
        }

        Assert.assertTrue("Consistent property broken for " + testedObjectClassName, obj.equals(equal1));
        Assert.assertFalse("Consistent property broken for " + testedObjectClassName, obj.equals(unequals));
        Assert.assertEquals("Hashcode consistent property broken for " + testedObjectClassName, obj.hashCode(),
                equal1.hashCode());
    }

    /**
     * Exerciser used when testing consistent property in equals method.
     * 
     * @param <T> type of the object to exercise
     */
    public static interface Exerciser<T> {

        /**
         * Exercises the object without modifying fields considered on equals method. This method is
         * used to test that {@link Object#equals(Object)} is consistent.
         * 
         * @param obj Object to exercise
         */
        public void exercise(T obj);
    }

    private static class DifferetType {

    }
}
