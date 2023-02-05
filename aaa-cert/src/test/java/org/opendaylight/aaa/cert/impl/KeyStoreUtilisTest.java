/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.junit.Test;

public class KeyStoreUtilisTest {
    private static final String FILE_NAME = "foo.pem";
    private static final String TXT = "test save text";
    private static final String KEYSTORE_PATH = "configuration" + File.separator + "ssl" + File.separator;

    @Test
    public void testKeyStoreUtils() {
        final String path = KeyStoreConstant.createDir(KEYSTORE_PATH);
        assertTrue(!path.isEmpty());
        final File dir = new File(path);
        assertTrue(dir.exists());
        //Test save file
        assertTrue(KeyStoreConstant.saveCert(FILE_NAME, TXT));
        //Test check file
        assertTrue(KeyStoreConstant.checkKeyStoreFile(FILE_NAME));
        //Test read file
        final String readTxt = KeyStoreConstant.readFile(FILE_NAME);
        assertEquals(TXT, readTxt);
    }
}
