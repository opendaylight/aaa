/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;

/**
 * ListODLDomains list the available roles at ODL aaa data store.
 *
 * @author mserngawy
 */
@Service
@Command(name = "get-roles", scope = "aaa", description = "get list of ODL roles.")
public class ListODLRoles extends AaaCliAbstractCommand {

    @Override
    public Object execute() throws Exception {
        if (super.execute() == null) {
            return LOGIN_FAILED_MESS;
        }
        list("Roles: ", identityStore.getRoles().getRoles());
        return null;
    }
}
