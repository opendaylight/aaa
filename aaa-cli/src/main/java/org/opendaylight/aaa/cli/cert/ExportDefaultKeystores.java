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
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;

@Command(name = "export-keystores", scope = "aaa", description = "Export default MD-SAL keystores to .jks files.")

/**
 * Export default MD-SAL keystores to .jks files under default path.
 *
 * @author mserngawy
 *
 */
public class ExportDefaultKeystores extends OsgiCommandSupport {

    protected volatile ICertificateManager certProvider;

    public ExportDefaultKeystores(final ICertificateManager aaaCertProvider) {
        this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        certProvider.exportSslDataKeystores();
        return "Default directory for keystores is " + KeyStoreConstant.KEY_STORE_PATH;
    }

}
