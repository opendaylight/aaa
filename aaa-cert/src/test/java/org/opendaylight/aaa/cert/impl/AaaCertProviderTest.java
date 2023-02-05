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

import java.security.Security;
import java.util.concurrent.ExecutionException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AaaCertProviderTest {

    private static AaaCertProvider aaaCertProv;
    private static CtlKeystore ctlKeyStore;
    private static TrustKeystore trustKeyStore;

    private final String dummyAlias = TestUtils.DUMMY_ALIAS;
    private final String dummyCert = TestUtils.DUMMY_CERT;

    @Test
    public void testCertificate() throws InterruptedException, ExecutionException {
        // Set up Tests
        String name = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
        Security.addProvider(new BouncyCastleProvider());
        ctlKeyStore = new CtlKeystoreBuilder()
                .setAlias("fooTest")
                .setDname(name)
                .setName("ctlTest.jks")
                .setStorePassword("passWord")
                .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
                .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
                .build();
        trustKeyStore = new TrustKeystoreBuilder()
                .setName("trustTest.jks")
                .setStorePassword("passWord")
                .build();
        aaaCertProv = new AaaCertProvider(ctlKeyStore, trustKeyStore);

        // getOldKeyStoreInfo
        final CtlKeystore ctl = aaaCertProv.getOdlKeyStoreInfo();
        assertNotNull(ctl);
        assertTrue(ctl.equals(ctlKeyStore));

        // getTrustKeyStoreInfo
        final TrustKeystore trust = aaaCertProv.getTrustKeyStoreInfo();
        assertNotNull(trust);
        assertTrue(trust.equals(trustKeyStore));

        // createKeyStores
        boolean result = aaaCertProv.createKeyStores();
        assertTrue(result);
        assertNotNull(aaaCertProv.getODLKeyStore());
        assertNotNull(aaaCertProv.getTrustKeyStore());

        // genODLCertificateReq
        String cert = aaaCertProv.genODLKeyStoreCertificateReq(true);
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(cert.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
        cert = aaaCertProv.genODLKeyStoreCertificateReq(false);
        assertTrue(!cert.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));

        // genODLCertificateReqWithPassword
        cert = aaaCertProv.genODLKeyStoreCertificateReq(ctlKeyStore.getStorePassword(), true);
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(cert.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));

        // getODLCertificate
        cert = aaaCertProv.getODLKeyStoreCertificate(true);
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(cert.contains(KeyStoreConstant.END_CERTIFICATE));
        cert = aaaCertProv.getODLKeyStoreCertificate(false);
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));

        // getODLCerticateWithPassword
        cert = aaaCertProv.getODLKeyStoreCertificate(ctlKeyStore.getStorePassword(), true);
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(cert.contains(KeyStoreConstant.END_CERTIFICATE));

        // addCertificateTrustStore
        result = aaaCertProv.addCertificateTrustStore(dummyAlias, dummyCert);
        assertTrue(result);
        cert = aaaCertProv.getCertificateTrustStore(dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());

        // addCertificateTrustStoreWithPassword
        result = aaaCertProv.addCertificateTrustStore(trustKeyStore.getStorePassword(), dummyAlias, dummyCert);
        assertTrue(result);
        cert = aaaCertProv.getCertificateTrustStore(dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());

        // getCertificateTrustStore
        cert = aaaCertProv.getCertificateTrustStore(dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());
        cert = aaaCertProv.getCertificateTrustStore(dummyAlias, false);
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));

        // getCertificateWithPasswordTrusStore
        cert = aaaCertProv.getCertificateTrustStore(trustKeyStore.getStorePassword(), dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());
    }
}
