/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.cert;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.aaa.cert.api.ICertificateManager;

/**
 * Import default MD-SAL keystores.
 *
 * @author mserngawy
 */
@Service
@Command(name = "import-keystores", scope = "aaa", description = "Import default MD-SAL keystores,"
        + " the keystores (odl and trust) should exist under default SSL directory configuration/ssl/")
public class ImportDefaultKeystores implements Action {

    @Reference private ICertificateManager certProvider;

    @Option(name = "-odlKeystoreName",
            aliases = { "--" },
            description = "",
            required = true,
            multiValued = false)
    private String odlKeystoreName;

    @Option(name = "-odlKeystoreAlias",
            aliases = { "--" },
            description = "",
            required = true,
            multiValued = false)
    private String odlKeystoreAlias;

    @Option(name = "-odlKeystorePwd",
            aliases = { "--" },
            description = "",
            required = true,
            multiValued = false)
    private String odlKeystorePwd;

    @Option(name = "-trustKeystoreName",
            aliases = { "--" },
            description = "",
            required = true,
            multiValued = false)
    private String trustKeystoreName;

    @Option(name = "-trustKeystorePwd",
            aliases = { "--" },
            description = "",
            required = true,
            multiValued = false)
    private String trustKeystorePwd;

    @Option(name = "-cipherSuites",
            aliases = { "--" },
            description = "Different Cipher suites should be seperated by ','",
            required = true,
            multiValued = false)
    private String cipherSuitesStr;

    @Option(name = "-tlsProtocols",
            aliases = { "--" },
            description = "Different TLS protocols should be seperated by ','",
            required = true,
            multiValued = false)
    private String tlsProtocols;

    @Override
    public Object execute() throws Exception {
        final String[] cipherSuites;
        if (!cipherSuitesStr.isEmpty()) {
            if (cipherSuitesStr.contains(",")) {
                cipherSuites = cipherSuitesStr.split(",");
            } else {
                cipherSuites = new String[] {cipherSuitesStr};
            }
        } else {
            cipherSuites = new String[]{};
        }
        if (certProvider.importSslDataKeystores(odlKeystoreName, odlKeystorePwd, odlKeystoreAlias,
                trustKeystoreName, trustKeystorePwd, cipherSuites, tlsProtocols)) {
            return "Default keystores successfully imported";
        } else {
            return "Failed to import the keystores";
        }
    }

}
