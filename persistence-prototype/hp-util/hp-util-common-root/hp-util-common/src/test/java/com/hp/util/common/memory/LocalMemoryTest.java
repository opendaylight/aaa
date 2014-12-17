/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.memory;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.Memory;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class LocalMemoryTest {

    @Test
    public void testConstruction() {
        Memory<String> sharedMemory = new LocalMemory<String>();
        Assert.assertNull(sharedMemory.read());

        sharedMemory = new LocalMemory<String>("Hello World");
        Assert.assertEquals("Hello World", sharedMemory.read());
    }

    @Test
    public void testWriteRead() {
        Memory<String> sharedMemory = new LocalMemory<String>();
        sharedMemory.write("Hello World");
        Assert.assertEquals("Hello World", sharedMemory.read());
    }
}
