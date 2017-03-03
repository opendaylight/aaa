/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.Security;
import java.util.concurrent.ExecutionException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.aaa.cert.impl.AaaCertProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class AaaCertProviderTest {

    private static AaaCertProvider aaaCertProv;
    private static CtlKeystore ctlKeyStore;
    private static TrustKeystore trustKeyStore;

    private String dummyAlias = "fooAlias";
    private String dummyCert = KeyStoreConstant.BEGIN_CERTIFICATE +
                               "MIICKTCCAZKgAwIBAgIECMgzyzANBgkqhkiG9w0BAQUFADBZMQwwCgYDV" +
                               "QQDDANPREwxDDAKBgNVBAsMA0RldjEYMBYGA1UECgwPTGludXhGb3VuZG" +
                               "F0aW9uMRQwEgYDVQQHDAtRQyBNb250cmVhbDELMAkGA1UEBhMCQ0EwHhc" +
                               "NMTcwMzAzMTYyMDA1WhcNMTgwMzAzMTYyMDA1WjBZMQwwCgYDVQQDDANP" +
                               "REwxDDAKBgNVBAsMA0RldjEYMBYGA1UECgwPTGludXhGb3VuZGF0aW9uM" +
                               "RQwEgYDVQQHDAtRQyBNb250cmVhbDELMAkGA1UEBhMCQ0EwgZ8wDQYJKo" +
                               "ZIhvcNAQEBBQADgY0AMIGJAoGBAJrQxIfdU230tedhXnM25r3ht5UQ5Jo" +
                               "G7+9H9b2WcrrkehJ++AZ2zq6SJDLVVnjgXh/YgFo3L6DOKnVTwnXUXGLk" +
                               "NiJhqL2ndu49zI63CxQ2EjBR8tlD5HctNH4SKj1RqmYvt0H3LUZSBKH8Y" +
                               "XGL0U0Qyxwe3flRh2Y6sMb3o47rAgMBAAEwDQYJKoZIhvcNAQEFBQADgY" +
                               "EAVBWCNC+bbJftOTfpL3sL3YO+aQSmPt5ICgrz7wXDkzpf+0FwSqt+kiR" +
                               "Wfw65RTgmn2hmYPh2QW4SaIN50ftLfUHgkf2zeMlodAQVYmBAd/woE3s7" +
                               "fkSa9vQkUowgHAxW//7NOOTonnQPi2gH6ubaOCG4ZeXTwqHy47DGA0c8z" +
                               "2Q="+
                               KeyStoreConstant.END_CERTIFICATE;

    @Test
    public void testCertificate() throws InterruptedException, ExecutionException {
        // Set up Tests
        KeyStoreConstant.KEY_STORE_PATH = "target" + File.separator + "test" + File.separator;
        String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
        Security.addProvider(new BouncyCastleProvider());
        ctlKeyStore = new CtlKeystoreBuilder()
                .setAlias("fooTest")
                .setDname(dName)
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
