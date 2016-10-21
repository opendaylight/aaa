/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;

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
     * Add certificate to ODL keystore, the certificate should be signed by a CA (Certificate Authority) based on a certificate
     * request generated by the ODL keystore.
     *
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true at successful adding certificate
     */
    boolean addCertificateODLKeyStore(@Nonnull String alias, @Nonnull String certificate);

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
     * Add certificate to Trust keystore.
     *
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true if certificate was added successfully
     */
    boolean addCertificateTrustStore(@Nonnull String alias, @Nonnull String certificate);

    /**
     * Generate certificate request from the ODL keystore to be signed by a CA
     *
     * @param storePasswd ODL keystore password
     * @param withTag return the certificate Req string with tag if true
     * @return the certificate request
     */
    @Nonnull String genODLKeyStoreCertificateReq(@Nonnull String storePasswd, boolean withTag);

    /**
     * Generate certificate request from the ODL keystore to be signed by a CA
     *
     * @param withTag return the certificate Req string with tag if true
     * @return the certificate request
     */
    @Nonnull String genODLKeyStoreCertificateReq(boolean withTag);

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
     * Get certificate from the Trust keystore
     *
     * @param alias the certificate alias
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @Nonnull String getCertificateTrustStore(@Nonnull String alias, final boolean withTag);

    /**
     * Get ODL keystore certificate.
     *
     * @param storePasswd ODL keystore password
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @Nonnull String getODLKeyStoreCertificate(@Nonnull String storePasswd, boolean withTag);

    /**
     * Get ODL keystore certificate
     *
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @Nonnull String getODLKeyStoreCertificate(boolean withTag);

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
     * Get the Trust key store Data
     *
     * @return Trust Keystore Object
     */
    TrustKeystore getTrustKeyStoreInfo();

    /**
     * Get the ODL key store Data
     *
     * @return Ctl Keystore Object
     */
    CtlKeystore getOdlKeyStoreInfo();

    /**
     * Create the ODL and Trust keystores based on the CtlKeystore and TrustKeystore data
     *
     * @return true if succeed otherwise false
     */
    boolean createKeyStores();
}