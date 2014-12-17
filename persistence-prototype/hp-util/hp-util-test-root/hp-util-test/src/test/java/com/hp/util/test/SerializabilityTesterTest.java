/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester.Instruction;
import com.hp.util.test.ThrowableTester.Validator;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class SerializabilityTesterTest {

    private static final Path PREVIOUS_VERSION_JAR_PATH = FileSystems.getDefault().getPath("src", "test", "resources",
            "portable-serialization-test", "previous-version.jar");

    @Test
    public void testTestSerialization() {
        PortableClass serializable = new PortableClass();
        serializable.setAttrPreviousVersion("previous attr");
        serializable.setAttrCurrentVersion("current attr");

        // Test binary and semantic compatibility

        SemanticCompatibilityVerifier<PortableClass> semanticVerifier = new SemanticCompatibilityVerifier<PortableClass>() {
            @Override
            public void assertSemanticCompatibility(PortableClass original, PortableClass replica) {
                // System.out.println(original);
                // System.out.println(replica);

                Assert.assertEquals(original.getAttrPreviousVersion(), replica.getAttrPreviousVersion());
                Assert.assertEquals(original.getAttrCurrentVersion(), replica.getAttrCurrentVersion());
            }
        };

        SerializabilityTester.testSerialization(serializable, semanticVerifier);
    }

    @Test
    public void testTestSerializationFailure() {
        final InvalidSerializable serializable = new InvalidSerializable();

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Serialization failure:";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());

            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                SerializabilityTester.testSerialization(serializable, null);
            }
        }, errorValidator);
    }

    @Test
    public void testTestPortableSerialization() throws Exception {
        SemanticCompatibilityVerifier<PortableClass> semanticVerifier = new SemanticCompatibilityVerifier<PortableClass>() {
            @Override
            public void assertSemanticCompatibility(PortableClass original, PortableClass replica) {
                Assert.assertEquals(original.getAttrPreviousVersion(), replica.getAttrPreviousVersion());
                Assert.assertNotNull(original.getAttrCurrentVersion());
                Assert.assertNull(replica.getAttrCurrentVersion());
            }
        };

        PortableClass currentVersion = new PortableClass();
        currentVersion.setAttrPreviousVersion("previous version value at: " + new Date());
        currentVersion.setAttrCurrentVersion("current version value at : " + new Date());

        SerializabilityTester.testPortableSerialization(PortableClass.class, currentVersion, PREVIOUS_VERSION_JAR_PATH,
                semanticVerifier);
    }

    @Test
    public void testTestNonportableSerialization() throws Exception {
        SemanticCompatibilityVerifier<NonportableClass> semanticVerifier = new SemanticCompatibilityVerifier<NonportableClass>() {
            @Override
            public void assertSemanticCompatibility(NonportableClass original, NonportableClass replica) {
                Assert.assertEquals(original.getAttrPreviousVersion(), replica.getAttrPreviousVersion());
                Assert.assertNotNull(original.getAttrCurrentVersion());
                Assert.assertNull(replica.getAttrCurrentVersion());
            }
        };

        // A new field was added to the NonportableClass compared to the previous version. This
        // should not break serialization compatibility.
        NonportableClass currentVersion = new NonportableClass();

        SerializabilityTester.testPortableSerialization(NonportableClass.class, currentVersion,
                PREVIOUS_VERSION_JAR_PATH, semanticVerifier);
    }

    private static class InvalidSerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private Object object = new Object();
    }
}
