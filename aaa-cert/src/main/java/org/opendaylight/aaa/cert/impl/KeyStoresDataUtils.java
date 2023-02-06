/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.util.List;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyStoresDataUtils manage the SslData operations add, delete and update.
 *
 * @author mserngawy
 *
 */
public class KeyStoresDataUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoresDataUtils.class);
    public static final String KEYSTORES_DATA_TREE = "KeyStores:1";

    private final AAAEncryptionService encryService;

    public KeyStoresDataUtils(final AAAEncryptionService encryService) {
        this.encryService = encryService;
    }

    public static InstanceIdentifier<KeyStores> getKeystoresIid() {
        return InstanceIdentifier.builder(KeyStores.class).build();
    }

    public static InstanceIdentifier<SslData> getSslDataIid() {
        return InstanceIdentifier.create(KeyStores.class).child(SslData.class);
    }

    public static InstanceIdentifier<SslData> getSslDataIid(final String bundleName) {
        final SslDataKey sslDataKey = new SslDataKey(bundleName);
        return InstanceIdentifier.create(KeyStores.class).child(SslData.class, sslDataKey);
    }

    public static OdlKeystore updateOdlKeystore(final OdlKeystore baseOdlKeyStore, final byte[] keyStoreBytes) {
        return new OdlKeystoreBuilder(baseOdlKeyStore).setKeystoreFile(keyStoreBytes).build();
    }

    public SslData addSslData(final DataBroker dataBroker, final String bundleName, final OdlKeystore odlKeystore,
            final TrustKeystore trustKeystore, final List<CipherSuites> cipherSuites, final String tlsProtocols) {
        final SslDataKey sslDataKey = new SslDataKey(bundleName);
        final SslData sslData = new SslDataBuilder().withKey(sslDataKey).setOdlKeystore(encryptOdlKeyStore(odlKeystore))
                .setTrustKeystore(encryptTrustKeystore(trustKeystore)).setCipherSuites(cipherSuites)
                .setTlsProtocols(tlsProtocols).build();

        if (MdsalUtils.put(dataBroker, LogicalDatastoreType.CONFIGURATION, getSslDataIid(bundleName), sslData)) {
            return new SslDataBuilder().withKey(sslDataKey).setOdlKeystore(odlKeystore).setTrustKeystore(trustKeystore)
                    .setCipherSuites(cipherSuites).build();
        } else {
            return null;
        }
    }

    public CipherSuites createCipherSuite(final String suiteName) {
        return new CipherSuitesBuilder().setSuiteName(suiteName).build();
    }

    public OdlKeystore createOdlKeystore(final String name, final String alias, final String password,
            final byte[] keyStoreBytes) {
        return new OdlKeystoreBuilder().setKeystoreFile(keyStoreBytes).setAlias(alias).setName(name)
                .setStorePassword(password).build();
    }

    public OdlKeystore createOdlKeystore(final String name, final String alias, final String password,
            final String dname, final ODLKeyTool odlKeyTool) {
        return createOdlKeystore(name, alias, password, dname, KeyStoreConstant.DEFAULT_SIGN_ALG,
                KeyStoreConstant.DEFAULT_KEY_ALG, KeyStoreConstant.DEFAULT_VALIDITY, KeyStoreConstant.DEFAULT_KEY_SIZE,
                odlKeyTool);
    }

    public OdlKeystore createOdlKeystore(final String name, final String alias, final String password,
            final String dname, final String sigAlg, final String keyAlg, final int validity, final int keySize,
            final ODLKeyTool odlKeyTool) {
        final byte[] keyStoreBytes = odlKeyTool.convertKeystoreToBytes(odlKeyTool.createKeyStoreWithSelfSignCert(name,
                password, dname, alias, validity, keyAlg, keySize, sigAlg), password);
        LOG.debug("Odl keystore string {} ", keyStoreBytes);

        return new OdlKeystoreBuilder().setKeystoreFile(keyStoreBytes)
                .setAlias(alias).setDname(dname).setKeyAlg(keyAlg)
                .setKeysize(keySize)
                .setName(name)
                .setSignAlg(sigAlg)
                .setStorePassword(password)
                .setValidity(validity)
                .build();
    }

    public TrustKeystore createTrustKeystore(final String name, final String password, final byte[] keyStoreBytes) {
        return new TrustKeystoreBuilder().setKeystoreFile(keyStoreBytes).setName(name).setStorePassword(password)
                .build();
    }

    public TrustKeystore createTrustKeystore(final String name, final String password, final ODLKeyTool odlKeyTool) {
        final byte[] keyStoreBytes = odlKeyTool.convertKeystoreToBytes(odlKeyTool.createEmptyKeyStore(password),
                password);
        LOG.debug("trust keystore string {} ", keyStoreBytes);
        return new TrustKeystoreBuilder()
                .setKeystoreFile(keyStoreBytes)
                .setName(name)
                .setStorePassword(password)
                .build();
    }

    private OdlKeystore decryptOdlKeyStore(final OdlKeystore odlKeystore) {
        if (odlKeystore == null) {
            return null;
        }
        final OdlKeystoreBuilder odlKeystoreBuilder = new OdlKeystoreBuilder(odlKeystore);
        odlKeystoreBuilder.setKeystoreFile(encryService.decrypt(odlKeystore.getKeystoreFile()));
        odlKeystoreBuilder.setStorePassword(encryService.decrypt(odlKeystore.getStorePassword()));
        return odlKeystoreBuilder.build();
    }

    private SslData decryptSslData(final SslData sslData) {
        if (sslData == null) {
            return null;
        }
        final SslDataBuilder sslDataBuilder = new SslDataBuilder(sslData)
                .setOdlKeystore(decryptOdlKeyStore(sslData.getOdlKeystore()))
                .setTrustKeystore(decryptTrustKeystore(sslData.getTrustKeystore()));
        return sslDataBuilder.build();
    }

    private TrustKeystore decryptTrustKeystore(final TrustKeystore trustKeyStore) {
        if (trustKeyStore == null) {
            return null;
        }
        final TrustKeystoreBuilder trustKeyStoreBuilder = new TrustKeystoreBuilder(trustKeyStore);
        trustKeyStoreBuilder.setKeystoreFile(encryService.decrypt(trustKeyStore.getKeystoreFile()));
        trustKeyStoreBuilder.setStorePassword(encryService.decrypt(trustKeyStore.getStorePassword()));
        return trustKeyStoreBuilder.build();
    }

    private OdlKeystore encryptOdlKeyStore(final OdlKeystore odlKeystore) {
        final OdlKeystoreBuilder odlKeystoreBuilder = new OdlKeystoreBuilder(odlKeystore);
        odlKeystoreBuilder.setKeystoreFile(encryService.encrypt(odlKeystore.getKeystoreFile()));
        odlKeystoreBuilder.setStorePassword(encryService.encrypt(odlKeystore.getStorePassword()));
        return odlKeystoreBuilder.build();
    }

    private SslData encryptSslData(final SslData sslData) {
        final SslDataBuilder sslDataBuilder = new SslDataBuilder(sslData)
                .setOdlKeystore(encryptOdlKeyStore(sslData.getOdlKeystore()))
                .setTrustKeystore(encryptTrustKeystore(sslData.getTrustKeystore()));
        return sslDataBuilder.build();
    }

    private TrustKeystore encryptTrustKeystore(final TrustKeystore trustKeyStore) {
        final TrustKeystoreBuilder trustKeyStoreBuilder = new TrustKeystoreBuilder(trustKeyStore);
        trustKeyStoreBuilder.setKeystoreFile(encryService.encrypt(trustKeyStore.getKeystoreFile()));
        trustKeyStoreBuilder.setStorePassword(encryService.encrypt(trustKeyStore.getStorePassword()));
        return trustKeyStoreBuilder.build();
    }

    public SslData getSslData(final DataBroker dataBroker, final String bundleName) {
        final InstanceIdentifier<SslData> sslDataIid = getSslDataIid(bundleName);
        return decryptSslData(MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION, sslDataIid));
    }

    public boolean removeSslData(final DataBroker dataBroker, final String bundleName) {
        final InstanceIdentifier<SslData> sslDataIid = getSslDataIid(bundleName);
        return MdsalUtils.delete(dataBroker, LogicalDatastoreType.CONFIGURATION, sslDataIid);
    }

    public boolean updateSslData(final DataBroker dataBroker, final SslData sslData) {
        final InstanceIdentifier<SslData> sslDataIid = getSslDataIid(sslData.getBundleName());
        return MdsalUtils.merge(dataBroker, LogicalDatastoreType.CONFIGURATION, sslDataIid, encryptSslData(sslData));
    }

    public boolean updateSslDataCipherSuites(final DataBroker dataBroker, final SslData baseSslData,
            final List<CipherSuites> cipherSuites) {
        final SslDataBuilder sslDataBuilder = new SslDataBuilder(baseSslData).setCipherSuites(cipherSuites);
        return updateSslData(dataBroker, sslDataBuilder.build());
    }

    public boolean updateSslDataOdlKeystore(final DataBroker dataBroker, final SslData baseSslData,
            final OdlKeystore odlKeyStore) {
        final SslDataBuilder sslDataBuilder = new SslDataBuilder(baseSslData).setOdlKeystore(odlKeyStore);
        return updateSslData(dataBroker, sslDataBuilder.build());
    }

    public boolean updateSslDataTrustKeystore(final DataBroker dataBroker, final SslData baseSslData,
            final TrustKeystore trustKeyStore) {
        final SslDataBuilder sslDataBuilder = new SslDataBuilder(baseSslData).setTrustKeystore(trustKeyStore);
        return updateSslData(dataBroker, sslDataBuilder.build());
    }

    public TrustKeystore updateTrustKeystore(final TrustKeystore baseTrustKeyStore, final byte[] keyStoreBytes) {
        return new TrustKeystoreBuilder(baseTrustKeyStore).setKeystoreFile(keyStoreBytes).build();
    }
}
