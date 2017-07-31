/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
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
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MdsalUtils.class)
public class KeyStoresDataUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final AAAEncryptionService aaaEncryptionService = mock(AAAEncryptionService.class);
    private final byte[] encryptedByte = new byte[]{1, 2, 3};
    private final DataBroker dataBroker = mock(DataBroker.class);
    private final String alias = "fooTest";
    private final String bundleName = "opendaylight";
    private final String cipherSuiteName = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private final String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private final String encryptedString = "encryptedPassword";
    private final String odlName = "odlTest.jks";
    private final String password = "passWord";
    private final String protocol = "TLSv1.2";
    private final String trustName = "trustTest.jks";
    private final String testPath = "target" + File.separator + "test" + File.separator;


    @Test
    public void keyStoresDataUtilsTest() {
        // Test vars setup
        final OdlKeystore odlKeystore = new OdlKeystoreBuilder()
                .setAlias(alias)
                .setDname(dName)
                .setName(odlName)
                .setStorePassword(password)
                .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
                .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
                .build();

        final TrustKeystore trustKeyStore = new TrustKeystoreBuilder()
                .setName(trustName)
                .setStorePassword(password)
                .build();

        final CipherSuites cipherSuite = new CipherSuitesBuilder()
                .setSuiteName(cipherSuiteName)
                .build();

        final List<CipherSuites> cipherSuites =  new ArrayList<>(Arrays.asList(cipherSuite));
        final SslData sslData = new SslDataBuilder()
                .setOdlKeystore(odlKeystore)
                .setTrustKeystore(trustKeyStore)
                .build();

        final ODLKeyTool odlKeyTool = new ODLKeyTool(testPath);
        final KeyStoresDataUtils keyStoresDataUtils = new KeyStoresDataUtils(aaaEncryptionService);

        // Mock setup
        PowerMockito.mockStatic(MdsalUtils.class);
        Mockito.when(MdsalUtils.put(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class), isA(SslData.class))).thenReturn(true);
        Mockito.when(MdsalUtils.read(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class))).thenReturn(sslData);
        Mockito.when(MdsalUtils.delete(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class))).thenReturn(true);
        Mockito.when(MdsalUtils.merge(isA(DataBroker.class), isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class), isA(SslData.class))).thenReturn(true);
        Mockito.when(aaaEncryptionService.encrypt(isA(byte[].class))).thenReturn(encryptedByte);
        Mockito.when(aaaEncryptionService.encrypt(isA(String.class))).thenReturn(encryptedString);

        // getKeystoresIid
        InstanceIdentifier instanceIdentifierResult = KeyStoresDataUtils.getKeystoresIid();
        assertNotNull(instanceIdentifierResult);

        // getSslIid()
        instanceIdentifierResult = KeyStoresDataUtils.getSslDataIid();
        assertNotNull(instanceIdentifierResult);

        // getSslDataIid(final String bundleName)
        instanceIdentifierResult = KeyStoresDataUtils.getSslDataIid(bundleName);
        assertNotNull(instanceIdentifierResult);

        // updateOdlKeystore
        OdlKeystore odlKeystoreResult = KeyStoresDataUtils.updateOdlKeystore(odlKeystore, encryptedByte);
        assertTrue(Arrays.equals(odlKeystoreResult.getKeystoreFile(), encryptedByte));

        // addSslData
        SslData sslDataResult = keyStoresDataUtils.addSslData(dataBroker, bundleName, odlKeystore, trustKeyStore,
                cipherSuites, protocol);
        assertTrue(sslDataResult.getBundleName() == bundleName);

        // createCipherSuite
        CipherSuites cipherSuiteResult = keyStoresDataUtils.createCipherSuite(cipherSuiteName);
        assertTrue(cipherSuiteResult.getSuiteName() == cipherSuiteName);

        // createOdlKeyStore
        odlKeystoreResult = keyStoresDataUtils.createOdlKeystore(odlName, alias, password, dName, odlKeyTool);
        assertTrue(odlKeystoreResult.getName() == odlName);

        // createTrustKeystore
        TrustKeystore trustKeystoreResult = keyStoresDataUtils.createTrustKeystore(trustName, password, odlKeyTool);
        assertTrue(trustKeystoreResult.getName() == trustName);

        // encryptOdlKeyStore
        sslDataResult = keyStoresDataUtils.getSslData(dataBroker, bundleName);
        assertTrue(sslDataResult.getOdlKeystore() != null && sslDataResult.getTrustKeystore() != null);

        // removeSslData
        Boolean booleanResult = keyStoresDataUtils.removeSslData(dataBroker, bundleName);
        assertTrue(booleanResult);

        // updateSslData
        booleanResult = keyStoresDataUtils.updateSslData(dataBroker, sslData);
        assertTrue(booleanResult);

        // updateSslDataCipherSuites
        booleanResult = keyStoresDataUtils.updateSslDataCipherSuites(dataBroker, sslData, cipherSuites);
        assertTrue(booleanResult);

        // updateSslDataOdlKeystore
        booleanResult = keyStoresDataUtils.updateSslDataOdlKeystore(dataBroker, sslData, odlKeystore);
        assertTrue(booleanResult);

        // updateSslDataTrustKeystore
        booleanResult = keyStoresDataUtils.updateSslDataTrustKeystore(dataBroker, sslData, trustKeyStore);
        assertTrue(booleanResult);
    }
}
