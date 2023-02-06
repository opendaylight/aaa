/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.aaa.cert.impl.TestUtils.mockDataBroker;

import com.google.common.util.concurrent.Futures;
import java.io.File;
import java.security.Security;
import java.util.List;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertificateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertificateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertificateInputBuilder;

public class AaaCertRpcServiceImplTest {
    private static final String ALIAS = TestUtils.DUMMY_ALIAS;
    private static final String BUNDLE_NAME = "opendaylight";
    private static final String CERTIFICATE = TestUtils.DUMMY_CERT;
    private static final String CIPHER_SUITE_NAME = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private static final String D_NAME = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static final String ODL_NAME = "odlTest.jks";
    private static final String PASSWORD = "passWord";
    private static final String PROTOCOL = "SSLv2Hello";
    private static final String TEST_PATH = "target" + File.separator + "test" + File.separator;
    private static final String TRUST_NAME = "trustTest.jks";

    private static AAAEncryptionService aaaEncryptionService;
    private static SslData signedSslData;
    private static SslData unsignedSslData;
    private static AaaCertRpcServiceImpl aaaCertRpcService;

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

        signedSslData = new SslDataBuilder()
            .setCipherSuites(List.of(new CipherSuitesBuilder().setSuiteName(CIPHER_SUITE_NAME).build()))
            .setOdlKeystore(signedOdlKeystore)
            .setTrustKeystore(signedTrustKeyStore)
            .setTlsProtocols(PROTOCOL)
            .setBundleName(BUNDLE_NAME)
            .build();

        unsignedSslData = new SslDataBuilder()
            .setOdlKeystore(new OdlKeystoreBuilder()
                .setAlias(ALIAS)
                .setDname(D_NAME)
                .setName(ODL_NAME)
                .setStorePassword(PASSWORD)
                .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
                .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
                .setKeystoreFile(unsignedTrustKeyStore.getKeystoreFile())
                .build())
            .setTrustKeystore(unsignedTrustKeyStore)
            .setBundleName(BUNDLE_NAME)
            .build();

        when(aaaEncryptionServiceInit.decrypt(unsignedTrustKeyStore.getKeystoreFile()))
                .thenReturn(unsignedTrustKeyStore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(signedOdlKeystore.getKeystoreFile()))
                .thenReturn(signedOdlKeystore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(any(String.class))).thenReturn(PASSWORD);
        aaaEncryptionService = aaaEncryptionServiceInit;

        // Create class
        aaaCertRpcService = new AaaCertRpcServiceImpl(mockMdsalProvider(signedSslData));
    }

    @Test
    public void getNodeCertificateTest() throws Exception {
        final var result = Futures.getDone(aaaCertRpcService.getNodeCertificate(
            new GetNodeCertificateInputBuilder().setNodeAlias(ALIAS).build()));
        assertTrue(result.isSuccessful());
        final String cert = result.getResult().getNodeCert();
        assertTrue(cert != null && !cert.isEmpty());
        assertFalse(cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void setODLCertificateTest() throws Exception {
        final var result = Futures.getDone(
            new AaaCertRpcServiceImpl(mockMdsalProvider(unsignedSslData))
                .setODLCertificate(
                    new SetODLCertificateInputBuilder().setOdlCertAlias(ALIAS).setOdlCert(CERTIFICATE).build()));
        assertTrue(result.isSuccessful());
    }

    @Test
    public void getODLCertificateTest() throws Exception {
        final var result = Futures.getDone(aaaCertRpcService.getODLCertificate(
            new GetODLCertificateInputBuilder().build()));
        assertTrue(result.isSuccessful());
        final String cert = result.getResult().getOdlCert();
        assertTrue(cert != null && !cert.isEmpty());
        assertFalse(cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void getODLCertificateReq() throws Exception {
        final var result = Futures.getDone(aaaCertRpcService.getODLCertificateReq(
            new GetODLCertificateReqInputBuilder().build()));
        assertTrue(result.isSuccessful());
        final String cert = result.getResult().getOdlCertReq();
        assertTrue(cert != null && !cert.isEmpty());
        assertFalse(cert.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
    }

    @Test
    public void setNodeCertificate() throws Exception {
        final var result = Futures.getDone(
            new AaaCertRpcServiceImpl(mockMdsalProvider(unsignedSslData))
                .setNodeCertificate(
                    new SetNodeCertificateInputBuilder().setNodeAlias(ALIAS).setNodeCert(CERTIFICATE).build()));
        assertTrue(result.isSuccessful());
    }

    private static IAaaCertProvider mockMdsalProvider(final SslData sslData) throws Exception {
        return new DefaultMdsalSslData(new AaaCertMdsalProvider(mockDataBroker(sslData), aaaEncryptionService),
            BUNDLE_NAME, null, null);
    }
}
