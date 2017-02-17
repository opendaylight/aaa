/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.ctlkeystore.CipherSuites;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AaaCertProvider use to manage the certificates manipulation operations add, revoke and update
 *
 * @author mserngawy
 *
 */
public class AaaCertProvider implements IAaaCertProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AaaCertProvider.class);

    private final CtlKeystore ctlKeyStore;
    private final ODLKeyTool odlKeyTool;
    private final TrustKeystore trustKeyStore;

    public AaaCertProvider(final CtlKeystore ctlKeyStore, final TrustKeystore trustKeyStore) {
        odlKeyTool = new ODLKeyTool();
        this.ctlKeyStore = ctlKeyStore;
        this.trustKeyStore = trustKeyStore;
        LOG.info("aaa Certificate Service Initalized");
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
    public boolean addCertificateODLKeyStore(final String storePasswd, final String alias, final String certificate) {
        final KeyStore keyStore = odlKeyTool.addCertificate(odlKeyTool.loadKeyStore(ctlKeyStore.getName(), storePasswd), certificate, alias, true);
        return odlKeyTool.exportKeystore(keyStore, storePasswd, ctlKeyStore.getName());
    }

    @Override
    public boolean addCertificateODLKeyStore(final String alias, final String certificate) {
        return addCertificateODLKeyStore(ctlKeyStore.getStorePassword(), alias, certificate);
    }

    @Override
    public boolean addCertificateTrustStore(final String storePasswd, final String alias, final String certificate) {
        final KeyStore keyStore = odlKeyTool.addCertificate(odlKeyTool.loadKeyStore(trustKeyStore.getName(), storePasswd), certificate, alias, true);
        return odlKeyTool.exportKeystore(keyStore, storePasswd, trustKeyStore.getName());
    }

    @Override
    public boolean addCertificateTrustStore(final String alias, final String certificate) {
        return addCertificateTrustStore(trustKeyStore.getStorePassword(), alias, certificate);
    }

    @Override
    public boolean createKeyStores() {
        if (!KeyStoreConstant.checkKeyStoreFile(ctlKeyStore.getName())) {
            final KeyStore keyStore = odlKeyTool.createKeyStoreWithSelfSignCert(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(), ctlKeyStore.getDname(),
                    ctlKeyStore.getAlias(), ctlKeyStore.getValidity(), ctlKeyStore.getKeyAlg(), ctlKeyStore.getKeysize(), ctlKeyStore.getSignAlg());
             if(!odlKeyTool.exportKeystore(keyStore, ctlKeyStore.getStorePassword(), ctlKeyStore.getName())) {
                return false;
             }
        }
        if (!KeyStoreConstant.checkKeyStoreFile(trustKeyStore.getName())) {
            final KeyStore keyStore = odlKeyTool.createEmptyKeyStore(trustKeyStore.getStorePassword());
            if (!odlKeyTool.exportKeystore(keyStore, trustKeyStore.getStorePassword(), trustKeyStore.getName()))
                return false;
        }
        return true;
    }

    @Override
    public String genODLKeyStoreCertificateReq(final String storePasswd, final boolean withTag) {
        return odlKeyTool.generateCertificateReq(odlKeyTool.loadKeyStore(ctlKeyStore.getName(), storePasswd),
                storePasswd, ctlKeyStore.getAlias(), ctlKeyStore.getSignAlg(), withTag);
    }

    @Override
    public String genODLKeyStoreCertificateReq(final boolean withTag) {
        return genODLKeyStoreCertificateReq(ctlKeyStore.getStorePassword(), withTag);
    }

    @Override
    public String getCertificateTrustStore(final String storePasswd, final String aliase, final boolean withTag) {
        return odlKeyTool.getCertificate(odlKeyTool.loadKeyStore(trustKeyStore.getName(), storePasswd), aliase, withTag);
    }

    @Override
    public String getCertificateTrustStore(final String aliase, final boolean withTag) {
        return getCertificateTrustStore(trustKeyStore.getStorePassword(), aliase, withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(final String storePasswd, final boolean withTag) {
        return odlKeyTool.getCertificate(odlKeyTool.loadKeyStore(ctlKeyStore.getName(), storePasswd), ctlKeyStore.getAlias(), withTag);
    }

    @Override
    public String getODLKeyStoreCertificate(final boolean withTag) {
        return getODLKeyStoreCertificate(ctlKeyStore.getStorePassword(), withTag);
    }

    @Override
    public KeyStore getODLKeyStore() {
        return odlKeyTool.loadKeyStore(ctlKeyStore.getName(), ctlKeyStore.getStorePassword());
    }

    @Override
    public KeyStore getTrustKeyStore() {
        return odlKeyTool.loadKeyStore(trustKeyStore.getName(), trustKeyStore.getStorePassword());
    }

    @Override
    public String[] getCipherSuites() {
        final List<CipherSuites> cipherSuites = ctlKeyStore.getCipherSuites();
        if ( cipherSuites != null && !cipherSuites.isEmpty()) {
            final List<String> suites = new ArrayList<String>();
            cipherSuites.stream().forEach(cs -> {
                if (!cs.getSuiteName().isEmpty()) {
                    suites.add(cs.getSuiteName());
                }
            });
            return suites.toArray(new String[suites.size()]);
        }
        return null;
    }

    @Override
    public String[] getTlsProtocols() {
        String tlsProtocols = ctlKeyStore.getTlsProtocols();
        if (tlsProtocols != null && !tlsProtocols.isEmpty()) {
            // remove white spaces in tlsProtocols string
            tlsProtocols = tlsProtocols.replace(" ", "");
            if (tlsProtocols.contains(",")) {
                return tlsProtocols.split(",");
            } else {
                return new String[] {tlsProtocols};
            }
        }
        return null;
    }
}