/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.utils;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import java.util.List;

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

public class SSLDataUtils {

    public static InstanceIdentifier<KeyStores> getKeystoresIid() {
        return InstanceIdentifier.builder(KeyStores.class).build();
    }

    public static InstanceIdentifier<SslData> getSslDataIid() {
        return InstanceIdentifier.builder(KeyStores.class).build().child(SslData.class);
    }

    public static SslData getSslData(DataBroker dataBroker, String bundleName) {
        SslDataKey sslDataKey = new SslDataKey(bundleName);
        InstanceIdentifier<SslData> sslDataIid = InstanceIdentifier.create(KeyStores.class)
                                                .child(SslData.class, sslDataKey);
        return MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION, sslDataIid);
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

        MdsalUtils.put(dataBroker, LogicalDatastoreType.CONFIGURATION, getSslDataIid(), sslData);
        return sslData;
    }

    public static OdlKeystore createOdlKeystore(String name, String alias, String password, String dname,
                        String sigAlg, String validity, String keySize, List<Certificates> certificates) {
        OdlKeystore odlKeystore = new OdlKeystoreBuilder().build();
        return odlKeystore;
    }

    public static OdlKeystore createOdlKeystore(String name, String alias, String password, String dname, 
                                    List<Certificates> certificates) {
        OdlKeystore odlKeystore = new OdlKeystoreBuilder().build();
        return odlKeystore;
    }

    public static TrustKeystore createTrustKeystore(String name, String alias, String password, String dname, 
                                    List<Certificates> certificates) {
        TrustKeystore trustKeystore = new TrustKeystoreBuilder().build();
        return trustKeystore;
    }

    public static TrustKeystore createTrustKeystore(String name, String alias, String password, String dname,
            String sigAlg, String validity, String keySize, List<Certificates> certificates) {
        TrustKeystore trustKeystore = new TrustKeystoreBuilder().build();
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
