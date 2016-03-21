/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.security.KeyStore;

import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.cert.mdsal.rpc.rev160321.AaaCertMdsalRpcService;

/**
 * @author mserngawy
 *
 */
public interface IAaaCertMdsalProvider extends AaaCertMdsalRpcService {

    KeyStore getODLKeyStore(final String bundleName);

    KeyStore getTrustKeyStore(final String bundleName);

    String[] getCipherSuites(final String bundleName);
}
