/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AaaCertMdsalProviderTest {
    private static final String alias = TestUtils.dummyAlias;
    private static final String certificate = TestUtils.dummyCert;
    private static final String bundleName = "opendaylight";
    private static final String cipherSuiteName = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private static final String[] cipherSuitesArray = {cipherSuiteName};
    private static final String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static final String odlName = "odlTest.jks";
    private static final String password = "passWord";
    private static final String protocol = "SSLv2Hello";
    private static final String trustName = "trustTest.jks";
    private static final String testPath = "target" + File.separator + "test" + File.separator;
    private static AAAEncryptionService aaaEncryptionService;
    private static SslData signedSslData;
    private static SslData unsignedSslData;
    private static AaaCertMdsalProvider aaaCertMdsalProvider;

    private static DataBroker mockDataBroker(SslData sslData) throws Exception {
        final Optional<DataObject> dataObjectOptional = mock(Optional.class);
        when(dataObjectOptional.get()).thenReturn(sslData);
        when(dataObjectOptional.isPresent()).thenReturn(true);
        final CheckedFuture<Optional<DataObject>, ReadFailedException> checkReadFuture = mock(CheckedFuture.class);
        when(checkReadFuture.checkedGet()).thenReturn(dataObjectOptional);
        when(checkReadFuture.get()).thenReturn(dataObjectOptional);
        final ReadOnlyTransaction readOnlyTransaction = mock(ReadOnlyTransaction.class);
        when(readOnlyTransaction.read(any(), any())).thenReturn(checkReadFuture);

        final CheckedFuture<Void, TransactionCommitFailedException> checkWriteFuture = mock(CheckedFuture.class);
        final WriteTransaction writeTransaction = mock(WriteTransaction.class);
        when(writeTransaction.submit()).thenReturn(checkWriteFuture);

        final DataBroker dataBrokerInit = mock(DataBroker.class);
        when(dataBrokerInit.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(dataBrokerInit.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        return dataBrokerInit;
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final AAAEncryptionService aaaEncryptionServiceInit = mock(AAAEncryptionService.class);
        final ODLKeyTool odlKeyTool = new ODLKeyTool(testPath);
        final KeyStoresDataUtils keyStoresDataUtils = new KeyStoresDataUtils(aaaEncryptionServiceInit);

        final OdlKeystore signedOdlKeystore = keyStoresDataUtils.createOdlKeystore(odlName, alias, password,
                dName, KeyStoreConstant.DEFAULT_SIGN_ALG, KeyStoreConstant.DEFAULT_KEY_ALG,
                KeyStoreConstant.DEFAULT_VALIDITY, KeyStoreConstant.DEFAULT_KEY_SIZE, odlKeyTool);
        final TrustKeystore signedTrustKeyStore = keyStoresDataUtils.createTrustKeystore(trustName, password,
                signedOdlKeystore.getKeystoreFile());
        final TrustKeystore unsignedTrustKeyStore = keyStoresDataUtils.createTrustKeystore(trustName,password, odlKeyTool);


        // setup tests
        final CipherSuites cipherSuite = new CipherSuitesBuilder()
                .setSuiteName(cipherSuiteName)
                .build();

        final List<CipherSuites> cipherSuites =  new ArrayList<>(Arrays.asList(cipherSuite));

        signedSslData = new SslDataBuilder()
                .setCipherSuites(cipherSuites)
                .setOdlKeystore(signedOdlKeystore)
                .setTrustKeystore(signedTrustKeyStore)
                .setTlsProtocols(protocol)
                .build();

        final OdlKeystore unsignedOdlKeystore = new OdlKeystoreBuilder()
                .setAlias(alias)
                .setDname(dName)
                .setName(odlName)
                .setStorePassword(password)
                .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
                .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
                .setKeystoreFile(unsignedTrustKeyStore.getKeystoreFile())
                .build();

        unsignedSslData = new SslDataBuilder()
                .setOdlKeystore(unsignedOdlKeystore)
                .setTrustKeystore(unsignedTrustKeyStore)
                .build();

        when(aaaEncryptionServiceInit.decrypt(unsignedTrustKeyStore.getKeystoreFile())).thenReturn(unsignedTrustKeyStore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(signedOdlKeystore.getKeystoreFile())).thenReturn(signedOdlKeystore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(isA(String.class))).thenReturn(password);
        aaaEncryptionService = aaaEncryptionServiceInit;

        aaaCertMdsalProvider = new AaaCertMdsalProvider(mockDataBroker(signedSslData), aaaEncryptionService);
        assertNotNull(aaaCertMdsalProvider);
    }

    @Test
    public void addSslDataKeystoresTest() throws Exception {
        SslData result = new AaaCertMdsalProvider(mockDataBroker(signedSslData), aaaEncryptionService).addSslDataKeystores(bundleName, odlName, password,
        alias, dName, trustName, password, cipherSuitesArray , protocol);
        assertTrue(result.getOdlKeystore().getDname() == dName);
        assertTrue(result.getOdlKeystore().getName() == odlName);
        assertTrue(result.getTrustKeystore().getName() == trustName);
    }

    @Test
    public void genODLKeyStoreCertificateReqTest() {
        String result = aaaCertMdsalProvider.genODLKeyStoreCertificateReq(bundleName, true);
        assertTrue(result != null && !result.isEmpty());
        assertTrue(result.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
        result = aaaCertMdsalProvider.genODLKeyStoreCertificateReq(bundleName, false);
        assertTrue(!result.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
    }

    @Test
    public void getCipherSuitesTest() {
        String[] result = aaaCertMdsalProvider.getCipherSuites(bundleName);
        assertTrue(Arrays.equals(result, cipherSuitesArray));
    }

    @Test
    public void getODLKeyStoreTest() {
        KeyStore result = aaaCertMdsalProvider.getODLKeyStore(bundleName);
        assertNotNull(result);
    }

    @Test
    public void getODLStoreCertificateTest() {
        String result = aaaCertMdsalProvider.getODLStoreCertificate(bundleName, true);
        assertTrue(result != null && !result.isEmpty());
        assertTrue(result.contains(KeyStoreConstant.END_CERTIFICATE));
        result = aaaCertMdsalProvider.getODLStoreCertificate(bundleName, false);
        assertTrue(!result.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void getSslDataTest() {
        SslData result = aaaCertMdsalProvider.getSslData(bundleName);
        assertTrue(result.equals(signedSslData));
    }

    @Test
    public void getTrustKeyStoreTest() {
        KeyStore result = aaaCertMdsalProvider.getTrustKeyStore(bundleName);
        assertNotNull(result);
    }

    @Test
    public void getTrustStoreCertificateTest() {
        String result = aaaCertMdsalProvider.getTrustStoreCertificate(bundleName, alias,true);
        assertTrue(result != null && !result.isEmpty());
        assertTrue(result.contains(KeyStoreConstant.END_CERTIFICATE));
        result = aaaCertMdsalProvider.getTrustStoreCertificate(bundleName, alias, false);
        assertTrue(!result.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void importSslDataKeystoresTest() {
        SslData result = aaaCertMdsalProvider.importSslDataKeystores(bundleName, odlName, password, alias, aaaCertMdsalProvider.getODLKeyStore(bundleName),
                trustName, password, aaaCertMdsalProvider.getTrustKeyStore(bundleName), cipherSuitesArray, protocol);
        assertTrue(result.getOdlKeystore().getKeystoreFile().length == signedSslData.getOdlKeystore().getKeystoreFile().length);
    }

    @Test
    public void removeSslDataTest() {
        Boolean result = aaaCertMdsalProvider.removeSslData(bundleName);
        assertTrue(result);
    }

    @Test
    public void updateSslDataTest() {
        SslData result = aaaCertMdsalProvider.updateSslData(signedSslData);
        assertTrue(result.equals(signedSslData));
    }

    @Test
    public void getTlsProtocolsTest() {
        String[] result = aaaCertMdsalProvider.getTlsProtocols(bundleName);
        assertNotNull(result);
        assertTrue(result.length == 1);
        assertTrue(result[0] == protocol);
    }

    @Test
    public void addTrustNodeCertificateTest() throws Exception {
        Boolean result = new AaaCertMdsalProvider(mockDataBroker(unsignedSslData), aaaEncryptionService)
                .addTrustNodeCertificate(bundleName, alias, certificate);
        assertTrue(result);
    }

    @Test
    public void addODLStoreSignedCertificate() throws Exception {
        Boolean result = new AaaCertMdsalProvider(mockDataBroker(unsignedSslData), aaaEncryptionService)
                .addODLStoreSignedCertificate(bundleName, alias, certificate);
        assertTrue(result);
    }
}
