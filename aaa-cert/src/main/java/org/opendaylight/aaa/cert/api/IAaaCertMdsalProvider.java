/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;

/**
 * IAaaCertMdsalProvider define the basic API required by AaaCertMdsalProvider.
 *
 * @author mserngawy
 *
 */
public interface IAaaCertMdsalProvider {

    /**
     * Add SslData object to Mdsal with default values of keyAlg=RSA, keySize=2048, validity=356
     * and signAlg=SHA1WithRSAEncryption.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param odlKeystoreName odl Keystore Name
     * @param odlKeystorePwd odl Keystore Password
     * @param odlKeystoreAlias odl Keystore Alias
     * @param odlKeystoreDname odl Keystore Dname
     * @param trustKeystoreName Trust Keystore Name
     * @param trustKeystorePwd Trust Keystore Password
     * @param cipherSuites cipher suites that will be used by the SSL connection
     * @param tlsProtocols supported TLS protocols such as SSLv2Hello,TLSv1.1
     *     ,TLSv1.2 protocols should be separated by ","
     * @return the created SslData object
     */
    SslData addSslDataKeystores(@Nonnull String bundleName, @Nonnull String odlKeystoreName,
            @Nonnull String odlKeystorePwd, @Nonnull String odlKeystoreAlias, @Nonnull String odlKeystoreDname,
            @Nonnull String trustKeystoreName, @Nonnull String trustKeystorePwd, @Nonnull String[] cipherSuites,
            @Nonnull String tlsProtocols);

    /**
     * Add SslData object to Mdsal.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param odlKeystoreName odl Keystore Name
     * @param odlKeystorePwd odl Keystore Password
     * @param odlKeystoreAlias odl Keystore Alias
     * @param odlKeystoreDname odl Keystore Dname
     * @param odlKeystoreKeyAlg Key algorithm to create secret key i.e RSA
     * @param odlKeystoreSignAlg sign algorithm i.e SHA1WithRSAEncryption
     * @param odlKeystoreKeysize the key size i.e 1024
     * @param odlKeystoreValidity validity of the key
     * @param trustKeystoreName Trust Keystore Name
     * @param trustKeystorePwd Trust Keystore Password
     * @param cipherSuites cipher suites that will be used by the SSL connection
     * @param tlsProtocols supported TLS protocols such as SSLv2Hello,TLSv1.1,TLSv1.2
     *     protocols should be separated by ","
     * @return the created SslData object
     */
    SslData addSslDataKeystores(@Nonnull String bundleName, @Nonnull String odlKeystoreName,
            @Nonnull String odlKeystorePwd, @Nonnull String odlKeystoreAlias, @Nonnull String odlKeystoreDname,
            @Nonnull String odlKeystoreKeyAlg, @Nonnull String odlKeystoreSignAlg, @Nonnull int odlKeystoreKeysize,
            @Nonnull int odlKeystoreValidity, @Nonnull String trustKeystoreName, @Nonnull String trustKeystorePwd,
            @Nonnull String[] cipherSuites, @Nonnull String tlsProtocols);

    /**
     * add a ODL signed certificate that is signed by a CA based on a certificate request generated by
     * ODL keystore.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param alias for the certificate
     * @param certificate as string
     * @return true for successful added certificate
     */
    boolean addODLStoreSignedCertificate(@Nonnull String bundleName, @Nonnull String alias,
            @Nonnull String certificate);

    /**
     * Add a network node certificate to the trust keystore.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param alias certificate alias
     * @param certificate as string
     * @return true for successful added certificate
     */
    boolean addTrustNodeCertificate(@Nonnull String bundleName, @Nonnull String alias, @Nonnull String certificate);

    /**
     * Export the ODL keystore and Trust keystore to a file under karaf ssl/ directory.
     *
     * @param bundleName name of the bundle that will use the keystores
     */
    void exportSslDataKeystores(@Nonnull String bundleName);

    /**
     * Generate a certificate request to be signed by a CA with default sign algorithm SHA1WithRSAEncryption.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param withTag boolean to add cert-req tag to the return string
     * @return certificate request as string
     */
    String genODLKeyStoreCertificateReq(@Nonnull String bundleName, boolean withTag);

    /**
     * Get the ODL keystore certificate.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param withTag  boolean to add cert tag to the return string
     * @return certificate as string
     */
    @Nullable
    String getODLStoreCertificate(@Nonnull String bundleName, boolean withTag);

    /**
     * Get a Network node certificate from the Trust keystore.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param alias of the certificate
     * @param withTag  boolean to add cert tag to the return string
     * @return certificate as string
     */
    @Nullable
    String getTrustStoreCertificate(@Nonnull String bundleName, @Nonnull String alias,  boolean withTag);

    /**
     * Get the SslData.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @return the SslData
     */
    SslData getSslData(@Nonnull String bundleName);

    /**
     * Get the ODL keystore object to be used by the SSLContext to establish the SSL connection.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @return ODL keystore
     */
    KeyStore getODLKeyStore(@Nonnull String bundleName);

    /**
     * Get the trust keystore object to be used by the SSLContext to establish the SSL connection.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @return Trust keystore
     */
    KeyStore getTrustKeyStore(@Nonnull String bundleName);

    /**
     * Get array of cipher suites that will be used in to establish the SSL connection.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @return Cipher Suites
     */
    String[] getCipherSuites(@Nonnull String bundleName);

    /**
     * Get list of the supported TLS protocols.
     *
     * @param bundleName of the required TLS protocols
     * @return TLS protocols
     */
    String[] getTlsProtocols(@Nonnull String bundleName);

    /**
     * Create SslData based on pre-established keystores for ODL and network nodes.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @param odlKeystoreName odl Keystore Name
     * @param odlKeystorePwd must be the same as the imported ODL keystore's password
     * @param odlKeystoreAlias must be the same as the imported ODL keystore's certificate alias
     * @param odlKeyStore a valid keystore
     * @param trustKeystoreName trust Keystore Name
     * @param trustKeystorePwd must be the same as the imported Trust keystore's password
     * @param trustKeyStore a valid keystore
     * @param cipherSuites cipher suites that will be used by the SSL connection
     * @param tlsProtocols supported TLS protocols such as SSLv2Hello,TLSv1.1,TLSv1.2
     *     protocols should be separated by ","
     * @return the created SslData object
     */
    SslData importSslDataKeystores(@Nonnull String bundleName, @Nonnull String odlKeystoreName,
            @Nonnull String odlKeystorePwd, @Nonnull String odlKeystoreAlias, @Nonnull KeyStore odlKeyStore,
            @Nonnull String trustKeystoreName, @Nonnull String trustKeystorePwd, @Nonnull KeyStore trustKeyStore,
            @Nonnull String[] cipherSuites, @Nonnull String tlsProtocols);

    /**
     * Delete the SslData from Mdsal.
     *
     * @param bundleName name of the bundle that will use the keystores
     * @return true for succeed delete
     */
    boolean removeSslData(@Nonnull String bundleName);

    /**
     * Update the SslData.
     *
     * @param sslData SSL Data
     * @return the updated SslData object
     */
    SslData updateSslData(@Nonnull SslData sslData);

    /**
     * Initialize the Keystore data tree at Mdsal.
     */
    void initializeKeystoreDataTree();
}
