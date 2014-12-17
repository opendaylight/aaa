/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.lang.reflect.Field;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

/**
 * Utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class TestUtil {
    private static final Measurable<Duration> TENTH_OF_A_SECOND = Measure.valueOf(100, SI.MILLI(SI.SECOND));
    private static final Measurable<Duration> HALF_SECOND = Measure.valueOf(500, SI.MILLI(SI.SECOND));
    private static final Measurable<Duration> ONE_SECOND = Measure.valueOf(1, SI.SECOND);

    private TestUtil() {

    }

    /**
     * Blocks the current thread. This method is at the test level because in general it's not a
     * good practice to block the current thread, other synchronization methods should be used at
     * the business logic level.
     * 
     * @param duration duration to block the current thread for
     */
    public static void waitFor(Measurable<Duration> duration) {
        try {
            Thread.sleep(duration.longValue(SI.MILLI(SI.SECOND)));
        }
        catch (Exception e) {
        }
    }

    /**
     * Blocks the current thread for 100 milliseconds.
     */
    public static void waitForTenthOfASecond() {
        waitFor(TENTH_OF_A_SECOND);
    }

    /**
     * Blocks the current thread for 0.5 second.
     */
    public static void waitForHalfSecond() {
        waitFor(HALF_SECOND);
    }

    /**
     * Blocks the current thread for 0.5 second.
     */
    public static void waitForOneSecond() {
        waitFor(ONE_SECOND);
    }

    /**
     * Updates the value of a private field using reflection. The private field must be declared in
     * the object's class and not in a predecessor.
     * <p>
     * The purpose of this method is to set mocks for dependency objects created internally by the
     * class being tested or injected by frameworks like OSGi. Reflection should not be used to
     * update fields which by design are private.
     * 
     * @param fieldName field name
     * @param fieldValue new field's value
     * @param obj instance to update the field on
     * @throws SecurityException If a security manager, s, is present and any of the following
     *             conditions is met: - invocation of s.checkMemberAccess(this, Member.DECLARED)
     *             denies access to the declared field - the caller's class loader is not the same
     *             as or an ancestor of the class loader for the current class and invocation of
     *             s.checkPackageAccess() denies access to the package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not found
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}
     * @throws IllegalAccessException if the underlying field is inaccessible
     * @throws NullPointerException if either the {@code fieldName} or {@code obj} is {@code null}
     */
    public static <T> void setPrivateField(String fieldName, Object fieldValue, T obj) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }

        @SuppressWarnings("unchecked")
        Class<? super T> objClass = (Class<? super T>) obj.getClass();
        setPrivateField(fieldName, fieldValue, obj, objClass);
    }

    /**
     * Updates the value of a private field using reflection. The private field must be declared in
     * the {@code declaredFieldClass} class and not in a predecessor or ancestor.
     * <p>
     * The purpose of this method is to set mocks for dependency objects created internally by the
     * class being tested or injected by frameworks like OSGi. Reflection should not be used to
     * update fields which by design are private.
     *
     * @param fieldName field name
     * @param fieldValue new field's value
     * @param obj instance to update the field on
     * @param declaredFieldClass class where the field is located
     * @throws SecurityException If a security manager, s, is present and any of the following
     *             conditions is met: - invocation of s.checkMemberAccess(this, Member.DECLARED)
     *             denies access to the declared field - the caller's class loader is not the same
     *             as or an ancestor of the class loader for the current class and invocation of
     *             s.checkPackageAccess() denies access to the package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not found
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}
     * @throws IllegalAccessException if the underlying field is inaccessible
     * @throws NullPointerException if either the {@code fieldName} or {@code obj} is {@code null}
     */
    public static <T> void setPrivateField(String fieldName, Object fieldValue, T obj,
            Class<? super T> declaredFieldClass) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }

        if (fieldName == null) {
            throw new NullPointerException("fieldName cannot be null");
        }

        if (fieldName.isEmpty()) {
            throw new NullPointerException("fieldName cannot be empty");
        }

        if (declaredFieldClass == null) {
            throw new NullPointerException("declaredFieldClass cannot be null");
        }

        Field field = declaredFieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, fieldValue);
    }

    /**
     * Gets a private field using reflection. The private field must be declared in the object's
     * class and not in a predecessor.
     *
     * @param fieldName field name
     * @param obj instance to get the field from
     * @return the private field's value
     * @throws SecurityException If a security manager, s, is present and any of the following
     *             conditions is met: - invocation of s.checkMemberAccess(this, Member.DECLARED)
     *             denies access to the declared field - the caller's class loader is not the same
     *             as or an ancestor of the class loader for the current class and invocation of
     *             s.checkPackageAccess() denies access to the package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not found
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}
     * @throws IllegalAccessException if the underlying field is inaccessible
     * @throws NullPointerException if either the {@code fieldName} or {@code obj} is {@code null}
     */
    public static <T, E> E getPrivateField(String fieldName, T obj) throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }

        @SuppressWarnings("unchecked")
        Class<? super T> objClass = (Class<? super T>) obj.getClass();
        return getPrivateField(fieldName, obj, objClass);
    }

    /**
     * Gets a private field using reflection. The private field must be declared in the
     * {@code declaredFieldClass} class and not in a predecessor or ancestor.
     *
     * @param fieldName field name
     * @param obj instance to get the field from
     * @param declaredFieldClass class where the field is located
     * @return the private field's value
     * @throws SecurityException If a security manager, s, is present and any of the following
     *             conditions is met: - invocation of s.checkMemberAccess(this, Member.DECLARED)
     *             denies access to the declared field - the caller's class loader is not the same
     *             as or an ancestor of the class loader for the current class and invocation of
     *             s.checkPackageAccess() denies access to the package of this class.
     * @throws NoSuchFieldException if a field with the specified name is not found
     * @throws IllegalArgumentException if {@code fieldName} is {@code null}
     * @throws IllegalAccessException if the underlying field is inaccessible
     * @throws NullPointerException if either the {@code fieldName} or {@code obj} is {@code null}
     */
    public static <T, E> E getPrivateField(String fieldName, T obj, Class<? super T> declaredFieldClass)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (obj == null) {
            throw new NullPointerException("obj cannot be null");
        }

        if (fieldName == null) {
            throw new NullPointerException("fieldName cannot be null");
        }

        if (fieldName.isEmpty()) {
            throw new NullPointerException("fieldName cannot be empty");
        }

        if (declaredFieldClass == null) {
            throw new NullPointerException("declaredFieldClass cannot be null");
        }

        Field field = declaredFieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        E value = (E) field.get(obj);
        return value;
    }

    /**
     * Gets the current executing method name. The following example prints:
     * "my.package.MyClassTest.testMyMethod(MyClassTest.java:21)"
     * 
     * <pre>
     * public class MyClassTest {
     *
     *     {@literal @}Test
     *     public void testMyMethod() {
     *         StackTraceElement executingMethod = TestUtil.getExecutingMethod();
     *         System.out.println(executingMethod.toString());
     *     }
     * }
     *
     * <pre>
     *
     * @return the current method name
     */
    public static StackTraceElement getExecutingMethod() {
        // Returns the method that called the method that creates a new instance of Log
        final int sourceStackTraceIndex = 2;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length >= sourceStackTraceIndex) {
            return stackTraceElements[sourceStackTraceIndex];
        }
        return null;
    }
}
