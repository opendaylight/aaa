/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
@Deprecated
class AAAEncryptServiceImplTest {
    private AAAEncryptionServiceImpl impl;

    @BeforeEach
    void setup() {
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

    private void changePadding() {
        impl = new AAAEncryptionServiceImpl(new EncryptServiceConfigImpl(
            OSGiEncryptionServiceConfigurator.generateConfig(new AaaEncryptServiceConfigBuilder()
                .setCipherTransforms("AES/CBC/NoPadding")
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
    void testShortString() throws Exception {
        final var before = "shortone".getBytes();
        final var encrypt = impl.encrypt(before);
        assertFalse(Arrays.equals(before, encrypt));
        assertArrayEquals(before, impl.decrypt(encrypt));
    }

    @Test
    void testLongString() throws Exception {
        final var before = "This is a very long string to encrypt for testing 1...2...3".getBytes();
        final var encrypt = impl.encrypt(before);
        assertFalse(Arrays.equals(before, encrypt));
        assertArrayEquals(before, impl.decrypt(encrypt));
    }

    @Test
    void testNetconfEncodedPasswordWithoutPadding() {
        changePadding();
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("netconf\n".getBytes()));
        assertEquals("Input length not multiple of 16 bytes", ex.getMessage());
    }

    @Test
    void testNetconfEncodedPasswordWithPadding() {
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("netconf\n".getBytes()));
        assertEquals("Input length must be multiple of 16 when decrypting with padded cipher", ex.getMessage());
    }

    @Test
    void testNetconfPasswordWithoutPadding() {
        changePadding();
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("netconf".getBytes()));
        assertEquals("Input length not multiple of 16 bytes", ex.getMessage());
    }

    @Test
    void testNetconfPasswordWithPadding() {
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("netconf".getBytes()));
        assertEquals("Input length must be multiple of 16 when decrypting with padded cipher", ex.getMessage());
    }

    @Test
    void testAdminEncodedPasswordWithoutPadding() {
        changePadding();
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("admin\n".getBytes()));
        assertEquals("Input length not multiple of 16 bytes", ex.getMessage());
    }

    @Test
    void testAdminEncodedPasswordWithPadding() {
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("admin\n".getBytes()));
        assertEquals("Input length must be multiple of 16 when decrypting with padded cipher", ex.getMessage());
    }

    @Test
    void testAdminPasswordWithoutPadding() {
        changePadding();
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("admin".getBytes()));
        assertEquals("Input length not multiple of 16 bytes", ex.getMessage());
    }

    @Test
    void testAdminPasswordWithPadding() {
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("admin".getBytes()));
        assertEquals("Input length must be multiple of 16 when decrypting with padded cipher", ex.getMessage());
    }

    @Test
    void testDecryptWithIllegalBlockSizeException() {
        final var ex = assertThrows(IllegalBlockSizeException.class, () -> impl.decrypt("adminadmin".getBytes()));
        assertEquals("Input length must be multiple of 16 when decrypting with padded cipher", ex.getMessage());
    }

    @Test
    void testDecryptWithBadPaddingException() {
        final var bytes = new byte[] { 85, -87, 98, 116, -23, -84, 123, -82, 4, -99, -54, 29, 121, -48, -38, -75 };
        final var ex = assertThrows(BadPaddingException.class, () -> impl.decrypt(bytes));
        assertEquals(
            "Given final block not properly padded. Such issues can arise if a bad key is used during decryption.",
            ex.getMessage());
    }

    @Test
    void testDecryptionAfterExceptionThrow() throws Exception {
        // Verify successful encryption/decryption.
        final var before = "shortone".getBytes();
        final var encrypt = impl.encrypt(before);
        assertFalse(Arrays.equals(before, encrypt));
        assertArrayEquals(before, impl.decrypt(encrypt));

        // Create a new AAAEncryptionServiceImpl instance with a changed encryptKey and encryptSalt to verify that
        // it cannot decrypt previously encrypted data and throws BadPaddingException exception.
        setup();
        assertThrows(BadPaddingException.class, () -> impl.decrypt(encrypt));

        // Verify that Cipher decrypt work after previous failure.
        final var encrypt2 = impl.encrypt(before);
        assertFalse(Arrays.equals(before, encrypt2));
        assertArrayEquals(before, impl.decrypt(encrypt2));
    }
}
