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

@Command(name = "gen-trust-ks", scope = "aaa", description = "Create the trust keystore for the opendaylight controller.")

/**
 *
 * @author mserngawy
 * CreateTrustKeyStore create trust key store with new configuration
 */
public class CreateTrustKeyStore extends OsgiCommandSupport{

    protected IAaaCertProvider certProvider;

    @Option(name = "-keystore",
            aliases = { "--KeyStore" },
            description = "The keystore name.\n-keystore / --default is truststore.jks",
            required = false,
            multiValued = false)
    private String keyStoreName = "truststore.jks";

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / --default is node",
            required = false,
            multiValued = false)
    private String alias = "node";

    public CreateTrustKeyStore(final IAaaCertProvider aaaCertProvider) {
        this.certProvider = aaaCertProvider;
     }

    @Override
    protected Object doExecute() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append(certProvider.createTrustKeyStore(keyStoreName, keyStorePassword, alias));
        sb.append("\n");
        sb.append("08-aaa-cert-config.xml file should be updated with new keystore info");
        return sb.toString();
    }

}
