/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opendaylight.aaa.cert.test.TestUtils.mockDataBroker;

import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.AaaCertRpcServiceImpl;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertifcateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertifcateInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class AaaCertRpcServiceImplTest {
    private static final String ALIAS = TestUtils.dummyAlias;
    private static final String BUNDLE_NAME = "opendaylight";
    private static final String CERTIFICATE = TestUtils.dummyCert;
    private static final String CIPHER_SUITE_NAME = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
    private static final String[] CIPHER_SUITES_ARRAY = { CIPHER_SUITE_NAME };
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
    private static AaaCertServiceConfig aaaCertServiceConfig;

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
                .setTrustKeystore(signedTrustKeyStore).setTlsProtocols(PROTOCOL).build();

        final OdlKeystore unsignedOdlKeystore = new OdlKeystoreBuilder().setAlias(ALIAS).setDname(D_NAME)
                .setName(ODL_NAME).setStorePassword(PASSWORD).setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG).setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG).setKeystoreFile(unsignedTrustKeyStore.getKeystoreFile())
                .build();

        unsignedSslData = new SslDataBuilder().setOdlKeystore(unsignedOdlKeystore)
                .setTrustKeystore(unsignedTrustKeyStore).build();

        when(aaaEncryptionServiceInit.decrypt(unsignedTrustKeyStore.getKeystoreFile()))
                .thenReturn(unsignedTrustKeyStore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(signedOdlKeystore.getKeystoreFile()))
                .thenReturn(signedOdlKeystore.getKeystoreFile());
        when(aaaEncryptionServiceInit.decrypt(isA(String.class))).thenReturn(PASSWORD);
        aaaEncryptionService = aaaEncryptionServiceInit;

        final AaaCertServiceConfig aaaCertServiceConfigInit = mock(AaaCertServiceConfig.class);
        when(aaaCertServiceConfigInit.isUseConfig()).thenReturn(true);
        when(aaaCertServiceConfigInit.isUseMdsal()).thenReturn(true);
        aaaCertServiceConfig = aaaCertServiceConfigInit;

        // Create class
        aaaCertRpcService = new AaaCertRpcServiceImpl(aaaCertServiceConfig, mockDataBroker(signedSslData),
                aaaEncryptionService);
        assertNotNull(aaaCertRpcService);
    }

    @Test
    public void getNodeCertifcateTest() throws Exception {
        final GetNodeCertifcateInput nodeCertifcateInput = mock(GetNodeCertifcateInput.class);
        when(nodeCertifcateInput.getNodeAlias()).thenReturn(ALIAS);
        Future<RpcResult<GetNodeCertifcateOutput>> result = aaaCertRpcService.getNodeCertifcate(nodeCertifcateInput);
        assertTrue(result.get().isSuccessful());
        final String cert = result.get().getResult().getNodeCert();
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void setODLCertifcateTest() throws Exception {
        final SetODLCertifcateInput input = mock(SetODLCertifcateInput.class);
        when(input.getOdlCertAlias()).thenReturn(ALIAS);
        when(input.getOdlCert()).thenReturn(CERTIFICATE);
        Future<RpcResult<Void>> result = new AaaCertRpcServiceImpl(aaaCertServiceConfig,
                mockDataBroker(unsignedSslData), aaaEncryptionService).setODLCertifcate(input);
        assertTrue(result.get().isSuccessful());
    }

    @Test
    public void getODLCertificateTest() throws Exception {
        Future<RpcResult<GetODLCertificateOutput>> result = aaaCertRpcService.getODLCertificate();
        assertTrue(result.get().isSuccessful());
        final String cert = result.get().getResult().getOdlCert();
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void getODLCertificateReq() throws Exception {
        Future<RpcResult<GetODLCertificateReqOutput>> result = aaaCertRpcService.getODLCertificateReq();
        assertTrue(result.get().isSuccessful());
        final String cert = result.get().getResult().getOdlCertReq();
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
    }

    @Test
    public void setNodeCertifcate() throws Exception {
        final SetNodeCertifcateInput input = mock(SetNodeCertifcateInput.class);
        when(input.getNodeAlias()).thenReturn(ALIAS);
        when(input.getNodeCert()).thenReturn(CERTIFICATE);
        Future<RpcResult<Void>> result = new AaaCertRpcServiceImpl(aaaCertServiceConfig,
                mockDataBroker(unsignedSslData), aaaEncryptionService).setNodeCertifcate(input);
        assertTrue(result.get().isSuccessful());
    }
}
