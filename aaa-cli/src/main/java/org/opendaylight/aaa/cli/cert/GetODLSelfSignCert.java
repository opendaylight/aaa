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
import org.opendaylight.aaa.cli.utils.CliUtils;

@Command(name = "get-odl-cert", scope = "aaa", description = "get self sign certificate for the opendaylight controller.")

/**
 * GetODLSelfSignCert get the ODL key store self sign certificate.
 *
 * @author mserngawy
 *
 */
public class GetODLSelfSignCert extends OsgiCommandSupport{

    protected ICertificateManager certProvider;

    public GetODLSelfSignCert(final ICertificateManager aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        final String pwd = CliUtils.readPassword(this.session, "Enter Keystore Password:");
        return certProvider.getODLKeyStoreCertificate(pwd, true);
    }

}
