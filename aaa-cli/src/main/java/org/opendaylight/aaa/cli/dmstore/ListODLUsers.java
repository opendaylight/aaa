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
 * ListODLDomains list the available users at ODL aaa data store.
 *
 * @author mserngawy
 */
@Service
@Command(name = "get-users", scope = "aaa", description = "get list of ODL users.")
public class ListODLUsers extends AaaCliAbstractCommand {

    @Override
    public Object execute() throws Exception {
        if (super.execute() == null) {
            return LOGIN_FAILED_MESS;
        }
        list("Users: ", identityStore.getUsers().getUsers());
        return null;
    }
}
