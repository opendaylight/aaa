/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptServiceImplTest {

    private AAAEncryptionServiceImpl impl = null;
    private AaaEncryptServiceConfig module = mock(AaaEncryptServiceConfig.class);

    private static final String ENCRYPT_KEY = "";
    private static final String ENCRYPT_SALT = "";
    private static final String ENCRYPTION_METHOD = "PBKDF2WithHmacSHA1";
    private static final String ENCRYPTION_TYPE = "AES";
    private static final int ENCRYPTION_ITERATION_COUNT = 32768;
    private static final int ENCRYPTION_KEY_LENGTH = 128;
    private static final String CIPHER_TRANSFORMS = "AES/CBC/PKCS5Padding";

    @Before
    public void setup(){

        Mockito.when(module.getEncryptSalt()).thenReturn(ENCRYPT_SALT);
        Mockito.when(module.getEncryptKey()).thenReturn(ENCRYPT_KEY);
        Mockito.when(module.getEncryptMethod()).thenReturn(ENCRYPTION_METHOD);
        Mockito.when(module.getEncryptType()).thenReturn(ENCRYPTION_TYPE);
        Mockito.when(module.getEncryptIterationCount()).thenReturn(ENCRYPTION_ITERATION_COUNT);
        Mockito.when(module.getEncryptKeyLength()).thenReturn(ENCRYPTION_KEY_LENGTH);
        Mockito.when(module.getCipherTransforms()).thenReturn(CIPHER_TRANSFORMS);
        impl = new AAAEncryptionServiceImpl(module);
    }

    @Test
    public void testShortString(){
        String before = "shortone";
        String encrypt = impl.encrypt(before);
        Assert.assertNotEquals(before,encrypt);
        String after = impl.decrypt(encrypt);
        Assert.assertEquals(before,after);
    }

    @Test
    public void testLongString(){
        String before = "This is a very long string to encrypt for testing 1...2...3";
        String encrypt = impl.encrypt(before);
        Assert.assertNotEquals(before,encrypt);
        String after = impl.decrypt(encrypt);
        Assert.assertEquals(before,after);
    }
}
