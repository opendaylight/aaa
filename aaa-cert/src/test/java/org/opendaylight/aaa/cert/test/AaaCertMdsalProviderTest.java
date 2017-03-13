/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MdsalUtils.class)
public class AaaCertMdsalProviderTest {
    private static final String alias = TestUtils.dummyAlias;
    private static final String certificate = TestUtils.dummyCert;
    private static final String bundleName = "opendaylight";
    private static final String cipherSuiteName = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private static final String[] cipherSuites = {cipherSuiteName};
    private static final String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static final String encryptedString = "encryptedPassword";
    private static final String odlName = "odlTest.jks";
    private static final String password = "passWord";
    private static final String protocol = "SSLv2Hello,TLSv1.1,TLSv1.2";
    private static final String trustName = "trustTest.jks";
    private static final String testPath = "target" + File.separator + "test" + File.separator;
    private static final DataBroker dataBroker = mock(DataBroker.class);
    private static final AAAEncryptionService encryptionSrv = mock(AAAEncryptionService.class);
    private KeyStoresDataUtils keyStoresDataUtils;
    private static SslData sslData;
    private static ODLKeyTool odlKeyTool;
    private static AaaCertMdsalProvider aaaCertMdsalProvider;

    @BeforeClass
    public static void setUpBeforeClass() {
        // setup tests
        OdlKeystore odlKeystore = new OdlKeystoreBuilder()
                .setAlias(alias)
                .setDname(dName)
                .setName("odlTest.jks")
                .setStorePassword("passWord")
                .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
                .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
                .build();

        TrustKeystore trustKeyStore = new TrustKeystoreBuilder()
                .setName("trustTest.jks")
                .setStorePassword("passWord")
                .build();

        sslData = new SslDataBuilder()
                .setOdlKeystore(odlKeystore)
                .setTrustKeystore(trustKeyStore)
                .build();

        // Mock setup
        PowerMockito.mockStatic(MdsalUtils.class);
        Mockito.when(MdsalUtils.put(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class), isA(SslData.class))).thenReturn(true);
        Mockito.when(MdsalUtils.read(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class))).thenReturn(sslData);

        aaaCertMdsalProvider = new AaaCertMdsalProvider(dataBroker, encryptionSrv);
        assertNotNull(aaaCertMdsalProvider);
    }

    @Test
    public void addSslDataKeystoresTest() {
        SslData result = aaaCertMdsalProvider.addSslDataKeystores(bundleName, odlName, password,
        alias, dName, trustName, password, cipherSuites , protocol);
        assertTrue(result.getOdlKeystore().getDname() == dName);
        assertTrue(result.getOdlKeystore().getName() == odlName);
        assertTrue(result.getTrustKeystore().getName() == trustName);
    }

    @Test
    public void addTrustNodeCertificateTest() {
        Boolean result = aaaCertMdsalProvider.addTrustNodeCertificate(bundleName, alias, certificate);
        assertTrue(result);
    }

}
