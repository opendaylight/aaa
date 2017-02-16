/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
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
import java.util.Base64;
import java.util.Date;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ODLKeyTool implements the basic operations that manage the Java keyStores such as create, generate, add and delete certificates.
 *
 * @author mserngawy
 *
 */
public class ODLKeyTool {

    private static final Logger LOG = LoggerFactory.getLogger(ODLKeyTool.class);

    private final String workingDir;

    protected ODLKeyTool() {
        workingDir = KeyStoreConstant.KEY_STORE_PATH;
        KeyStoreConstant.createDir(workingDir);
    }

    public ODLKeyTool(final String workingDirectory) {
        workingDir = workingDirectory;
        KeyStoreConstant.createDir(workingDir);
    }

    /**
     * Add certificate to the given keystore
     *
     * @param keyStore java keystore object
     * @param certificate to add as string
     * @param alias of the certificate
     * @param deleteOld true to delete the old certificate that has the same alias otherwise it will fail if there is a certificate has same given alias.
     * @return the given Keystore containing the certificate otherwise return null.
     */
    public KeyStore addCertificate(final KeyStore keyStore, final String certificate, final String alias, final boolean deleteOld) {
        try {
            final X509Certificate newCert = getCertificate(certificate);
            if(keyStore.isCertificateEntry(alias) && deleteOld) {
                keyStore.deleteEntry(alias);
            }
            if (newCert != null ) {
                keyStore.setCertificateEntry(alias, newCert);
            } else {
                LOG.warn("{} Not a valid certificate {}", alias, certificate);
                return null;
            }
            return keyStore;
        } catch (final KeyStoreException e) {
            LOG.error("failed to add certificate", e);
            return null;
        }
    }

    /**
     * Convert the given java keystore object to byte array
     *
     * @param keyStore object
     * @param keystorePassword the password of the given keystore
     * @return byte array
     */
    public byte[] convertKeystoreToBytes(final KeyStore keyStore, final String keystorePassword) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            keyStore.store(byteArrayOutputStream, keystorePassword.toCharArray());
        } catch (final KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Fatal error convert keystore to bytes", e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Create a keystore that has self sign private/public keys using the default key algorithm (RSA), size (2048)
     * and signing algorithm (SHA1WithRSAEncryption)
     *
     * @param keyStoreName the keystore name
     * @param keystorePassword the keystore password
     * @param dName the generated key's Dname
     * @param keyAlias the private key alias
     * @param validity the key validity
     * @return keystore object
     */
    public KeyStore createKeyStoreWithSelfSignCert(final String keyStoreName, final String keystorePassword, final String dName, final String keyAlias, final int validity) {
        return createKeyStoreWithSelfSignCert(keyStoreName, keystorePassword, dName, keyAlias, validity, KeyStoreConstant.DEFAULT_KEY_ALG,
                KeyStoreConstant.DEFAULT_KEY_SIZE, KeyStoreConstant.DEFAULT_SIGN_ALG);
    }

    /**
     * Create a keystore that has self sign private/public keys
     *
     * @param keyStoreName the keystore name
     * @param keystorePassword the keystore password
     * @param dName the generated key's Dname
     * @param keyAlias the private key alias
     * @param validity the key validity
     * @param keyAlg the algorithm that will be used to generate the key
     * @param keySize the key size
     * @param signAlg the signing algorithm
     * @return keystore object
     */
    public KeyStore createKeyStoreWithSelfSignCert(final String keyStoreName, final String keystorePassword, final String dName,
            final String keyAlias, final int validity, final String keyAlg, final int keySize, final String signAlg) {
        try {
            final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlg);
            keyPairGenerator.initialize(keySize);
            final KeyPair keyPair = keyPairGenerator.generateKeyPair();
            final X509V3CertificateGenerator x509V3CertGen = new X509V3CertificateGenerator();
            x509V3CertGen.setSerialNumber(getSecureRandomeInt());
            x509V3CertGen.setIssuerDN(new X509Principal(dName));
            x509V3CertGen.setNotBefore(new Date(System.currentTimeMillis()));
            x509V3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (KeyStoreConstant.DAY_TIME * validity)));
            x509V3CertGen.setSubjectDN(new X509Principal(dName));
            x509V3CertGen.setPublicKey(keyPair.getPublic());
            x509V3CertGen.setSignatureAlgorithm(signAlg);
            final X509Certificate x509Cert = x509V3CertGen.generateX509Certificate(keyPair.getPrivate());
            final KeyStore ctlKeyStore = KeyStore.getInstance("JKS");
            ctlKeyStore.load(null, keystorePassword.toCharArray());
            ctlKeyStore.setKeyEntry(keyAlias, keyPair.getPrivate(), keystorePassword.toCharArray(),
                       new java.security.cert.Certificate[]{x509Cert});
            LOG.info("{} is created", keyStoreName);
            return ctlKeyStore;
        }
        catch (final NoSuchAlgorithmException | InvalidKeyException | SecurityException | SignatureException | KeyStoreException | CertificateException | IOException e) {
            LOG.error("Fatal error creating keystore", e);
            return null;
        }
    }

    /**
     * Create empty keystore does not has private or public key.
     *
     * @param keystorePassword the keystore password
     * @return keystore object
     */
    public KeyStore createEmptyKeyStore(final String keystorePassword) {
        try {
            final KeyStore trustKeyStore = KeyStore.getInstance("JKS");
            trustKeyStore.load(null, keystorePassword.toCharArray());
            return trustKeyStore;
        } catch (final KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Failed to create trust keystore", e);
            return null;
        }
    }

    /**
     * Export the given keystore as a file under the working directory
     *
     * @param keystore object
     * @param keystorePassword the keystore password
     * @param fileName of the keystore
     * @return true if successes to export the keystore
     */
    public boolean exportKeystore(final KeyStore keystore, final String keystorePassword, final String fileName) {
        if (keystore == null) {
            return false;
        }
        try (final FileOutputStream fOutputStream = new FileOutputStream(workingDir + fileName)) {
            keystore.store(fOutputStream, keystorePassword.toCharArray());
            return true;
        } catch (final KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Fatal error export keystore", e);
            return false;
        }
    }

    /**
     * Generate a certificate signing request based on the given keystore private/public key
     *
     * @param keyStore object
     * @param keystorePassword the keystore password
     * @param keyAlias Alias of the given keystore's private key.
     * @param signAlg the signing algorithm
     * @param withTag true to add the certificate request tag to the certificate request string.
     * @return certificate request as string.
     */
    public String generateCertificateReq(final KeyStore keyStore, final String keystorePassword, final String keyAlias, final String signAlg,
            final boolean withTag) {
        try {
            if (keyStore.containsAlias(keyAlias)) {
                final X509Certificate odlCert = (X509Certificate)keyStore.getCertificate(keyAlias);
                final PublicKey pubKey = odlCert.getPublicKey();
                final PrivateKey privKey = (PrivateKey)keyStore.getKey(keyAlias, keystorePassword.toCharArray());
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
            return StringUtils.EMPTY;
        } catch (final NoSuchAlgorithmException | KeyStoreException |
                 UnrecoverableKeyException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            LOG.error("Failed to generate certificate request", e);
            return StringUtils.EMPTY;
        }
}

    /**
     * Get a certificate as String based on the given alias
     *
     * @param keyStore keystore that has the certificate
     * @param certAlias certificate alias
     * @param withTag true to add the certificate tag to the certificate string.
     * @return certificate as string.
     */
    public String getCertificate(final KeyStore keyStore, final String certAlias, final boolean withTag) {
        try {
            if (keyStore.containsAlias(certAlias)) {
                final X509Certificate odlCert = (X509Certificate) keyStore.getCertificate(certAlias);
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
            return StringUtils.EMPTY;
        } catch (final CertificateException | KeyStoreException e) {
            LOG.error("Failed to get Certificate", e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Get a X509Certificate object based on given certificate string.
     *
     * @param certificate as string
     * @return X509Certificate if the certificate string is not well formated will return null
     */
    private X509Certificate getCertificate(String certificate) {
        if (certificate.isEmpty()) {
            return null;
        }

        if (certificate.contains(KeyStoreConstant.BEGIN_CERTIFICATE)) {
            final int fIdx = certificate.indexOf(KeyStoreConstant.BEGIN_CERTIFICATE) + KeyStoreConstant.BEGIN_CERTIFICATE.length();
            final int sIdx = certificate.indexOf(KeyStoreConstant.END_CERTIFICATE);
            certificate = certificate.substring(fIdx, sIdx);
        }
        final byte[] byteCert = Base64.getDecoder().decode(certificate);
        final InputStream inputStreamCert = new ByteArrayInputStream(byteCert);
        CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
            final X509Certificate newCert = (X509Certificate) certFactory.generateCertificate(inputStreamCert);
            newCert.checkValidity();
            return newCert;
        } catch (final CertificateException e) {
            LOG.error("Failed to get certificate", e);
            return null;
        }
    }

    /**
     * generate secure random number
     *
     * @return secure random number as BigInteger.
     */
    private BigInteger getSecureRandomeInt() {
        final SecureRandom secureRandom = new SecureRandom();
        final BigInteger bigInt = BigInteger.valueOf(secureRandom.nextInt());
        return new BigInteger(1, bigInt.toByteArray());
    }

    /**
     * Load the keystore object from the given byte array
     *
     * @param keyStoreBytes array of byte contain keystore object
     * @param keystorePassword the keystore password
     * @return keystore object otherwise return null if it fails to load.
     */
    public KeyStore loadKeyStore(final byte[] keyStoreBytes, final String keystorePassword) {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(keyStoreBytes), keystorePassword.toCharArray());
            return keyStore;
        } catch (final KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            LOG.error("Fatal error load keystore", e);
            return null;
        }
    }

    /**
     * Load the keystore from the working directory
     *
     * @param keyStoreName keystore file name
     * @param keystorePassword keystore password
     * @return keystore object otherwise return null if it fails to load.
     */
    public KeyStore loadKeyStore(final String keyStoreName, final String keystorePassword) {
        try {
            final KeyStore keyStore = KeyStore.getInstance("JKS");
            final FileInputStream fInputStream = new FileInputStream(workingDir + keyStoreName);
            keyStore.load(fInputStream, keystorePassword.toCharArray());
            return keyStore;
        } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
            LOG.error("failed to get keystore {}", e.getMessage());
            return null;
        }
    }
}
