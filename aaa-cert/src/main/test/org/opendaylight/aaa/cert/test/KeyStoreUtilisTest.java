/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.KeyStoreUtilis;

public class KeyStoreUtilisTest {

    private String fileName = "foo.pem";
    private String txt = "test save text";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyStoreUtilis.keyStorePath = "target/test/";
    }

    @Test
    public void testCreateDir() {
        String path = KeyStoreUtilis.createDir(KeyStoreUtilis.keyStorePath);
        assertTrue(!path.isEmpty());
        File dir = new File(path);
        assertTrue(dir.exists());
    }

    @Test
    public void testSaveCert() {
        assertTrue(KeyStoreUtilis.saveCert(fileName, txt));
    }

    @Test
    public void testCheckKeyStoreFile() {
        assertTrue(KeyStoreUtilis.checkKeyStoreFile(fileName));
        assertTrue(!KeyStoreUtilis.checkKeyStoreFile("notExist.txt"));
    }

    @Test
    public void testReadFile() {
        String readTxt = KeyStoreUtilis.readFile(fileName);
        assertEquals(txt, readTxt);
    }
}
