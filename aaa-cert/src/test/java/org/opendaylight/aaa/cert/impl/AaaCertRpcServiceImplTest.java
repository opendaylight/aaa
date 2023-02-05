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
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertificateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertificateOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class AaaCertRpcServiceImplTest {
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

        final AaaCertServiceConfig aaaCertServiceConfigInit = mock(AaaCertServiceConfig.class);
        when(aaaCertServiceConfigInit.getUseConfig()).thenReturn(true);
        when(aaaCertServiceConfigInit.getUseMdsal()).thenReturn(true);
        when(aaaCertServiceConfigInit.getBundleName()).thenReturn(BUNDLE_NAME);
        aaaCertServiceConfig = aaaCertServiceConfigInit;

        // Create class
        aaaCertRpcService = new AaaCertRpcServiceImpl(aaaCertServiceConfig, mockDataBroker(signedSslData),
                aaaEncryptionService);
        assertNotNull(aaaCertRpcService);
    }

    @Test
    public void getNodeCertificateTest() throws Exception {
        final GetNodeCertificateInput nodeCertificateInput = mock(GetNodeCertificateInput.class);
        when(nodeCertificateInput.getNodeAlias()).thenReturn(ALIAS);
        Future<RpcResult<GetNodeCertificateOutput>> result = aaaCertRpcService.getNodeCertificate(nodeCertificateInput);
        assertTrue(result.get().isSuccessful());
        final String cert = result.get().getResult().getNodeCert();
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void setODLCertificateTest() throws Exception {
        final SetODLCertificateInput input = mock(SetODLCertificateInput.class);
        when(input.getOdlCertAlias()).thenReturn(ALIAS);
        when(input.getOdlCert()).thenReturn(CERTIFICATE);
        Future<RpcResult<SetODLCertificateOutput>> result = new AaaCertRpcServiceImpl(aaaCertServiceConfig,
                mockDataBroker(unsignedSslData), aaaEncryptionService).setODLCertificate(input);
        assertTrue(result.get().isSuccessful());
    }

    @Test
    public void getODLCertificateTest() throws Exception {
        Future<RpcResult<GetODLCertificateOutput>> result = aaaCertRpcService.getODLCertificate(
            new GetODLCertificateInputBuilder().build());
        assertTrue(result.get().isSuccessful());
        final String cert = result.get().getResult().getOdlCert();
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void getODLCertificateReq() throws Exception {
        Future<RpcResult<GetODLCertificateReqOutput>> result = aaaCertRpcService.getODLCertificateReq(
            new GetODLCertificateReqInputBuilder().build());
        assertTrue(result.get().isSuccessful());
        final String cert = result.get().getResult().getOdlCertReq();
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE_REQUEST));
    }

    @Test
    public void setNodeCertificate() throws Exception {
        final SetNodeCertificateInput input = mock(SetNodeCertificateInput.class);
        when(input.getNodeAlias()).thenReturn(ALIAS);
        when(input.getNodeCert()).thenReturn(CERTIFICATE);
        Future<RpcResult<SetNodeCertificateOutput>> result = new AaaCertRpcServiceImpl(aaaCertServiceConfig,
                mockDataBroker(unsignedSslData), aaaEncryptionService).setNodeCertificate(input);
        assertTrue(result.get().isSuccessful());
    }
}
