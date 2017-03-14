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
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yangtools.yang.binding.DataObject;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utilities for test, the certicate needs to be updated yearly.
 * Last update: 8 march 2017
 */

public class TestUtils {
    public static String dummyAlias = "fooAlias";
    public static String dummyCert = KeyStoreConstant.BEGIN_CERTIFICATE +
            "MIICKTCCAZKgAwIBAgIECMgzyzANBgkqhkiG9w0BAQUFADBZMQwwCgYDV" +
            "QQDDANPREwxDDAKBgNVBAsMA0RldjEYMBYGA1UECgwPTGludXhGb3VuZG" +
            "F0aW9uMRQwEgYDVQQHDAtRQyBNb250cmVhbDELMAkGA1UEBhMCQ0EwHhc" +
            "NMTcwMzAzMTYyMDA1WhcNMTgwMzAzMTYyMDA1WjBZMQwwCgYDVQQDDANP" +
            "REwxDDAKBgNVBAsMA0RldjEYMBYGA1UECgwPTGludXhGb3VuZGF0aW9uM" +
            "RQwEgYDVQQHDAtRQyBNb250cmVhbDELMAkGA1UEBhMCQ0EwgZ8wDQYJKo" +
            "ZIhvcNAQEBBQADgY0AMIGJAoGBAJrQxIfdU230tedhXnM25r3ht5UQ5Jo" +
            "G7+9H9b2WcrrkehJ++AZ2zq6SJDLVVnjgXh/YgFo3L6DOKnVTwnXUXGLk" +
            "NiJhqL2ndu49zI63CxQ2EjBR8tlD5HctNH4SKj1RqmYvt0H3LUZSBKH8Y" +
            "XGL0U0Qyxwe3flRh2Y6sMb3o47rAgMBAAEwDQYJKoZIhvcNAQEFBQADgY" +
            "EAVBWCNC+bbJftOTfpL3sL3YO+aQSmPt5ICgrz7wXDkzpf+0FwSqt+kiR" +
            "Wfw65RTgmn2hmYPh2QW4SaIN50ftLfUHgkf2zeMlodAQVYmBAd/woE3s7" +
            "fkSa9vQkUowgHAxW//7NOOTonnQPi2gH6ubaOCG4ZeXTwqHy47DGA0c8z" +
            "2Q="+
            KeyStoreConstant.END_CERTIFICATE;

    public static DataBroker mockDataBroker(SslData sslData) throws Exception {
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
        return dataBrokerInit;
    }
}
