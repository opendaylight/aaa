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
                          "MIIDLjCCAhagAwIBAgIELsFzhjANBgkqhkiG9w0BAQUFADBZMQwwCgYDV"+
                          "QQDDANPREwxDDAKBgNVBAsMA0RldjEYMBYGA1UECgwPTGludXhGb3VuZG"+
                          "F0aW9uMRQwEgYDVQQHDAtRQyBNb250cmVhbDELMAkGA1UEBhMCQ0EwHhc"+
                          "NMTYwMTA0MTcxNDM3WhcNMTcwMTAzMTcxNDM3WjBZMQwwCgYDVQQDDANP"+
                          "REwxDDAKBgNVBAsMA0RldjEYMBYGA1UECgwPTGludXhGb3VuZGF0aW9uM"+
                          "RQwEgYDVQQHDAtRQyBNb250cmVhbDELMAkGA1UEBhMCQ0EwggEiMA0GCS"+
                          "qGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCsmMPHlF5pfAO3HzvM1pVIPwg"+
                          "at1gq8cHi5wF8d+qt4+jK2uihp9LhAZ3aAZEbRqvZjDYnXaavCFRXZKUN"+
                          "3AjxvYV0VHtVILK7+xOJGUWgJ5BxZ4utTvQ/3LavTQGZHNH3jGeqWMf3f"+
                          "t1T1jiM72nNxN3KZykDVKoUPLpmci0OCMo+IFkcelVojRJGC9q7MSHcbY"+
                          "XBU/HI+frmp4UkfBTcUWJidTj3jJvT8azCEoysy0HSt85x/IZukN2goco"+
                          "kDm6uyavImdqac/c2ApzEAkBVM/+NkvMBIrRjX4AsmejYSP6nMIPbYRV0"+
                          "V6oWL1sMmrvCb5Kt8/jNDa493jO/dDiRAgMBAAEwDQYJKoZIhvcNAQEFB"+
                          "QADggEBAHKFTBRPqXFp4VYECTSdUsn7nad1LawrYE4DB16j5pbmnNwNIH"+
                          "D4W+Wh0EJEfd6iEdu7DJfHS6OqjYKj9ruqyO6LOGBy8eYzyvtq9dkYEOy"+
                          "i86CIb6NRfVR/ycJgeC7sc+y91wPbZlRXtY+UA7RohebC8Cyg6Kr/zEwv"+
                          "OT0fAjQi6Mypje08OstA2sklTSPfYtrDFJUpJW7+5fGic/wf5ITPmMVJl"+
                          "rt6aSStfyOLhCSAWXmU/1Pn1pixltJvaLnd0HYQdhcFOS9XG5LfA3Mlqm"+
                          "ZEwGEjhpmk810dJyRjoCEsokljWyhmJGW6hTK1j+2V+PCHqyawghiTB0jQFRTt2zo="+
                          KeyStoreConstant.END_CERTIFICATE;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    }

    @Test
    public void testGetOdlKeyStoreInfo() {
        final CtlKeystore ctl = aaaCertProv.getOdlKeyStoreInfo();
        assertNotNull(ctl);
        assertTrue(ctl.equals(ctlKeyStore));
    }

    @Test
    public void testGetTrustKeyStoreInfo() {
        final TrustKeystore trust = aaaCertProv.getTrustKeyStoreInfo();
        assertNotNull(trust);
        assertTrue(trust.equals(trustKeyStore));
    }

    @Test
    public void testCreateKeystores() {
        final boolean result = aaaCertProv.createKeyStores();
        assertTrue(result);
        assertNotNull(aaaCertProv.getODLKeyStore());
        assertNotNull(aaaCertProv.getTrustKeyStore());
    }

    @Test
    public void testGenODLCertificateReq() throws InterruptedException, ExecutionException {
        String certReq = aaaCertProv.genODLKeyStoreCertificateReq(true);
        assertTrue(certReq != null && !certReq.isEmpty());
        assertTrue(certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
        certReq = aaaCertProv.genODLKeyStoreCertificateReq(false);
        assertTrue(!certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
    }

    @Test
    public void testGenODLCertificateReqWithPassword() throws InterruptedException, ExecutionException {
        final String certReq = aaaCertProv.genODLKeyStoreCertificateReq(ctlKeyStore.getStorePassword(), true);
        assertTrue(certReq != null && !certReq.isEmpty());
        assertTrue(certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
    }

    @Test
    public void testGetODLCertificate() throws InterruptedException, ExecutionException {
        String cert = aaaCertProv.getODLKeyStoreCertificate(true);
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(cert.contains(KeyStoreConstant.END_CERTIFICATE));
        cert = aaaCertProv.getODLKeyStoreCertificate(false);
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void testGetODLCertificateWithPassword() throws InterruptedException, ExecutionException {
        String cert = aaaCertProv.getODLKeyStoreCertificate(ctlKeyStore.getStorePassword(), true);
        assertTrue(cert != null && !cert.isEmpty());
        assertTrue(cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void testAddCertificateTrustStore() throws InterruptedException, ExecutionException {
        final boolean result = aaaCertProv.addCertificateTrustStore(dummyAlias, dummyCert);
        assertTrue(result);
        final String cert = aaaCertProv.getCertificateTrustStore(dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());
    }

    @Test
    public void testAddCertificateTrustStoreWithPassword() throws InterruptedException, ExecutionException {
        final boolean result = aaaCertProv.addCertificateTrustStore(trustKeyStore.getStorePassword(), dummyAlias, dummyCert);
        assertTrue(result);
        final String cert = aaaCertProv.getCertificateTrustStore(dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());
    }

    @Test
    public void testGetCertificateTrustStore() throws InterruptedException, ExecutionException {
        String cert = aaaCertProv.getCertificateTrustStore(dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());
        cert = aaaCertProv.getCertificateTrustStore(dummyAlias, false);
        assertTrue(!cert.contains(KeyStoreConstant.END_CERTIFICATE));
    }

    @Test
    public void testGetCertificateTrustStoreWithPassword() throws InterruptedException, ExecutionException {
        final String cert = aaaCertProv.getCertificateTrustStore(trustKeyStore.getStorePassword(), dummyAlias, true);
        assertTrue(cert != null && !cert.isEmpty());
    }
}
