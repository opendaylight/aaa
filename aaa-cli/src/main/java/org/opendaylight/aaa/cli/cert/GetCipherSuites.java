/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.cert;

import java.util.Arrays;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.aaa.cert.api.ICertificateManager;

/**
 * GetCipherSuites get the allowed cipher suites for TLS communication.
 *
 * @author mserngawy
 *
 */
@Service
@Command(name = "get-cipher-suites", scope = "aaa",
        description = "Get the allowed cipher suites for TLS communication.")
public class GetCipherSuites implements Action {

    @Reference private ICertificateManager certProvider;

    @Override
    public Object execute() throws Exception {
        return "Cipher suites: " + Arrays.toString(certProvider.getCipherSuites());
    }
}
