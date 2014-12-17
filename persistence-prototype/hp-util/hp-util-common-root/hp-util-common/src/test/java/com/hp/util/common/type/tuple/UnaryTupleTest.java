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

import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class UnaryTupleTest {

    @Test
    public void testEqualsAndHashCode() {
        UnaryTuple<String> obj = UnaryTuple.valueOf("Hello World");
        UnaryTuple<String> equal1 = UnaryTuple.valueOf("Hello World");
        UnaryTuple<String> equal2 = UnaryTuple.valueOf("Hello World");
        UnaryTuple<String> unequal1 = UnaryTuple.valueOf("Different value");

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1);

        obj = UnaryTuple.valueOf(null);
        equal1 = UnaryTuple.valueOf(null);
        equal2 = UnaryTuple.valueOf(null);
        unequal1 = UnaryTuple.valueOf("Different value");
        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<UnaryTuple<?>> semanticVerifier = new SemanticCompatibilityVerifier<UnaryTuple<?>>() {
            @Override
            public void assertSemanticCompatibility(UnaryTuple<?> original, UnaryTuple<?> replica) {
                Assert.assertEquals(original.getFirst(), replica.getFirst());
            }
        };

        SerializabilityTester.testSerialization(UnaryTuple.valueOf("Hello World"), semanticVerifier);
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(UnaryTuple.valueOf("Hello World").toString());
    }
}
