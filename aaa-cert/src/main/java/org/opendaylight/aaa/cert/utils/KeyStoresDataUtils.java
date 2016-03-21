/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.utils;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.ODLKeyTool;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuites;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.cipher.suite.CipherSuitesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.keystore.Certificates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.keystore.CertificatesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoresDataUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoresDataUtils.class);
    public static final String KEYSTORES_DATA_TREE = "KeyStores:1";
 
    public static InstanceIdentifier<KeyStores> getKeystoresIid() {
        return InstanceIdentifier.builder(KeyStores.class).build();
    }

    public static InstanceIdentifier<SslData> getSslDataIid() {
        return InstanceIdentifier.create(KeyStores.class).child(SslData.class);
    }

    public static InstanceIdentifier<SslData> getSslDataIid(String bundleName) {
        SslDataKey sslDataKey = new SslDataKey(bundleName);
        return InstanceIdentifier.create(KeyStores.class).child(SslData.class, sslDataKey);
    }

    public static SslData getSslData(DataBroker dataBroker, String bundleName) {
        InstanceIdentifier<SslData> sslDataIid = getSslDataIid(bundleName);
        return MdsalUtils.read(dataBroker, LogicalDatastoreType.OPERATIONAL, sslDataIid);
    }

    public static SslData addSslData(DataBroker dataBroker, String bundleName, OdlKeystore odlKeystore,
            TrustKeystore trustKeystore, List<CipherSuites> cipherSuites) {
        SslDataKey sslDataKey = new SslDataKey(bundleName);
        SslData sslData = new SslDataBuilder()
                            .setKey(sslDataKey)
                            .setOdlKeystore(odlKeystore)
                            .setTrustKeystore(trustKeystore)
                            .setCipherSuites(cipherSuites)
                            .build();

        if (MdsalUtils.put(dataBroker, LogicalDatastoreType.OPERATIONAL, getSslDataIid(bundleName), sslData))
            return sslData;
        else
            return null;
    }

    public static OdlKeystore createOdlKeystore(String name, String alias, String password, String dname,
                        String sigAlg, String keyAlg, int validity, int keySize, ODLKeyTool odlKeyTool) {
        byte[] keyStoreBytes = odlKeyTool.convertKeystoreToBytes(odlKeyTool.createKeyStoreWithSelfSignCert(name, password, dname, alias,
                validity, keyAlg, keySize, sigAlg), password);
        LOG.info("Odl keystore string {} ", keyStoreBytes);
        OdlKeystore odlKeystore = new OdlKeystoreBuilder()
                                    .setKeystoreFile(keyStoreBytes)
                                    .setAlias(alias)
                                    .setDname(dname)
                                    .setKeyAlg(keyAlg)
                                    .setKeysize(keySize)
                                    .setName(name)
                                    .setSignAlg(sigAlg)
                                    .setStorePassword(password)
                                    .setValidity(validity)
                                    .setCertificates(new ArrayList<>())
                                    .build();
        return odlKeystore;
    }

    public static OdlKeystore createOdlKeystore(String name, String alias, String password, String dname, 
                                                ODLKeyTool odlKeyTool) {
        return createOdlKeystore(name, alias, password, dname, KeyStoreConstant.defaultSignAlg, KeyStoreConstant.defaultKeyAlg,
                KeyStoreConstant.defaultValidity, KeyStoreConstant.defaultKeySize, odlKeyTool);
    }

    public static TrustKeystore createTrustKeystore(String name, String password, List<Certificates> certificates, ODLKeyTool odlKeyTool) {
        Map<String, String> mapAliasCert = new HashMap<>();
        if (certificates != null) {
            for (Certificates cert: certificates) {
                mapAliasCert.put(cert.getAlias(), cert.getX500Certificate());
            }
        }
        byte[] keyStoreBytes = odlKeyTool.convertKeystoreToBytes(odlKeyTool.createTrustKeyStoreImportCert(password, mapAliasCert), password);
        LOG.info("trust keystore string {} ", keyStoreBytes);
        TrustKeystore trustKeystore = new TrustKeystoreBuilder()
                                        .setCertificates(certificates)
                                        .setKeystoreFile(keyStoreBytes)
                                        .setName(name)
                                        .setStorePassword(password)
                                        .build();
        return trustKeystore;
    }

    public static CipherSuites createCipherSuite(String suiteName) {
        CipherSuites cipherSuite = new CipherSuitesBuilder()
                                    .setSuiteName(suiteName)
                                    .build();
        return cipherSuite;
    }

    public static Certificates createCertificates(String alias, String x509Cert) {
        Certificates cert = new CertificatesBuilder()
                            .setAlias(alias)
                            .setX500Certificate(x509Cert)
                            .build();
        return cert;
    }
}
