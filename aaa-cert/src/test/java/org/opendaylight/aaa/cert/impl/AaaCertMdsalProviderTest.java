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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.aaa.cert.impl.TestUtils.mockDataBroker;

import java.io.File;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;

public class AaaCertMdsalProviderTest {
    private static final String ALIAS = TestUtils.DUMMY_ALIAS;
    private static final String BUNDLE_NAME = "opendaylight";
    private static final String CERTIFICATE = TestUtils.DUMMY_CERT;
    private static final String CIPHER_SUITE_NAME = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private static final String[] CIPHER_SUITES_ARRAY = { CIPHER_SUITE_NAME };
    private static final String D_NAME = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static final String ODL_NAME = "odlTest.jks";
    private static final String PASSWORD = "passWord";
    private static final String PROTOCOL = "SSLv2Hello";
    private static final String TEST_PATH = "target" + File.separator + "test" + File.separator;
    private static final String TRUST_NAME = "trustTest.jks";
    private static AAAEncryptionService aaaEncryptionService;
    private static AaaCertMdsalProvider aaaCertMdsalProvider;
    private static SslData signedSslData;
    private static SslData unsignedSslData;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Setup tests
        final AAAEncryptionService aaaEncryptionServiceInit = mock(AAAEncryptionService.class);
        final ODLKeyTool odlKeyTool = new ODLKeyTool(TEST_PATH);
        final KeyStoresDataUtils keyStoresDataUtils = new KeyStoresDataUtils(aaaEncryptionServiceInit);

        final OdlKeystore signedOdlKeystore = keyStoresDataUtils.createOdlKeystore(ODL_NAME, ALIAS, PASSWORD, D_NAME,
                KeyStoreConstant.DEFAULT_SIGN_ALG, KeyStoreConstant.DEFAULT_KEY_ALG, KeyStoreConstant.DEFAULT_VALIDITY,
                KeyStoreConstant.DEFAULT_KEY_SIZE, odlKeyTool);
        final TrustKeystore signedTrustKeyStore = keyStoresDataUtils.createTrustKeystore(TRUST_NAME, PASSWORD,
                signedOdlKeystore.getKeystoreFile());
        final TrustKeystore unsignedTrustKeyStore = keyStoresDataUtils.createTrustKeystore(TRUST_NAME, PASSWORD,
                odlKeyTool);

        final CipherSuites cipherSuite = new CipherSuitesBuilder().setSuiteName(CIPHER_SUITE_NAME).build();

        final List<CipherSuites> cipherSuites = new ArrayList<>(Arrays.asList(cipherSuite));

        signedSslData = new SslDataBuilder().setCipherSuites(cipherSuites).setOdlKeystore(signedOdlKeystore)
                .setTrustKeystore(signedTrustKeyStore).setTlsProtocols(PROTOCOL).setBundleName(BUNDLE_NAME).build();

        final OdlKeystore unsignedOdlKeystore = new OdlKeystoreBuilder().setAlias(ALIAS).setDname(D_NAME)
                .setName(ODL_NAME).setStorePassword(PASSWORD).setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG).setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG).setKeystoreFile(unsignedTrustKeyStore.getKeystoreFile())
                .build();

        unsignedSslData = new SslDataBuilder().setOdlKeystore(unsignedOdlKeystore)
                .setTrustKeystore(unsignedTrustKeyStore).setBundleName(BUNDLE_NAME).build();

        when(aaaEncryptionServiceInit.decrypt(unsignedTrustKeyStore.getKeystoreFile()))
                .thenReturn(unsignedTrustKeyStore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(signedOdlKeystore.getKeystoreFile()))
                .thenReturn(signedOdlKeystore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(isA(String.class))).thenReturn(PASSWORD);
        aaaEncryptionService = aaaEncryptionServiceInit;

        // Create class
        aaaCertMdsalProvider = new AaaCertMdsalProvider(mockDataBroker(signedSslData), aaaEncryptionService);
        assertNotNull(aaaCertMdsalProvider);
    }

    @Test
    public void addSslDataKeystoresTest() throws Exception {
        SslData result = new AaaCertMdsalProvider(mockDataBroker(signedSslData), aaaEncryptionService)
                .addSslDataKeystores(BUNDLE_NAME, ODL_NAME, PASSWORD, ALIAS, D_NAME, TRUST_NAME, PASSWORD,
                        CIPHER_SUITES_ARRAY, PROTOCOL);
        assertTrue(result.getOdlKeystore().getDname() == D_NAME);
        assertTrue(result.getOdlKeystore().getName() == ODL_NAME);
        assertTrue(result.getTrustKeystore().getName() == TRUST_NAME);
    }

    @Test
    public void genODLKeyStoreCertificateReqTest() {
        String result = aaaCertMdsalProvider.genODLKeyStoreCertificateReq(BUNDLE_NAME, true);
        assertTrue(result != null && !result.isEmpty());
        assertTrue(result.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
        result = aaaCertMdsalProvider.genODLKeyStoreCertificateReq(BUNDLE_NAME, false);
        assertTrue(!result.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
    }

    @Test
    public void getCipherSuitesTest() {
        String[] result = aaaCertMdsalProvider.getCipherSuites(BUNDLE_NAME);
        assertTrue(Arrays.equals(result, CIPHER_SUITES_ARRAY));
    }

    @Test
    public void getODLKeyStoreTest() {
        KeyStore result = aaaCertMdsalProvider.getODLKeyStore(BUNDLE_NAME);
        assertNotNull(result);
    }

    @Test
    public void getODLStoreCertificateTest() {
        String result = aaaCertMdsalProvider.getODLStoreCertificate(BUNDLE_NAME, true);
        assertTrue(result != null && !result.isEmpty());
        assertTrue(result.contains(KeyStoreConstant.END_CERTIFICATE));
        result = aaaCertMdsalProvider.getODLStoreCertificate(BUNDLE_NAME, false);
        assertTrue(!result.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void getSslDataTest() {
        SslData result = aaaCertMdsalProvider.getSslData(BUNDLE_NAME);
        assertTrue(result.equals(signedSslData));
    }

    @Test
    public void getTrustKeyStoreTest() {
        KeyStore result = aaaCertMdsalProvider.getTrustKeyStore(BUNDLE_NAME);
        assertNotNull(result);
    }

    @Test
    public void getTrustStoreCertificateTest() {
        String result = aaaCertMdsalProvider.getTrustStoreCertificate(BUNDLE_NAME, ALIAS, true);
        assertTrue(result != null && !result.isEmpty());
        assertTrue(result.contains(KeyStoreConstant.END_CERTIFICATE));
        result = aaaCertMdsalProvider.getTrustStoreCertificate(BUNDLE_NAME, ALIAS, false);
        assertTrue(!result.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void importSslDataKeystoresTest() {
        SslData result = aaaCertMdsalProvider.importSslDataKeystores(BUNDLE_NAME, ODL_NAME, PASSWORD, ALIAS,
                aaaCertMdsalProvider.getODLKeyStore(BUNDLE_NAME), TRUST_NAME, PASSWORD,
                aaaCertMdsalProvider.getTrustKeyStore(BUNDLE_NAME), CIPHER_SUITES_ARRAY, PROTOCOL);
        assertTrue(result.getOdlKeystore().getKeystoreFile().length == signedSslData.getOdlKeystore()
                .getKeystoreFile().length);
    }

    @Test
    public void removeSslDataTest() {
        Boolean result = aaaCertMdsalProvider.removeSslData(BUNDLE_NAME);
        assertTrue(result);
    }

    @Test
    public void updateSslDataTest() {
        SslData result = aaaCertMdsalProvider.updateSslData(signedSslData);
        assertTrue(result.equals(signedSslData));
    }

    @Test
    public void getTlsProtocolsTest() {
        String[] result = aaaCertMdsalProvider.getTlsProtocols(BUNDLE_NAME);
        assertNotNull(result);
        assertTrue(result.length == 1);
        assertTrue(result[0] == PROTOCOL);
    }

    @Test
    public void addTrustNodeCertificateTest() throws Exception {
        Boolean result = new AaaCertMdsalProvider(mockDataBroker(unsignedSslData), aaaEncryptionService)
                .addTrustNodeCertificate(BUNDLE_NAME, ALIAS, CERTIFICATE);
        assertTrue(result);
    }

    @Test
    public void addODLStoreSignedCertificate() throws Exception {
        Boolean result = new AaaCertMdsalProvider(mockDataBroker(unsignedSslData), aaaEncryptionService)
                .addODLStoreSignedCertificate(BUNDLE_NAME, ALIAS, CERTIFICATE);
        assertTrue(result);
    }
}
