/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
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
 * GetODLSelfSignCert get the ODL key store self sign certificate.
 *
 * @author mserngawy
 *
 */
@Service
@Command(name = "get-odl-cert", scope = "aaa",
        description = "get self sign certificate for the opendaylight controller.")
public class GetODLSelfSignCert implements Action {

    @Reference private ICertificateManager certProvider;
    @Option(name = "-keyStorePass",
            description = "Keystore Password",
            required = true,
            censor = true,
            multiValued = false)
    private String pwd;

    @Override
    public Object execute() throws Exception {
        return certProvider.getODLKeyStoreCertificate(pwd, true);
    }
}
