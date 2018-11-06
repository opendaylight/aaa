/*
 * Copyright (c) 2015, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;

/**
 * IAaaCertProvider defines the basic operation for certificates management.
 *
 * @author mserngawy
 */
public interface IAaaCertProvider {

    /**
     * Add certificate to ODL keystore, the certificate should be signed by a CA
     * (Certificate Authority) based on a certificate request generated by the ODL keystore.
     *
     * @param storePasswd ODL keystore password
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true at successful adding certificate
     */
    boolean addCertificateODLKeyStore(@NonNull String storePasswd, @NonNull String alias, @NonNull String certificate);

    /**
     * Add certificate to ODL keystore, the certificate should be signed
     * by a CA (Certificate Authority) based on a certificate request generated by the ODL keystore.
     *
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true at successful adding certificate
     */
    boolean addCertificateODLKeyStore(@NonNull String alias, @NonNull String certificate);

    /**
     * Add certificate to Trust keystore.
     *
     * @param storePasswd ODL keystore password
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true at successful adding certificate
     */
    boolean addCertificateTrustStore(@NonNull String storePasswd, @NonNull String alias, @NonNull String certificate);

    /**
     * Add certificate to Trust keystore.
     *
     * @param alias certificate alias
     * @param certificate certificate @Nonnull String
     * @return true if certificate was added successfully
     */
    boolean addCertificateTrustStore(@NonNull String alias, @NonNull String certificate);

    /**
     * Generate certificate request from the ODL keystore to be signed by a CA.
     *
     * @param storePasswd ODL keystore password
     * @param withTag return the certificate Req string with tag if true
     * @return the certificate request
     */
    @NonNull String genODLKeyStoreCertificateReq(@NonNull String storePasswd, boolean withTag);

    /**
     * Generate certificate request from the ODL keystore to be signed by a CA.
     *
     * @param withTag return the certificate Req string with tag if true
     * @return the certificate request
     */
    @NonNull String genODLKeyStoreCertificateReq(boolean withTag);

    /**
     * Get certificate from the Trust keystore.
     *
     * @param storePasswd Trust keystore password
     * @param alias the certificate alias
     * @param withTag return the certificate string with tag if true
     * @return the certificate
     */
    @NonNull String getCertificateTrustStore(@NonNull String storePasswd, @NonNull String alias, boolean withTag);

    /**
     * Get certificate from the Trust keystore.
     *
     * @param alias the certificate alias
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @NonNull String getCertificateTrustStore(@NonNull String alias, boolean withTag);

    /**
     * Get ODL keystore certificate.
     *
     * @param storePasswd ODL keystore password
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @NonNull String getODLKeyStoreCertificate(@NonNull String storePasswd, boolean withTag);

    /**
     * Get ODL keystore certificate.
     *
     * @param withTag return certificate string with tag if true
     * @return the certificate
     */
    @NonNull String getODLKeyStoreCertificate(boolean withTag);

    /**
     * Get ODL Keystore as java keystore object.
     *
     * @return ODL keystore
     */
    KeyStore getODLKeyStore();

    /**
     * Get Trust Keystore as java keystore object.
     *
     * @return Trust keystore
     */
    KeyStore getTrustKeyStore();

    /**
     * Get list of of the allowed cipher suites otherwise empty array.
     *
     * @return Cipher suites
     */
    String[] getCipherSuites();

    /**
     * Get list of the supported TLS protocols.
     *
     * @return TLS protocols
     */
    String[] getTlsProtocols();

    /**
     * Get the Trust key store Data.
     *
     * @return Trust Keystore Object
     */
    TrustKeystore getTrustKeyStoreInfo();

    /**
     * Get the ODL key store Data.
     *
     * @return Ctl Keystore Object
     */
    CtlKeystore getOdlKeyStoreInfo();

    /**
     * Create the ODL and Trust keystores based on the CtlKeystore and TrustKeystore data.
     *
     * @return true if success
     */
    boolean createKeyStores();
}
