/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Base64;
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
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeyStoresDataUtils manage the SslData operations add, delete and update.
 *
 * @author mserngawy
 */
public class KeyStoresDataUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoresDataUtils.class);
    public static final String KEYSTORES_DATA_TREE = "KeyStores:1";

    private final AAAEncryptionService encryService;

    public KeyStoresDataUtils(final AAAEncryptionService encryService) {
        this.encryService = encryService;
    }

    public static DataObjectIdentifier<KeyStores> getKeystoresIid() {
        return DataObjectIdentifier.builder(KeyStores.class).build();
    }

    public static DataObjectReference<SslData> getSslDataIid() {
        return DataObjectReference.builder(KeyStores.class).child(SslData.class).build();
    }

    public static DataObjectIdentifier<SslData> getSslDataIid(final String bundleName) {
        return DataObjectIdentifier.builder(KeyStores.class).child(SslData.class, new SslDataKey(bundleName)).build();
    }

    public static OdlKeystore updateOdlKeystore(final OdlKeystore baseOdlKeyStore, final byte[] keyStoreBytes) {
        return new OdlKeystoreBuilder(baseOdlKeyStore).setKeystoreFile(keyStoreBytes).build();
    }

    public SslData addSslData(final DataBroker dataBroker, final String bundleName, final OdlKeystore odlKeystore,
            final TrustKeystore trustKeystore, final List<CipherSuites> cipherSuites, final String tlsProtocols) {
        final SslDataKey sslDataKey = new SslDataKey(bundleName);
        final SslData sslData;
        try {
            sslData = new SslDataBuilder().withKey(sslDataKey).setOdlKeystore(encryptOdlKeyStore(odlKeystore))
                    .setTrustKeystore(encryptTrustKeystore(trustKeystore)).setCipherSuites(cipherSuites)
                    .setTlsProtocols(tlsProtocols).build();
        } catch (GeneralSecurityException e) {
            LOG.error("Encryption of TrustKeystore for SslData failed.", e);
            return null;
        }

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

    private OdlKeystore decryptOdlKeyStore(final OdlKeystore odlKeystore) throws GeneralSecurityException {
        return odlKeystore == null ? null : new OdlKeystoreBuilder(odlKeystore)
            .setKeystoreFile(decryptNullable(odlKeystore.getKeystoreFile()))
            .setStorePassword(decryptStringFromBase64(odlKeystore.getStorePassword()))
            .build();
    }

    private SslData decryptSslData(final SslData sslData) throws GeneralSecurityException {
        return sslData == null ? null : new SslDataBuilder(sslData)
            .setOdlKeystore(decryptOdlKeyStore(sslData.getOdlKeystore()))
            .setTrustKeystore(decryptTrustKeystore(sslData.getTrustKeystore()))
            .build();
    }

    private TrustKeystore decryptTrustKeystore(final TrustKeystore trustKeyStore) throws GeneralSecurityException {
        return trustKeyStore == null ? null : new TrustKeystoreBuilder(trustKeyStore)
            .setKeystoreFile(decryptNullable(trustKeyStore.getKeystoreFile()))
            .setStorePassword(decryptStringFromBase64(trustKeyStore.getStorePassword()))
            .build();
    }

    private byte[] decryptNullable(final byte[] bytes) throws GeneralSecurityException {
        return bytes == null ? null : encryService.decrypt(bytes);
    }

    private String decryptStringFromBase64(final String base64) throws GeneralSecurityException {
        return base64 == null ? null
            : new String(encryService.decrypt(Base64.getDecoder().decode(base64)), Charset.defaultCharset());
    }

    private String encryptStringToBase64(final String str) throws GeneralSecurityException {
        return str == null ? null
            : Base64.getEncoder().encodeToString(encryService.encrypt(str.getBytes(Charset.defaultCharset())));
    }

    private OdlKeystore encryptOdlKeyStore(final OdlKeystore odlKeystore) throws GeneralSecurityException {
        return new OdlKeystoreBuilder(odlKeystore)
            .setKeystoreFile(encryService.encrypt(odlKeystore.getKeystoreFile()))
            .setStorePassword(encryptStringToBase64(odlKeystore.getStorePassword()))
            .build();
    }

    private SslData encryptSslData(final SslData sslData) throws GeneralSecurityException {
        return new SslDataBuilder(sslData)
            .setOdlKeystore(encryptOdlKeyStore(sslData.getOdlKeystore()))
            .setTrustKeystore(encryptTrustKeystore(sslData.getTrustKeystore()))
            .build();
    }

    private TrustKeystore encryptTrustKeystore(final TrustKeystore trustKeyStore) throws GeneralSecurityException {
        return new TrustKeystoreBuilder(trustKeyStore)
            .setKeystoreFile(encryService.encrypt(trustKeyStore.getKeystoreFile()))
            .setStorePassword(encryptStringToBase64(trustKeyStore.getStorePassword()))
            .build();
    }

    public SslData getSslData(final DataBroker dataBroker, final String bundleName) {
        try {
            return decryptSslData(MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION,
                getSslDataIid(bundleName)));
        } catch (GeneralSecurityException e) {
            LOG.error("Decryption of KeyStore for SslData failed.", e);
            return null;
        }
    }

    public boolean removeSslData(final DataBroker dataBroker, final String bundleName) {
        return MdsalUtils.delete(dataBroker, LogicalDatastoreType.CONFIGURATION, getSslDataIid(bundleName));
    }

    public boolean updateSslData(final DataBroker dataBroker, final SslData sslData) {
        final SslData encryptedSslData;
        try {
            encryptedSslData = encryptSslData(sslData);
        } catch (GeneralSecurityException e) {
            LOG.error("Encryption of KeyStore for SslData failed.", e);
            return false;
        }
        return MdsalUtils.merge(dataBroker, LogicalDatastoreType.CONFIGURATION, getSslDataIid(sslData.getBundleName()),
            encryptedSslData);
    }

    public boolean updateSslDataCipherSuites(final DataBroker dataBroker, final SslData baseSslData,
            final List<CipherSuites> cipherSuites) {
        return updateSslData(dataBroker, new SslDataBuilder(baseSslData).setCipherSuites(cipherSuites).build());
    }

    public boolean updateSslDataOdlKeystore(final DataBroker dataBroker, final SslData baseSslData,
            final OdlKeystore odlKeyStore) {
        return updateSslData(dataBroker, new SslDataBuilder(baseSslData).setOdlKeystore(odlKeyStore).build());
    }

    public boolean updateSslDataTrustKeystore(final DataBroker dataBroker, final SslData baseSslData,
            final TrustKeystore trustKeyStore) {
        return updateSslData(dataBroker, new SslDataBuilder(baseSslData).setTrustKeystore(trustKeyStore).build());
    }

    public TrustKeystore updateTrustKeystore(final TrustKeystore baseTrustKeyStore, final byte[] keyStoreBytes) {
        return new TrustKeystoreBuilder(baseTrustKeyStore).setKeystoreFile(keyStoreBytes).build();
    }
}
