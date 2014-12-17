/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.page;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.EqualityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class PageRequestTest {

    @Test
    public void testConstruction() {
        PageRequest pageRequest = new PageRequest(10);
        Assert.assertEquals(10, pageRequest.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstruction() {
        final int invalidSize = 0;

        @SuppressWarnings("unused")
        PageRequest pageRequest = new PageRequest(invalidSize);
    }

    @Test
    public void testEqualsAndHashCode() {
        PageRequest objA0 = new PageRequest(10);
        PageRequest objA1 = new PageRequest(10);
        PageRequest objA2 = new PageRequest(10);
        PageRequest objB = new PageRequest(1);

        EqualityTester.testEqualsAndHashCode(objA0, objA1, objA2, objB);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<PageRequest> semanticVerifier = new SemanticCompatibilityVerifier<PageRequest>() {
            @Override
            public void assertSemanticCompatibility(PageRequest original, PageRequest replica) {
                Assert.assertEquals(original.getSize(), replica.getSize());
            }
        };

        SerializabilityTester.testSerialization(new PageRequest(10), semanticVerifier);
    }
}
