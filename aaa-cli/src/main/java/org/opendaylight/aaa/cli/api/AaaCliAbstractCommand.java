/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.api;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AaaCliAbstractCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AaaCliAbstractCommand.class);
    protected final IIDMStore identityStore;

    public AaaCliAbstractCommand(IIDMStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    protected Object doExecute() throws Exception {
        final String userName = CliUtils.readPassword(this.session, "Enter Username:");
        final String passwd = CliUtils.readPassword(this.session, "Enter Password:");
        LOG.info("valuse {} - {}", userName, passwd);
        return DataStoreUtils.isAdminUser(identityStore, userName, passwd);
    }
}
