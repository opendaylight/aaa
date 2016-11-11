/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;

public class KeyStoreUtilisTest {

    private final String fileName = "foo.pem";
    private final String txt = "test save text";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyStoreConstant.KEY_STORE_PATH = "target" + File.separator + "test" + File.separator;
    }

    @Test
    public void testKeyStoreUtils() {
        final String path = KeyStoreConstant.createDir(KeyStoreConstant.KEY_STORE_PATH);
        assertTrue(!path.isEmpty());
        final File dir = new File(path);
        assertTrue(dir.exists());
        //Test save file
        assertTrue(KeyStoreConstant.saveCert(fileName, txt));
        //Test check file
        assertTrue(KeyStoreConstant.checkKeyStoreFile(fileName));
        //Test read file
        final String readTxt = KeyStoreConstant.readFile(fileName);
        assertEquals(txt, readTxt);
    }
}
