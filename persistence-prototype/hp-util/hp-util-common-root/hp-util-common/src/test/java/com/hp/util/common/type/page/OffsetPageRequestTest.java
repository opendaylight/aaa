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
import com.hp.util.test.ThrowableTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class OffsetPageRequestTest {

    @Test
    public void testConstruction() {
        OffsetPageRequest pageRequest = new OffsetPageRequest(10);
        Assert.assertEquals(0, pageRequest.getOffset());
        Assert.assertEquals(10, pageRequest.getSize());

        pageRequest = new OffsetPageRequest(20, 30);
        Assert.assertEquals(20, pageRequest.getOffset());
        Assert.assertEquals(30, pageRequest.getSize());
    }

    @Test
    @SuppressWarnings("unused")
    public void testInvalidConstruction() {
        final long validOffset = 0;
        final long invalidOffset = -1;

        final int validSize = 1;
        final int invalidSize = 0;

        ThrowableTester.testThrows(IllegalArgumentException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new OffsetPageRequest(invalidSize);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new OffsetPageRequest(invalidOffset, validSize);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new OffsetPageRequest(validOffset, invalidSize);
            }
        });
    }

    @Test
    public void testGetPageIndex() {
        OffsetPageRequest pageRequest = new OffsetPageRequest(10);
        Assert.assertEquals(0, pageRequest.getPageIndex());

        pageRequest = new OffsetPageRequest(0, 10);
        Assert.assertEquals(0, pageRequest.getPageIndex());

        pageRequest = new OffsetPageRequest(1, 10);
        Assert.assertEquals(1, pageRequest.getPageIndex());

        pageRequest = new OffsetPageRequest(10, 10);
        Assert.assertEquals(1, pageRequest.getPageIndex());

        pageRequest = new OffsetPageRequest(11, 10);
        Assert.assertEquals(2, pageRequest.getPageIndex());

        pageRequest = new OffsetPageRequest(30, 5);
        Assert.assertEquals(6, pageRequest.getPageIndex());
    }

    @Test
    public void testEqualsAndHashCode() {
        OffsetPageRequest objA0 = new OffsetPageRequest(0, 10);
        OffsetPageRequest objA1 = new OffsetPageRequest(0, 10);
        OffsetPageRequest objA2 = new OffsetPageRequest(0, 10);
        OffsetPageRequest objB = new OffsetPageRequest(1, 10);

        EqualityTester.testEqualsAndHashCode(objA0, objA1, objA2, objB);
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<OffsetPageRequest> semanticVerifier = new SemanticCompatibilityVerifier<OffsetPageRequest>() {
            @Override
            public void assertSemanticCompatibility(OffsetPageRequest original, OffsetPageRequest replica) {
                Assert.assertEquals(original.getSize(), replica.getSize());
                Assert.assertEquals(original.getOffset(), replica.getOffset());
            }
        };

        SerializabilityTester.testSerialization(new OffsetPageRequest(0, 10), semanticVerifier);
    }
}
