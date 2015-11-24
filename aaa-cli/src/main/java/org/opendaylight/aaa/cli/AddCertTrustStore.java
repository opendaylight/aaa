/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;

@Command(name = "add-trust-cert", scope = "aaa", description = "Add node certificaet to trust key store.")

/**
*
* @author mserngawy
* AddCertTrustStore adding certificate to the ODL keyStore
*/
public class AddCertTrustStore extends OsgiCommandSupport {

    protected IAaaCertProvider certProvider;

    @Option(name = "-cert",
            aliases = { "--CertFile" },
            description = "The node certificate file.\n-file / --should be accesable by the karaf command line",
            required = true,
            multiValued = false)
    private String certFile = "";

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The Trust keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / node alias should be unique",
            required = true,
            multiValued = false)
    private String alias = "";

    public AddCertTrustStore(final IAaaCertProvider aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        final FileInputStream fInputStream = new FileInputStream(certFile);
        final byte[] certBytes = new byte[fInputStream.available()];
        fInputStream.read(certBytes);
        fInputStream.close();
        final String certificate = new String(certBytes, StandardCharsets.UTF_8);
        if (certProvider.addCertificateTrustStore(keyStorePassword, alias, certificate)) {
            return alias + " certificate successfully added to trust keystore";
        } else {
            return "Failed to add " + alias + " certificate to trust keystore";
        }
    }
}