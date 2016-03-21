/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.aaa.cert.api.IAaaCertMdsalProvider;
import org.opendaylight.aaa.cert.utils.KeyStoresDataUtils;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStoresBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements AutoCloseable, BindingAwareProvider, IAaaCertMdsalProvider {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);
    private ServiceRegistration<IAaaCertMdsalProvider> aaaCertMdsalServiceRegisteration;
    private DataBroker dataBroker;
    private final ODLMdsalKeyTool odlKeyTool;
    private KeyStoresDataUtils keyStoresData;

    public AaaCertMdsalProvider() {
        odlKeyTool = new ODLMdsalKeyTool();
        LOG.info("AaaCertMdsalProvider Initialized");
    }

    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        LOG.info("Aaa Certificate Mdsal Service Session Initiated");
        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        aaaCertMdsalServiceRegisteration = context.registerService(IAaaCertMdsalProvider.class, this, null);

        // Retrieve the data broker to create transactions
        dataBroker =  arg0.getSALService(DataBroker.class);
        KeyStores keyStoreData = new KeyStoresBuilder().setId(KeyStoresDataUtils.KEYSTORES_DATA_TREE).build();
        MdsalUtils.initalizeDatastore(LogicalDatastoreType.CONFIGURATION, dataBroker, KeyStoresDataUtils.getKeystoresIid(), keyStoreData);
        ServiceReference<?> serviceReference = context.getServiceReference(AAAEncryptionService.class);
        if (serviceReference != null) {
            AAAEncryptionService encryptionSrv = (AAAEncryptionService) context.getService(serviceReference);
            keyStoresData = new KeyStoresDataUtils(encryptionSrv);
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("Aaa Certificate Mdsal Service Closed");
        aaaCertMdsalServiceRegisteration.unregister();
    }

    @Override
    public SslData addSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, String odlKeystoreDname, String trustKeystoreName, String trustKeystorePwd,
            String[] cipherSuites) {
        return addSslDataKeystores(bundleName, odlKeystoreName, odlKeystorePwd, odlKeystoreAlias, odlKeystoreDname,
                KeyStoreConstant.defaultKeyAlg, KeyStoreConstant.defaultSignAlg, KeyStoreConstant.defaultKeySize,
                KeyStoreConstant.defaultValidity, trustKeystoreName, trustKeystorePwd, cipherSuites);
    }

    @Override
    public SslData addSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, String odlKeystoreDname, String odlKeystoreKeyAlg, String odlKeystoreSignAlg,
            int odlKeystoreKeysize, int odlKeystoreValidity, String trustKeystoreName, String trustKeystorePwd,
            String[] cipherSuites) {
        OdlKeystore odlKeystore = keyStoresData.createOdlKeystore(odlKeystoreName, odlKeystoreAlias, odlKeystorePwd,
                    odlKeystoreDname, odlKeystoreSignAlg, odlKeystoreKeyAlg, odlKeystoreValidity, odlKeystoreKeysize, odlKeyTool);
        TrustKeystore trustKeystore = keyStoresData.createTrustKeystore(trustKeystoreName, trustKeystorePwd, null, odlKeyTool);
        List<CipherSuites> cipherSuitesList = new ArrayList<>();
        if (cipherSuites != null) {
            for (String suit : cipherSuites) {
                CipherSuites cipherSuite = new CipherSuitesBuilder().setSuiteName(suit).build();
                cipherSuitesList.add(cipherSuite);
            }
        }
        return keyStoresData.addSslData(dataBroker, bundleName, odlKeystore, trustKeystore, cipherSuitesList);
    }

    @Override
    public KeyStore getODLKeyStore(String bundleName) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getOdlKeystore() != null) {
                return odlKeyTool.loadKeyStore(sslData.getOdlKeystore().getKeystoreFile(), sslData.getOdlKeystore().getStorePassword());
            }
        }
        return null;
    }

    @Override
    public KeyStore getTrustKeyStore(String bundleName) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getTrustKeystore() != null) {
                return odlKeyTool.loadKeyStore(sslData.getTrustKeystore().getKeystoreFile(), sslData.getTrustKeystore().getStorePassword());
            }
        }
        return null;
    }

    @Override
    public String[] getCipherSuites(String bundleName) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getCipherSuites() != null) {
                List<String> cipherSuites = new ArrayList<>();
                for (CipherSuites suite : sslData.getCipherSuites()) {
                    cipherSuites.add(suite.getSuiteName());
                }
                return (String[]) cipherSuites.toArray();
            }
        }
        return null;
    }

    @Override
    public boolean addODLStoreSignedCertificate(String bundleName, String alias, String certificate) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getOdlKeystore() != null) {
                KeyStore keystore = odlKeyTool.loadKeyStore(sslData.getOdlKeystore().getKeystoreFile(), sslData.getOdlKeystore().getStorePassword());
                keystore = odlKeyTool.addCertificate(keystore, certificate, alias, false);
                if (keystore != null) {
                    OdlKeystore odlKeystore = KeyStoresDataUtils.updateOdlKeystore(sslData.getOdlKeystore(), odlKeyTool.convertKeystoreToBytes(keystore,
                            sslData.getOdlKeystore().getStorePassword()));
                    return keyStoresData.updateSslDataOdlKeystore(dataBroker, sslData, odlKeystore);
                }
            }
        }
        return false;
    }

    @Override
    public boolean addTrustNodeCertificate(String bundleName, String alias, String certificate) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            if (sslData.getTrustKeystore() != null) {
                KeyStore keystore = odlKeyTool.loadKeyStore(sslData.getTrustKeystore().getKeystoreFile(), sslData.getTrustKeystore().getStorePassword());
                keystore = odlKeyTool.addCertificate(keystore, certificate, alias, true);
                if (keystore != null) {
                    TrustKeystore trustKeystore = keyStoresData.updateTrustKeystore(sslData.getTrustKeystore(), odlKeyTool.convertKeystoreToBytes(keystore,
                            sslData.getTrustKeystore().getStorePassword()));
                    return keyStoresData.updateSslDataTrustKeystore(dataBroker, sslData, trustKeystore);
                }
            }
        }
        return false;
    }

    @Override
    public String genODLStorCertificateReq(String bundleName, String signAlg, boolean withTag) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            OdlKeystore odlKeyStore = sslData.getOdlKeystore();
            KeyStore keystore = odlKeyTool.loadKeyStore(odlKeyStore.getKeystoreFile(), sslData.getOdlKeystore().getStorePassword());
            return odlKeyTool.generateCertificateReq(keystore, odlKeyStore.getStorePassword(), odlKeyStore.getAlias(), signAlg, withTag);
        }
        return null;
    }

    public String genODLStorCertificateReq(String bundleName, boolean withTag) {
        return genODLStorCertificateReq(bundleName, KeyStoreConstant.defaultSignAlg, withTag);
    }

    @Override
    public String getODLStoreCertificate(String bundleName, boolean withTag) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            OdlKeystore odlKeyStore = sslData.getOdlKeystore();
            KeyStore keystore = odlKeyTool.loadKeyStore(odlKeyStore.getKeystoreFile(), odlKeyStore.getStorePassword());
            return odlKeyTool.getCertificate(keystore, odlKeyStore.getStorePassword(), odlKeyStore.getAlias(), withTag);
        }
        return null;
    }

    @Override
    public String getTrustStoreCertificate(String bundleName, String alias, boolean withTag) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            TrustKeystore trustKeyStore = sslData.getTrustKeystore();
            KeyStore keystore = odlKeyTool.loadKeyStore(trustKeyStore.getKeystoreFile(), trustKeyStore.getStorePassword());
            return odlKeyTool.getCertificate(keystore, trustKeyStore.getStorePassword(), alias, withTag);
        }
        return null;
    }

    @Override
    public SslData getSslData(String bundleName) {
        return keyStoresData.getSslData(dataBroker, bundleName);
    }

    @Override
    public SslData importSslDataKeystores(String bundleName, String odlKeystoreName, String odlKeystorePwd,
            String odlKeystoreAlias, KeyStore odlKeyStore, String trustKeystoreName, String trustKeystorePwd,
            KeyStore trustKeyStore, String[] cipherSuites) {
        OdlKeystore odlKeystore = keyStoresData.createOdlKeystore(odlKeystoreName, odlKeystoreAlias, odlKeystorePwd,
                                    odlKeyTool.convertKeystoreToBytes(odlKeyStore, odlKeystorePwd));
        TrustKeystore trustKeystore = keyStoresData.createTrustKeystore(trustKeystoreName, trustKeystorePwd,
                                    odlKeyTool.convertKeystoreToBytes(trustKeyStore, trustKeystorePwd));
        List<CipherSuites> cipherSuitesList = new ArrayList<>();
        for (String suit : cipherSuites) {
            CipherSuites cipherSuite = new CipherSuitesBuilder().setSuiteName(suit).build();
            cipherSuitesList.add(cipherSuite);
        }
        return keyStoresData.addSslData(dataBroker, bundleName, odlKeystore, trustKeystore, cipherSuitesList);
    }

    @Override
    public boolean removeSslData(String bundleName) {
        return keyStoresData.removeSslData(dataBroker, bundleName);
    }

    @Override
    public SslData updateSslData(SslData sslData) {
        if (keyStoresData.updateSslData(dataBroker, sslData)) {
            return sslData;
        }
        return null;
    }

    @Override
    public void exportSSlDataKeystores(String bundleName) {
        SslData sslData = keyStoresData.getSslData(dataBroker, bundleName);
        if (sslData != null) {
            KeyStore keystore = odlKeyTool.loadKeyStore(sslData.getTrustKeystore().getKeystoreFile(),
                    sslData.getTrustKeystore().getStorePassword());
            odlKeyTool.exportKeystore(keystore, sslData.getTrustKeystore().getStorePassword(), bundleName + "_trustKeystore");
            keystore = odlKeyTool.loadKeyStore(sslData.getOdlKeystore().getKeystoreFile(),
                    sslData.getOdlKeystore().getStorePassword());
            odlKeyTool.exportKeystore(keystore, sslData.getOdlKeystore().getStorePassword(), bundleName + "_odlKeystore");
        }
    }
}
