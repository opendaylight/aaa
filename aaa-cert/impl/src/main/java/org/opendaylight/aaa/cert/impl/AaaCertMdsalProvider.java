/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.aaa.cert.api.IAaaCertMdsalProvider;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStoresBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AaaCertMdsalProvider use to manage the certificates manipulation operations
 * using Mdsal as data store.
 *
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements IAaaCertMdsalProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);

    private final DataBroker dataBroker;
    private final KeyStoresDataUtils keyStoresData;
    private final ODLKeyTool odlKeyTool;

    public AaaCertMdsalProvider(final DataBroker dataBroker, final AAAEncryptionService encryptionSrv) {
        this.dataBroker = dataBroker;
        keyStoresData = new KeyStoresDataUtils(encryptionSrv);
        odlKeyTool = new ODLKeyTool();
        LOG.info("AaaCertMdsalProvider Initialized");
    }

    @Override
    public boolean addODLStoreSignedCertificate(final String bundleName, final String alias, final String certificate) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getOdlKeystore() != null) {
                KeyStore keystore = odlKeyTool.loadKeyStore(sslData.getOdlKeystore().getKeystoreFile(),
                        sslData.getOdlKeystore().getStorePassword());
                keystore = odlKeyTool.addCertificate(keystore, certificate, alias, false);
                if (keystore != null) {
                    final OdlKeystore odlKeystore = KeyStoresDataUtils.updateOdlKeystore(sslData.getOdlKeystore(),
                            odlKeyTool.convertKeystoreToBytes(keystore, sslData.getOdlKeystore().getStorePassword()));
                    return keyStoresData.updateSslDataOdlKeystore(dataBroker, sslData, odlKeystore);
                }
            }
        }
        return false;
    }

    @Override
    public SslData addSslDataKeystores(final String bundleName, final String odlKeystoreName,
            final String odlKeystorePwd, final String odlKeystoreAlias, final String odlKeystoreDname,
            final String odlKeystoreKeyAlg, final String odlKeystoreSignAlg, final int odlKeystoreKeysize,
            final int odlKeystoreValidity, final String trustKeystoreName, final String trustKeystorePwd,
            final String[] cipherSuites, final String tlsProtocols) {
        final OdlKeystore odlKeystore = keyStoresData.createOdlKeystore(odlKeystoreName, odlKeystoreAlias,
                odlKeystorePwd, odlKeystoreDname, odlKeystoreSignAlg, odlKeystoreKeyAlg, odlKeystoreValidity,
                odlKeystoreKeysize, odlKeyTool);
        final TrustKeystore trustKeystore = keyStoresData.createTrustKeystore(trustKeystoreName, trustKeystorePwd,
                odlKeyTool);
        final List<CipherSuites> cipherSuitesList = new ArrayList<>();
        if (cipherSuites != null) {
            for (final String suite : cipherSuites) {
                final CipherSuites cipherSuite = new CipherSuitesBuilder().setSuiteName(suite).build();
                cipherSuitesList.add(cipherSuite);
            }
        }
        return keyStoresData.addSslData(dataBroker, bundleName, odlKeystore, trustKeystore, cipherSuitesList,
                tlsProtocols);
    }

    @Override
    public SslData addSslDataKeystores(final String bundleName, final String odlKeystoreName,
            final String odlKeystorePwd, final String odlKeystoreAlias, final String odlKeystoreDname,
            final String trustKeystoreName, final String trustKeystorePwd, final String[] cipherSuites,
            final String tlsProtocols) {
        return addSslDataKeystores(bundleName, odlKeystoreName, odlKeystorePwd, odlKeystoreAlias, odlKeystoreDname,
                KeyStoreConstant.DEFAULT_KEY_ALG, KeyStoreConstant.DEFAULT_SIGN_ALG, KeyStoreConstant.DEFAULT_KEY_SIZE,
                KeyStoreConstant.DEFAULT_VALIDITY, trustKeystoreName, trustKeystorePwd, cipherSuites, tlsProtocols);
    }

    @Override
    public boolean addTrustNodeCertificate(final String bundleName, final String alias, final String certificate) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getTrustKeystore() != null) {
                KeyStore keystore = odlKeyTool.loadKeyStore(sslData.getTrustKeystore().getKeystoreFile(),
                        sslData.getTrustKeystore().getStorePassword());
                keystore = odlKeyTool.addCertificate(keystore, certificate, alias, true);
                if (keystore != null) {
                    final TrustKeystore trustKeystore = keyStoresData.updateTrustKeystore(sslData.getTrustKeystore(),
                            odlKeyTool.convertKeystoreToBytes(keystore, sslData.getTrustKeystore().getStorePassword()));
                    return keyStoresData.updateSslDataTrustKeystore(dataBroker, sslData, trustKeystore);
                }
            }
        }
        return false;
    }

    @Override
    public void exportSslDataKeystores(final String bundleName) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            KeyStore keystore = odlKeyTool.loadKeyStore(sslData.getTrustKeystore().getKeystoreFile(),
                    sslData.getTrustKeystore().getStorePassword());
            odlKeyTool.exportKeystore(keystore, sslData.getTrustKeystore().getStorePassword(),
                    bundleName + "_trustKeystore");
            keystore = odlKeyTool.loadKeyStore(sslData.getOdlKeystore().getKeystoreFile(),
                    sslData.getOdlKeystore().getStorePassword());
            odlKeyTool.exportKeystore(keystore, sslData.getOdlKeystore().getStorePassword(),
                    bundleName + "_odlKeystore");
        }
    }

    @Override
    public String genODLKeyStoreCertificateReq(final String bundleName, final boolean withTag) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            final OdlKeystore odlKeyStore = sslData.getOdlKeystore();
            final KeyStore keystore = odlKeyTool.loadKeyStore(odlKeyStore.getKeystoreFile(),
                    sslData.getOdlKeystore().getStorePassword());
            return odlKeyTool.generateCertificateReq(keystore, odlKeyStore.getStorePassword(), odlKeyStore.getAlias(),
                    odlKeyStore.getSignAlg(), withTag);
        }
        return null;
    }

    @Override
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public String[] getCipherSuites(final String bundleName) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null && sslData.getCipherSuites() != null && !sslData.getCipherSuites().isEmpty()) {
            final List<String> suites = new ArrayList<>();
            sslData.getCipherSuites().stream().forEach(cs -> {
                if (!cs.getSuiteName().isEmpty()) {
                    suites.add(cs.getSuiteName());
                }
            });
            return suites.toArray(new String[suites.size()]);
        }
        return null;
    }

    @Override
    public KeyStore getODLKeyStore(final String bundleName) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getOdlKeystore() != null) {
                return odlKeyTool.loadKeyStore(sslData.getOdlKeystore().getKeystoreFile(),
                        sslData.getOdlKeystore().getStorePassword());
            }
        }
        return null;
    }

    @Override
    public String getODLStoreCertificate(final String bundleName, final boolean withTag) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            final OdlKeystore odlKeyStore = sslData.getOdlKeystore();
            final KeyStore keystore = odlKeyTool.loadKeyStore(odlKeyStore.getKeystoreFile(),
                    odlKeyStore.getStorePassword());
            return odlKeyTool.getCertificate(keystore, odlKeyStore.getAlias(), withTag);
        }
        return null;
    }

    @Override
    public SslData getSslData(final String bundleName) {
        return keyStoresData.getSslData(dataBroker, bundleName);
    }

    @Override
    public KeyStore getTrustKeyStore(final String bundleName) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getTrustKeystore() != null) {
                return odlKeyTool.loadKeyStore(sslData.getTrustKeystore().getKeystoreFile(),
                        sslData.getTrustKeystore().getStorePassword());
            }
        }
        return null;
    }

    @Override
    public String getTrustStoreCertificate(final String bundleName, final String alias, final boolean withTag) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            final TrustKeystore trustKeyStore = sslData.getTrustKeystore();
            final KeyStore keystore = odlKeyTool.loadKeyStore(trustKeyStore.getKeystoreFile(),
                    trustKeyStore.getStorePassword());
            return odlKeyTool.getCertificate(keystore, alias, withTag);
        }
        return null;
    }

    @Override
    public SslData importSslDataKeystores(final String bundleName, final String odlKeystoreName,
            final String odlKeystorePwd, final String odlKeystoreAlias, final KeyStore odlKeyStore,
            final String trustKeystoreName, final String trustKeystorePwd, final KeyStore trustKeyStore,
            final String[] cipherSuites, final String tlsProtocols) {
        final OdlKeystore odlKeystore = keyStoresData.createOdlKeystore(odlKeystoreName, odlKeystoreAlias,
                odlKeystorePwd, odlKeyTool.convertKeystoreToBytes(odlKeyStore, odlKeystorePwd));
        final TrustKeystore trustKeystore = keyStoresData.createTrustKeystore(trustKeystoreName, trustKeystorePwd,
                odlKeyTool.convertKeystoreToBytes(trustKeyStore, trustKeystorePwd));
        final List<CipherSuites> cipherSuitesList = new ArrayList<>();
        for (final String suit : cipherSuites) {
            final CipherSuites cipherSuite = new CipherSuitesBuilder().setSuiteName(suit).build();
            cipherSuitesList.add(cipherSuite);
        }
        return keyStoresData.addSslData(dataBroker, bundleName, odlKeystore, trustKeystore, cipherSuitesList,
                tlsProtocols);
    }

    @Override
    public boolean removeSslData(final String bundleName) {
        return keyStoresData.removeSslData(dataBroker, bundleName);
    }

    @Override
    public SslData updateSslData(final SslData sslData) {
        if (keyStoresData.updateSslData(dataBroker, sslData)) {
            return sslData;
        }
        return null;
    }

    @Override
    public void initializeKeystoreDataTree() {
        if (MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION,
                KeyStoresDataUtils.getKeystoresIid()) == null) {
            final KeyStores keyStoreData = new KeyStoresBuilder().setId(KeyStoresDataUtils.KEYSTORES_DATA_TREE).build();
            MdsalUtils.initalizeDatastore(LogicalDatastoreType.CONFIGURATION, dataBroker,
                    KeyStoresDataUtils.getKeystoresIid(), keyStoreData);
        }
    }

    @Override
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public String[] getTlsProtocols(final String bundleName) {
        final SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            String tlsProtocols = sslData.getTlsProtocols();
            if (tlsProtocols != null && !tlsProtocols.isEmpty()) {
                // remove white spaces in tlsProtocols string
                tlsProtocols = tlsProtocols.replace(" ", "");
                if (tlsProtocols.contains(",")) {
                    return tlsProtocols.split(",");
                } else {
                    return new String[] { tlsProtocols };
                }
            }
        }
        return null;
    }
}
