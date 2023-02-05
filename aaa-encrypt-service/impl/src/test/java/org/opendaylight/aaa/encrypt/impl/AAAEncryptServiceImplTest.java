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
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfigBuilder;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
@Deprecated
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AAAEncryptServiceImplTest {
    @Mock
    private DataBroker dataBroker;
    @Mock
    private ReadWriteTransaction tx;

    private AAAEncryptionServiceImpl impl;

    @Before
    public void setup() {
        final var module = new AaaEncryptServiceConfigBuilder()
                .setCipherTransforms("AES/CBC/PKCS5Padding").setEncryptIterationCount(32768).setEncryptKey("")
                .setEncryptKeyLength(128).setEncryptMethod("PBKDF2WithHmacSHA1").setEncryptSalt("")
                .setEncryptType("AES").setPasswordLength(12).build();

        doReturn(tx).when(dataBroker).newReadWriteTransaction();
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(tx)
            .exists(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(AaaEncryptServiceConfig.class));

        impl = new AAAEncryptionServiceImpl(new AAAEncryptionServiceConfigurator(dataBroker, module));
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
}
