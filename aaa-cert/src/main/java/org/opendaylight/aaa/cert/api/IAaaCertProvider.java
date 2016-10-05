/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import javax.annotation.Nonnull;
import java.security.KeyStore;

/**
 * IAaaCertProvider defines the basic operation for certificates management
 *
 * @author mserngawy
 *
 */
public interface IAaaCertProvider {

    /**
     * Add certificate to ODL keystore, the certificate should be signed by a CA (Certificate Authority) based on a certificate
     * request generated by the ODL keystore.
     *
     * @param storePasswd ODL keystore password
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true at successful adding certificate
     */
    boolean addCertificateODLKeyStore(@Nonnull String storePasswd, @Nonnull String alias, @Nonnull String certificate);

    /**
     * Add certificate to Trust keystore.
     *
     * @param storePasswd ODL keystore password
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true at successful adding certificate
     */
    boolean addCertificateTrustStore(@Nonnull String storePasswd, @Nonnull String alias, @Nonnull String certificate);

    /**
     * Create ODL keyStore.
     *
     * @param keyStoreName keystore Name
     * @param storePasswd keystore password
     * @param alias key alias
     * @param dName of the keystore
     * @param validity of the keystore
     * @return succeed or failed message
     */
    @Nonnull String createODLKeyStore(@Nonnull String keyStoreName, @Nonnull String storePasswd, @Nonnull String alias, @Nonnull String dName, int validity);

    /**
     * Create Trust keyStore.
     *
     * @param keyStoreName keystore Name
     * @param storePasswd keystore password
     * @param alias key alias
     * @return succeed or failed message
     */
    @Nonnull String createTrustKeyStore(@Nonnull String keyStoreName, @Nonnull String storePasswd, @Nonnull String alias);

    /**
     * Generate certificate request from the ODL keystore to be signed by a CA
     *
     * @param storePasswd ODL keystore password
     * @param alias ODL keystore alias
     * @return the certificate request
     */
    @Nonnull String genODLKeyStorCertificateReq(@Nonnull String storePasswd, @Nonnull String alias);

    /**
     * Get certificate from the Trust keystore
     *
     * @param storePasswd Trust keystore password
     * @param aliase the certificate alias
     * @return certificate
     */
    @Nonnull String getCertificateTrustStore(@Nonnull String storePasswd, @Nonnull String aliase);

    /**
     * Get ODL keystore certificate.
     *
     * @param storePasswd ODL keystore password
     * @param alias ODL keystore alias
     * @return certificate
     */
    @Nonnull String getODLKeyStorCertificate(@Nonnull String storePasswd, @Nonnull String alias);

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
     * @return
     */
    String[] getCipherSuites();
}