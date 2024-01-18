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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.Before;
import org.junit.Test;
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
    public void testShortString() {
        String before = "shortone";
        String encrypt = impl.encrypt(before);
        assertNotEquals(before, encrypt);
        String after = impl.decrypt(encrypt);
        assertEquals(before, after);
    }

    @Test
    public void testLongString() {
        String before = "This is a very long string to encrypt for testing 1...2...3";
        String encrypt = impl.encrypt(before);
        assertNotEquals(before, encrypt);
        String after = impl.decrypt(encrypt);
        assertEquals(before, after);
    }

    @Test
    public void testNetconfEncodedPasswordWithoutPadding() {
        changePadding();
        String password = "bmV0Y29uZgo=";
        String unencrypted = impl.decrypt(password);
        assertEquals(password, unencrypted);
    }

    @Test
    public void testNetconfEncodedPasswordWithPadding() {
        String password = "bmV0Y29uZgo=";
        String unencrypted = impl.decrypt(password);
        assertEquals(password, unencrypted);
    }

    @Test
    public void testNetconfPasswordWithoutPadding() {
        changePadding();
        String password = "netconf";
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        String unencrypted = impl.decrypt(encodedPassword);
        assertEquals(encodedPassword, unencrypted);
    }

    @Test
    public void testNetconfPasswordWithPadding() {
        String password = "netconf";
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        String unencrypted = impl.decrypt(encodedPassword);
        assertEquals(encodedPassword, unencrypted);
    }

    @Test
    public void testAdminEncodedPasswordWithoutPadding() {
        changePadding();
        String password = "YWRtaW4K";
        String unencrypted = impl.decrypt(password);
        assertEquals(password, unencrypted);
    }

    @Test
    public void testAdminEncodedPasswordWithPadding() {
        String password = "YWRtaW4K";
        String unencrypted = impl.decrypt(password);
        assertEquals(password, unencrypted);
    }

    @Test
    public void testAdminPasswordWithoutPadding() {
        changePadding();
        String password = "admin";
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        String unencrypted = impl.decrypt(encodedPassword);
        assertEquals(encodedPassword, unencrypted);
    }

    @Test
    public void testAdminPasswordWithPadding() {
        String password = "admin";
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        String unencrypted = impl.decrypt(encodedPassword);
        assertEquals(encodedPassword, unencrypted);
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
}
