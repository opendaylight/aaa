/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ComparabilityTester;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class PortTest {

    @Test
    public void testConstruction() {
        Port port = Port.valueOf(6000);
        Assert.assertEquals(Integer.valueOf(6000), port.getValue());
    }

    @Test
    public void testInvalidConstruction() {
        final Integer invalidPortValue = null;
        final int invalidPortPrimitiveValueTooSmall = -1;
        final int invalidPortPrimitiveValueTooBig = Port.MAX_VALUE + 1;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @SuppressWarnings("unused")
            @Override
            public void execute() throws Throwable {
                new Port(invalidPortValue);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Port.valueOf(invalidPortPrimitiveValueTooSmall);
            }
        });

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                Port.valueOf(invalidPortPrimitiveValueTooBig);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Port> semanticVerifier = new SemanticCompatibilityVerifier<Port>() {
            @Override
            public void assertSemanticCompatibility(Port original, Port replica) {
                Assert.assertEquals(original.getValue(), replica.getValue());
            }
        };

        SerializabilityTester.testSerialization(Port.valueOf(1), semanticVerifier);
    }

    @Test
    public void testIsAvailable() throws IOException {
        Port portInUse = Port.valueOf(9998);
        Port availablePort = Port.valueOf(9999);

        try (ServerSocket serverSocket = new ServerSocket(portInUse.getValue().intValue())) {
            Assert.assertFalse(portInUse.isAvailable());
            Assert.assertFalse(portInUse.isAvailable());
            Assert.assertTrue(availablePort.isAvailable());
            Assert.assertTrue(availablePort.isAvailable());
        }
    }

    @Test
    public void testComparison() {
        Port first = Port.valueOf(1);
        Port equallyInOrderToFirst = Port.valueOf(1);
        Port second = Port.valueOf(2);
        Port third = Port.valueOf(3);
        ComparabilityTester.testComparison(first, equallyInOrderToFirst, second, third);
    }
}
