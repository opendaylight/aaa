/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ProxiedSerializableTest {

    // Serialization proxy pattern example:

    @Test
    public void testTestSerialization() {
        ProxiedSerializable serializable = new ProxiedSerializable(new Date(1), new Date(2));

        // Test binary and semantic compatibility

        SemanticCompatibilityVerifier<ProxiedSerializable> semanticVerifier = new SemanticCompatibilityVerifier<ProxiedSerializable>() {
            @Override
            public void assertSemanticCompatibility(ProxiedSerializable original, ProxiedSerializable replica) {
                Assert.assertEquals(original.getAttr1(), replica.getAttr1());
                Assert.assertEquals(original.getAttr2(), replica.getAttr2());
            }
        };

        SerializabilityTester.testSerialization(serializable, semanticVerifier);
    }
}
