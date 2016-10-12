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
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "gen-cert-req", scope = "aaa", description = "generate a certificate request for the opendaylight controller.")

/**
 *
 * @author mserngawy
 * GenerateCertReq from the ODL key store to be signed by the Certificate Authority 'CA'
 */
public class GenerateCertReq extends OsgiCommandSupport{

    private static final Logger LOG = LoggerFactory.getLogger(GenerateCertReq.class);
    protected IAaaCertProvider certProvider;

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / --default is controller",
            required = false,
            multiValued = false)
    private String alias = "controller";

    public GenerateCertReq(final IAaaCertProvider aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return certProvider.genODLKeyStorCertificateReq(keyStorePassword, alias, true);
    }

}
