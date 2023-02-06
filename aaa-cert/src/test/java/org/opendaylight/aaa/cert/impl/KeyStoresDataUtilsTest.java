/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class KeyStoresDataUtilsTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final AAAEncryptionService AAA_ENCRYPTION_SERVICE = mock(AAAEncryptionService.class);
    private static final byte[] ENCRYPTED_BYTE = new byte[] { 1, 2, 3 };
    private static final String ALIAS = "fooTest";
    private static final String BUNDLE_NAME = "opendaylight";
    private static final String CIPHER_SUITE_NAME = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private static final String D_NAME = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static final String ENCRYPTED_STRING = "encryptedPassword";
    private static final String ODL_NAME = "odlTest.jks";
    private static final String PASSWORD = "passWord";
    private static final String PROTOCOL = "TLSv1.2";
    private static final String TRUST_NAME = "trustTest.jks";
    private static final String TEST_PATH = "target" + File.separator + "test" + File.separator;

    private final DataBroker dataBroker = mock(DataBroker.class);

    @Test
    public void keyStoresDataUtilsTest() {
        // Test vars setup
        final OdlKeystore odlKeystore = new OdlKeystoreBuilder().setAlias(ALIAS).setDname(D_NAME).setName(ODL_NAME)
                .setStorePassword(PASSWORD).setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG).setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG).build();

        final TrustKeystore trustKeyStore = new TrustKeystoreBuilder().setName(TRUST_NAME).setStorePassword(PASSWORD)
                .build();

        final CipherSuites cipherSuite = new CipherSuitesBuilder().setSuiteName(CIPHER_SUITE_NAME).build();

        final List<CipherSuites> cipherSuites = new ArrayList<>(Arrays.asList(cipherSuite));
        final SslData sslData = new SslDataBuilder().setOdlKeystore(odlKeystore).setTrustKeystore(trustKeyStore)
                .setBundleName(BUNDLE_NAME).build();

        final ODLKeyTool odlKeyTool = new ODLKeyTool(TEST_PATH);
        final KeyStoresDataUtils keyStoresDataUtils = new KeyStoresDataUtils(AAA_ENCRYPTION_SERVICE);

        // Mock setup
        final WriteTransaction wtx = mock(WriteTransaction.class);
        doReturn(CommitInfo.emptyFluentFuture()).when(wtx).commit();
        doReturn(wtx).when(dataBroker).newWriteOnlyTransaction();

        final ReadTransaction rtx = mock(ReadTransaction.class);
        doReturn(FluentFutures.immediateFluentFuture(Optional.of(sslData))).when(rtx).read(
            any(LogicalDatastoreType.class), any(InstanceIdentifier.class));
        doReturn(rtx).when(dataBroker).newReadOnlyTransaction();

        doReturn(ENCRYPTED_STRING).when(AAA_ENCRYPTION_SERVICE).encrypt(isA(String.class));

        // getKeystoresIid
        InstanceIdentifier instanceIdentifierResult = KeyStoresDataUtils.getKeystoresIid();
        assertNotNull(instanceIdentifierResult);

        // getSslIid()
        instanceIdentifierResult = KeyStoresDataUtils.getSslDataIid();
        assertNotNull(instanceIdentifierResult);

        // getSslDataIid(final String bundleName)
        instanceIdentifierResult = KeyStoresDataUtils.getSslDataIid(BUNDLE_NAME);
        assertNotNull(instanceIdentifierResult);

        // updateOdlKeystore
        OdlKeystore odlKeystoreResult = KeyStoresDataUtils.updateOdlKeystore(odlKeystore, ENCRYPTED_BYTE);
        assertTrue(Arrays.equals(odlKeystoreResult.getKeystoreFile(), ENCRYPTED_BYTE));

        // addSslData
        SslData sslDataResult = keyStoresDataUtils.addSslData(dataBroker, BUNDLE_NAME, odlKeystore, trustKeyStore,
                cipherSuites, PROTOCOL);
        assertTrue(sslDataResult.getBundleName() == BUNDLE_NAME);

        // createCipherSuite
        CipherSuites cipherSuiteResult = keyStoresDataUtils.createCipherSuite(CIPHER_SUITE_NAME);
        assertTrue(cipherSuiteResult.getSuiteName() == CIPHER_SUITE_NAME);

        // createOdlKeyStore
        odlKeystoreResult = keyStoresDataUtils.createOdlKeystore(ODL_NAME, ALIAS, PASSWORD, D_NAME, odlKeyTool);
        assertTrue(odlKeystoreResult.getName() == ODL_NAME);

        // createTrustKeystore
        TrustKeystore trustKeystoreResult = keyStoresDataUtils.createTrustKeystore(TRUST_NAME, PASSWORD, odlKeyTool);
        assertTrue(trustKeystoreResult.getName() == TRUST_NAME);

        // encryptOdlKeyStore
        sslDataResult = keyStoresDataUtils.getSslData(dataBroker, BUNDLE_NAME);
        assertTrue(sslDataResult.getOdlKeystore() != null && sslDataResult.getTrustKeystore() != null);

        // removeSslData
        Boolean booleanResult = keyStoresDataUtils.removeSslData(dataBroker, BUNDLE_NAME);
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
