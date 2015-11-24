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

@Command(name = "gen-odl-ks", scope = "aaa", description = "Create the default keystore for the opendaylight controller.")

public class CreateODLKeyStore extends OsgiCommandSupport{

    protected IAaaCertProvider certProvider;

    @Option(name = "-keystore",
            aliases = { "--KeyStore" },
            description = "The keystore name.\n-keystore / --default is ctl.jks",
            required = false,
            multiValued = false)
    private String keyStoreName = "ctl.jks";

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

    @Option(name = "-validity",
            aliases = { "--validity" },
            description = "The validity.\n-validity of the keystore certificate / --default is 365",
            required = false,
            multiValued = false)
    private int validity = 365;

    @Option(name = "-dName",
            aliases = { "--dName" },
            description = "The dName.\n-dName / --should be in the following formate CN=, OU=, O=, L= C=",
            required = false,
            multiValued = false)
    private String dName = "CN=ODL, OU=Dev, O=LinuxFoundation, L=QC. Montreal, C=CA";

    public CreateODLKeyStore(IAaaCertProvider aaaCertProvider) {
       this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
          StringBuilder sb = new StringBuilder();
          sb.append(certProvider.createODLKeyStore(keyStoreName, keyStorePassword, alias, dName, validity));
          sb.append("\n");
          sb.append("08-aaa-cert-config.xml file should be updated with new keystore info");
          return sb.toString();
    }

}