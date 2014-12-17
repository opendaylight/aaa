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
public class QuadrupletTest {

    @Test
    public void testEqualsAndHashCode() {
        Date date = Date.currentTime();
        Date otherDate = Date.valueOf(date.getTime() + 1);
        Quadruplet<String, Integer, Date, Boolean> obj = Quadruplet.valueOf("Hello World", Integer.valueOf(1), date,
                Boolean.TRUE);
        Quadruplet<String, Integer, Date, Boolean> equal1 = Quadruplet.valueOf("Hello World", Integer.valueOf(1), date,
                Boolean.TRUE);
        Quadruplet<String, Integer, Date, Boolean> equal2 = Quadruplet.valueOf("Hello World", Integer.valueOf(1), date,
                Boolean.TRUE);
        Quadruplet<String, Integer, Date, Boolean> unequal1 = Quadruplet.valueOf("Different value", Integer.valueOf(1),
                date, Boolean.TRUE);
        Quadruplet<String, Integer, Date, Boolean> unequal2 = Quadruplet.valueOf("Hello World", Integer.valueOf(2),
                date, Boolean.TRUE);
        Quadruplet<String, Integer, Date, Boolean> unequal3 = Quadruplet.valueOf("Hello World", Integer.valueOf(1),
                otherDate, Boolean.TRUE);
        Quadruplet<String, Integer, Date, Boolean> unequal4 = Quadruplet.valueOf("Hello World", Integer.valueOf(1),
                date, Boolean.FALSE);
        Quadruplet<String, Integer, Date, Boolean> unequal5 = Quadruplet.valueOf("Different value", Integer.valueOf(2),
                otherDate, Boolean.FALSE);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2, unequal3, unequal4, unequal5);

        obj = Quadruplet.valueOf(null, null, null, null);
        equal1 = Quadruplet.valueOf(null, null, null, null);
        equal2 = Quadruplet.valueOf(null, null, null, null);
        unequal1 = Quadruplet.valueOf("Different value", Integer.valueOf(1), date, Boolean.TRUE);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Quadruplet<?, ?, ?, ?>> semanticVerifier = new SemanticCompatibilityVerifier<Quadruplet<?, ?, ?, ?>>() {
            @Override
            public void assertSemanticCompatibility(Quadruplet<?, ?, ?, ?> original, Quadruplet<?, ?, ?, ?> replica) {
                Assert.assertEquals(original.getFirst(), replica.getFirst());
                Assert.assertEquals(original.getSecond(), replica.getSecond());
                Assert.assertEquals(original.getThird(), replica.getThird());
                Assert.assertEquals(original.getFourth(), replica.getFourth());
            }
        };

        SerializabilityTester.testSerialization(
                Quadruplet.valueOf(Integer.valueOf(0), "Hello World", Date.currentTime(), Boolean.TRUE),
                semanticVerifier);
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(Quadruplet.valueOf("Hello World", Integer.valueOf(0), Date.currentTime(), Boolean.TRUE)
                .toString());
    }
}
