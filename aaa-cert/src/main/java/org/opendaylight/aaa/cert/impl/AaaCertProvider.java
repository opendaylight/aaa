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
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.AaaCertServiceConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.CtlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.rev151126.aaa.cert.service.config.TrustKeystoreBuilder;
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

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertProvider.class);
    private CtlKeystore ctlKeyStore;
    private final ODLKeyTool odlKeyTool;
    private TrustKeystore trustKeyStore;

    public AaaCertProvider(final AaaCertServiceConfig aaaCertServiceConfig) {
        odlKeyTool = new ODLKeyTool();
        this.ctlKeyStore = aaaCertServiceConfig.getCtlKeystore();
        this.trustKeyStore = aaaCertServiceConfig.getTrustKeystore();
        if (aaaCertServiceConfig.isUseConfig() && !KeyStoreConstant.checkKeyStoreFile(ctlKeyStore.getName())) {
            LOG.info("Creating keystore based on given configuration");
            this.createODLKeyStore();
            this.createTrustKeyStore();
        }
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
        return odlKeyTool.addCertificate(ctlKeyStore.getName(), storePasswd, certificate, alias);
    }

    @Override
    public boolean addCertificateODLKeyStore(final String alias, final String certificate) {
        return addCertificateODLKeyStore(ctlKeyStore.getStorePassword(), alias, certificate);
    }

    @Override
    public boolean addCertificateTrustStore(final String storePasswd, final String alias, final String certificate) {
        return odlKeyTool.addCertificate(trustKeyStore.getName(), storePasswd, certificate, alias);
    }

    @Override
    public boolean addCertificateTrustStore(final String alias, final String certificate) {
        return addCertificateTrustStore(trustKeyStore.getStorePassword(), alias, certificate);
    }

    @Override
    public void createODLKeyStore() {
        createODLKeyStore(ctlKeyStore.getName(),ctlKeyStore.getStorePassword(), ctlKeyStore.getAlias(),
                  ctlKeyStore.getDname(), ctlKeyStore.getValidity());
    }

    @Override
    public String createODLKeyStore(final String keyStore, final String storePasswd, final String alias,
            final String dName, final int validity) {
        ctlKeyStore = new CtlKeystoreBuilder().setAlias(alias)
                                              .setDname(dName)
                                              .setName(keyStore)
                                              .setStorePassword(storePasswd)
                                              .setValidity(validity)
                                              .build();
        if(odlKeyTool.createKeyStoreWithSelfSignCert(keyStore, storePasswd, dName, alias, validity)) {
            return keyStore + " Keystore created.";
        } else {
            return "Failed to create keystore " + keyStore;
        }
    }

    
    @Override
    public void createTrustKeyStore() {
        odlKeyTool.createKeyStoreImportCert(trustKeyStore.getName(), trustKeyStore.getStorePassword(),
                trustKeyStore.getCertFile(), trustKeyStore.getAlias());
    }

    @Override
    public String createTrustKeyStore(final String keyStore, final String storePasswd, final String alias) {
        trustKeyStore = new TrustKeystoreBuilder().setAlias(alias)
                                                  .setName(keyStore)
                                                  .setStorePassword(storePasswd)
                                                  .build();
        if(odlKeyTool.createKeyStoreImportCert(keyStore, storePasswd, trustKeyStore.getCertFile(), alias)) {
            return keyStore + " Keystore created.";
        } else {
            return "Failed to create keystore " + keyStore;
        }
    }

    @Override
    public String genODLKeyStorCertificateReq(final String storePasswd, final String alias, final boolean withTag) {
        return odlKeyTool.generateCertificateReq(ctlKeyStore.getName(), storePasswd,
                     alias, KeyStoreConstant.DEFAULT_SIGN_ALG, withTag);
    }

    @Override
    public String genODLKeyStorCertificateReq(final String alias, final boolean withTag) {
        return genODLKeyStorCertificateReq(ctlKeyStore.getStorePassword(), alias, withTag);
    }

    @Override
    public String getCertificateTrustStore(final String storePasswd, final String aliase, final boolean withTag) {
        return odlKeyTool.getCertificate(trustKeyStore.getName(), storePasswd, aliase, withTag);
    }

    @Override
    public String getCertificateTrustStore(final String aliase, final boolean withTag) {
        return getCertificateTrustStore(trustKeyStore.getStorePassword(), aliase, withTag);
    }

    @Override
    public String getODLKeyStorCertificate(final String storePasswd, final String alias, final boolean withTag) {
        return odlKeyTool.getCertificate(ctlKeyStore.getName(), storePasswd, alias, withTag);
    }

    @Override
    public String getODLKeyStorCertificate(final String alias, final boolean withTag) {
        return odlKeyTool.getCertificate(ctlKeyStore.getName(), ctlKeyStore.getStorePassword(), alias, withTag);
    }

    @Override
    public KeyStore getODLKeyStore() {
        return odlKeyTool.getKeyStore(ctlKeyStore.getName(), ctlKeyStore.getStorePassword());
    }

    @Override
    public KeyStore getTrustKeyStore() {
        return odlKeyTool.getKeyStore(trustKeyStore.getName(), trustKeyStore.getStorePassword());
    }

    @Override
    public String[] getCipherSuites() {
        List<String> suites = new ArrayList<>();
        if (ctlKeyStore.getCipherSuites() != null && !ctlKeyStore.getCipherSuites().isEmpty()) {
            for (CipherSuites cipherSuite : ctlKeyStore.getCipherSuites()) {
                suites.add(cipherSuite.getSuiteName());
            }
        }
        return (String[]) suites.toArray();
    }
}