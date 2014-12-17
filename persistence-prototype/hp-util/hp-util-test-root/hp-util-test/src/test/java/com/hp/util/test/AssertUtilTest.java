/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.ThrowableTester.Instruction;
import com.hp.util.test.ThrowableTester.Validator;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class AssertUtilTest {

    @Test
    public void testAssertEqualsStreams() throws IOException {
        Path testFilePath = FileSystems.getDefault().getPath("test-file");

        try (InputStream expected = ClassLoader.getSystemResourceAsStream(testFilePath.toFile().getPath());
                InputStream actual = ClassLoader.getSystemResourceAsStream(testFilePath.toFile().getPath())) {
            AssertUtil.assertEquals(expected, actual);
        }

        try (InputStream expected = ClassLoader.getSystemResourceAsStream(testFilePath.toFile().getPath());
                InputStream actual = ClassLoader.getSystemResourceAsStream(testFilePath.toFile().getPath())) {
            AssertUtil.assertEquals(expected, actual, "Error message in case of failure");
        }
    }

    @Test
    public void testAssertEqualsStreamsFailure1() throws IOException {
        Path testFilePath = FileSystems.getDefault().getPath("test-file");
        Path differentTestFilePath = FileSystems.getDefault().getPath("portable-serialization-test",
                "previous-version.jar");

        try (InputStream expected = ClassLoader.getSystemResourceAsStream(testFilePath.toFile().getPath());
                InputStream actual = ClassLoader.getSystemResourceAsStream(differentTestFilePath.toFile().getPath())) {
            ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
                @Override
                public void execute() throws Throwable {
                    AssertUtil.assertEquals(expected, actual);
                }
            });
        }
    }

    @Test
    public void testAssertEqualsStreamsFailure2() throws IOException {
        Path testFilePath = FileSystems.getDefault().getPath("test-file");
        Path differentTestFilePath = FileSystems.getDefault().getPath("portable-serialization-test",
                "previous-version.jar");

        final String errorMessage = "Custom message to tell the streams are not equal";

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                AssertUtil.assertStartsWith(errorMessage, error.getMessage());
            }
        };

        try (InputStream expected = ClassLoader.getSystemResourceAsStream(testFilePath.toFile().getPath());
                InputStream actual = ClassLoader.getSystemResourceAsStream(differentTestFilePath.toFile().getPath())) {
            ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
                @Override
                public void execute() throws Throwable {
                    AssertUtil.assertEquals(expected, actual, errorMessage);
                }
            }, errorValidator);
        }
    }

    @Test
    public void testAssertContains() {
        String str = "Hello World";
        String infix = "lo Wo";
        AssertUtil.assertContains(infix, str);
    }

    @Test
    public void testAssertContainsFail() {
        final String validString = "Hello World";
        final String validInfix = "lo Wo";
        final String invalidStringNull = null;
        final String invalidInfix = "invalid-infix";
        final String invalidInfixNull = null;

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertContains(validInfix, invalidStringNull);
            }
        });

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertContains(invalidInfixNull, validString);
            }
        });

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectdError = "Expected <" + invalidInfix + "> contained in <" + validString + ">";
                Assert.assertEquals(expectdError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertContains(invalidInfix, validString);
            }
        }, errorValidator);
    }

    @Test
    public void testAssertStartsWith() {
        String str = "Hello World";
        String prefix = "Hello";
        AssertUtil.assertStartsWith(prefix, str);
    }

    @Test
    public void testAssertStartsWithFail() {
        final String validString = "Hello World";
        final String validPrefix = "Hello";
        final String invalidStringNull = null;
        final String invalidPrefix = "invalid-infix";
        final String invalidPrefixNull = null;

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertStartsWith(validPrefix, invalidStringNull);
            }
        });

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertStartsWith(invalidPrefixNull, validString);
            }
        });

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectdError = "Expected <" + invalidPrefix + "> as prefix of <" + validString + ">";
                Assert.assertEquals(expectdError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertStartsWith(invalidPrefix, validString);
            }
        }, errorValidator);
    }

    @Test
    public void testAssertEndsWith() {
        String str = "Hello World";
        String suffix = "World";
        AssertUtil.assertEndsWith(suffix, str);
    }

    @Test
    public void testAssertEndsWithFail() {
        final String validString = "Hello World";
        final String validSuffix = "World";
        final String invalidStringNull = null;
        final String invalidSuffix = "invalid-infix";
        final String invalidSuffixNull = null;

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertEndsWith(validSuffix, invalidStringNull);
            }
        });

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertEndsWith(invalidSuffixNull, validString);
            }
        });

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectdError = "Expected <" + invalidSuffix + "> as suffix of <" + validString + ">";
                Assert.assertEquals(expectdError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                AssertUtil.assertEndsWith(invalidSuffix, validString);
            }
        }, errorValidator);
    }
}
