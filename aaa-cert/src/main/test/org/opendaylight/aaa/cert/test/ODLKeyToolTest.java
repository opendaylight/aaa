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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;

public class ODLKeyToolTest {

    private static ODLKeyTool odlKeyTool;
    private static String testPath = "target" + File.separator + "test" + File.separator;
    private final String keyStore = "fooTest.jks";
    private final String trustKeyStore = "footrust.jks";
    private final String passwd = "Password";
    private final String alias = "FooTest";
    private final String certFile = "cert.pem";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        KeyStoreConstant.KEY_STORE_PATH = testPath;
        odlKeyTool = new ODLKeyTool(testPath);
    }

    @Test
    public void testCreateKeyStoreWithSelfSignCert() {
        final String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
        assertTrue(odlKeyTool.createKeyStoreWithSelfSignCert(keyStore, passwd, dName, alias,
                KeyStoreConstant.DEFAULT_VALIDITY));
    }

    @Test
    public void testGetCertificate() {
        String cert = odlKeyTool.getCertificate(keyStore, passwd, alias, false);
        assertTrue(cert != null && cert.length() > 0);
        cert = odlKeyTool.getCertificate(keyStore, passwd, alias, true);
        assertTrue(cert.contains(KeyStoreConstant.BEGIN_CERTIFICATE));
    }

    @Test
    public void testGenerateCertificateReq() {
        String certReq = odlKeyTool.generateCertificateReq(keyStore, passwd, alias,
                KeyStoreConstant.DEFAULT_SIGN_ALG, false);
        assertTrue(certReq != null && certReq.length() > 0);
        certReq = odlKeyTool.generateCertificateReq(keyStore, passwd, alias,
                KeyStoreConstant.DEFAULT_SIGN_ALG, true);
        assertTrue(certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
    }

    @Test
    public void testCreateKeyStoreImportCert() {
        assertTrue(odlKeyTool.createKeyStoreImportCert(trustKeyStore, passwd, null, alias));
        final String cert = odlKeyTool.getCertificate(keyStore, passwd, alias, false);
        KeyStoreConstant.saveCert(certFile, cert);
        assertTrue(odlKeyTool.createKeyStoreImportCert(trustKeyStore, passwd, certFile, alias));
    }

    @Test
    public void testAddCertificate() {
        final String cert = KeyStoreConstant.readFile(certFile);
        assertTrue(odlKeyTool.addCertificate(trustKeyStore, passwd, cert, alias));
    }

    @Test
    public void testGetKeyStore() {
        assertNotNull(odlKeyTool.getKeyStore(keyStore, passwd));
        assertNotNull(odlKeyTool.getKeyStore(trustKeyStore, passwd));
    }

}
