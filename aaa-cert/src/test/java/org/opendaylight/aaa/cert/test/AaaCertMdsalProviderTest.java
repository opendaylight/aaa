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
@PrepareForTest(AaaCertMdsalProvider.class)
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
    private static AAAEncryptionServiceImpl encryptionService = mock(AAAEncryptionServiceImpl.class);
    private static String dummyAlias = AaaCertProviderUtilsTest.dummyAlias;
    private static String dummyCert = AaaCertProviderUtilsTest.dummyCert;
    private static KeyStoresDataUtils keyStoresDataUtils = mock(KeyStoresDataUtils.class);
    private final static OdlKeystore odlKeystore = new OdlKeystoreBuilder()
            .setAlias("fooTest")
            .setDname(dName)
            .setName("odlTest.jks")
            .setStorePassword("passWord")
            .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
            .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
            .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
            .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
            .build();
    private final static TrustKeystore trustKeyStore = new TrustKeystoreBuilder()
                .setName("trustTest.jks")
                .setStorePassword("passWord")
                .build();
    private final static SslData sslData = new SslDataBuilder()
            .setKey(new SslDataKey(bundleName))
            .setOdlKeystore(odlKeystore)
            .setTrustKeystore(trustKeyStore)
            .build();

    @PrepareForTest({ MdsalUtils.class })
    @Test
    public void addSslDataKeystoresTest() throws Throwable {
        PowerMockito.whenNew(KeyStoresDataUtils.class).withArguments(Mockito.any(AAAEncryptionServiceImpl.class)).thenReturn(keyStoresDataUtils);
//        PowerMockito.whenNew(ODLKeyTool.class).withNoArguments().thenReturn(odlKeyTool);

        Mockito.when(keyStoresDataUtils.getSslData(dataBroker, bundleName)).thenReturn(sslData);
        PowerMockito.mockStatic(MdsalUtils.class);
        PowerMockito.when(MdsalUtils.put(dataBroker, isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class), isA(SslData.class))).thenReturn(true);
        AaaCertMdsalProvider provider = new AaaCertMdsalProvider(dataBroker, encryptionService);
        SslData result = provider.addSslDataKeystores(
                bundleName, "odlTest.jks", "passWord", "fooTest", dName, "trustTest.jks", "passWord", cipherSuite, "TLSv1.2");
        assertNotNull(result);
//        boolean result = provider.addODLStoreSignedCertificate(bundleName, dummyAlias, dummyCert);
//        assertTrue(result);
    }
//
//    @Nonnull String bundleName, @Nonnull String odlKeystoreName, @Nonnull String odlKeystorePwd,
//    @Nonnull String odlKeystoreAlias, @Nonnull String odlKeystoreDname, @Nonnull String trustKeystoreName,
//    @Nonnull String trustKeystorePwd, @Nonnull String[] cipherSuites, @Nonnull String tlsProtocols);
}
