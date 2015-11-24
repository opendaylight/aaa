package org.opendaylight.aaa.cert.impl;

import java.io.ByteArrayInputStream;
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
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODLKeyTool {

    private final Logger LOG = LoggerFactory.getLogger(ODLKeyTool.class);
    private String workingDir = KeyStoreUtilis.keyStorePath;

    public ODLKeyTool(String workingDirectory) {
        workingDir = workingDirectory;
        KeyStoreUtilis.createDir(workingDir);
    }

    public ODLKeyTool() {
        KeyStoreUtilis.createDir(workingDir);
    }

    public boolean createKeyStoreWithSelfSignCert(String keyStoreName, String keyStorePwd, String dName, String keyAlias, int validity) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyStoreUtilis.defaultKeyAlg);
            keyPairGenerator.initialize(KeyStoreUtilis.defaultKeySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            X509V3CertificateGenerator x509V3CertGen = new X509V3CertificateGenerator();
            x509V3CertGen.setSerialNumber(getSecureRandomeInt());
            x509V3CertGen.setIssuerDN(new X509Principal(dName));
            x509V3CertGen.setNotBefore(new Date(System.currentTimeMillis()));
            x509V3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * validity)));
            x509V3CertGen.setSubjectDN(new X509Principal(dName));
            x509V3CertGen.setPublicKey(keyPair.getPublic());
            x509V3CertGen.setSignatureAlgorithm(KeyStoreUtilis.defaultSignAlg);
            X509Certificate x509Cert = x509V3CertGen.generateX509Certificate(keyPair.getPrivate());
            KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            ctlKeyStore.load(null, keyStorePwd.toCharArray());
            ctlKeyStore.setKeyEntry(keyAlias, keyPair.getPrivate(), keyStorePwd.toCharArray(),
                       new java.security.cert.Certificate[]{x509Cert});
            final FileOutputStream fOutputStream = new FileOutputStream(workingDir + keyStoreName);
            ctlKeyStore.store( fOutputStream, keyStorePwd.toCharArray());
            LOG.info(keyStoreName + " is created");
            return true;
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SecurityException | SignatureException | KeyStoreException | CertificateException | IOException e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    private BigInteger getSecureRandomeInt() {
        BigInteger bigInt = BigInteger.valueOf(new SecureRandom().nextInt());
        return new BigInteger(1, bigInt.toByteArray());
    }

    public String getCertificate(String keyStoreName, String keyStorePwd, String certAlias, boolean withTag) {
        try {
            KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            ctlKeyStore.load(fInputStream, keyStorePwd.toCharArray());
            if (ctlKeyStore.containsAlias(certAlias)) {
                X509Certificate odlCert = (X509Certificate)ctlKeyStore.getCertificate(certAlias);
                String cert = DatatypeConverter.printBase64Binary(odlCert.getEncoded());
                if (withTag) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(KeyStoreUtilis.BEGIN_CERTIFICATE);
                    sb.append("\n");
                    sb.append(cert);
                    sb.append("\n");
                    sb.append(KeyStoreUtilis.END_CERTIFICATE);
                    return sb.toString();
                }
                return cert;
            }
            LOG.info(keyStoreName + " KeyStore does not contain alias " + certAlias);
            return null;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            LOG.error("Failed to get Certificate " + e.getMessage());
            return null;
        }
    }

    public String generateCertificateReq(String keyStoreName, String keyStorePwd, String keyAlias, String signAlg,
                  boolean withTag) {
        try {
            KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            ctlKeyStore.load(fInputStream, keyStorePwd.toCharArray());
            if (ctlKeyStore.containsAlias(keyAlias)) {
                X509Certificate odlCert = (X509Certificate)ctlKeyStore.getCertificate(keyAlias);
                PublicKey pubKey = odlCert.getPublicKey();
                PrivateKey privKey = (PrivateKey)ctlKeyStore.getKey(keyAlias, keyStorePwd.toCharArray());
                String subject = odlCert.getSubjectDN().getName();
                X509Name xname = new X509Name(subject);
                String signatureAlgorithm = signAlg;
                PKCS10CertificationRequest csr =
                        new PKCS10CertificationRequest(signatureAlgorithm, xname, pubKey, null, privKey);
                String certReq = DatatypeConverter.printBase64Binary(csr.getEncoded());
                if (withTag) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(KeyStoreUtilis.BEGIN_CERTIFICATE_REQUEST);
                    sb.append("\n");
                    sb.append(certReq);
                    sb.append("\n");
                    sb.append(KeyStoreUtilis.END_CERTIFICATE_REQUEST);
                    return sb.toString();
                }
                return certReq;
            }
            LOG.info(keyStoreName + " KeyStore does not contain alias " + keyAlias);
            return null;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException |
                 UnrecoverableKeyException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            LOG.error("Failed to generate certificate request " + e.getMessage());
            return null;
        }
    }

    private X509Certificate getCertificate(String certificate) throws CertificateException {
        if (certificate.contains(KeyStoreUtilis.BEGIN_CERTIFICATE)) {
            int fIdx = certificate.indexOf(KeyStoreUtilis.BEGIN_CERTIFICATE) + KeyStoreUtilis.BEGIN_CERTIFICATE.length();
            int sIdx = certificate.indexOf(KeyStoreUtilis.END_CERTIFICATE);
            certificate = certificate.substring(fIdx, sIdx);
        }
        byte[] byteCert = Base64.decodeBase64(certificate);
        final InputStream inputStreamCert = new ByteArrayInputStream(byteCert);
        CertificateFactory certFactory;
        certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate newCert = (X509Certificate) certFactory.generateCertificate(inputStreamCert);
        newCert.checkValidity();
        return newCert;
    }

    public boolean createKeyStoreImportCert(String keyStoreName, String keyStorePwd, String certFile, String alias) {
        KeyStore trustKeyStore;
        try {
            trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(null, keyStorePwd.toCharArray());
            if(KeyStoreUtilis.checkKeyStoreFile(certFile)) {
                String certificate = KeyStoreUtilis.readFile(certFile);
                X509Certificate newCert = getCertificate(certificate);
                trustKeyStore.setCertificateEntry(alias, newCert);
            }
            trustKeyStore.store( new FileOutputStream(workingDir + keyStoreName), keyStorePwd.toCharArray());
            LOG.info(keyStoreName + " is created");
            return true;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Failed to create keystore " + keyStoreName);
            return false;
        }
    }

    public boolean addCertificate(String keyStoreName, String keyStorePwd, String certificate, String alias) {
        try {
            X509Certificate newCert = getCertificate(certificate);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            keyStore.load(fInputStream, keyStorePwd.toCharArray());
            keyStore.setCertificateEntry(alias, newCert);
            keyStore.store( new FileOutputStream(workingDir + keyStoreName), keyStorePwd.toCharArray());
            LOG.info("Certificate "+alias+" Added to keyStore "+keyStoreName);
            return true;
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            LOG.error("failed to add certificate " + e.getMessage());
            return false;
        }
    }

    public KeyStore getKeyStore(String keyStoreName, String keyStorePwd) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            keyStore.load(fInputStream, keyStorePwd.toCharArray());
            return keyStore;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            LOG.error("failed to get keystore " + e.getMessage());
            return null;
        }
    }
}
