/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.ICertificateManager;

@Command(name = "gen-cert-req", scope = "aaa", description = "generate a certificate request for the opendaylight controller.")

/**
 * GenerateCertReq from the ODL key store to be signed by the Certificate Authority 'CA'
 *
 * @author mserngawy
 *
 */
public class GenerateCertReq extends OsgiCommandSupport{

    protected ICertificateManager certProvider;

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    public GenerateCertReq(final ICertificateManager aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return certProvider.genODLKeyStoreCertificateReq(keyStorePassword, true);
    }

}
