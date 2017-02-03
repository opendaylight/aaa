/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.cert;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.ICertificateManager;

@Command(name = "get-tls-protocols", scope = "aaa", description = "Get the allowed TLS Protocols.")

/**
 * GetCipherSuites get the allowed cipher suites for TLS communication.
 *
 * @author mserngawy
 *
 */
public class GetTlsProtocols extends OsgiCommandSupport {

    protected volatile ICertificateManager certProvider;

    public GetTlsProtocols(final ICertificateManager aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return certProvider.getTlsProtocols();
    }

}
