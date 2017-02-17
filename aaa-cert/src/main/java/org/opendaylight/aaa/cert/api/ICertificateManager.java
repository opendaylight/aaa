/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

/**
 * ICertifcateManager defines the basic functions that are consumed by other bundles to establish the SSLContext
 *
 * @author mserngawy
 *
 */
public interface ICertificateManager {

    /**
     * Get ODL Keystore as java keystore object
     *
     * @return ODL keystore
     */
    KeyStore getODLKeyStore();

    /**
     * Get Trust Keystore as java keystore object
     *
     * @return Trust keystore
     */
    KeyStore getTrustKeyStore();

    /**
     * Get list of of the allowed cipher suites otherwise empty array
     *
     * @return Cipher suites
     */
    String[] getCipherSuites();

    /**
     * Get list of the supported TLS protocols
     *
     * @return
     */
    String[] getTlsProtocols();

    /**
     * Get certificate from the Trust keystore
     *
     * @param storePasswd Trust keystore password
     * @param alias the certificate alias
     * @param withTag return the certificate string with tag if true
     * @return the certificate
     */
    @Nonnull String getCertificateTrustStore(@Nonnull String storePasswd, @Nonnull String alias, boolean withTag);

    /**
     * Get ODL keystore certificate.
     *
     * @param storePasswd ODL keystore password
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @Nonnull String getODLKeyStoreCertificate(@Nonnull String storePasswd, boolean withTag);

    /**
     * Generate certificate request from the ODL keystore to be signed by a CA
     *
     * @param storePasswd ODL keystore password
     * @param withTag return the certificate Req string with tag if true
     * @return the certificate request
     */
    @Nonnull String genODLKeyStoreCertificateReq(@Nonnull String storePasswd, boolean withTag);

    /**
     * Get the SSL Context that will be used to establish the connection
     *
     * @return SSLContext object
     */
    SSLContext getServerContext();

    /**
     * Create SslData based on pre-established keystores for ODL and network nodes.
     *
     * @param odlKeystoreName odl Keystore Name
     * @param odlKeystorePwd must be the same as the imported ODL keystore's password
     * @param odlKeystoreAlias must be the same as the imported ODL keystore's certificate alias
     * @param trustKeystoreName trust Keystore Name
     * @param trustKeystorePwd must be the same as the imported Trust keystore's password
     * @param cipherSuites cipher suites that will be used by the SSL connection
     * @param tlsProtocols supported TLS protocols such as SSLv2Hello,TLSv1.1,TLSv1.2 protocols should be separated by ","
     * @return true if succeed
     */
    boolean importSslDataKeystores(@Nonnull String odlKeystoreName, @Nonnull String odlKeystorePwd,
                        @Nonnull String odlKeystoreAlias, @Nonnull String trustKeystoreName,
                        @Nonnull String trustKeystorePwd, @Nonnull String[] cipherSuites,
                                   @Nonnull String tlsProtocols);

    /**
     * Export the ODL keystore and Trust keystore to keystore files under configuration/ssl/ directory
     *
     */
    void exportSslDataKeystores();

}
