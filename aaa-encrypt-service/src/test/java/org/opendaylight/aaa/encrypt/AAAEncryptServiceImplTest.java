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
import org.junit.runner.RunWith;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.crypto.*")
@PrepareForTest(MdsalUtils.class)
public class AAAEncryptServiceImplTest {

    private AAAEncryptionServiceImpl impl = null;
    private DataBroker dataBroker = mock(DataBroker.class);

    @Before
    public void setup(){
        AaaEncryptServiceConfig module = new AaaEncryptServiceConfigBuilder()
                .setCipherTransforms("AES/CBC/PKCS5Padding")
                .setEncryptIterationCount(32768)
                .setEncryptKey("")
                .setEncryptKeyLength(128)
                .setEncryptMethod("PBKDF2WithHmacSHA1")
                .setEncryptSalt("")
                .setEncryptType("AES")
                .setPasswordLength(12)
                .build();

        PowerMockito.mockStatic(MdsalUtils.class);
        PowerMockito.when(MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION,
                MdsalUtils.getEncryptionSrvConfigIid())).thenReturn(module);
        impl = new AAAEncryptionServiceImpl(module, dataBroker);
    }

    @Test
    public void testShortString(){
        String before = "shortone";
        String encrypt = impl.encrypt(before);
        Assert.assertNotEquals(before, encrypt);
        String after = impl.decrypt(encrypt);
        Assert.assertEquals(before, after);
    }

    @Test
    public void testLongString(){
        String before = "This is a very long string to encrypt for testing 1...2...3";
        String encrypt = impl.encrypt(before);
        Assert.assertNotEquals(before, encrypt);
        String after = impl.decrypt(encrypt);
        Assert.assertEquals(before, after);
    }
}
