/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.impl.MdsalUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MdsalUtilsTest {
    private static final String ALIAS = "fooTest";
    private static final String BUNDLE_NAME = "opendaylight";
    private static final String D_NAME = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private static final String ODL_NAME = "odlTest.jks";
    private static final String PASSWORD = "passWord";
    private static final String TRUST_NAME = "trustTest.jks";

    private static DataBroker dataBroker;
    private static SslData sslData;
    private static InstanceIdentifier<SslData> instanceIdentifier;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final OdlKeystore odlKeystore = new OdlKeystoreBuilder().setAlias(ALIAS).setDname(D_NAME).setName(ODL_NAME)
                .setStorePassword(PASSWORD).setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG).setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG).build();

        final TrustKeystore trustKeyStore = new TrustKeystoreBuilder().setName(TRUST_NAME).setStorePassword(PASSWORD)
                .build();

        sslData = new SslDataBuilder().setOdlKeystore(odlKeystore).setTrustKeystore(trustKeyStore)
            .setBundleName("bundle").build();

        final SslDataKey sslDataKey = new SslDataKey(BUNDLE_NAME);
        instanceIdentifier = InstanceIdentifier.create(KeyStores.class).child(SslData.class, sslDataKey);

        dataBroker = TestUtils.mockDataBroker(sslData);
    }

    @Test
    public void readTest() {
        SslData result = MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        assertTrue(result.getOdlKeystore().getDname() == D_NAME);
    }

    @Test
    public void putTest() {
        boolean result = MdsalUtils.put(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier, sslData);
        assertTrue(result);
    }

    @Test
    public void deleteTest() {
        boolean result = MdsalUtils.delete(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        assertTrue(result);
    }

    @Test
    public void mergeTest() {
        boolean result = MdsalUtils.merge(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier, sslData);
        assertTrue(result);
    }
}
