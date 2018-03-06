/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

/**
 * Utilities for test, the certificate needs to be updated every 10 years.
 * Last update: 5 march 2018
 */

public class TestUtils {
    public static String dummyAlias = "fooAlias";
    public static String dummyCert = KeyStoreConstant.BEGIN_CERTIFICATE
            + "MIIDTzCCAjegAwIBAgIJAMrURYFr+EdHMA0GCSqGSIb3DQEBCwUAMD4xPDA6BgNV"
            + "BAMMM09ETCwgT1U9RGV2LCBPPUxpbnV4Rm91bmRhdGlvbiwgTD1RQyBNb250cmVh"
            + "bCwgQz1DQTAeFw0xODAzMDUxNjAxNTFaFw0yODAzMDIxNjAxNTFaMD4xPDA6BgNV"
            + "BAMMM09ETCwgT1U9RGV2LCBPPUxpbnV4Rm91bmRhdGlvbiwgTD1RQyBNb250cmVh"
            + "bCwgQz1DQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAK5XhMEOXslX"
            + "pLaL9Ws5X0QLzGuFXiLIhyYG8XBZAI55UNVMbrTqKd7461F2r72ljI2pdQ2hUAlv"
            + "dyjX58TTMRVgfEKe9U7pr394Wqc7Kg+r3MnBxiVfPoq8fR0K2/RsuPU2wTmjDhIY"
            + "wrnKcfAZ8IEMcPQuT3w2rSX98b28uXeurGwvK+3G5LEVr3OrsGT9o57UERaPf+nq"
            + "EPMNlU6ix7523P0vj9Riw58Zc0R22H5HE87VX9Ck2nQxaAvyHuHYJfIH+VBDDY9w"
            + "h1rlb8aRpBi9yyDEw6EuN2CZg9VVs1dfEwfFl90mpDk/OELGAh6ya8KP2iqVXP8u"
            + "tYC6M8LnkfkCAwEAAaNQME4wHQYDVR0OBBYEFG3VOc6a6ti9+wSmPiVP0Za77hyX"
            + "MB8GA1UdIwQYMBaAFG3VOc6a6ti9+wSmPiVP0Za77hyXMAwGA1UdEwQFMAMBAf8w"
            + "DQYJKoZIhvcNAQELBQADggEBAFFHa2nywM2xf+B56EwRoPLfDXc1qZUJpC5BtnsJ"
            + "hNfiy3kaEnsEwBOXMsw87jy4Y3Dn+XcH1elKNpArwUj8vq3as4rlobqY1UFmFVGV"
            + "0BnSBYFnHNUwha5c0vhdhB++Q1YJduOLWJi0a+sXP/sBkzTjfWdqX1vcu0a0QtIm"
            + "rrngV+qcu6yJl/duwx5TCzf6lVElFjNoV6zchwz7uqblC/aFlraPzQWFCYci+3zd"
            + "FF3x8s9EX9TJj9smmPtHBzthkIWxEikk+gpOg5RHhILkOZsDbxVxXJoPQZXERdWH"
            + "8XtYREVKt5CTN6ZEF0742hXUvkkuz1YvvCbJQb/Pe9fokQ8="
            + KeyStoreConstant.END_CERTIFICATE;

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
