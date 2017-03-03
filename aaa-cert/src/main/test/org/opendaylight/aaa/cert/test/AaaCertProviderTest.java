/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.security.Security;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.aaa.cert.impl.AaaCertProvider;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetNodeCertifcateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.GetODLCertificateReqOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetNodeCertifcateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rpc.rev151215.SetODLCertifcateInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
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
        ctlKeyStore = new CtlKeystore();
        ctlKeyStore.setAlias("fooTest");
        ctlKeyStore.setDname(dName);
        ctlKeyStore.setName("fooTest.jks");
        ctlKeyStore.setStorePassword("passWord");
        ctlKeyStore.setValidity(KeyStoreConstant.DEFAULT_VALIDITY);
        trustKeyStore = new TrustKeystore();
        trustKeyStore.setAlias("trustTest");
        trustKeyStore.setCertFile("cacert.pem");
        trustKeyStore.setName("trustTest.jks");
        trustKeyStore.setStorePassword("passWord");
        aaaCertProv = new AaaCertProvider(ctlKeyStore, trustKeyStore);
    }

    @Test
    public void testCreateTrustKeyStore() {
        String result = aaaCertProv.createTrustKeyStore(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                trustKeyStore.getAlias());
        assertEquals(result, trustKeyStore.getName() + " Keystore created.");
    }

    @Test
    public void testCreateODLKeyStoreString() {
        String result = aaaCertProv.createODLKeyStore(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                ctlKeyStore.getAlias(), ctlKeyStore.getDname(), ctlKeyStore.getValidity());
        assertEquals(result, ctlKeyStore.getName() + " Keystore created.");
    }

    @Test
    public void testGetODLCertificateReq() throws InterruptedException, ExecutionException {
        Future<RpcResult<GetODLCertificateReqOutput>> future = aaaCertProv.getODLCertificateReq();
        assertTrue(future != null);
        RpcResult<GetODLCertificateReqOutput> rpc = future.get();
        assertTrue(rpc.isSuccessful());
        String certReq = rpc.getResult().getOdlCertReq();
        assertTrue(certReq != null);
        assertTrue(certReq.contains(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST));
    }

    @Test
    public void testSetODLCertifcate() throws InterruptedException, ExecutionException {
        SetODLCertifcateInput input = mock(SetODLCertifcateInput.class, Mockito.RETURNS_MOCKS);
        when(input.getOdlCert()).thenReturn(dummyCert);
        Future<RpcResult<Void>> future = aaaCertProv.setODLCertifcate(input);
        assertTrue(future != null);
        assertTrue(future.get().isSuccessful());
    }

    @Test
    public void testGetODLCertificate() throws InterruptedException, ExecutionException {
        Future<RpcResult<GetODLCertificateOutput>> future = aaaCertProv.getODLCertificate();
        assertTrue(future != null);
        RpcResult<GetODLCertificateOutput> rpc = future.get();
        assertTrue(rpc.isSuccessful());
        String cert = aaaCertProv.getODLKeyStorCertificate(ctlKeyStore.getStorePassword(), ctlKeyStore.getAlias());
        assertEquals(rpc.getResult().getOdlCert(), cert);
    }

    @Test
    public void testSetNodeCertifcate() throws InterruptedException, ExecutionException {
        SetNodeCertifcateInput input = mock(SetNodeCertifcateInput.class, Mockito.RETURNS_MOCKS);
        when(input.getNodeCert()).thenReturn(dummyCert);
        when(input.getNodeAlias()).thenReturn(dummyAlias);
        Future<RpcResult<Void>> future = aaaCertProv.setNodeCertifcate(input);
        assertTrue(future != null);
        assertTrue(future.get().isSuccessful());
    }

    @Test
    public void testGetNodeCertifcate() throws InterruptedException, ExecutionException {
        GetNodeCertifcateInput input = mock(GetNodeCertifcateInput.class, Mockito.RETURNS_MOCKS);
        when(input.getNodeAlias()).thenReturn(dummyAlias);
        Future<RpcResult<GetNodeCertifcateOutput>> future = aaaCertProv.getNodeCertifcate(input);
        assertTrue(future != null);
        RpcResult<GetNodeCertifcateOutput> rpc = future.get();
        assertTrue(rpc.isSuccessful());
        String cert = aaaCertProv.getCertificateTrustStore(trustKeyStore.getStorePassword(), dummyAlias);
        assertEquals(cert, rpc.getResult().getNodeCert());
    }
}
