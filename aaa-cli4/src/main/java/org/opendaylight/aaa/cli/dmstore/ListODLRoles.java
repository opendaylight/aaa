/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;

import org.apache.karaf.shell.commands.Command;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;

@Command(name = "get-roles", scope = "aaa", description = "get list of ODL roles.")

/**
 * ListODLDomains list the available roles at ODL aaa data store.
 *
 * @author mserngawy
 *
 */
public class ListODLRoles extends AaaCliAbstractCommand {

    public ListODLRoles(final IIDMStore identityStore) {
        super(identityStore);
    }

    @Override
    protected Object doExecute() throws Exception {
        if (super.doExecute() == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }
        return identityStore.getRoles().getRoles();
    }

}
