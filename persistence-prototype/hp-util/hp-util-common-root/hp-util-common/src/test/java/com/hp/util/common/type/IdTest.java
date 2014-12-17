/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class IdTest {

    public void testConstruction() {
        Id<String, String> id = Id.valueOf("id");
        Assert.assertEquals("id", id.getValue());
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidConstruction() {
        Id<String, String> invalidId = null;
        Id.valueOf(invalidId);
    }

    @Test
    public void testEqualsAndHashCode() {
        Id<String, String> obj = Id.valueOf("id");
        Id<String, String> equal1 = Id.valueOf("id");
        Id<String, String> equal2 = Id.valueOf("id");
        Id<String, String> unequal = Id.valueOf("other id");

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Id<?, ?>> semanticVerifier = new SemanticCompatibilityVerifier<Id<?, ?>>() {
            @Override
            public void assertSemanticCompatibility(Id<?, ?> original, Id<?, ?> replica) {
                Assert.assertEquals(original.getValue(), replica.getValue());
            }
        };

        SerializabilityTester.testSerialization(Id.valueOf("id"), semanticVerifier);
    }
}
