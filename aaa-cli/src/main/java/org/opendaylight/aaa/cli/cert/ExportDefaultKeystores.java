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
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.cert.impl.KeyStoreConstant;

/**
 * Export default MD-SAL keystores to .jks files under default path.
 *
 * @author mserngawy
 *
 */
@Service
@Command(name = "export-keystores", scope = "aaa", description = "Export default MD-SAL keystores to .jks files.")
public class ExportDefaultKeystores implements Action {

    @Reference private ICertificateManager certProvider;

    @Override
    public Object execute() throws Exception {
        certProvider.exportSslDataKeystores();
        return "Default directory for keystores is " + KeyStoreConstant.KEY_STORE_PATH;
    }

}
