/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.ctlkeystore.CipherSuites;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultMdsalSslData Implements the default Mdsal SslData based on the
 * configuration exist in the aaa-cert-config.xml.
 *
 * @author mserngawy
 *
 */
public class DefaultMdsalSslData implements IAaaCertProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMdsalSslData.class);
    private static final String ERROR_MESSAGE = "password is not correct or keystore has been corrupted";

    private final AaaCertMdsalProvider aaaCertMdsalProv;
    private final CtlKeystore ctlKeyStore;
    private final TrustKeystore trustKeystore;
    private final String bundleName;

    public DefaultMdsalSslData(final AaaCertMdsalProvider aaaCertMdsalProv, final String bundleName,
            final CtlKeystore ctlKeyStore, final TrustKeystore trustKeystore) {
        this.bundleName = requireNonNull(bundleName);
        this.aaaCertMdsalProv = aaaCertMdsalProv;
        this.ctlKeyStore = ctlKeyStore;
        this.trustKeystore = trustKeystore;
        this.aaaCertMdsalProv.initializeKeystoreDataTree();
    }

    @Override
    public boolean addCertificateODLKeyStore(String storePasswd, String alias, String certificate) {
        if (!aaaCertMdsalProv.getSslData(bundleName).getOdlKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(ERROR_MESSAGE);
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
            LOG.debug(ERROR_MESSAGE);
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
            LOG.debug(ERROR_MESSAGE);
            return StringUtils.EMPTY;
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
            LOG.debug(ERROR_MESSAGE);
            return StringUtils.EMPTY;
        }
        return aaaCertMdsalProv.getTrustStoreCertificate(bundleName, alias, withTag);
    }

    @Override
    public String getCertificateTrustStore(String alias, boolean withTag) {
        return requireNonNullElse(aaaCertMdsalProv.getTrustStoreCertificate(bundleName, alias, withTag), "");
    }

    @Override
    public String getODLKeyStoreCertificate(String storePasswd, boolean withTag) {
        if (!aaaCertMdsalProv.getSslData(bundleName).getOdlKeystore().getStorePassword().equals(storePasswd)) {
            LOG.debug(ERROR_MESSAGE);
            return StringUtils.EMPTY;
        }
        return aaaCertMdsalProv.getODLStoreCertificate(bundleName, withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(final boolean withTag) {
        return requireNonNullElse(aaaCertMdsalProv.getODLStoreCertificate(bundleName, withTag), "");
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

    private static String[] getCipherSuites(final List<CipherSuites> cipherSuites) {
        final var suites = new ArrayList<String>();
        if (cipherSuites != null && !cipherSuites.isEmpty()) {
            cipherSuites.forEach(cs -> suites.add(cs.getSuiteName()));
        }
        return suites.toArray(new String[0]);
    }

    @Override
    public TrustKeystore getTrustKeyStoreInfo() {
        return trustKeystore;
    }

    @Override
    public CtlKeystore getOdlKeyStoreInfo() {
        return ctlKeyStore;
    }

    @Override
    public boolean createKeyStores() {
        if (aaaCertMdsalProv.getSslData(bundleName) == null) {
            return aaaCertMdsalProv.addSslDataKeystores(bundleName, ctlKeyStore.getName(),
                    ctlKeyStore.getStorePassword(), ctlKeyStore.getAlias(), ctlKeyStore.getDname(),
                    ctlKeyStore.getKeyAlg(), ctlKeyStore.getSignAlg(), ctlKeyStore.getKeysize(),
                    ctlKeyStore.getValidity(), trustKeystore.getName(), trustKeystore.getStorePassword(),
                    getCipherSuites(ctlKeyStore.getCipherSuites()), ctlKeyStore.getTlsProtocols()) != null;
        }
        return true;
    }

    @Override
    public String[] getTlsProtocols() {
        return aaaCertMdsalProv.getTlsProtocols(bundleName);
    }

    public void exportSslDataKeystores() {
        aaaCertMdsalProv.exportSslDataKeystores(bundleName);
    }

    public boolean importSslDataKeystores(String odlKeystoreName, String odlKeystorePwd, String odlKeystoreAlias,
            String trustKeystoreName, String trustKeystorePwd, String[] cipherSuites, String tlsProtocols) {
        final ODLKeyTool keyTool = new ODLKeyTool();
        final KeyStore odlKeyStore = keyTool.loadKeyStore(odlKeystoreName, odlKeystorePwd);
        final KeyStore trustKeyStore = keyTool.loadKeyStore(trustKeystoreName, trustKeystorePwd);
        return aaaCertMdsalProv.importSslDataKeystores(bundleName, odlKeystoreName, odlKeystorePwd, odlKeystoreAlias,
                odlKeyStore, trustKeystoreName, trustKeystorePwd, trustKeyStore, cipherSuites, tlsProtocols) != null;
    }
}
