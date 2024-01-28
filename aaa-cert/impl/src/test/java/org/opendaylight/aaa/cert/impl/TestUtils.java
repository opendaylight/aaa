/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.key.stores.SslData;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

/**
 * Utilities for test, the certificate needs to be updated yearly.
 */
public final class TestUtils {
    public static final String DUMMY_ALIAS = "fooAlias";
    public static final String DUMMY_CERT = KeyStoreConstant.BEGIN_CERTIFICATE
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

    private TestUtils() {

    }

    public static DataBroker mockDataBroker(final SslData sslData) throws Exception {
        final ReadTransaction readOnlyTransaction = mock(ReadTransaction.class);
        when(readOnlyTransaction.read(any(), any())).thenReturn(FluentFutures.immediateFluentFuture(
            Optional.of(sslData)));

        final WriteTransaction writeTransaction = mock(WriteTransaction.class);
        doReturn(CommitInfo.emptyFluentFuture()).when(writeTransaction).commit();

        final DataBroker mockDataBroker = mock(DataBroker.class);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        return mockDataBroker;
    }
}
