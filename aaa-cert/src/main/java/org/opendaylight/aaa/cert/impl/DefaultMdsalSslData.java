/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;
import java.util.List;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.ctlkeystore.CipherSuites;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the default Mdsal SslData for ODL based on the configuration exist on aaa-cert-config.xml
 *
 * @author mserngawy
 *
 */
public class DefaultMdsalSslData implements IAaaCertProvider{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMdsalSslData.class);

    private final AaaCertMdsalProvider aaaCertMdsalProv;
    private final CtlKeystore ctlKeyStore;
    private final TrustKeystore trustKeyStore;
    private final String errorMessage = "password is not correct or keystore has been corrupted";
    public final String bundleName;

    public DefaultMdsalSslData(final AaaCertMdsalProvider aaaCertMdsalProv, final String bundleName,
            final CtlKeystore ctlKeyStore, final TrustKeystore trustKeyStore) {
        this.bundleName = bundleName;
        this.aaaCertMdsalProv = aaaCertMdsalProv;
        this.ctlKeyStore = ctlKeyStore;
        this.trustKeyStore = trustKeyStore;
        this.aaaCertMdsalProv.initializeKeystoreDataTree();
    }

    @Override
    public boolean addCertificateODLKeyStore(String storePasswd, String alias, String certificate) {
        if (!aaaCertMdsalProv.getSslData(bundleName).getOdlKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(errorMessage);
            return false;
        }
        return aaaCertMdsalProv.addODLStoreSignedCertificate(bundleName, alias, certificate);
    }

    @Override
    public boolean addCertificateODLKeyStore(String alias, String certificate) {
        return aaaCertMdsalProv.addODLStoreSignedCertificate(bundleName, alias, certificate);
    }

    @Override
    public boolean addCertificateTrustStore(String storePasswd, String alias, String certificate) {
        if (aaaCertMdsalProv.getSslData(bundleName).getTrustKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(errorMessage);
            return false;
        }
        return aaaCertMdsalProv.addTrustNodeCertificate(bundleName, alias, certificate);
    }

    @Override
    public boolean addCertificateTrustStore(String alias, String certificate) {
        return aaaCertMdsalProv.addTrustNodeCertificate(bundleName, alias, certificate);
    }

    @Override
    public String genODLKeyStoreCertificateReq(String storePasswd, boolean withTag) {
        if (!aaaCertMdsalProv.getSslData(bundleName).getOdlKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(errorMessage);
            return "";
        }
        return aaaCertMdsalProv.genODLKeyStoreCertificateReq(bundleName, withTag);
    }

    @Override
    public String genODLKeyStoreCertificateReq(boolean withTag) {
        return aaaCertMdsalProv.genODLKeyStoreCertificateReq(bundleName, withTag);
    }

    @Override
    public String getCertificateTrustStore(String storePasswd, String alias, boolean withTag) {
        if (!aaaCertMdsalProv.getSslData(bundleName).getTrustKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(errorMessage);
            return "";
        }
        return aaaCertMdsalProv.getTrustStoreCertificate(bundleName, alias, withTag);
    }

    @Override
    public String getCertificateTrustStore(String alias, boolean withTag) {
        return aaaCertMdsalProv.getTrustStoreCertificate(bundleName, alias, withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(String storePasswd, boolean withTag) {
        if (!aaaCertMdsalProv.getSslData(bundleName).getOdlKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(errorMessage);
            return "";
        }
        return aaaCertMdsalProv.getODLStoreCertificate(bundleName, withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(final boolean withTag) {
        return aaaCertMdsalProv.getODLStoreCertificate(bundleName, withTag);
    }

    @Override
    public KeyStore getODLKeyStore() {
        return aaaCertMdsalProv.getODLKeyStore(bundleName);
    }

    @Override
    public KeyStore getTrustKeyStore() {
        return aaaCertMdsalProv.getTrustKeyStore(bundleName);
    }

    @Override
    public String[] getCipherSuites() {
        return aaaCertMdsalProv.getCipherSuites(bundleName);
    }

    @Override
    public TrustKeystore getTrustKeyStoreInfo() {
        return trustKeyStore;
    }

    @Override
    public CtlKeystore getOdlKeyStoreInfo() {
        return ctlKeyStore;
    }

    @Override
    public boolean createKeyStores() {
        return aaaCertMdsalProv.addSslDataKeystores(bundleName, ctlKeyStore.getName(), ctlKeyStore.getStorePassword(),
                ctlKeyStore.getAlias(), ctlKeyStore.getDname(), ctlKeyStore.getKeyAlg(), ctlKeyStore.getSignAlg(),
                ctlKeyStore.getKeysize(), ctlKeyStore.getValidity(), trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                getCipherSuites(ctlKeyStore.getCipherSuites())) != null;
    }

    private String[] getCipherSuites(List<CipherSuites> cipherSuites) {
        String[] suites = null;
        if (cipherSuites != null & !cipherSuites.isEmpty()) {
            suites = new String[cipherSuites.size()];
            for(int i = 0 ; i < cipherSuites.size() ; i++) {
                suites[i] = cipherSuites.get(i).getSuiteName();
            }
        }
        return suites;
    }
}
