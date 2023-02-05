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

import java.io.File;
import java.security.KeyStore;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class ODLKeyToolTest {

    private static ODLKeyTool odlKeyTool;
    private static KeyStore fooKeystore;
    private static String passwd = "Password";
    private static String alias = "FooTest";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final String testPath = "target" + File.separator + "test" + File.separator;
        final String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
        final String keyStore = "fooTest.jks";
        odlKeyTool = new ODLKeyTool(testPath);
        fooKeystore = odlKeyTool.createKeyStoreWithSelfSignCert(keyStore, passwd, dName, alias,
                KeyStoreConstant.DEFAULT_VALIDITY);
        assertNotNull(fooKeystore);
    }

    @Test
    public void testConvertKeystoreToBytes() {
        byte[] keyStoreBytes = odlKeyTool.convertKeystoreToBytes(fooKeystore, passwd);
        assertTrue(keyStoreBytes != null && keyStoreBytes.length > 0);
        fooKeystore = odlKeyTool.loadKeyStore(keyStoreBytes, passwd);
        assertNotNull(fooKeystore);
    }

    @Test
    public void testGetCertificate() {
        String cert = odlKeyTool.getCertificate(fooKeystore, alias, false);
        assertTrue(cert != null && !cert.isEmpty());
        cert = odlKeyTool.getCertificate(fooKeystore, alias, true);
        assertTrue(cert != null && cert.contains(KeyStoreConstant.BEGIN_CERTIFICATE));
    }

    @Test
    public void testGenerateCertificateReq() {
        String certReq = odlKeyTool.generateCertificateReq(fooKeystore, passwd, alias,
                KeyStoreConstant.DEFAULT_SIGN_ALG, true);
        assertTrue(certReq != null && !certReq.isEmpty());
        assertTrue(certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
        certReq = odlKeyTool.generateCertificateReq(fooKeystore, passwd, alias, KeyStoreConstant.DEFAULT_SIGN_ALG,
                false);
        assertTrue(!certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
    }

    @Test
    public void testExportKeystore() {
        final String keystoreName = "export.jks";
        final boolean result = odlKeyTool.exportKeystore(fooKeystore, passwd, keystoreName);
        assertTrue(result);
        final KeyStore exportKS = odlKeyTool.loadKeyStore(keystoreName, passwd);
        assertNotNull(exportKS);
    }
}
