/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ComparabilityTester;
import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class DateTest {

    @Test
    public void valueOfDateLong() {
        Date date = Date.valueOf(1);
        Assert.assertEquals(1, date.getTime());
    }

    @Test
    public void valueOfDate() {
        Date date = Date.valueOf(new java.util.Date(1));
        Assert.assertEquals(1, date.getTime());

        Assert.assertNull(Date.valueOf(null));
    }

    @Test
    public void valueCurrentTime() {
        long currenTimeMillisecons = System.currentTimeMillis();
        Date currenTime = Date.currentTime();
        long error = Math.abs(currenTimeMillisecons - currenTime.getTime());
        Assert.assertTrue(error <= 1);
    }

    @Test
    public void testToDate() {
        java.util.Date expected = new java.util.Date(1);
        Date date = Date.valueOf(expected);
        java.util.Date actual = date.toDate();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testComparison() {
        Date first = Date.valueOf(1);
        Date equallyInOrderToFirst = Date.valueOf(1);
        Date second = Date.valueOf(2);
        Date third = Date.valueOf(3);
        ComparabilityTester.testComparison(first, equallyInOrderToFirst, second, third);
    }

    @Test
    public void testEqualsAndHashCode() {
        Date obj = Date.valueOf(1);
        Date equal1 = Date.valueOf(1);
        Date equal2 = Date.valueOf(new java.util.Date(1));
        Date unequals = Date.valueOf(2);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequals);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Date> semanticCompatibilityVerifier = new SemanticCompatibilityVerifier<Date>() {
            @Override
            public void assertSemanticCompatibility(Date original, Date replica) {
                Assert.assertEquals(original, replica);
            }
        };

        SerializabilityTester.testSerialization(Date.valueOf(1), semanticCompatibilityVerifier);
    }

    @Test
    public void testToString() {
        Assert.assertFalse(Date.valueOf(1).toString().isEmpty());
    }
}
