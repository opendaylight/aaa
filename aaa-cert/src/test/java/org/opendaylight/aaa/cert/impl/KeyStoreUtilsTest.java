/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class KeyStoreUtilsTest {
    private static final String FILE_NAME = "foo.pem";
    private static final String TXT = "test save text";
    private static final Path KEYSTORE_PATH = Path.of("configuration", "ssl");

    @Test
    public void testKeyStoreUtils() {
        final var path = KeyStoreConstant.createDir(KEYSTORE_PATH.toString());
        assertNotNull(path);
        assertNotEquals("", path);

        assertTrue(Files.exists(Path.of(path)));

        //Test save file
        assertTrue(KeyStoreConstant.saveCert(FILE_NAME, TXT));
        //Test check file
        assertTrue(KeyStoreConstant.checkKeyStoreFile(FILE_NAME));
        //Test read file
        final var readTxt = KeyStoreConstant.readFile(FILE_NAME);
        assertEquals(TXT, readTxt);
    }
}
