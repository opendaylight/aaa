/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.encrypt.exception.DecryptionException;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
@Deprecated
public class AAAEncryptServiceImplTest {
    private AAAEncryptionServiceImpl impl;

    @Before
    public void setup() {
        impl = new AAAEncryptionServiceImpl(new EncryptServiceConfigImpl(
            OSGiEncryptionServiceConfigurator.generateConfig(new AaaEncryptServiceConfigBuilder()
                .setCipherTransforms("AES/CBC/PKCS5Padding")
                .setEncryptIterationCount(32768)
                .setEncryptKey("")
                .setEncryptKeyLength(128)
                .setEncryptMethod("PBKDF2WithHmacSHA1")
                .setEncryptSalt("")
                .setEncryptType("AES")
                .setPasswordLength(12)
                .build())));
    }

    @Test
    public void testShortString() throws Exception {
        String before = "shortone";
        String encrypt = impl.encrypt(before);
        assertNotEquals(before, encrypt);
        String after = impl.decrypt(encrypt);
        assertEquals(before, after);
    }

    @Test
    public void testLongString() throws Exception {
        String before = "This is a very long string to encrypt for testing 1...2...3";
        String encrypt = impl.encrypt(before);
        assertNotEquals(before, encrypt);
        String after = impl.decrypt(encrypt);
        assertEquals(before, after);
    }

    @Test
    public void testDecryptWithIllegalArgumentException() {
        assertThrows(DecryptionException.class, () -> impl.decrypt("admin"));
    }

    @Test
    public void testDecryptWithIllegalBlockSizeException() {
        assertThrows(DecryptionException.class, () -> impl.decrypt("adminadmin"));
    }

    @Test
    public void testDecryptWithBadPaddingException() {
        assertThrows(DecryptionException.class, () -> impl.decrypt("ValidOmse64EncodedDatQ=="));
    }

}
