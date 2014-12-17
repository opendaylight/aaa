/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.tuple;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.type.Date;
import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class TripletTest {

    @Test
    public void testEqualsAndHashCode() {
        Date date = Date.currentTime();
        Date otherDate = Date.valueOf(date.getTime() + 1);
        Triplet<String, Integer, Date> obj = Triplet.valueOf("Hello World", Integer.valueOf(1), date);
        Triplet<String, Integer, Date> equal1 = Triplet.valueOf("Hello World", Integer.valueOf(1), date);
        Triplet<String, Integer, Date> equal2 = Triplet.valueOf("Hello World", Integer.valueOf(1), date);
        Triplet<String, Integer, Date> unequal1 = Triplet.valueOf("Different value", Integer.valueOf(1), date);
        Triplet<String, Integer, Date> unequal2 = Triplet.valueOf("Hello World", Integer.valueOf(2), date);
        Triplet<String, Integer, Date> unequal3 = Triplet.valueOf("Hello World", Integer.valueOf(1), otherDate);
        Triplet<String, Integer, Date> unequal4 = Triplet.valueOf("Different value", Integer.valueOf(2), otherDate);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2, unequal3, unequal4);

        obj = Triplet.valueOf(null, null, null);
        equal1 = Triplet.valueOf(null, null, null);
        equal2 = Triplet.valueOf(null, null, null);
        unequal1 = Triplet.valueOf("Different value", Integer.valueOf(1), date);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Triplet<?, ?, ?>> semanticVerifier = new SemanticCompatibilityVerifier<Triplet<?, ?, ?>>() {
            @Override
            public void assertSemanticCompatibility(Triplet<?, ?, ?> original, Triplet<?, ?, ?> replica) {
                Assert.assertEquals(original.getFirst(), replica.getFirst());
                Assert.assertEquals(original.getSecond(), replica.getSecond());
                Assert.assertEquals(original.getThird(), replica.getThird());
            }
        };

        SerializabilityTester.testSerialization(Triplet.valueOf(Integer.valueOf(0), "Hello World", Date.currentTime()),
                semanticVerifier);
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(Triplet.valueOf("Hello World", Integer.valueOf(0), Date.currentTime()).toString());
    }
}
