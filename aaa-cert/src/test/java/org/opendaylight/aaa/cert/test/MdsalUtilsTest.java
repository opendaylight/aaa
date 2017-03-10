/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.test;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.aaa.cert.utils.MdsalUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.KeyStores;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslDataKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.OdlKeystoreBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.ssl.data.TrustKeystoreBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MdsalUtilsTest {
    private final static String alias = "fooTest";
    private final static String bundleName = "opendaylight";
    private final static String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC Montreal, C=CA";
    private final static String odlName = "odlTest.jks";
    private final static String password = "passWord";
    private final static String trustName = "trustTest.jks";

    private static DataBroker dataBroker;
    private static SslData sslData;
    private static InstanceIdentifier<SslData> instanceIdentifier;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final OdlKeystore odlKeystore = new OdlKeystoreBuilder()
                .setAlias(alias)
                .setDname(dName)
                .setName(odlName)
                .setStorePassword(password)
                .setValidity(KeyStoreConstant.DEFAULT_VALIDITY)
                .setKeyAlg(KeyStoreConstant.DEFAULT_KEY_ALG)
                .setKeysize(KeyStoreConstant.DEFAULT_KEY_SIZE)
                .setSignAlg(KeyStoreConstant.DEFAULT_SIGN_ALG)
                .build();

        final TrustKeystore trustKeyStore = new TrustKeystoreBuilder()
                .setName(trustName)
                .setStorePassword(password)
                .build();

        sslData = new SslDataBuilder()
                .setOdlKeystore(odlKeystore)
                .setTrustKeystore(trustKeyStore)
                .build();

        final SslDataKey sslDataKey = new SslDataKey(bundleName);
        instanceIdentifier = InstanceIdentifier.create(KeyStores.class).child(SslData.class, sslDataKey);

        // mock setup
        final Optional<DataObject> dataObjectOptional = mock(Optional.class);
        when(dataObjectOptional.get()).thenReturn(sslData);
        when(dataObjectOptional.isPresent()).thenReturn(true);
        final CheckedFuture<Optional<DataObject>, ReadFailedException> checkReadFuture = mock(CheckedFuture.class);
        when(checkReadFuture.checkedGet()).thenReturn(dataObjectOptional);
        when(checkReadFuture.get()).thenReturn(dataObjectOptional);
        final ReadOnlyTransaction readOnlyTransaction = mock(ReadOnlyTransaction.class);
        when(readOnlyTransaction.read(any(), any())).thenReturn(checkReadFuture);

        final CheckedFuture<Void, TransactionCommitFailedException> checkWriteFuture = mock(CheckedFuture.class);
        final WriteTransaction writeTransaction = mock(WriteTransaction.class);
        when(writeTransaction.submit()).thenReturn(checkWriteFuture);

        final DataBroker dataBrokerInit = mock(DataBroker.class);
        when(dataBrokerInit.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(dataBrokerInit.newWriteOnlyTransaction()).thenReturn(writeTransaction);

        dataBroker = dataBrokerInit;
    }

    @Test
    public void ReadTest() {
        SslData result = MdsalUtils.read(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        assertTrue(result.getOdlKeystore().getDname() == dName);
    }

    @Test
    public void PutTest() {
        boolean result = MdsalUtils.put(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier, sslData);
        assertTrue(result);
    }

    @Test
    public void DeleteTest() {
        boolean result = MdsalUtils.delete(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        assertTrue(result);
    }

    @Test
    public void MergeTest() {
        boolean result = MdsalUtils.merge(dataBroker, LogicalDatastoreType.CONFIGURATION, instanceIdentifier, sslData);
        assertTrue(result);
    }
}
