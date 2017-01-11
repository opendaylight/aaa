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

@Command(name = "get-cipher-suites", scope = "aaa", description = "Get the allowed cipher suites for TLS communication.")

/**
 * GetCipherSuites get the allowed cipher suites for TLS communication.
 *
 * @author mserngawy
 *
 */
public class GetCipherSuites extends OsgiCommandSupport {

    protected volatile ICertificateManager certProvider;

    public GetCipherSuites(final ICertificateManager aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return certProvider.getCipherSuites();
    }

}
