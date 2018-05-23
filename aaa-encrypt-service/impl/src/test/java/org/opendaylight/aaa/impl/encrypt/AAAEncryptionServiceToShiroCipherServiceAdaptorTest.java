/*
 * Copyright (c) 2018 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.encrypt;

import static org.junit.Assert.assertEquals;

import org.apache.shiro.codec.CodecSupport;
import org.junit.Test;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;

public class AAAEncryptionServiceToShiroCipherServiceAdaptorTest {

    final AAAEncryptionService aaaEncryptionService = new AAAEncryptionServiceToShiroCipherServiceAdaptor();

    @Test
    public void testEncryptDecryptStrings() {
        final String clearText = "this is my original string";
        final String encrypted = aaaEncryptionService.encrypt(clearText);
        final String decrypted = aaaEncryptionService.decrypt(encrypted);
        assertEquals(clearText, decrypted);
    }

    @Test
    public void testEncryptDecryptBytes() {
        final String clearText = "this is my original string4567";
        final byte [] clearBytes = clearText.getBytes();
        final byte [] encryptedBytes = aaaEncryptionService.encrypt(clearBytes);
        final byte [] decryptedBytes = aaaEncryptionService.decrypt(encryptedBytes);
        assertEquals(clearText, CodecSupport.toString(decryptedBytes));
    }
}
