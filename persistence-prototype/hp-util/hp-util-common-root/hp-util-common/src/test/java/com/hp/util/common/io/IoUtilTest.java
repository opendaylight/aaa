/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class IoUtilTest {

    @Test
    public void testRead() throws IOException {
        byte[] data = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        try (InputStream input = new ByteArrayInputStream(data)) {
            Assert.assertArrayEquals(data, IoUtil.read(input));
        }
    }

    @Test
    public void testReadWithLength() throws IOException {
        byte[] data = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        byte[] expected = new byte[] { (byte) 0, (byte) 1, (byte) 2 };
        try (InputStream input = new ByteArrayInputStream(data)) {
            Assert.assertArrayEquals(expected, IoUtil.read(input, expected.length));
            Assert.assertEquals(data.length - expected.length, input.available());
        }
    }

    @Test
    public void testCopy() throws IOException {
        byte[] data = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        try (InputStream input = new ByteArrayInputStream(data);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            long copiedBytes = IoUtil.copy(input, output);
            Assert.assertArrayEquals(data, output.toByteArray());
            Assert.assertEquals(copiedBytes, data.length);
        }
    }

    @Test
    public void testCopyWithLength() throws IOException {
        byte[] data = new byte[] { (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5 };
        byte[] expected = new byte[] { (byte) 0, (byte) 1, (byte) 2 };
        try (InputStream input = new ByteArrayInputStream(data);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            long copiedBytes = IoUtil.copy(input, output, expected.length);
            Assert.assertArrayEquals(expected, output.toByteArray());
            Assert.assertEquals(copiedBytes, expected.length);
            Assert.assertEquals(data.length - expected.length, input.available());
        }
    }
}
