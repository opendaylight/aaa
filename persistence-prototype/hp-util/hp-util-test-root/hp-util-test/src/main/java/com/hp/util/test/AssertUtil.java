/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.InputStream;

import org.junit.Assert;

/**
 * Assert utility methods.
 * 
 * @author Fabiel Zuniga
 */
public final class AssertUtil {

    private AssertUtil() {

    }

    /*
     * All methods in this class could return boolean instead of executing asserts internally,
     * however returning a boolean would make detecting the failure's root cause more complicated.
     * Normally the methods provided by this class assert complex conditions which usually involve
     * multiple comparisons (internal asserts). When the test that is using this class fails, the
     * error is more precise: the line number will tell which condition failed for example along
     * with the expected and actual values.
     */

    /**
     * Asserts the data read from the given input streams are equals.
     *
     * @param expected input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @param actual input stream to read. This stream will remain opened after the invocation of
     *            this method.
     */
    public static void assertEquals(InputStream expected, InputStream actual) {
        assertEquals(expected, actual, null);
    }

    /**
     * Asserts the data read from the given input streams are equals. If any of the input streams is
     * {@code null} the test fails.
     * 
     * @param expected input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @param actual input stream to read. This stream will remain opened after the invocation of
     *            this method.
     * @param message message in case of failure
     */
    public static void assertEquals(InputStream expected, InputStream actual, String message) {
        Assert.assertNotNull(message, expected);
        Assert.assertNotNull(message, actual);

        /*
        // This implementation took 18 seconds with a 7.36 MB file. It reads one byte at a time.
        int expectedByte = -1;
        int actualByte = -1;
        do {
            try {
                expectedByte = expected.read();
                actualByte = actual.read();
                Assert.assertEquals(message, expectedByte, actualByte);
            }
            catch (Exception e) {
                e.printStackTrace();
                String failureMessage = message != null ? message + " - " + e.getMessage() : e.getMessage();
                Assert.fail(failureMessage);
            }
        }
        while (expectedByte != -1);
        */

        // This implementation took 140 milliseconds with a 7.36 MB file.

        byte[] expectedBuffer = new byte[8192];
        byte[] actualBuffer = new byte[8192];
        long expectedBytesCount = 0;
        long actualBytesCount = 0;
        byte expectedByte = 0;
        long actualByte = 0;

        int expectedReadBytes = 0;
        int actualReadBytes = 0;
        int expectedBufferIndex = 0;
        int actualBufferIndex = 0;

        try {
            do {
                // Gets next expected byte

                if (expectedBufferIndex == expectedReadBytes) {
                    expectedReadBytes = expected.read(expectedBuffer);
                    expectedBufferIndex = 0;
                }

                if (expectedReadBytes > 0) {
                    expectedByte = expectedBuffer[expectedBufferIndex++];
                    expectedBytesCount++;
                }

                // Gets next actual byte

                if (actualBufferIndex == actualReadBytes) {
                    actualReadBytes = actual.read(actualBuffer);
                    actualBufferIndex = 0;
                }

                if (actualReadBytes > 0) {
                    actualByte = actualBuffer[actualBufferIndex++];
                    actualBytesCount++;
                }

                // Compares

                if (expectedReadBytes > 0 && actualReadBytes > 0) {
                    Assert.assertEquals(message, expectedByte, actualByte);
                } else {
                    if(expectedReadBytes != actualReadBytes) {
                        Assert.fail(message);
                    }
                }
            }
            while (expectedReadBytes > 0 && actualReadBytes > 0);
        }
        catch (Exception e) {
            e.printStackTrace();
            String failureMessage = message != null ? message + " - " + e.getMessage() : e.getMessage();
            Assert.fail(failureMessage);
        }

        Assert.assertEquals(message, expectedBytesCount, actualBytesCount);
    }

    /**
     * Asserts that {@code str} contains {@code infix}.
     * 
     * @param infix expected content
     * @param str string to assert
     */
    public static void assertContains(String infix, String str) {
        Assert.assertNotNull(infix);
        Assert.assertNotNull(str);
        Assert.assertTrue("Expected <" + infix + "> contained in <" + str + ">", str.contains(infix));
    }

    /**
     * Asserts that {@code str} starts with {@code prefix}.
     * 
     * @param prefix expected prefix
     * @param str string to assert
     */
    public static void assertStartsWith(String prefix, String str) {
        Assert.assertNotNull(prefix);
        Assert.assertNotNull(str);
        Assert.assertTrue("Expected <" + prefix + "> as prefix of <" + str + ">", str.startsWith(prefix));
    }

    /**
     * Asserts that {@code str} ends with {@code suffix}.
     * 
     * @param suffix expected prefix
     * @param str string to assert
     */
    public static void assertEndsWith(String suffix, String str) {
        Assert.assertNotNull(suffix);
        Assert.assertNotNull(str);
        Assert.assertTrue("Expected <" + suffix + "> as suffix of <" + str + ">", str.endsWith(suffix));
    }
}
