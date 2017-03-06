/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import javassist.bytecode.ByteArray;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionServiceImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.AaaEncryptServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MdsalUtils.class)
public class AaaCertMdsalProviderTest {
    private static String[] cipherSuite = {
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
          "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
          "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
          };
    private static String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static DataBroker dataBroker = mock(DataBroker.class);
    private static String bundleName = "opendaylight";
    private static String odlKeystoreName = "odlTest.jks";
    private static String trustKeyStoreName = "trustTest.jks";
    private static String password = "passWord";
    private static String keyStoreAlias = "fooTest";
    private static String protocol = "TLSv1.2";

    private static AAAEncryptionServiceImpl encryptionService = mock(AAAEncryptionServiceImpl.class);
    private static String dummyAlias = AaaCertProviderUtilsTest.dummyAlias;
    private static String dummyCert = AaaCertProviderUtilsTest.dummyCert;
    private static KeyStoresDataUtils keyStoresDataUtils = mock(KeyStoresDataUtils.class);

    @Test
    public void addSslDataKeystoresTest() throws Throwable {
        // setup tests
        OdlKeystore odlKeystore = new OdlKeystoreBuilder()
                .setAlias("fooTest")
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

        SslData sslData = new SslDataBuilder()
                .setOdlKeystore(odlKeystore)
                .setTrustKeystore(trustKeyStore)
                .build();


        // addSslDataKeystoresTest
        PowerMockito.whenNew(KeyStoresDataUtils.class).withArguments(Mockito.any(AAAEncryptionServiceImpl.class)).thenReturn(keyStoresDataUtils);
        PowerMockito.mockStatic(MdsalUtils.class);
        Mockito.when(MdsalUtils.put(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class), isA(SslData.class))).thenReturn(true);
        AaaCertMdsalProvider provider = new AaaCertMdsalProvider(dataBroker, encryptionService);
        SslData sslResult = provider.addSslDataKeystores(
                bundleName, odlKeystoreName, password, keyStoreAlias, dName, trustKeyStoreName, password, cipherSuite, protocol);
        assertNotNull(sslResult);

        sslResult = provider.importSslDataKeystores(bundleName, odlKeystoreName, password, keyStoreAlias, sslResult.getOdlKeystore(), trustKeyStoreName, password,
                sslResult.getTrustKeystore(), cipherSuite, protocol);
//        // addTrustNodeCertificate
        Mockito.when(MdsalUtils.read(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class))).thenReturn(sslData);
        boolean boolResult = provider.addTrustNodeCertificate(bundleName, dummyAlias, dummyAlias);
        assertTrue(boolResult);
    }
}
