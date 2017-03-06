/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import org.junit.Test;
import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;
import org.opendaylight.aaa.encrypt.AAAEncryptionServiceImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AaaCertMdsalProviderTest {
    private static DataBroker dataBroker = mock(DataBroker.class);
    private AaaEncryptServiceConfig module = mock(AaaEncryptServiceConfig.class);
    private static String dummyAlias = AaaCertProviderUtilsTest.dummyAlias;
    private static String dummyCert = AaaCertProviderUtilsTest.dummyCert;

    @Test
    public void addODLStoreSignedCertificateTest() {
        AaaCertMdsalProvider provider = new AaaCertMdsalProvider(dataBroker, new AAAEncryptionServiceImpl(module));
        boolean result = provider.addODLStoreSignedCertificate("opendaylight", dummyAlias, dummyCert);
        assertTrue(result);
    }
}
