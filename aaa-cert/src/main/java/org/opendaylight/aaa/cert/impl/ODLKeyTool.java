/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mserngawy
 *
 * ODLKeyTool has the basic operation to manage the Java keyStores such as generate, add and delete certificates
 */
public class ODLKeyTool {

    private final static Logger LOG = LoggerFactory.getLogger(ODLKeyTool.class);
    // Day time in millisecond
    private final long dayTime = 1000L * 60 * 60 * 24;
    private String workingDir = KeyStoreConstant.keyStorePath;
    private SecretKey secretKey;

    protected ODLKeyTool(final byte[] initVector, final String cipherKey) {
        SecretKey tmp = null;
        if (cipherKey != null && !cipherKey.isEmpty()) {
            try {
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec spec = new PBEKeySpec(cipherKey.toCharArray(), initVector, 32768, 128);
                tmp = keyFactory.generateSecret(spec);
            } catch (Exception e) {
                LOG.error("Couldn't initialize key factory", e);
            }
            if (tmp != null) {
                secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            }else {
                secretKey = null;
                LOG.warn("Void crypto key passed! AuthN Store Encryption disabled");
            }
        }
    }

    protected ODLKeyTool() {
        KeyStoreConstant.createDir(workingDir);
    }

    public ODLKeyTool(final String workingDirectory) {
        workingDir = workingDirectory;
        KeyStoreConstant.createDir(workingDir);
    }

    public boolean addCertificate(final String keyStoreName, final String keyStorePwd, final String certificate, final String alias) {
        try {
            final X509Certificate newCert = getCertificate(certificate);
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            keyStore.load(fInputStream, keyStorePwd.toCharArray());
            if(keyStore.isCertificateEntry(alias)) {
                keyStore.deleteEntry(alias);
            }
            keyStore.setCertificateEntry(alias, newCert);
            keyStore.store( new FileOutputStream(workingDir + keyStoreName), keyStorePwd.toCharArray());
            LOG.info("Certificate {}  Added to keyStore {}", alias, keyStoreName);
            return true;
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            LOG.error("failed to add certificate {}", e.getMessage());
            return false;
        }
    }

    public boolean createKeyStoreImportCert(final String keyStoreName, final String keyStorePwd, final String certFile, final String alias) {
        KeyStore trustKeyStore;
        try {
            trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(null, keyStorePwd.toCharArray());
            if(KeyStoreConstant.checkKeyStoreFile(certFile)) {
                final String certificate = KeyStoreConstant.readFile(certFile);
                final X509Certificate newCert = getCertificate(certificate);
                trustKeyStore.setCertificateEntry(alias, newCert);
            }
            trustKeyStore.store( new FileOutputStream(workingDir + keyStoreName), keyStorePwd.toCharArray());
            LOG.info("{} is created", keyStoreName);
            return true;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Failed to create keystore {}", keyStoreName);
            return false;
        }
    }

    public KeyStore createTrustKeyStoreImportCert(final String keyStorePwd, Map<String,String> mapAliasCert) {
        try {
            KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(null, keyStorePwd.toCharArray());
            for (String alias: mapAliasCert.keySet()) {
                final String certificate = mapAliasCert.get(alias);
                final X509Certificate newCert = getCertificate(certificate);
                trustKeyStore.setCertificateEntry(alias, newCert);
            }
            LOG.info("trust keystore is created");
            return trustKeyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Failed to create trust keystore");
            return null;
        }
    }

    public KeyStore createKeyStoreWithSelfSignCert(final String keyStoreName, final String keyStorePwd, final String dName,
            final String keyAlias, final int validity, final String keyAlg, final int keySize, final String signAlg) {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlg);
            keyPairGenerator.initialize(keySize);
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            final X509V3CertificateGenerator x509V3CertGen = new X509V3CertificateGenerator();
            x509V3CertGen.setSerialNumber(getSecureRandomeInt());
            x509V3CertGen.setIssuerDN(new X509Principal(dName));
            x509V3CertGen.setNotBefore(new Date(System.currentTimeMillis()));
            x509V3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (dayTime * validity)));
            x509V3CertGen.setSubjectDN(new X509Principal(dName));
            x509V3CertGen.setPublicKey(keyPair.getPublic());
            x509V3CertGen.setSignatureAlgorithm(signAlg);
            final X509Certificate x509Cert = x509V3CertGen.generateX509Certificate(keyPair.getPrivate());
            final KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            ctlKeyStore.load(null, keyStorePwd.toCharArray());
            ctlKeyStore.setKeyEntry(keyAlias, keyPair.getPrivate(), keyStorePwd.toCharArray(),
                       new java.security.cert.Certificate[]{x509Cert});
            LOG.info("{} is created", keyStoreName);
            return ctlKeyStore;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SecurityException | SignatureException | KeyStoreException | CertificateException | IOException e) {
            LOG.error("Fatal error creating keystore: {}", e.getMessage());
            return null;
        }
    }

    public KeyStore createKeyStoreWithSelfSignCert(final String keyStoreName, final String keyStorePwd, final String dName, final String keyAlias, final int validity) {
        return createKeyStoreWithSelfSignCert(keyStoreName, keyStorePwd, dName, keyAlias, validity, KeyStoreConstant.defaultKeyAlg,
                KeyStoreConstant.defaultKeySize, KeyStoreConstant.defaultSignAlg);
    }

    public byte[] convertKeystoreToBytes(final KeyStore keyStore, final String keyStorePwd) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            keyStore.store(byteArrayOutputStream, keyStorePwd.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Fatal error convert keystore to byets {}", e.getMessage());
        }
        return byteArrayOutputStream.toByteArray();
    }

    public KeyStore loadKeyStore(final byte[] keyStoreBytes, final String keyStorePwd) {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(keyStoreBytes), keyStorePwd.toCharArray());
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Fatal error load keystore {}", e.getMessage());
            return null;
        }
    }

    public void exportKeystore(KeyStore keystore, String keystorePassword, String fileName) {
        try {
        final FileOutputStream fOutputStream = new FileOutputStream(workingDir + fileName);
        keystore.store( fOutputStream, keystorePassword.toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Fatal error export keystore {}", e.getMessage());
        }
    }

    public String generateCertificateReq(final KeyStore odlKeyStore, final String keyStorePwd, final String keyAlias, final String signAlg,
            final boolean withTag) {
        try {
            if (odlKeyStore.containsAlias(keyAlias)) {
                final X509Certificate odlCert = (X509Certificate)odlKeyStore.getCertificate(keyAlias);
                final PublicKey pubKey = odlCert.getPublicKey();
                final PrivateKey privKey = (PrivateKey)odlKeyStore.getKey(keyAlias, keyStorePwd.toCharArray());
                final String subject = odlCert.getSubjectDN().getName();
                final X509Name xname = new X509Name(subject);
                final String signatureAlgorithm = signAlg;
                final PKCS10CertificationRequest csr =
                        new PKCS10CertificationRequest(signatureAlgorithm, xname, pubKey, null, privKey);
                final String certReq = DatatypeConverter.printBase64Binary(csr.getEncoded());
                if (withTag) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST);
                    sb.append("\n");
                    sb.append(certReq);
                    sb.append("\n");
                    sb.append(KeyStoreConstant.END_CERTIFICATE_REQUEST);
                    return sb.toString();
                }
                return certReq;
            }
            LOG.info("KeyStore does not contain alias {}", keyAlias);
            return null;
        } catch (NoSuchAlgorithmException | KeyStoreException |
                 UnrecoverableKeyException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            LOG.error("Failed to generate certificate request {}", e.getMessage());
            return null;
        }
}

    public String generateCertificateReq(final String keyStoreName, final String keyStorePwd, final String keyAlias, final String signAlg,
                  final boolean withTag) {
        try {
            final KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            ctlKeyStore.load(fInputStream, keyStorePwd.toCharArray());
            if (ctlKeyStore.containsAlias(keyAlias)) {
                final X509Certificate odlCert = (X509Certificate)ctlKeyStore.getCertificate(keyAlias);
                final PublicKey pubKey = odlCert.getPublicKey();
                final PrivateKey privKey = (PrivateKey)ctlKeyStore.getKey(keyAlias, keyStorePwd.toCharArray());
                final String subject = odlCert.getSubjectDN().getName();
                final X509Name xname = new X509Name(subject);
                final String signatureAlgorithm = signAlg;
                final PKCS10CertificationRequest csr =
                        new PKCS10CertificationRequest(signatureAlgorithm, xname, pubKey, null, privKey);
                final String certReq = DatatypeConverter.printBase64Binary(csr.getEncoded());
                if (withTag) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(KeyStoreConstant.BEGIN_CERTIFICATE_REQUEST);
                    sb.append("\n");
                    sb.append(certReq);
                    sb.append("\n");
                    sb.append(KeyStoreConstant.END_CERTIFICATE_REQUEST);
                    return sb.toString();
                }
                return certReq;
            }
            LOG.info("{} KeyStore does not contain alias {}", keyStoreName, keyAlias);
            return null;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException |
                 UnrecoverableKeyException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            LOG.error("Failed to generate certificate request {}", e.getMessage());
            return null;
        }
    }

    private X509Certificate getCertificate(String certificate) {
        if (certificate.isEmpty()) {
            return null;
        }

        if (certificate.contains(KeyStoreConstant.BEGIN_CERTIFICATE)) {
            final int fIdx = certificate.indexOf(KeyStoreConstant.BEGIN_CERTIFICATE) + KeyStoreConstant.BEGIN_CERTIFICATE.length();
            final int sIdx = certificate.indexOf(KeyStoreConstant.END_CERTIFICATE);
            certificate = certificate.substring(fIdx, sIdx);
        }
        final byte[] byteCert = Base64.decodeBase64(certificate);
        final InputStream inputStreamCert = new ByteArrayInputStream(byteCert);
        CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate newCert = (X509Certificate) certFactory.generateCertificate(inputStreamCert);
            newCert.checkValidity();
            return newCert;
        } catch (final CertificateException e) {
            LOG.error("Failed to get certificate {}", e.getMessage());
            return null;
        }
    }

    public String getCertificate(final KeyStore ctlKeyStore, final String keyStorePwd, final String certAlias, final boolean withTag) {
        try {
            if (ctlKeyStore.containsAlias(certAlias)) {
                final X509Certificate odlCert = (X509Certificate)ctlKeyStore.getCertificate(certAlias);
                final String cert = DatatypeConverter.printBase64Binary(odlCert.getEncoded());
                if (withTag) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(KeyStoreConstant.BEGIN_CERTIFICATE);
                    sb.append("\n");
                    sb.append(cert);
                    sb.append("\n");
                    sb.append(KeyStoreConstant.END_CERTIFICATE);
                    return sb.toString();
                }
                return cert;
            }
            LOG.info("KeyStore does not contain alias {}", certAlias);
            return null;
        } catch (CertificateException | KeyStoreException e) {
            LOG.error("Failed to get Certificate {}", e.getMessage());
            return null;
        }
    }

    public String getCertificate(final String keyStoreName, final String keyStorePwd, final String certAlias, final boolean withTag) {
        try {
            final KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            ctlKeyStore.load(fInputStream, keyStorePwd.toCharArray());
            if (ctlKeyStore.containsAlias(certAlias)) {
                final X509Certificate odlCert = (X509Certificate)ctlKeyStore.getCertificate(certAlias);
                final String cert = DatatypeConverter.printBase64Binary(odlCert.getEncoded());
                if (withTag) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(KeyStoreConstant.BEGIN_CERTIFICATE);
                    sb.append("\n");
                    sb.append(cert);
                    sb.append("\n");
                    sb.append(KeyStoreConstant.END_CERTIFICATE);
                    return sb.toString();
                }
                return cert;
            }
            LOG.info("{} KeyStore does not contain alias {}", keyStoreName, certAlias);
            return null;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            LOG.error("Failed to get Certificate {}", e.getMessage());
            return null;
        }
    }

    public KeyStore getKeyStore(final String keyStoreName, final String keyStorePwd) {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            keyStore.load(fInputStream, keyStorePwd.toCharArray());
            return keyStore;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            LOG.error("failed to get keystore {}", e.getMessage());
            return null;
        }
    }

    private BigInteger getSecureRandomeInt() {
        final SecureRandom secureRandom = new SecureRandom();
        final BigInteger bigInt = BigInteger.valueOf(secureRandom.nextInt());
        return new BigInteger(1, bigInt.toByteArray());
    }
}
