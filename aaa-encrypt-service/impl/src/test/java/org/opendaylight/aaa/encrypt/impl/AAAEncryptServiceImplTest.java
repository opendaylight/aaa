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

import java.security.ProviderException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
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
                .setCipherTransforms("AES/GCM/NoPadding")
                .setEncryptIterationCount(32768)
                .setEncryptKey("")
                .setEncryptKeyLength(128)
                .setAuthTagLength(128)
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
    void testNetconfPassword() {
        final var ex = assertThrows(ProviderException.class, () -> impl.decrypt("netconf".getBytes()));
        assertEquals("javax.crypto.ShortBufferException: Output buffer invalid", ex.getMessage());
    }

    @Test
    void testNetconfEncodedPassword() {
        final var ex = assertThrows(ProviderException.class,
            () -> impl.decrypt(Base64.getEncoder().encode("netconf".getBytes())));
        assertEquals("javax.crypto.ShortBufferException: Output buffer invalid", ex.getMessage());
    }

    @Test
    void testAdminEncodedPasswordWithoutPadding() {
        final var ex = assertThrows(ProviderException.class,
            () -> impl.decrypt(Base64.getEncoder().encode("netconf".getBytes())));
        assertEquals("javax.crypto.ShortBufferException: Output buffer invalid", ex.getMessage());
    }

    @Test
    void testAdminPasswordWithoutPadding() {
        final var ex = assertThrows(ProviderException.class, () -> impl.decrypt("admin".getBytes()));
        assertEquals("javax.crypto.ShortBufferException: Output buffer invalid", ex.getMessage());
    }

    @Test
    void testDecryptWithValidPasswordLength() {
        final var bytes = new byte[] { 85, -87, 98, 116, -23, -84, 123, -82, 4, -99, -54, 29, 121, -48, -38, -75 };
        final var ex = assertThrows(AEADBadTagException.class, () -> impl.decrypt(bytes));
        assertEquals("Tag mismatch!", ex.getMessage());
    }
}
